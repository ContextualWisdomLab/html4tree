package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class MainTest4 {
    @Test
    fun testSelfReference() {
        val tempDir = Files.createTempDirectory("test_html4tree_self_ref").toFile()
        try {
            // Can we create a subclass of File? Yes!
            val spyDir = object : File(tempDir.absolutePath) {
                override fun listFiles(): Array<File> {
                    // return ourselves as a child!
                    // So that it == curr_dir
                    val child = object : File(this.absolutePath) {
                         override fun getName(): String = "fakedir"
                    }
                    return arrayOf(this) // return ourselves to trigger it == curr_dir
                }
            }

            // call process_dir(spyDir)
            process_dir(spyDir)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
