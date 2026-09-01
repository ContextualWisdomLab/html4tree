package html4tree

import java.io.File
import java.io.IOException
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
                when {
                    read < 0 -> {
                        reachedEnd = true
                        break
                    }
                    read == 0 -> return@use null
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
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: UnsupportedOperationException) {
        null
    }
}

internal fun process_ignore_file_with_reader(
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
            // If the path changed or the bounded open failed after metadata checks,
            // do not publish entries that the unavailable ignore policy might hide.
            listedNames?.let(filesToExclude::addAll)
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
