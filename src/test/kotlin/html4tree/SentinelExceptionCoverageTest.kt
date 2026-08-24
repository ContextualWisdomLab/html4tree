package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import java.io.IOException

class SentinelExceptionCoverageTest {
    @Test
    fun testProcessIgnoreFileWithIOException() {
        val mockDir = object : File("test") {
            override fun getAbsolutePath(): String {
                throw IOException("mock")
            }
        }
        val excluded = process_ignore_file(mockDir, arrayOf())
        assertTrue(excluded != null)
    }

    @Test
    fun testProcessIgnoreFileWithSecurityException() {
        val mockDir = object : File("test") {
            override fun getAbsolutePath(): String {
                throw SecurityException("mock")
            }
        }
        val excluded = process_ignore_file(mockDir, arrayOf())
        assertTrue(excluded != null)
    }
}
