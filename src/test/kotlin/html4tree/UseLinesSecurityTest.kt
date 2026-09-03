package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import java.io.IOException
import java.io.UncheckedIOException

class UseLinesSecurityTest {
    @Test
    fun testProcessIgnoreFileWithIOException() {
        val f = object : File("test") {
            override fun getAbsolutePath(): String {
                throw IOException("Mocked IOException")
            }
        }
        val excluded = process_ignore_file(f, null)
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileWithUncheckedIOException() {
        val f = object : File("test") {
            override fun getAbsolutePath(): String {
                throw UncheckedIOException(IOException("Mocked IOException"))
            }
        }
        val excluded = process_ignore_file(f, null)
        assertTrue(excluded.contains("index.html"))
    }
}
