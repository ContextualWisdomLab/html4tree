package html4tree

import org.junit.After
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
}
