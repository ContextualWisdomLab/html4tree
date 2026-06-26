package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class BranchTest4 {
    @Test
    fun testProcessIgnoreFileWithoutIndex() {
        val tempDir = Files.createTempDirectory("ignore_no_index").toFile()
        tempDir.deleteOnExit()

        File(tempDir, ".html4ignore").writeText("index\\.html")
        val excluded = process_ignore_file(tempDir)
        assert(excluded.contains("index.html"))
    }
}
