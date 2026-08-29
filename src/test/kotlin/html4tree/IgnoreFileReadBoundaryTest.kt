package html4tree

import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Paths
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IgnoreFileReadBoundaryTest {
    @Test
    fun testReadIgnoreFileBytesAcceptsExactLimit() {
        val bytes = ByteArray(1_048_576) { 97.toByte() }

        val result = read_ignore_file_bytes(ByteArrayInputStream(bytes))

        assertEquals(1_048_576, result?.size)
    }

    @Test
    fun testReadIgnoreFileBytesRejectsBytesPastLimit() {
        val bytes = ByteArray(1_048_577) { 97.toByte() }

        val result = read_ignore_file_bytes(ByteArrayInputStream(bytes))

        assertNull(result)
    }

    @Test
    fun testReadIgnoreFileSafelyTreatsIOExceptionAsAbsent() {
        val result = read_ignore_file_safely(Paths.get(".html4ignore")) {
            throw IOException("simulated path replacement")
        }

        assertNull(result)
    }
}
