package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class Html4treeTest {

    @Test
    fun testEscapeHtml() {
        val input = """<script>alert("XSS" & 'hack')</script>"""
        val expected = """&lt;script&gt;alert(&quot;XSS&quot; &amp; &#x27;hack&#x27;)&lt;/script&gt;"""
        assertEquals(expected, input.escapeHtml())
    }

    @Test
    fun testEncodeUrlPath() {
        val input = "file name with spaces.txt"
        val expected = "file%20name%20with%20spaces.txt"
        assertEquals(expected, input.encodeUrlPath())

        val input2 = """<script>"""
        val expected2 = "%3Cscript%3E"
        assertEquals(expected2, input2.encodeUrlPath())
    }

    @Test
    fun testProcessDirGeneratesSafeHtml() {
        val tempDir = Files.createTempDirectory("html4tree_test").toFile()
        tempDir.deleteOnExit()

        val badDirName = """<img src=x onerror=alert(1)>"""
        val badDir = File(tempDir, badDirName)
        badDir.mkdir()
        badDir.deleteOnExit()

        val badFileName = "file name.txt"
        val badFile = File(tempDir, badFileName)
        badFile.createNewFile()
        badFile.deleteOnExit()

        process_dir(tempDir)

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())
        indexFile.deleteOnExit()

        val content = indexFile.readText()

        // title, h1 escape
        assertTrue(content.contains("<title>${tempDir.name.escapeHtml()}</title>"))
        assertTrue(content.contains("<h1>${tempDir.name.escapeHtml()}</h1>"))

        // child dir escape and encode
        assertTrue(content.contains("""href="./%3Cimg%20src%3Dx%20onerror%3Dalert%281%29%3E/""""))
        assertTrue(content.contains("""&lt;img src=x onerror=alert(1)&gt;"""))

        // child file escape and encode
        assertTrue(content.contains("""href="./file%20name.txt""""))
        assertTrue(content.contains("""file name.txt"""))
    }

    @Test
    fun testGoMaxLevel() {
        val tempDir = Files.createTempDirectory("html4tree_test_go").toFile()
        tempDir.deleteOnExit()

        val subDir = File(tempDir, "subdir")
        subDir.mkdir()
        subDir.deleteOnExit()

        val subSubDir = File(subDir, "subsubdir")
        subSubDir.mkdir()
        subSubDir.deleteOnExit()

        // maxLevel 0
        go(tempDir.absolutePath, 0)
        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subDir, "index.html").exists())
        assertFalse(File(subSubDir, "index.html").exists())

        File(tempDir, "index.html").delete()

        // maxLevel 1
        go(tempDir.absolutePath, 1)
        assertTrue(File(tempDir, "index.html").exists())
        assertTrue(File(subDir, "index.html").exists())
        assertFalse(File(subSubDir, "index.html").exists())
    }

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("html4tree_test_ignore").toFile()
        tempDir.deleteOnExit()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt\nsecret\\.dat")
        ignoreFile.deleteOnExit()

        val txtFile = File(tempDir, "a.txt")
        txtFile.createNewFile()
        txtFile.deleteOnExit()

        val secretFile = File(tempDir, "secret.dat")
        secretFile.createNewFile()
        secretFile.deleteOnExit()

        val normalFile = File(tempDir, "b.png")
        normalFile.createNewFile()
        normalFile.deleteOnExit()

        val excludeList = process_ignore_file(tempDir)

        assertTrue(excludeList.contains("a.txt"))
        assertTrue(excludeList.contains("secret.dat"))
        assertTrue(excludeList.contains("index.html"))
        assertFalse(excludeList.contains("b.png"))
    }

    @Test
    fun testHelp() {
        help()
    }
}
