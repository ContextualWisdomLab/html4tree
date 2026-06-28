package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class MainTest {

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("test%20path%2B1", "test path+1".urlEncodePath())
    }

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("ignore_test").toFile()
        tempDir.deleteOnExit()

        File(tempDir, "test.txt").createNewFile()
        File(tempDir, "keep.md").createNewFile()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt")

        val excluded = process_ignore_file(tempDir)

        assertTrue("test.txt" in excluded)
        assertTrue("index.html" in excluded)
        assertFalse("keep.md" in excluded)
    }

    @Test
    fun testProcessIgnoreFileNoIgnoreFile() {
        val tempDir = Files.createTempDirectory("ignore_test_no_file").toFile()
        tempDir.deleteOnExit()

        val excluded = process_ignore_file(tempDir)
        assertTrue("index.html" in excluded)
    }

    @Test
    fun testProcessDir() {
        val tempDir = Files.createTempDirectory("process_dir_test").toFile()
        tempDir.deleteOnExit()

        File(tempDir, "subdir").mkdir()
        File(tempDir, "test.txt").createNewFile()

        // Excluded file via ignore file to cover branch
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt")

        // Exclude self (index.html)
        File(tempDir, "index.html").createNewFile()

        process_dir(tempDir)

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())

        val content = indexFile.readText()
        assertTrue(content.contains("<html lang=\"en\">"))
        assertTrue(content.contains("<meta charset=\"UTF-8\">"))
        assertTrue(content.contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"))
        assertTrue(content.contains("a:hover"))
        assertTrue(content.contains("a:focus"))
        assertTrue(content.contains("subdir"))
        // test.txt should be ignored and not present in output
        assertFalse(content.contains("test.txt"))

        // ensure index.html doesn't contain a link to itself
        assertFalse(content.contains(">index.html</a>"))
    }

    // Test a file that is not excluded, but is equal to curr_dir.
    // This isn't normally possible from `listFiles()` unless a file is returned that has the exact path
    // Let's create a custom list of files? `curr_dir.listFiles()` is used directly.
    // In kotlin, `it != curr_dir` checks equality of `File` object (which is path).
    // `listFiles()` children always have `curr_dir` as parent, so they can never equal `curr_dir`.
    // So `it != curr_dir` is practically always true.
    // Let's call process_dir with a directory to ensure 100% coverage or let it be.
    // The missing branch in `MainKt` is likely `if(it.isDirectory()) ll.push(...)` in `go` where a child is NOT a directory
    // or `if(maxLevel == -1 || currentLevel <= maxLevel)` where maxLevel != -1 AND currentLevel > maxLevel
    // Wait, let's create a non-directory file to cover `it.isDirectory()` being false in `go`

    @Test
    fun testGo() {
        val tempDir = Files.createTempDirectory("go_test").toFile()
        tempDir.deleteOnExit()
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val subsubdir = File(subdir, "subsubdir")
        subsubdir.mkdir()

        // This file will cover the branch `if(it.isDirectory())` evaluating to false
        File(tempDir, "a_file.txt").createNewFile()

        // This covers `if(maxLevel == -1 || currentLevel <= maxLevel)`
        // maxLevel = 0, currentLevel = 0 -> true
        // maxLevel = 0, currentLevel = 1 -> false -> skips `process_dir`
        go(tempDir.absolutePath, 0)
        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subdir, "index.html").exists())

        go(tempDir.absolutePath, 1)
        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subdir, "index.html").exists())
        assertFalse(File(subsubdir, "index.html").exists())
    }

    @Test
    fun testGoInfiniteMaxLevel() {
        val tempDir = Files.createTempDirectory("go_test_infinite").toFile()
        tempDir.deleteOnExit()
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val subsubdir = File(subdir, "subsubdir")
        subsubdir.mkdir()

        go(tempDir.absolutePath, -1)
        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subdir, "index.html").exists())
        assertTrue(File(subsubdir, "index.html").exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        val tempFile = Files.createTempFile("go_invalid_dir", ".tmp").toFile()
        tempFile.deleteOnExit()
        go(tempFile.absolutePath, 0)
    }

    @Test
    fun testHtml4treeCommand() {
        val tempDir = Files.createTempDirectory("command_test").toFile()
        tempDir.deleteOnExit()
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        main(arrayOf(tempDir.absolutePath, "--max-level", "0"))
        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subdir, "index.html").exists())

        val cmd = Html4tree()
        cmd.main(arrayOf(tempDir.absolutePath))
        assertTrue(File(subdir, "index.html").exists())
    }

    @Test
    fun testHelp() {
        // Just calling it to get coverage
        help()
    }
}
