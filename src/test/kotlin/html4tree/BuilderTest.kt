package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class BuilderTest {

    @Test
    fun testIndexMiddleStringBuilderPreAllocation() {
        val tempPath = Files.createTempDirectory("html4tree-test")
        val tempDir = tempPath.toFile()
        try {
            File(tempDir, "file1.txt").createNewFile()
            File(tempDir, "file2.txt").createNewFile()
            File(tempDir, "dir1").mkdir()

            process_dir(tempDir)

            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists(), "index.html should be created")
            val content = indexFile.readText()
            assertTrue(content.contains("file1.txt"), "Content should contain file1.txt")
            assertTrue(content.contains("dir1"), "Content should contain dir1")

        } finally {
            tempDir.deleteRecursively()
        }
    }
}
