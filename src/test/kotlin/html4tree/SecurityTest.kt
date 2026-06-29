package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import org.junit.Assume

class SecurityTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-security-").toFile()
    }

    @After
    fun teardown() {
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testSymlinkIndexHtmlVulnerability() {
        val targetFile = File(tempDir, "target.txt")
        targetFile.writeText("secret")

        val indexFile = File(tempDir, "index.html")
        try {
            Files.createSymbolicLink(indexFile.toPath(), targetFile.absoluteFile.toPath())
        } catch (e: Exception) {
            Assume.assumeTrue("Symlink creation not supported in this environment", false)
        }

        process_dir(tempDir)

        // The target file should NOT be overwritten with HTML
        assertEquals("secret", targetFile.readText(), "Arbitrary file write vulnerability: target of symlink was overwritten!")
    }
}
