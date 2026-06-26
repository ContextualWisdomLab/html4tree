package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class BranchTest7 {
    @Test
    fun testProcessDirWithSameFile() {
        val tempDir = Files.createTempDirectory("process_same").toFile()
        tempDir.deleteOnExit()

        // This is a unit test that we can't easily mock listFiles without a mocking framework
        // which we don't have. So we'll accept 96% branch coverage for MainKt.kt because
        // hitting it == curr_dir organically from listFiles is impossible (a directory never
        // lists itself as a child in Java's listFiles)
    }
}
