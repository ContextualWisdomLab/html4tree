package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class MainTest3 {
    @Test
    fun testRaceConditionForNotDirectory() {
        val tempDir = Files.createTempDirectory("test_html4tree_race3").toFile()
        try {
            // we will create a mock directory by subclassing File
            // oh wait, we can't easily pass a subclass of File to go() because it calls File(topDir)
            // `val top_dir = File(topDir)`

            // However, we can use a small trick:
            // What if a directory becomes a file while go() is executing?
            // "while(lle != null && lle.file.isDirectory())"
            val targetDir = File(tempDir, "target")
            targetDir.mkdir()
            File(targetDir, "f1.txt").writeText("a")

            // Wait, "var lle: LinkedListEntry? = ll.pull()
            // while(lle != null && lle.file.isDirectory())"
            // The first `lle.file` is `top_dir`. We can't delete `top_dir` while listing it on some OS, but on Linux we can.
            // But we need `lle != null` and `lle.file.isDirectory()` to be false.
            // Since we push subdirectories, when we pull them, they are tested.
            // So if we have a subdirectory, and we replace it with a file before it gets pulled, it will trigger it.

            val subdir = File(targetDir, "sub")
            subdir.mkdir()

            val thread = kotlin.concurrent.thread(start = true) {
                // Keep trying to replace sub with a file
                while(true) {
                    if (subdir.exists() && subdir.isDirectory) {
                        subdir.deleteRecursively()
                        subdir.writeText("not a dir")
                        break
                    }
                }
            }

            go(targetDir.absolutePath, 1)
            thread.join()

        } finally {
            tempDir.deleteRecursively()
        }
    }
}
