package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class MainTest {
    @Test
    fun testProcessIgnoreFile() {
        val testDir = File("test_dir_junit")
        testDir.mkdir()
        try {
            File(testDir, "file1.txt").writeText("test")
            File(testDir, "file2.png").writeText("test")
            File(testDir, ".html4ignore").writeText(".*\\.txt")

            val excludeList = process_ignore_file(testDir)

            assertTrue(excludeList.contains("file1.txt"))
            assertFalse(excludeList.contains("file2.png"))
            assertTrue(excludeList.contains("index.html"))
        } finally {
            testDir.deleteRecursively()
        }
    }
}
