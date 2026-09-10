package html4tree

import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardOpenOption

private const val MAX_IGNORE_FILE_BYTES = 1_048_576L
private const val MAX_IGNORE_FILE_LINES = 1000

/**
 * Input stream that rejects policy content as soon as consumption exceeds the byte limit.
 *
 * The opened channel can grow after the initial size check, so the limit must remain an
 * invariant of the read itself rather than a one-time pathname or descriptor observation.
 */
private class BoundedPolicyInputStream(
    input: InputStream,
    private val maxBytes: Long
) : FilterInputStream(input) {
    private var bytesRead = 0L

    override fun read(): Int {
        val value = super.read()
        if (value >= 0) {
            recordBytes(1)
        }
        return value
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val count = super.read(buffer, offset, length)
        if (count > 0) {
            recordBytes(count)
        }
        return count
    }

    private fun recordBytes(count: Int) {
        bytesRead += count.toLong()
        if (bytesRead > maxBytes) {
            throw IOException("Ignore file exceeds 1 MiB")
        }
    }
}

/**
 * Reads an ignore-policy file without following a final-component symlink.
 *
 * This package-local extension intentionally takes precedence over Kotlin's
 * default [File.useLines] extension for html4tree call sites. The policy file
 * is opened once with [LinkOption.NOFOLLOW_LINKS]. The descriptor size is
 * checked immediately and bounded consumption enforces both the 1 MiB byte
 * limit and the 1000-line policy limit, so growth or silent truncation cannot
 * turn an over-limit policy into a partially applied allowlist.
 */
internal fun <T> File.useLines(block: (Sequence<String>) -> T): T =
    Files.newByteChannel(
        toPath(),
        StandardOpenOption.READ,
        LinkOption.NOFOLLOW_LINKS
    ).use { channel ->
        if (channel.size() > MAX_IGNORE_FILE_BYTES) {
            throw IOException("Ignore file exceeds 1 MiB")
        }

        val boundedInput = BoundedPolicyInputStream(
            Channels.newInputStream(channel),
            MAX_IGNORE_FILE_BYTES
        )
        InputStreamReader(
            boundedInput,
            StandardCharsets.UTF_8.newDecoder()
        ).buffered().use { reader ->
            val boundedLines = sequence {
                var lineCount = 0
                for (line in reader.lineSequence()) {
                    lineCount += 1
                    if (lineCount > MAX_IGNORE_FILE_LINES) {
                        throw IOException("Ignore file exceeds 1000 lines")
                    }
                    yield(line)
                }
            }
            block(boundedLines)
        }
    }
