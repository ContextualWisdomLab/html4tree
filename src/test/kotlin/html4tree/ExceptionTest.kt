package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertFailsWith

class ExceptionTest {
    @Test
    fun testGoRequire() {
        assertFailsWith(IllegalArgumentException::class) {
            go("non_existent_dir_123", 0)
        }

        val tempFile = File.createTempFile("test", "file")
        try {
            assertFailsWith(IllegalArgumentException::class) {
                go(tempFile.absolutePath, 0)
            }
        } finally {
            tempFile.delete()
        }
    }
}
