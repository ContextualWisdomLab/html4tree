package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class IgnoreFileTest {
    @Test
    fun testIgnoreFileShortcut() {
        val currDir = File(".")
        val dirFilesNames = arrayOf("a.txt", "b.txt")
        val exclude = process_ignore_file(currDir, dirFilesNames)
        assertTrue(exclude.contains("index.html"))
    }
}
