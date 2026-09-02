package html4tree

import java.io.File
import java.nio.ByteBuffer
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

private const val MAX_IGNORE_FILE_BYTES = 1_048_576
private const val MAX_IGNORE_LINES = 1_000
private const val MAX_IGNORE_PATTERN_LENGTH = 100

internal fun read_ignore_file_lines_no_follow(ignoreFile: File): List<String>? {
    val options = setOf<OpenOption>(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)
    return try {
        Files.newByteChannel(ignoreFile.toPath(), options).use { channel ->
            val buffer = ByteBuffer.allocate(MAX_IGNORE_FILE_BYTES + 1)
            var reachedEnd = false
            while (buffer.hasRemaining()) {
                val read = channel.read(buffer)
                if (read < 0) {
                    reachedEnd = true
                    break
                } else if (read == 0) {
                    return@use null
                }
            }
            if (!reachedEnd) {
                null
            } else {
                String(buffer.array(), 0, buffer.position(), Charsets.UTF_8)
                    .lineSequence()
                    .take(MAX_IGNORE_LINES)
                    .toList()
            }
        }
    } catch (_: Exception) {
        null
    }
}

internal fun collect_ignore_file_exclusions(
    currDir: File,
    dirFilesNames: Array<String>? = null,
    readIgnoreLines: (File) -> List<String>? = ::read_ignore_file_lines_no_follow
): Set<String> {
    val ignoreFile = File(currDir, ".html4ignore")
    val listedNames = dirFilesNames ?: currDir.list()
    val filesToExclude = mutableSetOf<String>()

    if (
        ignoreFile.isFile &&
        !Files.isSymbolicLink(ignoreFile.toPath()) &&
        ignoreFile.canRead() &&
        ignoreFile.length() <= MAX_IGNORE_FILE_BYTES
    ) {
        val lines = readIgnoreLines(ignoreFile)
        if (lines == null) {
            // A failed point-of-use read means the ignore policy is unknown.
            // Withhold the captured listing rather than publishing around it.
            listedNames?.let { filesToExclude.addAll(it) }
        } else {
            val ignoredMatchers = mutableListOf<java.nio.file.PathMatcher>()
            for (line in lines) {
                val pattern = line.trim()
                if (pattern.isNotEmpty() && pattern.length <= MAX_IGNORE_PATTERN_LENGTH) {
                    try {
                        ignoredMatchers.add(FileSystems.getDefault().getPathMatcher("glob:$pattern"))
                    } catch (_: IllegalArgumentException) {
                    }
                }
            }

            listedNames?.forEach { current ->
                val currentPath = try {
                    Paths.get(current)
                } catch (_: InvalidPathException) {
                    filesToExclude.add(current)
                    return@forEach
                }
                if (ignoredMatchers.any { matcher -> matcher.matches(currentPath) }) {
                    filesToExclude.add(current)
                }
            }
        }
    }

    filesToExclude.add("index.html")
    return filesToExclude
}
