package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class UseLinesExceptionTest {
    @Test
    fun testProcessIgnoreFileThrowsIOException() {
        val mockFile = object : File("test") {
            override fun getAbsolutePath(): String {
                throw java.io.IOException("Mock IO Exception")
            }
        }
        val result = process_ignore_file(mockFile, emptyArray())
        assertTrue(result.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileThrowsUncheckedIOException() {
        val mockFile = object : File("test") {
            override fun getAbsolutePath(): String {
                throw java.io.UncheckedIOException(java.io.IOException("Mock Unchecked IO Exception"))
            }
        }
        val result = process_ignore_file(mockFile, emptyArray())
        assertTrue(result.contains("index.html"))
    }
}
