package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("some%20dir", "some dir".urlEncodePath())
    }

    @Test
    fun testHelp() {
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))
        help()
        assertEquals("ERROR: help has not been written yet!\n", outContent.toString())
        System.setOut(originalOut)
    }

    @Test
    fun testGoAndProcessDir() {
        val tempDir = Files.createTempDirectory("test_html4tree").toFile()
        tempDir.deleteOnExit()

        val subDir1 = File(tempDir, "subdir1")
        subDir1.mkdir()
        subDir1.deleteOnExit()

        val file1 = File(tempDir, "file1.txt")
        file1.writeText("test")
        file1.deleteOnExit()

        // Create ignore file
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("file1.*")
        ignoreFile.deleteOnExit()

        // Test with max level 0
        go(tempDir.absolutePath, 0)
        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subDir1, "index.html").exists())
        File(tempDir, "index.html").deleteOnExit()

        // Test with max level 1
        go(tempDir.absolutePath, 1)
        assertTrue(File(subDir1, "index.html").exists())
        File(subDir1, "index.html").deleteOnExit()
    }

    @Test
    fun testSymlinkSkip() {
        val tempDir = Files.createTempDirectory("test_html4tree_symlink").toFile()
        tempDir.deleteOnExit()

        val targetDir = Files.createTempDirectory("test_html4tree_symlink_target").toFile()
        targetDir.deleteOnExit()

        val symlinkDir = File(tempDir, "symlink")
        Files.createSymbolicLink(symlinkDir.toPath(), targetDir.toPath())
        symlinkDir.deleteOnExit()

        go(tempDir.absolutePath, -1)
        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(targetDir, "index.html").exists())
        File(tempDir, "index.html").deleteOnExit()
    }

    @Test
    fun testMainClikt() {
        val tempDir = Files.createTempDirectory("test_clikt").toFile()
        tempDir.deleteOnExit()
        Html4tree().parse(arrayOf(tempDir.absolutePath, "--max-level", "1"))
        assertTrue(File(tempDir, "index.html").exists())
        File(tempDir, "index.html").deleteOnExit()
    }

    @Test
    fun testMainCliktDefault() {
        val tempDir = Files.createTempDirectory("test_clikt_default").toFile()
        tempDir.deleteOnExit()
        Html4tree().parse(arrayOf(tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
        File(tempDir, "index.html").deleteOnExit()
    }

    @Test
    fun testMainArrayArgs() {
        val tempDir = Files.createTempDirectory("test_main_args").toFile()
        tempDir.deleteOnExit()

        // Note: main(args) will call System.exit, so we need to intercept it or use a SecurityManager if testing normally.
        // However, we shouldn't call main() directly in unit tests since we test Html4tree().parse above.
        // Let's call it via reflection or try/catch if it causes issues, but for coverage, Clikt's main calls System.exit.
        // Wait, Html4tree().main() calls system exit on normal run too, not just parse! We just use .parse to avoid it.
        // But mainkt calls Html4tree().main(args), which calls System.exit(0) effectively via Clikt if we do it.
        // Wait, Clikt 2.7.1 .main() doesn't always System.exit unless there's an error.
        // Let's run it.
        try {
            main(arrayOf(tempDir.absolutePath))
        } catch (e: Exception) {
            // expected or not, just catching it.
        }
        assertTrue(File(tempDir, "index.html").exists())
        File(tempDir, "index.html").deleteOnExit()
    }

    @Test
    fun testUnwritableDir() {
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        val tempDir = Files.createTempDirectory("test_unwritable").toFile()
        tempDir.deleteOnExit()

        val unwritableDir = File(tempDir, "no_write")
        unwritableDir.mkdir()
        unwritableDir.deleteOnExit()

        // Ensure no write access
        unwritableDir.setWritable(false)

        go(tempDir.absolutePath, 1)

        assertTrue(outContent.toString().contains("Error writing index.html"))

        // Restore permissions to allow cleanup
        unwritableDir.setWritable(true)
        System.setOut(originalOut)
    }

    @Test
    fun testGoExceptions() {
        val tempDir = Files.createTempDirectory("test_go_exceptions").toFile()
        tempDir.deleteOnExit()
        val notADir = File(tempDir, "notadir.txt")
        notADir.writeText("test")
        notADir.deleteOnExit()

        try {
            go(notADir.absolutePath, 0)
            fail("Expected exception")
        } catch (e: Exception) {}

        try {
            go(File(tempDir, "doesnotexist").absolutePath, 0)
            fail("Expected exception")
        } catch (e: Exception) {}

        // Unreadable directory in go() listFiles
        val unreadable = File(tempDir, "unreadable")
        unreadable.mkdir()
        unreadable.deleteOnExit()
        unreadable.setReadable(false)
        go(unreadable.absolutePath, 0)
        unreadable.setReadable(true)

        // Test index_middle missing branch
        val unreadableListDir = File(tempDir, "unreadableList")
        unreadableListDir.mkdir()
        unreadableListDir.deleteOnExit()
        unreadableListDir.setReadable(false)
        unreadableListDir.setExecutable(false)
        process_dir(unreadableListDir)
        unreadableListDir.setExecutable(true)
        unreadableListDir.setReadable(true)
    }
}
