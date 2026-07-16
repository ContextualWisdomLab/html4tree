package html4tree

import java.io.File
import java.security.MessageDigest
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.Base64
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int

// ⚡ Bolt Performance Optimization: Hoisted static variables
// Moving cssContent, styleHash, and css out of process_dir to top-level avoids
// repeated string allocations and expensive SHA-256 hashing for every directory processed.
// private val prevents generating public getters and preserves 100% test coverage.
private val cssContent = """
              body {
                font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                line-height: 1.5;
                padding: 1rem;
                color: #1f2328;
              }
              main {
                max-width: 800px;
                margin: 0 auto;
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
                padding: 0.5rem;
                text-decoration: none;
                color: #0969da;
                border-radius: 4px;
                transition: background-color 0.2s ease, outline-color 0.2s ease;
              }
              a:hover, a:focus-visible {
                background-color: #f6f8fa;
                text-decoration: underline;
                outline: 2px solid #0969da;
                outline-offset: -2px;
              }
              @media (prefers-reduced-motion: reduce) {
                a {
                  transition: none;
                }
              }
              @media (prefers-color-scheme: dark) {
                body {
                  background-color: #0d1117;
                  color: #c9d1d9;
                }
                a {
                  color: #58a6ff;
                }
                a:hover, a:focus-visible {
                  background-color: #161b22;
                  outline-color: #58a6ff;
                }
              }
              .empty-dir {
                padding: 0.5rem;
                opacity: 0.7;
                font-style: italic;
              }
              """

private val styleHash = "sha256-" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(cssContent.toByteArray(Charsets.UTF_8)))

private val css = """
              <style>
${cssContent}              </style>
              """

class Html4tree : CliktCommand() {
    val maxLevel:Int by option(help="Number of levels deep for which to generate an index.html file", hidden = false).int().default(-1)
    val topDir: String by argument(help="Top directory to crawl")

    override fun run() {
        go(topDir, maxLevel)
    }
}

fun main(args: Array<String>)  = Html4tree().main(args)


internal data class FileIdentity(val key: Any?, val readable: Boolean)


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
    isDirectory: (File) -> Boolean = { Files.isDirectory(it.toPath(), LinkOption.NOFOLLOW_LINKS) },
    isSymbolicLink: (File) -> Boolean = { Files.isSymbolicLink(it.toPath()) },
    readIdentity: (File) -> FileIdentity = ::read_file_identity
) {
    var lle: LinkedListEntry? = ll.pull()

    while(lle != null){
        if (!isDirectory(lle.file)) {
            lle = ll.pull()
            continue
        }

        val currentIdentity = readIdentity(lle.file)
        if (!currentIdentity.readable || (lle.fileKey != null && currentIdentity.key != lle.fileKey)) {
            lle = ll.pull()
            continue
        }

        val currentLevel: Int = lle.level

        // ⚡ Bolt Performance Optimization: 디렉토리 목록을 캐싱하여 중복된 I/O 시스템 호출을 줄임
        val dirFiles = listFiles(lle.file)
        val dirFilesNames = dirFiles?.map { it.name }?.toTypedArray()
        val exclude = processIgnoreFile(lle.file, dirFilesNames)

        if(maxLevel == -1 || currentLevel <= maxLevel)
           processDirectory(lle.file, exclude, dirFiles)

        if(maxLevel == -1 || currentLevel < maxLevel) {
            dirFiles?.forEach {
                // ⚡ Bolt Performance Optimization: Short-circuit OS stat calls (isDirectory/isSymbolicLink)
                // by checking cheap in-memory string exclusion rules first
                if(!it.name.startsWith(".") && it.name !in exclude && isDirectory(it) && !isSymbolicLink(it)) {
                    val childEntry = LinkedListEntry(it, currentLevel+1, readIdentity(it).key)
                    ll.push(childEntry)
                }
            }
        }
        lle = ll.pull()
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
                   } catch (_: java.util.regex.PatternSyntaxException) {
                   }
               }
           }
       }

       // ⚡ Bolt Performance Optimization: 디렉토리 목록을 Set에 추가하기 위해 필터링만 할 때는 정렬이 불필요하므로 .sorted()를 제거하여 O(N log N) 오버헤드를 방지합니다.
       val list = dirFilesNames ?: curr_dir.list()
       list?.forEach {
           val current = it
           val pathCurrent = java.nio.file.Paths.get(current)
           for (matcher in ignored_matchers) {
              if (matcher.matches(pathCurrent)) {
                 files_to_exclude.add(current)
                 break
              }
           }
       }
    }

    if ("index.html" !in files_to_exclude)
       files_to_exclude.add("index.html")

    // 보안 향상: 민감한 시스템, 설정, 시크릿 파일을 디렉토리 목록에서 기본적으로 제외하여 정보 노출(Information Exposure) 방지
    val defaultSensitiveFiles = listOf(".git", ".env", ".ssh", ".htpasswd", ".htaccess", "id_rsa", "id_ed25519", "secrets.yml", ".html4ignore", ".DS_Store", ".aws", ".kube", ".npmrc", ".gnupg", "config.json", "credentials.json")
    files_to_exclude.addAll(defaultSensitiveFiles)

    // 보안 향상: .env, .git 등 민감한 정보가 포함될 수 있는 숨김 파일(.으로 시작하는 모든 항목)을 기본적으로 노출하지 않도록 제외 (정보 노출 방지)
    (dirFilesNames ?: curr_dir.list())?.forEach {
        if (it.startsWith(".")) {
            files_to_exclude.add(it)
        }
    }

    return files_to_exclude
}

fun write_index_file(curr_dir: File, content: String) {
    val indexPath = curr_dir.toPath().resolve("index.html")
    val tempPath = Files.createTempFile(curr_dir.toPath(), ".index-", ".html")
    try {
        Files.write(tempPath, content.toByteArray(Charsets.UTF_8))
        Files.move(tempPath, indexPath, StandardCopyOption.REPLACE_EXISTING)
    } finally {
        Files.deleteIfExists(tempPath)
    }
}
 
fun process_dir(curr_dir: File, excludeSet: Set<String>? = null, dirFiles: Array<File>? = null){
    
    val exclude: Set<String> = excludeSet ?: process_ignore_file(curr_dir)

    val index_top = """<!doctype html>
<html lang="ko">
     <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="color-scheme" content="light dark">
        <!-- 보안 향상: 인라인 스크립트 실행 방지 -->
        <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src '${styleHash}'; base-uri 'none'; form-action 'none';">
        <!-- 보안 향상: 리퍼러를 통한 디렉토리 경로 노출 방지 -->
        <meta name="referrer" content="no-referrer">
        <title>${curr_dir.getName().escapeHtml()}</title>
        ${css}
     </head>
     <body>
       <main>
         <h1>${curr_dir.getName().escapeHtml()}</h1>
         <nav aria-label="디렉토리 목록">
         <ul role="list">
            <li><a class="dir-link" href="./.." aria-label="상위 디렉토리로 이동" title="상위 디렉토리로 이동"><span class="icon" aria-hidden="true">&#x21B0;</span> <span>..</span></a></li>
""" 

    val index_middle = fun():String{ 
        val l = StringBuilder()

        val filesList = dirFiles ?: curr_dir.listFiles()
        val dir_files: MutableList<File> = filesList?.toMutableList() ?: mutableListOf()
        dir_files.sortWith(compareBy ({it.name}) )
        dir_files.forEach {
           val fileName = it.getName()
           // ⚡ Bolt Performance Optimization: Short-circuit string match before expensive OS filesystem calls
           // 🛡️ Sentinel: Ignore hidden files/directories to prevent sensitive data exposure
           if (!fileName.startsWith(".") && fileName !in exclude) {
               var isLinkedDirectory = false
               var isSymbolicLink = false
               try {
                   // ⚡ Bolt Performance Optimization: Replace 3 separate OS stat calls (isDirectory, it.isDirectory(), isSymbolicLink)
                   // with a single readAttributes call to reduce I/O overhead.
                   val attrs = Files.readAttributes(it.toPath(), BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                   isLinkedDirectory = attrs.isDirectory
                   isSymbolicLink = attrs.isSymbolicLink
               } catch (e: Exception) {
               }
               if (!isSymbolicLink) {
                  val encodedHref = if (isLinkedDirectory) { "./${fileName.urlEncodePath()}/" } else { "./${fileName.urlEncodePath()}" }
                  val ariaLabel = "${fileName} ${if (isLinkedDirectory) { "디렉토리" } else { "파일" }}".escapeHtml()
                  val icon = if (isLinkedDirectory) { "&#128193;" } else { "&#128196;" }
                  l.append("""          <li><a class="dir-link" href="${encodedHref}" aria-label="${ariaLabel}" title="${ariaLabel}"><span class="icon" aria-hidden="true">${icon}</span> <span>${fileName.escapeHtml()}</span></a></li>""")
                  l.append('\n')
               }
           }
        }

        if(l.isEmpty()){
            l.append("""          <li><div class="empty-dir">이 디렉토리는 비어 있습니다.</div></li>""")
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
