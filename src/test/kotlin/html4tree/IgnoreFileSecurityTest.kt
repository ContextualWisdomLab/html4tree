package html4tree

import java.io.File
import java.nio.file.Files
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.Test

class IgnoreFileSecurityTest {
    @Test
    fun processIgnoreFileNormalizesSecurityExceptionFromMetadataRead() {
        val rootDir = Files.createTempDirectory("html4tree-policy-metadata-security-").toFile()
        val ignoreFile = File(rootDir, ".html4ignore")
        ignoreFile.writeText("test")

        try {
            val error = assertFailsWith<IgnoreFileReadException> {
                process_ignore_file(rootDir, null, readAttributes = { _ -> throw SecurityException("metadata access was denied") })
            }
            assertTrue(error.message?.contains("metadata access was denied") == true)
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun processIgnoreFileNormalizesIOExceptionFromMetadataRead() {
        // Since we can't easily mock Files.readAttributes without a mock library, and we aren't using one,
        // and we cannot use named pipes as they return false for isFile.
        // Wait! The user's PR added a test that mocks reading lines, not reading metadata.
        // But how to get coverage for Files.readAttributes throwing IOException or SecurityException?
        // Let's create a directory with no execute permission to its parent, which throws AccessDeniedException (an IOException subclass).
        val rootDir = Files.createTempDirectory("html4tree-policy-metadata-io-").toFile()
        val ignoreDir = File(rootDir, "subdir")
        ignoreDir.mkdir()
        val ignoreFile = File(ignoreDir, ".html4ignore")
        ignoreFile.writeText("test")
        ignoreDir.setExecutable(false)

        try {
            val error = assertFailsWith<IgnoreFileReadException> {
                process_ignore_file(ignoreDir, null)
            }
            assertTrue(error.message?.contains("metadata cannot be read securely") == true || error.message?.contains("metadata access was denied") == true)
        } finally {
            ignoreDir.setExecutable(true)
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun processIgnoreFileNormalizesSecurityExceptionFromContentRead() {
        val rootDir = Files.createTempDirectory("html4tree-policy-security-").toFile()
        val ignoreFile = File(rootDir, ".html4ignore")
        ignoreFile.writeText("secret.txt\n")

        try {
            val error = assertFailsWith<IgnoreFileReadException> {
                process_ignore_file(rootDir, arrayOf("secret.txt"), processLines = { _, _ ->
                    throw SecurityException("content read denied")
                })
            }

            assertTrue(error.message?.contains("content access was denied") == true)
        } finally {
            rootDir.deleteRecursively()
        }
    }
}
