package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class GoNormalizeTest {
    @Test
    fun testGoNormalize() {
        val tempDir = Files.createTempDirectory("html4tree-test-").toFile()
        try {
            // Test that normalize() logic works without errors by using a path ending with /..
            val subdir = File(tempDir, "subdir")
            subdir.mkdir()
            go(subdir.absolutePath + "/..", -1)
            assertTrue(File(tempDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
