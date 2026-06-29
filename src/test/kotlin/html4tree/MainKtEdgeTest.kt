package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class MainKtEdgeTest {
    @Test
    fun testProcessDirSelfDirExclusion() {
        val tempDir = Files.createTempDirectory("html4tree_test_self").toFile()
        tempDir.deleteOnExit()

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*")

        val f1 = File(tempDir, "test.txt")
        f1.createNewFile()

        process_dir(tempDir)

        val content = File(tempDir, "index.html").readText()
        assert(!content.contains("test.txt"))
    }
}
