package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IgnoreFileReadBoundaryTest {
    private lateinit var tempDir: File
    private lateinit var ignoreFile: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-ignore-boundary-").toFile()
        ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("*.txt\n")
    }

    @After
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun acceptsExactlyOneMegabyteFromOpenedStream() {
        val payload = ByteArray(1_048_576) { 0x61.toByte() }

        val result = read_ignore_file_bytes(ignoreFile) { ByteArrayInputStream(payload) }

        assertEquals(payload.size, result?.size)
    }

    @Test
    fun rejectsOpenedStreamThatGrowsPastOneMegabyte() {
        val payload = ByteArray(1_048_577) { 0x61.toByte() }

        val result = read_ignore_file_bytes(ignoreFile) { ByteArrayInputStream(payload) }

        assertNull(result)
    }

    @Test
    fun treatsOpenRaceAsAbsentIgnoreFile() {
        val result = read_ignore_file_bytes(ignoreFile) {
            throw IOException("simulated replacement race")
        }

        assertNull(result)
    }
}
