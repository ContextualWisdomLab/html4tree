package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class MainTest {
    @Test
    fun testProcessIgnoreFile() {
        val dir = File("test_dir")
        dir.mkdir()
        File(dir, ".html4ignore").writeText(".*\\.txt\n.*\\.log")
        File(dir, "a.txt").writeText("a")
        File(dir, "b.log").writeText("b")
        File(dir, "c.html").writeText("c")

        val excluded = process_ignore_file(dir)
        assertTrue(excluded.contains("a.txt"))
        assertTrue(excluded.contains("b.log"))
        assertFalse(excluded.contains("c.html"))
        assertTrue(excluded.contains("index.html"))

        dir.deleteRecursively()
    }

    @Test
    fun testProcessDir() {
        val dir = File("test_dir2")
        dir.mkdir()
        File(dir, "a.txt").writeText("a")
        File(dir, "b.html").writeText("b")

        process_dir(dir)

        val index = File(dir, "index.html")
        assertTrue(index.exists())
        val text = index.readText()
        assertTrue(text.contains("a.txt"))
        assertTrue(text.contains("b.html"))

        dir.deleteRecursively()
    }

    @Test
    fun testGo() {
        val dir = File("test_dir3")
        dir.mkdir()
        val sub = File(dir, "sub")
        sub.mkdir()
        File(sub, "a.txt").writeText("a")

        go(dir.absolutePath, -1)

        assertTrue(File(dir, "index.html").exists())
        assertTrue(File(sub, "index.html").exists())

        dir.deleteRecursively()
    }

    @Test
    fun testMain() {
        val dir = File("test_dir4")
        dir.mkdir()
        main(arrayOf(dir.absolutePath))
        assertTrue(File(dir, "index.html").exists())
        dir.deleteRecursively()
    }
}
