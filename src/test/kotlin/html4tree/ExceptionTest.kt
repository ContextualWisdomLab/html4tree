package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertFailsWith

class ExceptionTest {
    @Test
    fun testGoRequireDirExists() {
        val notExists = File("non_existent_dir_12345")
        assertFailsWith<IllegalArgumentException> {
            go(notExists.absolutePath, -1)
        }
    }

    @Test
    fun testGoRequireIsDirectory() {
        val tempFile = File.createTempFile("temp", ".txt")
        tempFile.deleteOnExit()
        assertFailsWith<IllegalArgumentException> {
            go(tempFile.absolutePath, -1)
        }
    }
}
