package html4tree

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.NotDirectoryException
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

    @Test
    fun processIgnoreFileTreatsMissingPolicyAsAbsent() {
        val rootDir = Files.createTempDirectory("html4tree-policy-missing-").toFile()
        try {
            val excluded = process_ignore_file(
                rootDir,
                arrayOf("plain.txt"),
                readPolicyAttributes = { throw NoSuchFileException(it.path) }
            )

            assertTrue("index.html" in excluded)
            assertTrue(".html4ignore" in excluded)
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun processIgnoreFileTreatsNonDirectoryPolicyPathAsAbsent() {
        val rootDir = Files.createTempDirectory("html4tree-policy-not-directory-").toFile()
        try {
            val excluded = process_ignore_file(
                rootDir,
                arrayOf("plain.txt"),
                readPolicyAttributes = { throw NotDirectoryException(it.path) }
            )

            assertTrue("index.html" in excluded)
            assertTrue(".html4ignore" in excluded)
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun processIgnoreFileNormalizesIOExceptionFromMetadataRead() {
        val rootDir = Files.createTempDirectory("html4tree-policy-io-").toFile()
        try {
            val error = assertFailsWith<IgnoreFileReadException> {
                process_ignore_file(
                    rootDir,
                    arrayOf("plain.txt"),
                    readPolicyAttributes = { throw IOException("metadata read denied") }
                )
            }

            assertTrue(error.message?.contains("metadata cannot be read securely") == true)
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun processIgnoreFileNormalizesSecurityExceptionFromMetadataRead() {
        val rootDir = Files.createTempDirectory("html4tree-policy-metadata-security-").toFile()
        try {
            val error = assertFailsWith<IgnoreFileReadException> {
                process_ignore_file(
                    rootDir,
                    arrayOf("plain.txt"),
                    readPolicyAttributes = { throw SecurityException("metadata access denied") }
                )
            }

            assertTrue(error.message?.contains("metadata access was denied") == true)
        } finally {
            rootDir.deleteRecursively()
        }
    }
}
