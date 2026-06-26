package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class MainTest5 {
    @Test
    fun testRaceConditionForNotDirectory() {
        val tempDir = Files.createTempDirectory("test_html4tree_race3").toFile()
        try {
            val dir1 = File(tempDir, "dir1")
            dir1.mkdir()
            val dir2 = File(tempDir, "dir2")
            dir2.mkdir()

            // Limit loop
            val thread = kotlin.concurrent.thread(start = true) {
                var count = 0
                while (dir1.exists() && count < 1000) {
                    try {
                        dir1.delete()
                        dir1.writeText("not a dir anymore")
                    } catch (e: Exception) {}
                    count++
                }
            }

            go(tempDir.absolutePath, 1)
            thread.join(1000)

        } finally {
            tempDir.deleteRecursively()
        }
    }
}
