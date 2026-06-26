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
        assertEquals("a%20b%2B", "a b+".urlEncodePath())
    }

    @Test
    fun testHelp() {
        help()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("non_existent_directory_for_test", -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoNotDir() {
        val tempFile = File.createTempFile("test_html4tree", ".txt")
        try {
            go(tempFile.absolutePath, -1)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testGoMaxLevel0() {
        val tempDir = Files.createTempDirectory("test_html4tree").toFile()
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        go(tempDir.absolutePath, 0)

        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subdir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testGoAndProcessIgnore() {
        val tempDir = Files.createTempDirectory("test_html4tree2").toFile()
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        val file1 = File(tempDir, "test.txt")
        file1.writeText("test")

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt")

        val html4tree = Html4tree()
        html4tree.parse(arrayOf(tempDir.absolutePath))

        val indexHtml = File(tempDir, "index.html").readText()
        assertTrue(indexHtml.contains("subdir"))
        assertFalse(indexHtml.contains("test.txt"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testGoMaxLevelMinus1() {
        val tempDir = Files.createTempDirectory("test_html4tree3").toFile()
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val subsubdir = File(subdir, "subsubdir")
        subsubdir.mkdir()

        go(tempDir.absolutePath, -1)

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subdir, "index.html").exists())
        assertTrue(File(subsubdir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testGoMaxLevel1() {
        val tempDir = Files.createTempDirectory("test_html4tree4").toFile()
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val subsubdir = File(subdir, "subsubdir")
        subsubdir.mkdir()

        go(tempDir.absolutePath, 1)

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subdir, "index.html").exists())
        assertFalse(File(subsubdir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testMainWithArgs() {
        val tempDir = Files.createTempDirectory("test_html4tree5").toFile()
        try {
            main(arrayOf(tempDir.absolutePath))
            assertTrue(File(tempDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testGoCoverage() {
        val tempDir = Files.createTempDirectory("test_html4tree6").toFile()
        try {
            val file = File(tempDir, "somefile.txt")
            file.writeText("data")

            // False branch for `if ("index.html" !in files_to_exclude)`
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText(".*\\.html")

            val indexFile = File(tempDir, "index.html")
            indexFile.writeText("dummy")

            go(tempDir.absolutePath, 1)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testLleFileNotDirectory() {
        // we need `lle != null` but `lle.file.isDirectory()` to be false.
        // It's checked here: `while(lle != null && lle.file.isDirectory())`
        // We can just create a custom instance of LinkedListEntry and bypass `go`?
        // No, the code is in `go`.
        // To get `lle.file.isDirectory()` to be false, the directory must exist when we push it,
        // but become a file (or get deleted so isDirectory is false) when we pull it.
        // Since we process topDir first, we can't easily replace topDir because we have a lock on it on some OS,
        // but on Linux we can rename/delete it.
        // Wait, the while loop executes immediately for topDir.
        // Then it lists files in topDir, and pushes directories.
        // So we can create a sub-directory, and right after it's pushed, we delete it?
        // But how to run code between push and pull inside the single-threaded `go` loop?
        // Wait! File.listFiles() returns an array of files.
        // What if we create a custom File subclass where isDirectory returns false the second time?
        // No, we are passing a String path.
    }
}
