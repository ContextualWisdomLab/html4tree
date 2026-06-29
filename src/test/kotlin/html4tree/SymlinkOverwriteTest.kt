package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SymlinkOverwriteTest {

    @Test
    fun testSymlinkNotOverwritten() {
        val tempDir = Files.createTempDirectory("testdir").toFile()
        val secretFile = File(tempDir.parentFile, "secret.txt")
        secretFile.writeText("top_secret_content")

        val indexSymlink = File(tempDir, "index.html")
        Files.createSymbolicLink(indexSymlink.toPath(), secretFile.toPath())

        process_dir(tempDir)

        assertEquals("top_secret_content", secretFile.readText(), "Secret file should not be overwritten")
        val newIndexFile = File(tempDir, "index.html")
        assertTrue(newIndexFile.readText().contains("<!doctype html>"), "Index file should be generated")
    }
}
