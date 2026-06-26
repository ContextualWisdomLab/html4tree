package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files
import kotlin.concurrent.thread

class MainTest2 {
    @Test
    fun testRaceConditionForNotDirectory() {
        val tempDir = Files.createTempDirectory("test_html4tree_race").toFile()
        try {
            // We want to create many subdirectories so that go() takes a while
            // Then in a background thread, we delete some subdirectories so they become non-directories
            for (i in 1..100) {
                File(tempDir, "subdir_$i").mkdir()
            }

            val bgThread = thread {
                Thread.sleep(5)
                for (i in 1..100) {
                    val subdir = File(tempDir, "subdir_$i")
                    if (subdir.exists()) {
                        subdir.deleteRecursively()
                        // Alternatively, replace with a file to ensure it exists but isDirectory() is false
                        subdir.writeText("not a dir anymore")
                    }
                }
            }

            go(tempDir.absolutePath, 1)
            bgThread.join()
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
