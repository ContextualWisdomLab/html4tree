package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class MainFunTest {
    @Test
    fun testMain() {
        val tempDir = createTempDir("mainTestDir")
        try {
            main(arrayOf("--max-level", "0", tempDir.absolutePath))
            assertTrue(File(tempDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testHelp() {
        help()
    }
}
