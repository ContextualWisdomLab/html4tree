package html4tree

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.After
import java.io.File

class MainTest {

    val testDirName = "test_xss_dir"

    @After
    fun cleanup() {
        File(testDirName).deleteRecursively()
    }

    @Test
    fun testXssEscaping() {
        // Create test directory
        val testDir = File(testDirName)
        testDir.mkdir()

        // Create a file with XSS payload in its name
        // Use a simpler name to avoid OS restrictions on filenames
        val xssFileName = "file_with_<script>_and_\"_'_&_tags"
        val xssFile = File(testDir, xssFileName)
        xssFile.createNewFile()

        // Run process_dir
        process_dir(testDir)

        // Read generated index.html
        val indexFile = File(testDir, "index.html")
        assertTrue("index.html should exist", indexFile.exists())

        val content = indexFile.readText()

        // Check if characters are properly escaped in file names
        assertTrue("Should escape < to &lt;", content.contains("&lt;script&gt;"))
        assertTrue("Should escape \" to &quot;", content.contains("&quot;"))
        assertTrue("Should escape ' to &#x27;", content.contains("&#x27;"))
        assertTrue("Should escape & to &amp;", content.contains("&amp;"))

        // Ensure original payload does not exist in the file (except where we specifically want it to not be evaluated, though it's all escaped now)
        assertTrue("Should not contain unescaped <script>", !content.contains("<script>"))
    }
}
