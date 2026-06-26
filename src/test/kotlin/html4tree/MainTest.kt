package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainTest {

    @Test
    fun testSymlinkOverwritePrevention() {
        val tempDir = Files.createTempDirectory("symlink_test").toFile()
        tempDir.deleteOnExit()

        val targetFile = File(tempDir, "target.txt")
        targetFile.writeText("original content")
        targetFile.deleteOnExit()

        val subDir = File(tempDir, "sub")
        subDir.mkdir()
        subDir.deleteOnExit()

        val symlinkFile = File(subDir, "index.html")
        Files.createSymbolicLink(symlinkFile.toPath(), targetFile.toPath())

        go(tempDir.absolutePath, -1)

        assertEquals("original content", targetFile.readText())
        assertTrue(symlinkFile.exists())
        assertTrue(!Files.isSymbolicLink(symlinkFile.toPath()))
    }

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("test%20path", "test path".urlEncodePath())
    }

    @Test
    fun testHelp() {
        help()
    }

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("ignore_test").toFile()
        tempDir.deleteOnExit()

        File(tempDir, ".html4ignore").writeText("secret.*")
        File(tempDir, "secret1.txt").createNewFile()
        File(tempDir, "public.txt").createNewFile()

        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("secret1.txt"))
        assertTrue(!excluded.contains("public.txt"))
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testMainWithArgs() {
        val tempDir = Files.createTempDirectory("main_test").toFile()
        tempDir.deleteOnExit()
        val args = arrayOf("--max-level", "1", tempDir.absolutePath)
        main(args)
        assertTrue(File(tempDir, "index.html").exists())
    }
}
