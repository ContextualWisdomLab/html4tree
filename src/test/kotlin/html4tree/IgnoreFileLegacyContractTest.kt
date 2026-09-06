package html4tree

import org.junit.Assume
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IgnoreFileLegacyContractTest {
    private fun assertTypedReadFailure(block: () -> Unit) {
        val error = assertFailsWith<IgnoreFileReadException> { block() }
        assertTrue(error.cause is IOException, "declared invalid policy must preserve the I/O cause")
    }

    @Test
    fun declaredIgnoreDirectoryFailsClosedWithIoCause() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-dir-").toFile()
        try {
            File(tempDir, ".html4ignore").mkdir()

            assertTypedReadFailure {
                process_ignore_file(tempDir, null)
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun declaredIgnoreSymlinkFailsClosedWithIoCause() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-link-").toFile()
        try {
            val target = File(tempDir, "target.ignore")
            target.writeText("*.txt")
            val link = File(tempDir, ".html4ignore")
            try {
                Files.createSymbolicLink(link.toPath(), target.toPath())
            } catch (_: Exception) {
                Assume.assumeTrue("Symlink creation not supported in this environment", false)
            }

            assertTypedReadFailure {
                process_ignore_file(tempDir, null)
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun declaredOversizedIgnoreFileFailsClosedWithIoCause() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-large-").toFile()
        try {
            File(tempDir, ".html4ignore").writeText("a".repeat(1_048_577))

            assertTypedReadFailure {
                process_ignore_file(tempDir, null)
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
