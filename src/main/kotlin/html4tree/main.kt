package html4tree

import java.io.File
import java.security.MessageDigest
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.Base64
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int

private val CSS_CONTENT = """
:root {
  --listing-text: #1f2328;
  --listing-link: #0969da;
  --listing-surface-hover: #f6f8fa;
  --listing-row-rule: #d0d7de;
  --listing-empty-text: #656d76;
  --listing-dark-bg: #0d1117;
  --listing-dark-text: #c9d1d9;
  --listing-dark-link: #58a6ff;
  --listing-dark-surface-hover: #161b22;
  --listing-dark-row-rule: #21262d;
  --listing-dark-empty-text: #8b949e;
  --listing-meta: #656d76;
  --listing-dark-meta: #8b949e;
}
body {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  line-height: 1.5;
  padding: 1rem;
  color: var(--listing-text);
}
main {
  max-width: 800px;
  margin: 0 auto;
}
h1,
.entry-name {
  overflow-wrap: anywhere;
  unicode-bidi: isolate;
}
ul {
  list-style-type: none;
  padding-left: 0;
}
a.dir-link {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  width: 100%;
  overflow-wrap: anywhere;
  box-sizing: border-box;
}
.icon {
  flex-shrink: 0;
  width: 1.25rem;
  text-align: center;
}
a {
  padding: 0.75rem 0.5rem;
  text-decoration: none;
  color: var(--listing-link);
  border-radius: 4px;
  transition: background-color 0.2s ease, outline-color 0.2s ease;
}
a:hover, a:focus-visible {
  background-color: var(--listing-surface-hover);
  outline: 2px solid var(--listing-link);
  outline-offset: -2px;
}
a:hover .entry-name, a:focus-visible .entry-name {
  text-decoration: underline;
}
@media (prefers-reduced-motion: reduce) {
  a {
    transition: none;
  }
}
li + li {
  border-top: 1px solid var(--listing-row-rule);
}
.empty-dir {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.75rem 0.5rem;
  color: var(--listing-empty-text);
  font-style: italic;
}
.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  margin: -1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
.entry-meta {
  margin-left: auto;
  display: flex;
  gap: 0.75rem;
  flex-shrink: 0;
  color: var(--listing-meta);
  font-variant-numeric: tabular-nums;
  unicode-bidi: isolate;
}
.entry-size,
.entry-mtime {
  direction: ltr;
}
.entry-size {
  min-width: 4.5rem;
  text-align: right;
}
@media (prefers-color-scheme: dark) {
  body {
    background-color: var(--listing-dark-bg);
    color: var(--listing-dark-text);
  }
  a {
    color: var(--listing-dark-link);
  }
  a:hover, a:focus-visible {
    background-color: var(--listing-dark-surface-hover);
    outline-color: var(--listing-dark-link);
  }
  li + li {
    border-top-color: var(--listing-dark-row-rule);
  }
  .empty-dir {
    color: var(--listing-dark-empty-text);
  }
  .entry-meta {
    color: var(--listing-dark-meta);
  }
}
""".trimIndent()

private val STYLE_HASH = "sha256-" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(CSS_CONTENT.toByteArray(Charsets.UTF_8)))
private val FILE_NAME_COMPARATOR = compareBy<File> { it.name }
private const val MAX_SAFE_DEPTH: Int = 100 // Defense-in-depth: hard limit on directory traversal to prevent resource exhaustion
private const val KIBIBYTE: Long = 1024L
private const val MEBIBYTE: Long = 1024L * 1024L
private const val GIBIBYTE: Long = 1024L * 1024L * 1024L
private val UTC_MINUTE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC)

class Html4tree : CliktCommand() {
    val maxLevel:Int by option(help="Number of levels deep for which to generate an index.html file", hidden = false).int().default(-1)
    val topDir: String by argument(help="Top directory to crawl")

    override fun run() {
        go(topDir, maxLevel)
    }
}

fun main(args: Array<String>)  = Html4tree().main(args)


internal data class FileIdentity(val key: Any?, val readable: Boolean)

internal data class EntryListingMeta(val sizeBytes: Long, val mtimeMillis: Long)


internal fun read_file_identity(file: File): FileIdentity {
    return try {
        val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        FileIdentity(attrs.fileKey(), true)
    } catch (e: Exception) {
        FileIdentity(null, false)
    }
}

fun go(topDir: String, maxLevel: Int)  {
    require(topDir.isNotBlank())
    require(!topDir.contains("..")) { "Path traversal sequences are not allowed." }
    // 보안 수정: symlink 검사를 우회하는 canonicalFile 대신 absoluteFile을 사용
    // canonicalFile은 symlink를 대상 경로로 해석하여 이어지는 NOFOLLOW_LINKS 검사를 무력화합니다.
    val top_dir = File(topDir).absoluteFile.toPath().normalize().toFile()

    // 보안 향상: 시스템 전체 정보 노출 및 리소스 고갈(DoS) 방지를 위해 크로스 플랫폼 방식으로 루트 디렉토리 크롤링을 제한합니다.
    require(top_dir.parentFile != null) { "Crawling the root directory is not allowed for security reasons" }

    require(Files.isDirectory(top_dir.toPath(), LinkOption.NOFOLLOW_LINKS)) { "Top directory must be an existing non-symlink directory" }

    val ll = LinkedList()

    val topEntry = LinkedListEntry(top_dir,0, read_file_identity(top_dir).key)
    ll.push(topEntry)
    crawl_directories(ll, maxLevel)
}

internal fun crawl_directories(
    ll: LinkedList,
    maxLevel: Int,
    processDirectory: (File, Set<String>, Array<File>?) -> Unit = { file, exclude, files -> process_dir(file, exclude, files) },
    processIgnoreFile: (File, Array<String>?) -> Set<String> = { file, names -> process_ignore_file(file, names) },
    listFiles: (File) -> Array<File>? = { it.listFiles() },
    readAttributes: (File) -> BasicFileAttributes? = {
        try {
            Files.readAttributes(it.toPath(), BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        } catch (e: Exception) {
            null
        }
    },
    readIdentity: (File) -> FileIdentity = ::read_file_identity
) {
    var lle: LinkedListEntry? = ll.pull()

    while(lle != null){
        val attrs = readAttributes(lle.file)
        if (attrs == null || !attrs.isDirectory) {
            lle = ll.pull()
            continue
        }

        val currentIdentity = readIdentity(lle.file)
        if (!currentIdentity.readable || (lle.fileKey != null && currentIdentity.key != lle.fileKey)) {
            lle = ll.pull()
            continue
        }

        val currentLevel: Int = lle.level
        // Defense-in-depth: prevent excessive resource consumption by limiting directory traversal depth
        if (currentLevel >= MAX_SAFE_DEPTH) {
            lle = ll.pull()
            continue
        }

        // ⚡ Bolt Performance Optimization: 디렉토리 목록을 캐싱하여 중복된 I/O 시스템 호출을 줄임
        val dirFiles = listFiles(lle.file)

        // The path can be replaced between the initial identity check and
        // directory enumeration. Do not process or enqueue children from a
        // snapshot whose post-listing identity is unreadable or different.
        val postListingIdentity = readIdentity(lle.file)
        if (!postListingIdentity.readable || currentIdentity.key != postListingIdentity.key) {
            lle = ll.pull()
            continue
        }

        val dirFilesNames = dirFiles?.let { files ->
            Array(files.size) { index -> files[index].name }
        }
        val exclude = processIgnoreFile(lle.file, dirFilesNames)

        if(maxLevel == -1 || currentLevel <= maxLevel)
           processDirectory(lle.file, exclude, dirFiles)

        if(maxLevel == -1 || currentLevel < maxLevel) {
            dirFiles?.forEach {
                // ⚡ Bolt Performance Optimization: Short-circuit OS stat calls
                // by checking cheap in-memory string exclusion rules first
                if(!it.name.isHiddenFile() && it.name !in exclude) {
                    val childAttrs = readAttributes(it)
                    if(childAttrs != null && childAttrs.isDirectory && !childAttrs.isSymbolicLink) {
                        val childEntry = LinkedListEntry(it, currentLevel+1, readIdentity(it).key)
                        ll.push(childEntry)
                    }
                }
            }
        }
        lle = ll.pull()
    }
}

fun String.isHiddenFile(): Boolean {
    return when (firstOrNull()) {
        '.', '\u3002', '\uFF0E', '\uFF61' -> true
        else -> false
    }
}

// ⚡ Bolt Performance Optimization: Single-pass loop with lazy StringBuilder
// Chained `.replace()` calls allocate multiple intermediate strings.
// A single pass over the string lazily allocating a StringBuilder is much faster.
fun String.escapeHtml(): String {
    var sb: StringBuilder? = null
    for (i in 0 until this.length) {
        val c = this[i]
        val replacement = when (c) {
            '&' -> "&amp;"
            '<' -> "&lt;"
            '>' -> "&gt;"
            '"' -> "&quot;"
            '\'' -> "&#x27;"
            '`' -> "&#x60;"
            else -> null
        }
        if (replacement != null) {
            if (sb == null) {
                sb = StringBuilder(this.length + 16)
                sb.append(this as CharSequence, 0, i)
            }
            sb.append(replacement)
        } else {
            sb?.append(c)
        }
    }
    return sb?.toString() ?: this
}

/**
 * Wraps [value] in Unicode First Strong Isolate / Pop Directional Isolate
 * so a filename used in a plain-text `title` cannot reorder adjacent
 * Korean type labels. HTML `dir="auto"` already isolates element text;
 * attributes have no `dir`, so they need explicit isolate characters.
 *
 * See Unicode Standard Annex #9 (Unicode 17.0.0, revision 51).
 */
internal fun isolate_bidi_plain_text(value: String): String {
    return "\u2068$value\u2069"
}

/**
 * True for Unicode bidirectional format controls that can reorder
 * neighboring glyphs (embeddings, overrides, isolates, and marks).
 *
 * These are neutralized in the *display* name only. The real
 * filesystem name stays in `href` so the generated link still opens.
 */
internal fun is_bidi_control(character: Char): Boolean {
    val code = character.toInt()
    return code == 0x061C ||
        code == 0x200E ||
        code == 0x200F ||
        code in 0x202A..0x202E ||
        code in 0x2066..0x2069
}

/**
 * Replaces bidirectional format controls with U+FFFD so a filename
 * cannot hide its extension (Trojan Source) while still showing that
 * something was removed. Does not strip the FSI/PDI marks that
 * [isolate_bidi_plain_text] later wraps around the cleaned name.
 */
internal fun neutralize_bidi_controls(value: String): String {
    var builder: StringBuilder? = null
    for (index in 0 until value.length) {
        val character = value[index]
        if (is_bidi_control(character)) {
            if (builder == null) {
                builder = StringBuilder(value.length)
                builder.append(value as CharSequence, 0, index)
            }
            builder.append('\uFFFD')
        } else {
            builder?.append(character)
        }
    }
    return builder?.toString() ?: value
}

internal fun format_byte_size(size: Long): String {
    if (size < KIBIBYTE) {
        val bounded = if (size < 0L) 0L else size
        return "$bounded B"
    }
    if (size < MEBIBYTE) {
        return format_scaled_size(size, KIBIBYTE, "KiB")
    }
    if (size < GIBIBYTE) {
        return format_scaled_size(size, MEBIBYTE, "MiB")
    }
    return format_scaled_size(size, GIBIBYTE, "GiB")
}

internal fun format_scaled_size(size: Long, unit: Long, label: String): String {
    val tenths = (size * 10L + unit / 2L) / unit
    return "${tenths / 10L}.${tenths % 10L} $label"
}

internal fun format_iso_instant(millis: Long): String {
    return Instant.ofEpochMilli(millis).toString()
}

internal fun format_utc_minute(millis: Long): String {
    return UTC_MINUTE_FORMATTER.format(Instant.ofEpochMilli(millis))
}

internal fun directory_size_label(): String {
    return "\u2014"
}

fun String.urlEncodePath(): String {
    val bytes = this.toByteArray(Charsets.UTF_8)
    var encoded: StringBuilder? = null
    for (i in bytes.indices) {
        val byte = bytes[i].toInt() and 0xff
        val isUnreserved = (byte in 'A'.toInt()..'Z'.toInt()) ||
                           (byte in 'a'.toInt()..'z'.toInt()) ||
                           (byte in '0'.toInt()..'9'.toInt()) ||
                           byte == '-'.toInt() ||
                           byte == '.'.toInt() ||
                           byte == '_'.toInt() ||
                           byte == '~'.toInt()
        if (isUnreserved) {
            encoded?.append(byte.toChar())
        } else {
            var builder = encoded
            if (builder == null) {
                builder = StringBuilder(bytes.size + 16)
                for (j in 0 until i) {
                    builder.append((bytes[j].toInt() and 0xff).toChar())
                }
                encoded = builder
            }
            // ⚡ Bolt Performance Optimization: Direct character mapping
            // Avoids multiple string allocations (toString, padStart, toUpperCase) per reserved byte.
            builder.append('%')
            val hex1 = byte ushr 4
            val hex2 = byte and 0xf
            builder.append(if (hex1 < 10) (hex1 + 48).toChar() else (hex1 + 55).toChar())
            builder.append(if (hex2 < 10) (hex2 + 48).toChar() else (hex2 + 55).toChar())
        }
    }
    return encoded?.toString() ?: this
}

fun process_ignore_file(curr_dir: File, dirFilesNames: Array<String>? = null): Set<String> {

    val ignore_filename = ".html4ignore"
 
    val ignore_file_path = curr_dir.getAbsolutePath()+"/"+ignore_filename

    val ignore_file = File(ignore_file_path)

    val files_to_exclude = mutableSetOf<String>()

    // 보안 향상: .html4ignore 파일이 일반 파일인지 확인하고, 심볼릭 링크인 경우 무시하여 DoS 및 경로 조작을 방지합니다.
    // 보안 향상: 파일 크기(1MB 제한) 및 줄 수(1000줄), 정규식 길이(100자)를 제한하여 ReDoS 및 메모리 고갈(OOM) 방지
    // 보안 향상: 권한이 없는 파일 접근 시 발생하는 예외(DoS)를 방지하기 위해 canRead() 추가 확인
    if(ignore_file.isFile && !Files.isSymbolicLink(ignore_file.toPath()) && ignore_file.canRead() && ignore_file.length() <= 1048576){
       val ignored_matchers = mutableListOf<java.nio.file.PathMatcher>()

       ignore_file.useLines { lines ->
           for ((lineIndex, it) in lines.withIndex()) {
               // 줄 수 제한이 패턴 수도 함께 상한(줄당 최대 1개 패턴)하므로 별도 패턴 카운터는 불필요
               if (lineIndex >= 1000) break
               val pattern = it.trim()
               if (pattern.isNotEmpty() && pattern.length <= 100) {
                   try {
                       ignored_matchers.add(java.nio.file.FileSystems.getDefault().getPathMatcher("glob:$pattern"))
                   } catch (_: IllegalArgumentException) {
                   }
               }
           }
       }

       // Compare a normalized candidate, but always retain the exact observed filesystem entry
       // in the exclusion set because downstream admission compares against File.name verbatim.
       val list = dirFilesNames ?: curr_dir.list()
       list?.forEach { observedName ->
           val normalizedCandidate = observedName.trim()
           val pathCurrent = try {
               java.nio.file.Paths.get(normalizedCandidate)
           } catch (_: java.nio.file.InvalidPathException) {
               files_to_exclude.add(observedName)
               return@forEach
           }
           for (matcher in ignored_matchers) {
              if (matcher.matches(pathCurrent)) {
                 files_to_exclude.add(observedName)
                 break
              }
           }
       }
    }

    if ("index.html" !in files_to_exclude)
       files_to_exclude.add("index.html")

    // ⚡ Bolt Performance Optimization: Extract static list to prevent redundant allocations per directory
    // 보안 향상: 민감한 시스템, 설정, 시크릿 파일을 디렉토리 목록에서 기본적으로 제외하여 정보 노출(Information Exposure) 방지
    files_to_exclude.addAll(Constants.defaultSensitiveFiles)

    // 보안 향상: match sensitive aliases after trimming/case normalization, but exclude the exact
    // observed entry name so padded filesystem names cannot bypass downstream exact membership.
    (dirFilesNames ?: curr_dir.list())?.forEach { observedName ->
        val normalizedCandidate = observedName.trim()
        val normalizedName = normalizedCandidate.toLowerCase(java.util.Locale.ROOT)
        if (
            normalizedCandidate.isHiddenFile() ||
            normalizedName in Constants.defaultSensitiveFileNamesLowercase ||
            normalizedName.endsWith("~") ||
            Constants.defaultSensitiveExtensions.any { extension ->
                normalizedName.endsWith(extension)
            }
        ) {
            files_to_exclude.add(observedName)
        }
    }

    return files_to_exclude
}

fun write_index_file(
    curr_dir: File,
    content: String,
    moveFile: (
        java.nio.file.Path,
        java.nio.file.Path,
        Array<out java.nio.file.CopyOption>
    ) -> Unit = { source, target, options ->
        Files.move(source, target, *options)
        Unit
    }
) {
    val indexPath = curr_dir.toPath().resolve("index.html")
    val tempPath = Files.createTempFile(curr_dir.toPath(), ".index-", ".html")
    try {
        Files.write(tempPath, content.toByteArray(Charsets.UTF_8))
        try {
            // With ATOMIC_MOVE, Java ignores every other copy option and the
            // existing-target policy is provider-specific.
            moveFile(tempPath, indexPath, arrayOf(StandardCopyOption.ATOMIC_MOVE))
        } catch (error: java.io.IOException) {
            if (
                error !is java.nio.file.AtomicMoveNotSupportedException &&
                error !is java.nio.file.FileAlreadyExistsException
            ) {
                throw error
            }
            // This compatibility fallback preserves replacement semantics but
            // is explicitly non-atomic.
            moveFile(tempPath, indexPath, arrayOf(StandardCopyOption.REPLACE_EXISTING))
        }
    } finally {
        Files.deleteIfExists(tempPath)
    }
}
 
fun process_dir(curr_dir: File, excludeSet: Set<String>? = null, dirFiles: Array<File>? = null){
    
    val exclude: Set<String> = excludeSet ?: process_ignore_file(curr_dir)
    val directoryName = curr_dir.name.ifEmpty { "Root" }

    val index_top = """<!doctype html>
<html lang="ko">
     <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="color-scheme" content="light dark">
        <meta name="theme-color" content="#ffffff" media="(prefers-color-scheme: light)">
        <meta name="theme-color" content="#0d1117" media="(prefers-color-scheme: dark)">
        <!-- 보안 향상: 인라인 스크립트 실행 방지 -->
        <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src '${STYLE_HASH}'; base-uri 'none'; form-action 'none';">
        <!-- 보안 향상: 리퍼러를 통한 디렉토리 경로 노출 방지 -->
        <meta name="referrer" content="no-referrer">
        <meta name="robots" content="noindex, nofollow">
        <title>${directoryName.escapeHtml()} - 디렉토리 목록</title>
        <style>${CSS_CONTENT}</style>
     </head>
     <body>
       <main>
         <h1 dir="auto">${directoryName.escapeHtml()}</h1>
         <nav aria-label="디렉토리 목록">
         <ul role="list">
            <li><a class="dir-link" href="./.." title="상위 디렉토리로 이동"><span class="icon" aria-hidden="true">&#x21B0;</span> <span class="entry-name" aria-hidden="true">..</span> <span class="visually-hidden">상위 디렉토리로 이동</span></a></li>
""" 

    val index_middle = fun():String{ 
        val l = StringBuilder()

        val filesList = dirFiles ?: curr_dir.listFiles()
        // ⚡ Bolt Performance Optimization: Use Array clone instead of toMutableList
        // toMutableList() allocates a new ArrayList and a backing object array, whereas clone() only allocates a new array.
        val dir_files: Array<File> = filesList?.clone() ?: emptyArray()
        dir_files.sortWith(FILE_NAME_COMPARATOR)
        dir_files.forEach {
           val fileName = it.getName()
           // ⚡ Bolt Performance Optimization: Short-circuit string match before expensive OS filesystem calls
           // 🛡️ Sentinel: Ignore hidden files/directories to prevent sensitive data exposure
           if (!fileName.isHiddenFile() && fileName !in exclude) {
               var isLinkedDirectory = false
               var isSymbolicLink = false
               var listingMeta: EntryListingMeta? = null
               try {
                   // ⚡ Bolt Performance Optimization: Replace 3 separate OS stat calls (isDirectory, it.isDirectory(), isSymbolicLink)
                   // with a single readAttributes call to reduce I/O overhead.
                   val attrs = Files.readAttributes(it.toPath(), BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                   isLinkedDirectory = attrs.isDirectory
                   isSymbolicLink = attrs.isSymbolicLink
                   listingMeta = EntryListingMeta(attrs.size(), attrs.lastModifiedTime().toMillis())
               } catch (e: Exception) {
               }
               if (!isSymbolicLink) {
                  val displayName = neutralize_bidi_controls(fileName)
                  val encodedHref = if (isLinkedDirectory) { "./${fileName.urlEncodePath()}/" } else { "./${fileName.urlEncodePath()}" }
                  val typeLabel = if (isLinkedDirectory) { "디렉토리" } else { "파일" }
                  val titleText = "${isolate_bidi_plain_text(displayName)} $typeLabel".escapeHtml()
                  val icon = if (isLinkedDirectory) { "&#128193;" } else { "&#128196;" }
                  val bidiWarning = if (displayName != fileName) {
                      """ <span class="visually-hidden">이름에 방향 제어 문자가 있습니다</span>"""
                  } else {
                      ""
                  }
                  val observedMeta = listingMeta
                  val entryMeta = if (observedMeta != null) {
                      val isoTime = format_iso_instant(observedMeta.mtimeMillis)
                      val displayTime = format_utc_minute(observedMeta.mtimeMillis)
                      val sizeText = if (isLinkedDirectory) {
                          directory_size_label()
                      } else {
                          format_byte_size(observedMeta.sizeBytes)
                      }
                      """ <span class="entry-meta"><span class="entry-size">$sizeText</span> <time class="entry-mtime" datetime="$isoTime" dir="ltr">$displayTime</time></span>"""
                  } else {
                      ""
                  }
                  l.append("""          <li><a class="dir-link" href="${encodedHref}" title="${titleText}"><span class="icon" aria-hidden="true">${icon}</span> <span class="entry-name" dir="auto">${displayName.escapeHtml()}</span> <span class="visually-hidden">${typeLabel}</span>${bidiWarning}${entryMeta}</a></li>""")
                  l.append('\n')
               }
           }
        }

        if(l.isEmpty()){
            l.append("""          <li><div class="empty-dir" role="status"><span class="icon" aria-hidden="true">&#128194;</span> <span dir="auto">이 디렉토리는 비어 있습니다.</span></div></li>""")
            l.append('\n')
        }

        return l.toString();
     } 

   val index_bottom="""
         </ul>
         </nav>
       </main>
    </body>
</html>
"""

   try {
       write_index_file(curr_dir, index_top+index_middle()+index_bottom)
   } catch (e: Exception) {
       // 보안 향상: 디렉토리에 쓰기 권한이 없거나 파일 시스템 오류가 발생했을 때
       // 전체 크롤링(프로세스)이 중단되는 DoS를 방지합니다. (Fail Securely)
   }

}

fun help() {
    println("ERROR: help has not been written yet!")
}

private object Constants {
    @JvmField
    val defaultSensitiveFiles = listOf(".git", ".env", ".ssh", ".htpasswd", ".htaccess", "id_rsa", "id_ed25519", "secrets.yml", ".html4ignore", ".DS_Store", ".aws", ".kube", ".npmrc", ".gnupg", "config.json", "credentials.json")

    @JvmField
    val defaultSensitiveFileNamesLowercase =
        defaultSensitiveFiles.map { it.toLowerCase(java.util.Locale.ROOT) }.toSet()

    @JvmField
    val defaultSensitiveExtensions = listOf(
        ".pem",
        ".key",
        ".p12",
        ".pfx",
        ".crt",
        ".cer",
        ".der",
        ".keystore",
        ".truststore",
        ".jks",
        ".sqlite",
        ".db",
        ".bak",
        ".sql",
        ".pcap",
        ".pcapng",
        ".log",
        ".swp",
        ".swo",
        ".swpx"
    )
}
