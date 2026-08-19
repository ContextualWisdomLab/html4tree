package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class IgnoreFileDosTest {
    @Test
    fun testProcessIgnoreFileWithException() {
        val dir = Files.createTempDirectory("test-ignore-dos").toFile()
        val ignoreFile = File(dir, ".html4ignore")
        // Write invalid UTF-8 bytes
        ignoreFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0xFF.toByte()))

        try {
            process_ignore_file(dir)
        } catch (e: Exception) {
            e.printStackTrace()
            org.junit.Assert.fail("process_ignore_file should not crash: " + e)
        } finally {
            dir.deleteRecursively()
        }
    }
}
