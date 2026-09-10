package html4tree

import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IgnorePolicyOperationalTest {
    @Test
    fun securePolicyReaderRejectsGrowthAfterOpen() {
        val tempDir = Files.createTempDirectory("html4tree-policy-growth-").toFile()
        try {
            val policy = File(tempDir, ".html4ignore")
            policy.writeText("private.txt\n")

            assertFailsWith<IOException> {
                policy.useLines { lines ->
                    policy.appendText("x".repeat(1_048_577))
                    lines.toList()
                }
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun crawlReportsIgnorePolicyFailureBeforeSkippingDirectory() {
        val tempDir = Files.createTempDirectory("html4tree-policy-diagnostic-").toFile()
        val previousErr = System.err
        val capturedErr = ByteArrayOutputStream()
        try {
            val rootDir = File(tempDir, "root").apply { mkdir() }
            val pending = LinkedList().apply {
                push(LinkedListEntry(rootDir, 0, read_file_identity(rootDir).key))
            }
            var publicationCount = 0

            System.setErr(PrintStream(capturedErr, true, Charsets.UTF_8.name()))
            crawl_directories(
                pending,
                1,
                processDirectory = { _, _, _ -> publicationCount++ },
                processIgnoreFile = { _, _ ->
                    throw IgnoreFileReadException("policy denied")
                }
            )

            val diagnostic = capturedErr.toString(Charsets.UTF_8.name())
            assertEquals(0, publicationCount)
            assertTrue(diagnostic.contains(rootDir.absolutePath))
            assertTrue(diagnostic.contains("policy denied"))
        } finally {
            System.setErr(previousErr)
            tempDir.deleteRecursively()
        }
    }
}