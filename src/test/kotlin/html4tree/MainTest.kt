package html4tree

import org.junit.Test
import org.junit.After
import org.junit.Before
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class MainTest {
    private val outContent = ByteArrayOutputStream()
    private val originalOut = System.out

    @Before
    fun setUpStreams() {
        System.setOut(PrintStream(outContent))
    }

    @After
    fun restoreStreams() {
        System.setOut(originalOut)
        val dir = File("test_dir")
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    @Test
    fun testProcessIgnoreFileEmpty() {
        val dir = File("test_dir")
        dir.mkdir()
        val ignored = process_ignore_file(dir)
        assertEquals(listOf("index.html"), ignored)
    }

    @Test
    fun testProcessIgnoreFileWithContent() {
        val dir = File("test_dir")
        dir.mkdir()
        File(dir, ".html4ignore").writeText(".*\\.txt\nignored_dir")
        File(dir, "file1.txt").createNewFile()
        File(dir, "file2.doc").createNewFile()
        File(dir, "ignored_dir").mkdir()

        val ignored = process_ignore_file(dir)
        assertTrue(ignored.contains("file1.txt"))
        assertTrue(ignored.contains("ignored_dir"))
        assertTrue(ignored.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileIndexHtmlIncluded() {
        val dir = File("test_dir")
        dir.mkdir()
        File(dir, ".html4ignore").writeText("index\\.html")
        File(dir, "index.html").createNewFile()
        val ignored = process_ignore_file(dir)
        assertEquals(listOf("index.html"), ignored)
    }

    @Test
    fun testGoMaxLevelLogic() {
        val dir = File("test_dir")
        dir.mkdir()
        val subdir1 = File(dir, "subdir1")
        subdir1.mkdir()
        val subdir2 = File(subdir1, "subdir2")
        subdir2.mkdir()
        val subdir3 = File(subdir2, "subdir3")
        subdir3.mkdir()

        // Ensure maxLevel handles both conditions by explicitly testing maxLevel != -1 branch properly.
        // We also want to ensure the `if(maxLevel == -1 || currentLevel <= maxLevel)` evaluates the false condition.
        // This is covered when testing depth > maxLevel.
        go(dir.absolutePath, 1)
        assertTrue(File(dir, "index.html").exists())
        assertTrue(File(subdir1, "index.html").exists())
        assertTrue(!File(subdir2, "index.html").exists())
        assertTrue(!File(subdir3, "index.html").exists())

        // now maxLevel == -1 branch
        go(dir.absolutePath, -1)
        assertTrue(File(subdir3, "index.html").exists())
    }

    @Test
    fun testProcessDir() {
        val dir = File("test_dir")
        dir.mkdir()
        File(dir, "subdir").mkdir()
        File(dir, "file1.txt").createNewFile()

        process_dir(dir)

        val indexFile = File(dir, "index.html")
        assertTrue(indexFile.exists())
        val content = indexFile.readText()
        assertTrue(content.contains("test_dir"))
        assertTrue(content.contains("href=./subdir/"))
        assertTrue(content.contains("href=./file1.txt"))
        assertTrue(content.contains("subdir"))
        assertTrue(content.contains("file1.txt"))
    }

    @Test
    fun testGo() {
        val dir = File("test_dir")
        dir.mkdir()
        val subdir1 = File(dir, "subdir1")
        subdir1.mkdir()
        val subdir2 = File(subdir1, "subdir2")
        subdir2.mkdir()

        File(dir, "file1.txt").createNewFile()

        go(dir.absolutePath, 0)

        assertTrue(File(dir, "index.html").exists())
        assertTrue(!File(subdir1, "index.html").exists())

        go(dir.absolutePath, 1)
        assertTrue(File(subdir1, "index.html").exists())
        assertTrue(!File(subdir2, "index.html").exists())

        go(dir.absolutePath, -1)
        assertTrue(File(subdir2, "index.html").exists())
    }

    @Test
    fun testGoInvalidDir() {
        assertFailsWith<IllegalArgumentException> {
            go("non_existent_dir", -1)
        }
    }

    @Test
    fun testGoNotADir() {
        val file = File("test_file.txt")
        file.createNewFile()
        assertFailsWith<IllegalArgumentException> {
            go("test_file.txt", -1)
        }
        file.delete()
    }

    @Test
    fun testGoFileInQueue() {
        val dir = File("test_dir")
        dir.mkdir()
        val dummyFile = File(dir, "dummy.txt")
        dummyFile.createNewFile()

        // This exercises the false branch of `if (it.isDirectory())`
        // inside the `lle.file.listFiles().forEach` loop.
        go(dir.absolutePath, 0)

        // Also ensure while(lle != null && lle.file.isDirectory()) hits false on the second part.
        // It's impossible to push a file to `ll` via go(), but `go` logic assumes `lle.file.isDirectory()` check.
        // The check inside `go()` is mostly a defense, to hit it we could try reflecting but we just accept it's
        // covered as much as logically possible without mocking File or LinkedList internals.
    }

    @Test
    fun testCliMain() {
        val dir = File("test_dir")
        dir.mkdir()
        main(arrayOf("--max-level", "0", dir.absolutePath))
        assertTrue(File(dir, "index.html").exists())
    }

    @Test
    fun testHelp() {
        help()
        assertEquals("ERROR: help has not been written yet!\n", outContent.toString().replace("\r\n", "\n"))
    }
}