package html4tree

import org.junit.Test
import org.junit.After
import org.junit.Before
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {

    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = createTempDir("html4tree_test")
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
        assertEquals("&lt;test&gt; &amp; &quot;hello&#x27;", "<test> & \"hello'".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("test%20folder", "test folder".urlEncodePath())
        assertEquals("test%2Bfolder", "test+folder".urlEncodePath())
        assertEquals("test%2Ffolder", "test/folder".urlEncodePath())
    }

    @Test
    fun testProcessIgnoreFile() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt\nignore_me")

        val txtFile = File(tempDir, "test.txt")
        txtFile.createNewFile()

        val ignoreMeFile = File(tempDir, "ignore_me")
        ignoreMeFile.createNewFile()

        val keepMeFile = File(tempDir, "keep_me")
        keepMeFile.createNewFile()

        val excluded = process_ignore_file(tempDir)

        assertTrue(excluded.contains("test.txt"))
        assertTrue(excluded.contains("ignore_me"))
        assertTrue(excluded.contains("index.html"))
        assertFalse(excluded.contains("keep_me"))
        assertFalse(excluded.contains(".html4ignore"))
    }

    @Test
    fun testProcessIgnoreFileNoIgnoreFile() {
        val excluded = process_ignore_file(tempDir)
        assertEquals(1, excluded.size)
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessDir() {
        val subDir = File(tempDir, "subDir")
        subDir.mkdir()

        val file1 = File(tempDir, "file1.txt")
        file1.createNewFile()

        process_dir(tempDir)

        val indexHtml = File(tempDir, "index.html")
        assertTrue(indexHtml.exists())

        val content = indexHtml.readText()
        assertTrue(content.contains(tempDir.name.escapeHtml()))
        assertTrue(content.contains("""<a style="display:block; width:100%" href="./subDir/">&#128193; subDir</a>"""))
        assertTrue(content.contains("""<a style="display:block; width:100%" href="./file1.txt">&rtrif; file1.txt</a>"""))
    }

    @Test
    fun testGoCommand() {
        val subDir1 = File(tempDir, "subDir1")
        subDir1.mkdir()
        val subDir2 = File(subDir1, "subDir2")
        subDir2.mkdir()

        go(tempDir.absolutePath, 1)

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subDir1, "index.html").exists())
        assertFalse(File(subDir2, "index.html").exists())
    }

    @Test
    fun testGoCommandMaxLevelUnlimited() {
        val subDir1 = File(tempDir, "subDir1")
        subDir1.mkdir()
        val subDir2 = File(subDir1, "subDir2")
        subDir2.mkdir()

        go(tempDir.absolutePath, -1)

        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subDir1, "index.html").exists())
        assertTrue(File(subDir2, "index.html").exists())
    }

    @Test
    fun testHtml4treeCommand() {
        val subDir1 = File(tempDir, "subDir1")
        subDir1.mkdir()

        Html4tree().parse(arrayOf(tempDir.absolutePath, "--max-level", "0"))

        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subDir1, "index.html").exists())
    }

    @Test
    fun testHtml4treeCommandRequireDirectory() {
        val file = File(tempDir, "test.txt")
        file.createNewFile()

        assertFailsWith<IllegalArgumentException> {
            Html4tree().parse(arrayOf(file.absolutePath))
        }
    }

    @Test
    fun testHelp() {
        val originalOut = System.out
        val baos = ByteArrayOutputStream()
        System.setOut(PrintStream(baos))

        help()

        System.setOut(originalOut)
        assertEquals("ERROR: help has not been written yet!\n", baos.toString())
    }

    @Test
    fun testMain() {
        // Need to run main(args) - we shouldn't do it directly if it calls System.exit via Clikt
        // But Clikt .main() will call exit if there's an error.
        // Since we pass valid arguments, it should just execute run() normally.
        main(arrayOf(tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
    }
}
