package html4tree

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

internal const val IGNORE_FILE_MAX_BYTES = 1_048_576

/**
 * Reads an `.html4ignore` candidate through one no-follow stream with a hard byte ceiling.
 *
 * Returning `null` means the candidate must be treated as absent. This intentionally covers
 * pre-open validation failures, replacement races, symlink swaps, read failures, and files that
 * grow beyond [IGNORE_FILE_MAX_BYTES] after the initial filesystem observation.
 */
internal fun read_ignore_file_bytes(
    ignoreFile: File,
    openInputStream: (Path) -> InputStream = { path ->
        Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)
    }
): ByteArray? {
    if (!ignoreFile.isFile || Files.isSymbolicLink(ignoreFile.toPath()) || !ignoreFile.canRead()) {
        return null
    }

    val inputStream = try {
        openInputStream(ignoreFile.toPath())
    } catch (_: IOException) {
        return null
    } catch (_: SecurityException) {
        return null
    }

    return try {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var totalBytes = 0

        while (true) {
            val remainingWithSentinel = IGNORE_FILE_MAX_BYTES + 1 - totalBytes
            val readCount = inputStream.read(
                buffer,
                0,
                minOf(buffer.size, remainingWithSentinel)
            )
            if (readCount == -1) {
                break
            }

            totalBytes += readCount
            if (totalBytes > IGNORE_FILE_MAX_BYTES) {
                return null
            }
            output.write(buffer, 0, readCount)
        }

        output.toByteArray()
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    } finally {
        try {
            inputStream.close()
        } catch (_: IOException) {
            // The stream is already unusable; preserve fail-closed absent-file semantics.
        }
    }
}
