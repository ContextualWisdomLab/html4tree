package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File

class MainTest {

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = createTempDir("html4tree_test")
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText(".*\\.txt\nignored_file\\.log\n")

            File(tempDir, "file1.txt").createNewFile()
            File(tempDir, "file2.log").createNewFile()
            File(tempDir, "ignored_file.log").createNewFile()

            val excluded = process_ignore_file(tempDir)

            assertTrue("file1.txt" in excluded)
            assertTrue("ignored_file.log" in excluded)
            assertTrue("index.html" in excluded) // index.html is always excluded
            assertTrue("file2.log" !in excluded)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessIgnoreFileNoIgnoreFile() {
        val tempDir = createTempDir("html4tree_test")
        try {
            val excluded = process_ignore_file(tempDir)
            assertEquals(1, excluded.size)
            assertTrue("index.html" in excluded)

            // To cover "index.html" in files_to_exclude branch
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("index\\.html\n")
            val excluded2 = process_ignore_file(tempDir)
            assertEquals(1, excluded2.size)
            assertTrue("index.html" in excluded2)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessDir() {
        val tempDir = createTempDir("html4tree_test")
        try {
            File(tempDir, "file1.txt").createNewFile()
            val subDir = File(tempDir, "subdir")
            subDir.mkdir()
            File(subDir, "file2.txt").createNewFile()

            process_dir(tempDir)

            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists())

            val content = indexFile.readText()
            assertTrue(content.contains("file1.txt"))
            assertTrue(content.contains("subdir"))
            assertTrue(content.contains("&#128193;")) // Folder icon
            assertTrue(content.contains("&rtrif;")) // File icon
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testGo() {
        val tempDir = createTempDir("html4tree_test")
        try {
            val subDir1 = File(tempDir, "subdir1")
            subDir1.mkdir()
            val subDir2 = File(tempDir, "subdir2")
            subDir2.mkdir()
            val subSubDir = File(subDir1, "subsubdir")
            subSubDir.mkdir()

            // To cover lle != null && lle.file.isDirectory() - create a file instead of a dir
            // lle.file.isDirectory() will be false and terminate loop early or skip processing
            val file1 = File(tempDir, "file_as_dir")
            file1.createNewFile()

            // maxLevel 1 - should only process top level and first level subdirs
            go(tempDir.absolutePath, 1)

            assertTrue(File(tempDir, "index.html").exists())
            assertTrue(File(subDir1, "index.html").exists())
            assertTrue(File(subDir2, "index.html").exists())
            assertTrue(!File(subSubDir, "index.html").exists()) // Not generated due to level limit

            // maxLevel -1 (unlimited)
            go(tempDir.absolutePath, -1)
            assertTrue(File(subSubDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("non_existent_directory_12345", -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoNotADir() {
        val tempFile = File.createTempFile("html4tree_test", ".txt")
        try {
            go(tempFile.absolutePath, -1)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testHelp() {
        // Just call it to cover the lines
        help()
    }

    @Test
    fun testHtml4treeCommand() {
        val tempDir = createTempDir("html4tree_test")
        try {
            val cmd = Html4tree()
            cmd.main(arrayOf(tempDir.absolutePath))
            assertTrue(File(tempDir, "index.html").exists())

            // Test with max level argument
            val subDir = File(tempDir, "sub")
            subDir.mkdir()
            val cmd2 = Html4tree()
            cmd2.main(arrayOf("--max-level", "0", tempDir.absolutePath))
            assertTrue(!File(subDir, "index.html").exists())

            // Test main function wrapper directly to cover line 19
            main(arrayOf(tempDir.absolutePath))
            assertTrue(File(tempDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
