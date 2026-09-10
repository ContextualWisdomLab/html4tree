package html4tree

import java.io.File
import java.io.IOException
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardOpenOption

private const val MAX_IGNORE_FILE_BYTES = 1_048_576L

/**
 * Reads an ignore-policy file without following a final-component symlink.
 *
 * This package-local extension intentionally takes precedence over Kotlin's
 * default [File.useLines] extension for html4tree call sites. The policy file
 * is opened once with [LinkOption.NOFOLLOW_LINKS], and the size limit is then
 * checked on that opened channel. A path swapped to a symlink after metadata
 * validation therefore fails closed instead of being followed.
 */
internal fun <T> File.useLines(block: (Sequence<String>) -> T): T {
    Files.newByteChannel(
        toPath(),
        StandardOpenOption.READ,
        LinkOption.NOFOLLOW_LINKS
    ).use { channel ->
        if (channel.size() > MAX_IGNORE_FILE_BYTES) {
            throw IOException("Ignore file exceeds 1 MiB")
        }

        Channels.newReader(
            channel,
            StandardCharsets.UTF_8.newDecoder(),
            -1
        ).buffered().use { reader ->
            return block(reader.lineSequence())
        }
    }
}
