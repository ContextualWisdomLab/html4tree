package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals

class SymlinkOverwriteTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-test-overwrite-").toFile()
    }

    @After
    fun teardown() {
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testIndexHtmlSymlinkOverwrite() {
        val targetDir = File(tempDir, "target")
        targetDir.mkdir()
        val secretFile = File(targetDir, "secret.txt")
        secretFile.writeText("SECRET_CONTENT")

        val workDir = File(tempDir, "work")
        workDir.mkdir()

        val symlink = File(workDir, "index.html")
        Files.createSymbolicLink(symlink.toPath(), secretFile.toPath())

        // Run process_dir
        process_dir(workDir)

        // Read secret file
        val content = secretFile.readText()
        assertEquals("SECRET_CONTENT", content, "Secret file was overwritten!")
    }
}
