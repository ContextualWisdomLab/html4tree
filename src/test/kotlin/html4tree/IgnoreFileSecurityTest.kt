package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFailsWith

class IgnoreFileSecurityTest {
    @Test
    fun processIgnoreFileNormalizesSecurityExceptionFromContentRead() {
        val rootDir = Files.createTempDirectory("html4tree-ignore-security-").toFile()
        File(rootDir, ".html4ignore").writeText("secret.txt")

        assertFailsWith<IgnoreFileReadException> {
            process_ignore_file(rootDir, arrayOf("secret.txt", ".html4ignore")) { _, _ ->
                throw SecurityException("policy read denied")
            }
        }
    }
}
