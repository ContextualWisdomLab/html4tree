package html4tree

import org.junit.Test
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IgnorePolicyAtomicReadTest {
    @Test
    fun policySwapAfterValidationCannotChangeParsedAuthority() {
        val directory = Files.createTempDirectory("html4tree-policy-swap-")
        try {
            val policy = directory.resolve(".html4ignore")
            val replacement = directory.resolve("replacement.ignore")
            Files.write(policy, "secret.txt\n".toByteArray(Charsets.UTF_8))
            Files.write(replacement, "public.txt\n".toByteArray(Charsets.UTF_8))

            val excluded = process_ignore_file_with_validation_hook(
                directory.toFile(),
                arrayOf("secret.txt", "public.txt")
            ) {
                Files.move(replacement, policy, StandardCopyOption.REPLACE_EXISTING)
            }

            assertTrue("secret.txt" in excluded, "the opened policy must remain the authority")
            assertFalse("public.txt" in excluded, "a replacement policy must not become authoritative")
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun malformedUtf8PolicyFailsClosed() {
        val directory = Files.createTempDirectory("html4tree-policy-utf8-")
        try {
            Files.write(directory.resolve(".html4ignore"), byteArrayOf(0xC3.toByte(), 0x28))

            assertFailsWith<IgnoreFileReadException> {
                process_ignore_file(directory.toFile(), arrayOf("secret.txt"))
            }
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun actualReadLimitFailsClosedEvenWhenReportedSizeIsSmall() {
        var remaining = 1024 * 1024 + 1
        var emitZeroRead = true
        val channel = object : SeekableByteChannel {
            override fun read(dst: ByteBuffer): Int {
                if (emitZeroRead) {
                    emitZeroRead = false
                    return 0
                }
                if (remaining == 0) return -1
                val count = minOf(dst.remaining(), remaining)
                dst.put(ByteArray(count) { 'a'.toByte() })
                remaining -= count
                return count
            }

            override fun write(src: ByteBuffer): Int = throw UnsupportedOperationException()
            override fun position(): Long = 0L
            override fun position(newPosition: Long): SeekableByteChannel = this
            override fun size(): Long = 0L
            override fun truncate(size: Long): SeekableByteChannel = this
            override fun isOpen(): Boolean = true
            override fun close() {}
        }

        assertFailsWith<IgnoreFileReadException> {
            read_ignore_policy_bytes(channel)
        }
    }
}
