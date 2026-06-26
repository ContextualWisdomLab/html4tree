package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class BranchTest11 {
    @Test
    fun testProcessDirLoop() {
        val tempDir = Files.createTempDirectory("process_loop").toFile()
        tempDir.deleteOnExit()
        val d1 = File(tempDir, "a")
        d1.mkdir()
        val d2 = File(tempDir, "b")
        d2.mkdir()
        val f1 = File(tempDir, "c.txt")
        f1.createNewFile()

        process_dir(tempDir)
        assert(File(tempDir, "index.html").exists())
    }
}
