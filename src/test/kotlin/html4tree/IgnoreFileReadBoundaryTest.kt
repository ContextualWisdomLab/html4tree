package html4tree

import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IgnoreFileReadBoundaryTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-ignore-boundary-").toFile()
    }

    @After
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun readStageIOExceptionIsMappedToDomainExceptionWithCause() {
        val policy = File(tempDir, ".html4ignore")
        policy.writeText("*.txt\n")

        val error = assertFailsWith<IgnoreFileReadException> {
            read_ignore_patterns(policy.toPath()) {
                ReadFailingChannel()
            }
        }

        assertTrue(error.cause is IOException)
        assertTrue(error.cause?.message?.contains("forced read failure") == true)
    }

    @Test
    fun oversizedPolicyReadFailsClosedAtTheReaderBoundary() {
        val policy = File(tempDir, ".html4ignore")
        policy.writeText("a".repeat(1048577))

        assertFailsWith<IgnoreFileReadException> {
            read_ignore_patterns(policy.toPath())
        }
    }

    @Test
    fun growthAfterOpenFailsClosedInsteadOfParsingAPartialSnapshot() {
        val policy = File(tempDir, ".html4ignore")
        policy.writeText("a")

        assertFailsWith<IgnoreFileReadException> {
            read_ignore_patterns(policy.toPath()) {
                GrowingChannel()
            }
        }
    }

    @Test
    fun symlinkReplacementAtOpenIsRejectedByNoFollowChannel() {
        val policy = File(tempDir, ".html4ignore")
        val replacement = File(tempDir, "replacement.ignore")
        policy.writeText("*.log\n")
        replacement.writeText("*.txt\n")

        val probe = File(tempDir, "symlink-probe")
        try {
            Files.createSymbolicLink(probe.toPath(), replacement.toPath())
            Files.delete(probe.toPath())
        } catch (e: Exception) {
            Assume.assumeTrue("Symlink creation not supported in this environment", false)
        }

        val error = assertFailsWith<IgnoreFileReadException> {
            read_ignore_patterns(policy.toPath()) { path ->
                Files.delete(path)
                Files.createSymbolicLink(path, replacement.toPath())
                open_ignore_policy_channel(path)
            }
        }

        assertTrue(error.cause is IOException)
    }

    private class ReadFailingChannel : SeekableByteChannel {
        private var open = true

        override fun read(dst: ByteBuffer): Int {
            throw IOException("forced read failure")
        }

        override fun write(src: ByteBuffer): Int {
            throw UnsupportedOperationException()
        }

        override fun position(): Long = 0L

        override fun position(newPosition: Long): SeekableByteChannel = this

        override fun size(): Long = 0L

        override fun truncate(size: Long): SeekableByteChannel = this

        override fun isOpen(): Boolean = open

        override fun close() {
            open = false
        }
    }

    private class GrowingChannel : SeekableByteChannel {
        private val bytes = "ab".toByteArray(Charsets.UTF_8)
        private var offset = 0
        private var open = true

        override fun read(dst: ByteBuffer): Int {
            if (offset >= bytes.size) return -1
            val count = minOf(dst.remaining(), bytes.size - offset)
            dst.put(bytes, offset, count)
            offset += count
            return count
        }

        override fun write(src: ByteBuffer): Int {
            throw UnsupportedOperationException()
        }

        override fun position(): Long = offset.toLong()

        override fun position(newPosition: Long): SeekableByteChannel {
            offset = newPosition.toInt()
            return this
        }

        override fun size(): Long = 1L

        override fun truncate(size: Long): SeekableByteChannel = this

        override fun isOpen(): Boolean = open

        override fun close() {
            open = false
        }
    }
}
