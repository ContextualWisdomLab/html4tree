package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class MainKtTest {
    @Test
    fun testMain() {
        val tempDir = Files.createTempDirectory("html4tree_test_main").toFile()
        tempDir.deleteOnExit()
        val args = arrayOf(tempDir.absolutePath)
        main(args)

        val indexFile = File(tempDir, "index.html")
        assert(indexFile.exists())
    }
}
