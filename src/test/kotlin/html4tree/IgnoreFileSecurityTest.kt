package html4tree

import java.io.File
import java.nio.file.Files
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.Test

class IgnoreFileSecurityTest {
    @Test
    fun processIgnoreFileNormalizesSecurityExceptionFromContentRead() {
        val rootDir = Files.createTempDirectory("html4tree-policy-security-").toFile()
        val ignoreFile = File(rootDir, ".html4ignore")
        ignoreFile.writeText("secret.txt\n")

        try {
            val error = assertFailsWith<IgnoreFileReadException> {
                process_ignore_file(rootDir, arrayOf("secret.txt")) { _, _ ->
                    throw SecurityException("content read denied")
                }
            }

            assertTrue(error.message?.contains("content access was denied") == true)
        } finally {
            rootDir.deleteRecursively()
        }
    }
}
