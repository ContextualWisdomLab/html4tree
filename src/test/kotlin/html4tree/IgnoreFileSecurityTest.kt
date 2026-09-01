package html4tree

import org.junit.Assume
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IgnoreFileSecurityTest {
    @Test
    fun safeReadFailureAfterMetadataCheckExcludesEveryListedEntry() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-race-").toFile()
        try {
            File(tempDir, ".html4ignore").writeText("*.txt")
            val listedNames = arrayOf("public.md", "secret.txt")

            val excluded = collect_ignore_file_exclusions(tempDir, listedNames) { null }

            assertTrue(listedNames.all { it in excluded })
            assertTrue("index.html" in excluded)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun invalidListedPathIsExcludedInsteadOfAbortingMatching() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-invalid-path-").toFile()
        try {
            File(tempDir, ".html4ignore").writeText("*")
            val invalidName = "bad\u0000name"

            val excluded = collect_ignore_file_exclusions(
                tempDir,
                arrayOf(invalidName),
                readIgnoreLines = { listOf("*") }
            )

            assertTrue(invalidName in excluded)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun noFollowReaderReturnsLinesForSmallRegularFile() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-regular-").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("*.txt\n*.log")

            assertEquals(listOf("*.txt", "*.log"), read_ignore_file_lines_no_follow(ignoreFile))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun noFollowReaderRejectsSymbolicLinkAtOpenTime() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-link-").toFile()
        try {
            val target = File(tempDir, "outside-ignore")
            target.writeText("*.secret")
            val link = File(tempDir, "ignore-link")
            try {
                Files.createSymbolicLink(link.toPath(), target.toPath())
            } catch (error: Exception) {
                Assume.assumeTrue("Symlink creation not supported in this environment: ${error.message}", false)
            }

            assertNull(read_ignore_file_lines_no_follow(link))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun boundedReaderRejectsContentLargerThanOneMiBAtReadTime() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-size-").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            Files.write(ignoreFile.toPath(), ByteArray(1_048_577) { 0x61.toByte() })

            assertNull(read_ignore_file_lines_no_follow(ignoreFile))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
