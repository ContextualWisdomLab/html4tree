package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import java.io.IOException
import java.io.UncheckedIOException

class SentinelExceptionCoverageTest {
    @Test
    fun testProcessIgnoreFileWithIOException() {
        val mockDir = object : File("test") {
            override fun getAbsolutePath(): String {
                throw IOException("mock")
            }
        }
        val excluded = process_ignore_file(mockDir, arrayOf())
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileWithSecurityException() {
        val mockDir = object : File("test") {
            override fun getAbsolutePath(): String {
                throw SecurityException("mock")
            }
        }
        val excluded = process_ignore_file(mockDir, arrayOf())
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileWithUncheckedIOException() {
        val mockDir = object : File("test") {
            override fun getAbsolutePath(): String {
                throw UncheckedIOException(IOException("mock"))
            }
        }
        val excluded = process_ignore_file(mockDir, arrayOf())
        assertTrue(excluded.contains("index.html"))
    }
}
