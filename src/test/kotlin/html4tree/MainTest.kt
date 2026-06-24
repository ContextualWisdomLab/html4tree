package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun testProcessDir() {
        val tempDir = File("temp_test_dir")
        tempDir.mkdir()
        try {
            process_dir(tempDir)
            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists())
            val content = indexFile.readText()
            assertTrue(content.contains("<!doctype html>"))
            assertTrue(content.contains("<html lang=\"en\">"))
            assertTrue(content.contains("<meta charset=\"utf-8\">"))
            assertTrue(content.contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"))
            assertTrue(content.contains("<main>"))
            assertTrue(content.contains("aria-label=\"Go to parent directory\""))
        } finally {
            File(tempDir, "index.html").delete()
            tempDir.delete()
        }
    }
}
