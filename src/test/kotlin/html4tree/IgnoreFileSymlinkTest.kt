package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class IgnoreFileSymlinkTest {
    @Test
    fun testSymlinkIgnoreFileIsIgnored() {
        val tempDir = Files.createTempDirectory("ignore_test").toFile()
        try {
            val realIgnoreFile = File(tempDir, "real_ignore")
            realIgnoreFile.writeText("test_pattern")

            val ignoreLink = File(tempDir, ".html4ignore")
            Files.createSymbolicLink(ignoreLink.toPath(), realIgnoreFile.toPath())

            val excluded = process_ignore_file(tempDir, null)
            assertFalse(excluded.contains("test_pattern"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
