package html4tree

import java.io.File
import java.security.MessageDigest
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.Base64
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int

private val CSS_CONTENT = """
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
h1 {
  overflow-wrap: anywhere;
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
  color: #0969da;
  border-radius: 4px;
  transition: background-color 0.2s ease, outline-color 0.2s ease;
}
a:hover, a:focus-visible {
  background-color: #f6f8fa;
  outline: 2px solid #0969da;
  outline-offset: -2px;
}
a:hover span:last-child, a:focus-visible span:last-child {
  text-decoration: underline;
}
@media (prefers-reduced-motion: reduce) {
  a {
    transition: none;
  }
}
li + li {
  border-top: 1px solid #d0d7de;
}
.empty-dir {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.75rem 0.5rem;
  color: #656d76;
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
  li + li {
    border-top-color: #21262d;
  }
  .empty-dir {
    color: #8b949e;
  }
}
""".trimIndent()

private val STYLE_HASH = "sha256-" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(CSS_CONTENT.toByteArray(Charsets.UTF_8)))
private val FILE_NAME_COMPARATOR = compareBy<File> { it.name }

class Html4tree : CliktCommand() {
    val maxLevel:Int by option(help="Number of levels deep for which to generate an index.html file", hidden = false).int().default(-1)
    val forceOverwrite: Boolean by option(
        "--force-overwrite",
        help="Destructively replace an unmarked existing index.html. Symlinks and directories are still refused."
    ).flag()
    val cleanup: Boolean by option(
        "--cleanup",
        help="Delete only html4tree-owned index.html files under TOPDIR. Unowned files are preserved."
    ).flag()
    val dryRun: Boolean by option(
        "--dry-run",
        help="With --cleanup, report owned artifacts that would be deleted without deleting them."
    ).flag()
    val topDir: String by argument(help="Top directory to crawl")

    override fun run() {
        if (dryRun && !cleanup) {
            throw UsageError("--dry-run requires --cleanup")
        }
        go(topDir, maxLevel, forceOverwrite, cleanup, dryRun)
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

internal const val GENERATED_INDEX_NAME = "index.html"
internal const val GENERATED_OWNERSHIP_VERSION = 1
internal const val GENERATED_OWNERSHIP_MARKER =
    """<meta name="generator" content="html4tree/$GENERATED_OWNERSHIP_VERSION">"""
internal const val OWNERSHIP_PREFIX_LIMIT = 4096
internal const val OWNERSHIP_NEAR_START_LIMIT = 1024
private val OWNERSHIP_MARKER_REGEX = Regex("""<meta\s+name="generator"\s+content="html4tree/(\d+)">""")

enum class IndexTargetKind {
    ABSENT,
    OWNED,
    UNOWNED,
    UNSAFE
}

data class IndexTargetClassification(
    val kind: IndexTargetKind,
    val reason: String
)

enum class IndexWriteResult {
    CREATED,
    REPLACED,
    PRESERVED
}

internal fun generated_index_file(curr_dir: File): File {
    return File(curr_dir, GENERATED_INDEX_NAME)
}

internal fun read_index_prefix(
    path: Path,
    limit: Int = OWNERSHIP_PREFIX_LIMIT,
    openStream: (Path) -> java.io.InputStream = { Files.newInputStream(it, LinkOption.NOFOLLOW_LINKS) }
): ByteArray? {
    return try {
        if (Files.isSymbolicLink(path)) {
            null
        } else if (limit <= 0) {
            ByteArray(0)
        } else {
            openStream(path).use { input ->
                val buffer = ByteArray(limit)
                var offset = 0
                while (offset < limit) {
                    val n = input.read(buffer, offset, limit - offset)
                    if (n <= 0) {
                        break
                    }
                    offset += n
                }
                if (offset == limit) {
                    buffer
                } else {
                    buffer.copyOf(offset)
                }
            }
        }
    } catch (e: Exception) {
        null
    }
}

internal fun classify_index_prefix(prefix: ByteArray): IndexTargetClassification {
    val text = String(prefix, Charsets.UTF_8)
    val match = OWNERSHIP_MARKER_REGEX.find(text)
    if (match == null) {
        if (text.contains("""content="html4tree/""")) {
            return IndexTargetClassification(IndexTargetKind.UNOWNED, "malformed")
        }
        return IndexTargetClassification(IndexTargetKind.UNOWNED, "unowned")
    }
    if (match.range.start >= OWNERSHIP_NEAR_START_LIMIT) {
        return IndexTargetClassification(IndexTargetKind.UNOWNED, "late-marker")
    }
    val version = match.groupValues[1].toIntOrNull()
    if (version != GENERATED_OWNERSHIP_VERSION) {
        return IndexTargetClassification(IndexTargetKind.UNOWNED, "unsupported-version")
    }
    return IndexTargetClassification(IndexTargetKind.OWNED, "owned")
}

internal fun classify_index_target(target: File): IndexTargetClassification {
    return try {
        val path = target.toPath()
        if (Files.isSymbolicLink(path)) {
            return IndexTargetClassification(IndexTargetKind.UNSAFE, "symlink")
        }
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return IndexTargetClassification(IndexTargetKind.ABSENT, "absent")
        }
        val attrs = Files.readAttributes(path, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
        if (attrs.isDirectory) {
            return IndexTargetClassification(IndexTargetKind.UNSAFE, "directory")
        }
        if (!attrs.isRegularFile) {
            return IndexTargetClassification(IndexTargetKind.UNSAFE, "not-regular")
        }
        val prefix = read_index_prefix(path)
            ?: return IndexTargetClassification(IndexTargetKind.UNSAFE, "unreadable")
        classify_index_prefix(prefix)
    } catch (e: Exception) {
        IndexTargetClassification(IndexTargetKind.UNSAFE, "unreadable")
    }
}

internal fun default_index_reporter(message: String) {
    System.err.println(message)
}

/**
 * Publishes [source] to an absent [target] without replacing an occupant.
 *
 * `Files.createLink` maps to POSIX `link(2)`, which fails with `EEXIST` and
 * does not replace. `UnsupportedOperationException` is only the provider
 * "not implemented" case. OpenJDK's Unix provider implements `createLink`,
 * so a no-hard-link volume (`EPERM`, `ENOTSUP`, `EOPNOTSUPP`) surfaces as
 * `FileSystemException` or `AccessDeniedException`. Fall back to a
 * create-only `Files.move` (no `ATOMIC_MOVE`, no `REPLACE_EXISTING`) for
 * those I/O failures. `FileAlreadyExistsException` is rethrown so `EEXIST`
 * stays exclusive.
 */
internal fun publish_exclusive(
    source: Path,
    target: Path,
    createLink: (Path, Path) -> Unit = { existing, link ->
        Files.createLink(link, existing)
        Unit
    },
    moveFile: (
        Path,
        Path,
        Array<out java.nio.file.CopyOption>
    ) -> Unit = { from, to, options ->
        Files.move(from, to, *options)
        Unit
    }
) {
    try {
        createLink(source, target)
    } catch (exists: java.nio.file.FileAlreadyExistsException) {
        throw exists
    } catch (unsupported: UnsupportedOperationException) {
        moveFile(source, target, arrayOf<java.nio.file.CopyOption>())
    } catch (io: java.io.IOException) {
        moveFile(source, target, arrayOf<java.nio.file.CopyOption>())
    }
}

internal fun cleanup_owned_index(
    curr_dir: File,
    dryRun: Boolean,
    reporter: (String) -> Unit = ::default_index_reporter,
    classifyTarget: (File) -> IndexTargetClassification = ::classify_index_target,
    deleteFile: (Path) -> Unit = { Files.delete(it) }
): Boolean {
    val indexFile = generated_index_file(curr_dir)
    val classification = classifyTarget(indexFile)
    return when (classification.kind) {
        IndexTargetKind.OWNED -> {
            if (dryRun) {
                reporter("would-delete: ${indexFile.absolutePath}")
                true
            } else {
                val confirmed = classifyTarget(indexFile)
                if (confirmed.kind != IndexTargetKind.OWNED) {
                    reporter("preserved: ${indexFile.absolutePath} (${confirmed.reason})")
                    false
                } else {
                    try {
                        deleteFile(indexFile.toPath())
                        reporter("deleted: ${indexFile.absolutePath}")
                        true
                    } catch (e: Exception) {
                        reporter("failed: ${indexFile.absolutePath}")
                        false
                    }
                }
            }
        }
        IndexTargetKind.ABSENT -> false
        else -> {
            reporter("preserved: ${indexFile.absolutePath} (${classification.reason})")
            false
        }
    }
}

fun go(
    topDir: String,
    maxLevel: Int,
    forceOverwrite: Boolean = false,
    cleanup: Boolean = false,
    dryRun: Boolean = false,
    reporter: (String) -> Unit = ::default_index_reporter
)  {
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
    if (cleanup) {
        crawl_directories(ll, maxLevel, processDirectory = { file, _, _ ->
            cleanup_owned_index(file, dryRun, reporter)
        })
    } else {
        crawl_directories(ll, maxLevel, processDirectory = { file, exclude, files ->
            process_dir(file, exclude, files, forceOverwrite, reporter)
        })
    }
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

       // ⚡ Bolt Performance Optimization: 디렉토리 목록을 Set에 추가하기 위해 필터링만 할 때는 정렬이 불필요하므로 .sorted()를 제거하여 O(N log N) 오버헤드를 방지합니다.
       val list = dirFilesNames ?: curr_dir.list()
       list?.forEach {
           val current = it
           val pathCurrent = try {
               java.nio.file.Paths.get(current)
           } catch (_: java.nio.file.InvalidPathException) {
               files_to_exclude.add(current)
               return@forEach
           }
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

    // ⚡ Bolt Performance Optimization: Extract static list to prevent redundant allocations per directory
    // 보안 향상: 민감한 시스템, 설정, 시크릿 파일을 디렉토리 목록에서 기본적으로 제외하여 정보 노출(Information Exposure) 방지
    files_to_exclude.addAll(Constants.defaultSensitiveFiles)

    // 보안 향상: dot-like prefixes and case variants of known sensitive names are excluded.
    (dirFilesNames ?: curr_dir.list())?.forEach {
        val normalizedName = it.toLowerCase(java.util.Locale.ROOT)
        if (
            it.isHiddenFile() ||
            normalizedName in Constants.defaultSensitiveFileNamesLowercase ||
            normalizedName.endsWith("~") ||
            Constants.defaultSensitiveExtensions.any { extension ->
                normalizedName.endsWith(extension)
            }
        ) {
            files_to_exclude.add(it)
        }
    }

    return files_to_exclude
}

fun write_index_file(
    curr_dir: File,
    content: String,
    forceOverwrite: Boolean = false,
    reporter: (String) -> Unit = ::default_index_reporter,
    classifyTarget: (File) -> IndexTargetClassification = ::classify_index_target,
    copyFile: (
        java.nio.file.Path,
        java.nio.file.Path
    ) -> Unit = { source, target ->
        Files.copy(
            source,
            target,
            LinkOption.NOFOLLOW_LINKS,
            StandardCopyOption.REPLACE_EXISTING
        )
        Unit
    },
    createLink: (
        java.nio.file.Path,
        java.nio.file.Path
    ) -> Unit = { existing, link ->
        Files.createLink(link, existing)
        Unit
    },
    moveFile: (
        java.nio.file.Path,
        java.nio.file.Path,
        Array<out java.nio.file.CopyOption>
    ) -> Unit = { source, target, options ->
        Files.move(source, target, *options)
        Unit
    }
): IndexWriteResult {
    val indexPath = curr_dir.toPath().resolve(GENERATED_INDEX_NAME)
    val existing = classifyTarget(indexPath.toFile())
    val replacingOwned = existing.kind == IndexTargetKind.OWNED
    val replacingForced = existing.kind == IndexTargetKind.UNOWNED && forceOverwrite
    if (existing.kind == IndexTargetKind.UNSAFE ||
        (existing.kind == IndexTargetKind.UNOWNED && !forceOverwrite)
    ) {
        reporter("preserved: ${indexPath.toAbsolutePath()} (${existing.reason})")
        return IndexWriteResult.PRESERVED
    }

    val tempPath = Files.createTempFile(curr_dir.toPath(), ".index-", ".html")
    var backupPath: Path? = null
    var preserveBackupAfterRestoreFailure = false
    try {
        Files.write(tempPath, content.toByteArray(Charsets.UTF_8))
        if (replacingOwned || replacingForced) {
            val createdBackup = Files.createTempFile(curr_dir.toPath(), ".index-owned-backup-", ".html")
            try {
                copyFile(indexPath, createdBackup)
                backupPath = createdBackup
            } catch (e: Exception) {
                Files.deleteIfExists(createdBackup)
                reporter("preserved: ${indexPath.toAbsolutePath()} (backup-failed)")
                return IndexWriteResult.PRESERVED
            }
            val stillAllowed = classifyTarget(indexPath.toFile())
            val stillReplaceable = stillAllowed.kind == IndexTargetKind.OWNED ||
                (forceOverwrite && stillAllowed.kind == IndexTargetKind.UNOWNED)
            if (!stillReplaceable) {
                reporter("preserved: ${indexPath.toAbsolutePath()} (${stillAllowed.reason})")
                return IndexWriteResult.PRESERVED
            }
            try {
                // With ATOMIC_MOVE, Java ignores every other copy option and the
                // existing-target policy is provider-specific. Replacement is
                // allowed only after the occupant was reclassified as owned or
                // force-overwritable.
                moveFile(tempPath, indexPath, arrayOf(StandardCopyOption.ATOMIC_MOVE))
            } catch (error: java.io.IOException) {
                if (
                    error !is java.nio.file.AtomicMoveNotSupportedException &&
                    error !is java.nio.file.FileAlreadyExistsException
                ) {
                    throw error
                }
                val now = classifyTarget(indexPath.toFile())
                val canReplace = now.kind == IndexTargetKind.ABSENT ||
                    now.kind == IndexTargetKind.OWNED ||
                    (forceOverwrite && now.kind == IndexTargetKind.UNOWNED)
                if (!canReplace) {
                    reporter("preserved: ${indexPath.toAbsolutePath()} (${now.reason})")
                    return IndexWriteResult.PRESERVED
                }
                // Owned/forced replacement may use the documented non-atomic fallback.
                // Unowned existing targets never take this path.
                moveFile(tempPath, indexPath, arrayOf(StandardCopyOption.REPLACE_EXISTING))
            }
        } else {
            val now = classifyTarget(indexPath.toFile())
            if (now.kind != IndexTargetKind.ABSENT) {
                reporter("preserved: ${indexPath.toAbsolutePath()} (${now.reason})")
                return IndexWriteResult.PRESERVED
            }
            try {
                // Exclusive create: hard-link first (EEXIST does not replace).
                // Fall back to a create-only move when hard links are unavailable.
                publish_exclusive(tempPath, indexPath, createLink, moveFile)
            } catch (error: java.nio.file.FileAlreadyExistsException) {
                reporter("preserved: ${indexPath.toAbsolutePath()} (unowned)")
                return IndexWriteResult.PRESERVED
            }
        }
        if (backupPath != null) {
            Files.deleteIfExists(backupPath)
            backupPath = null
        }
        reporter(
            if (replacingOwned || replacingForced) {
                "replaced: ${indexPath.toAbsolutePath()}"
            } else {
                "created: ${indexPath.toAbsolutePath()}"
            }
        )
        return if (replacingOwned || replacingForced) {
            IndexWriteResult.REPLACED
        } else {
            IndexWriteResult.CREATED
        }
    } catch (error: Exception) {
        val restoreFrom = backupPath
        if (restoreFrom != null && Files.exists(restoreFrom, LinkOption.NOFOLLOW_LINKS)) {
            val occupant = classifyTarget(indexPath.toFile())
            var restored = false
            if (occupant.kind == IndexTargetKind.ABSENT || occupant.kind == IndexTargetKind.OWNED) {
                try {
                    if (occupant.kind == IndexTargetKind.ABSENT) {
                        publish_exclusive(restoreFrom, indexPath, createLink, moveFile)
                        Files.deleteIfExists(restoreFrom)
                    } else {
                        moveFile(restoreFrom, indexPath, arrayOf(StandardCopyOption.REPLACE_EXISTING))
                    }
                    restored = true
                    backupPath = null
                } catch (_: Exception) {
                    restored = false
                }
            }
            if (!restored) {
                preserveBackupAfterRestoreFailure = true
                reporter("backup-retained: ${restoreFrom.toAbsolutePath()}")
            }
        }
        throw error
    } finally {
        Files.deleteIfExists(tempPath)
        val leftoverBackup = backupPath
        if (leftoverBackup != null && !preserveBackupAfterRestoreFailure) {
            Files.deleteIfExists(leftoverBackup)
        }
    }
}
 
fun process_dir(
    curr_dir: File,
    excludeSet: Set<String>? = null,
    dirFiles: Array<File>? = null,
    forceOverwrite: Boolean = false,
    reporter: (String) -> Unit = ::default_index_reporter
){
    
    val exclude: Set<String> = excludeSet ?: process_ignore_file(curr_dir)
    val directoryName = curr_dir.name.ifEmpty { "Root" }

    val index_top = """<!doctype html>
<html lang="ko">
     <head>
        <meta charset="UTF-8">
        $GENERATED_OWNERSHIP_MARKER
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
         <h1>${directoryName.escapeHtml()}</h1>
         <nav aria-label="디렉토리 목록">
         <ul role="list">
            <li><a class="dir-link" href="./.." title="상위 디렉토리로 이동"><span class="icon" aria-hidden="true">&#x21B0;</span> <span aria-hidden="true">..</span> <span class="visually-hidden">상위 디렉토리로 이동</span></a></li>
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
                  val typeLabel = if (isLinkedDirectory) { "디렉토리" } else { "파일" }
                  val icon = if (isLinkedDirectory) { "&#128193;" } else { "&#128196;" }
                  l.append("""          <li><a class="dir-link" href="${encodedHref}" title="${ariaLabel}"><span class="icon" aria-hidden="true">${icon}</span> <span>${fileName.escapeHtml()}</span> <span class="visually-hidden">${typeLabel}</span></a></li>""")
                  l.append('\n')
               }
           }
        }

        if(l.isEmpty()){
            l.append("""          <li><div class="empty-dir" role="status"><span class="icon" aria-hidden="true">&#128194;</span> <span>이 디렉토리는 비어 있습니다.</span></div></li>""")
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
       write_index_file(curr_dir, index_top+index_middle()+index_bottom, forceOverwrite, reporter)
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
