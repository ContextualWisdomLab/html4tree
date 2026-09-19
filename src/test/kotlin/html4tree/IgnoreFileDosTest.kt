package html4tree

import org.junit.Test
import org.junit.Assert.assertTrue
import java.io.File
import java.nio.file.Files

class IgnoreFileDosTest {
    @Test
    fun testProcessIgnoreFileReplacesMalformedUtf8WithoutCrashing() {
        val dir = Files.createTempDirectory("test-ignore-dos").toFile()
        val ignoreFile = File(dir, ".html4ignore")
        // Write invalid UTF-8 bytes
        ignoreFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0xFF.toByte()))

        try {
            val excludedNames = process_ignore_file(dir)
            assertTrue(excludedNames.contains(".html4ignore"))
        } finally {
            dir.deleteRecursively()
        }
    }
}
