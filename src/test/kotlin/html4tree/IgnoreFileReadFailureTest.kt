package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IgnoreFileReadFailureTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-ignore-read-").toFile()
    }

    @After
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun processIgnoreFileWrapsInjectedReadFailure() {
        File(tempDir, ".html4ignore").writeText("private-*\n")

        val error = assertFailsWith<IgnoreFileReadException> {
            process_ignore_file(tempDir, arrayOf("private-report.txt")) { _, _ ->
                throw IOException("injected read failure")
            }
        }

        assertTrue(error.cause is IOException)
    }

    @Test
    fun crawlDoesNotPublishDirectoryWhenAdmittedIgnoreFileReadFails() {
        File(tempDir, "public.txt").writeText("visible only when policy evaluation succeeds")
        val queue = LinkedList()
        queue.push(LinkedListEntry(tempDir, 0, read_file_identity(tempDir).key))
        var published = false

        crawl_directories(
            queue,
            -1,
            processDirectory = { _, _, _ -> published = true },
            processIgnoreFile = { _, _ ->
                throw IgnoreFileReadException(IOException("injected read failure"))
            }
        )

        assertFalse(published)
        assertFalse(File(tempDir, "index.html").exists())
    }

    @Test
    fun readableIgnoreFileStillAppliesConfiguredGlob() {
        File(tempDir, ".html4ignore").writeText("private-*\n")

        val excluded = process_ignore_file(
            tempDir,
            arrayOf("private-report.txt", "public.txt")
        )

        assertTrue("private-report.txt" in excluded)
        assertFalse("public.txt" in excluded)
    }
}
