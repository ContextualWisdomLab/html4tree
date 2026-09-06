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
    fun processIgnoreFileFailsClosedWhenPolicyBecomesSymlinkAtOpenTime() {
        val ignoreFile = File(tempDir, ".html4ignore")
        val replacementTarget = File(tempDir, "replacement-policy")
        ignoreFile.writeText("private-*\n")
        replacementTarget.writeText("public-*\n")

        val error = assertFailsWith<IgnoreFileReadException> {
            process_ignore_file(
                tempDir,
                arrayOf(".html4ignore", "private-report.txt", "public-report.txt")
            ) { file, consume ->
                Files.delete(file.toPath())
                Files.createSymbolicLink(file.toPath(), replacementTarget.toPath())
                read_ignore_lines_no_follow(file, consume)
            }
        }

        assertTrue(error.cause is IOException)
    }

    @Test
    fun processIgnoreFileFailsClosedForDeclaredSymlinkWithoutInjectedSnapshot() {
        val replacementTarget = File(tempDir, "replacement-policy")
        replacementTarget.writeText("public-*\n")
        Files.createSymbolicLink(File(tempDir, ".html4ignore").toPath(), replacementTarget.toPath())

        val error = assertFailsWith<IgnoreFileReadException> {
            process_ignore_file(tempDir)
        }

        assertTrue(error.cause is IOException)
    }

    @Test
    fun processIgnoreFileFailsClosedForDeclaredDirectoryWithoutInjectedSnapshot() {
        File(tempDir, ".html4ignore").mkdir()

        val error = assertFailsWith<IgnoreFileReadException> {
            process_ignore_file(tempDir)
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
    fun crawlDoesNotPublishWhenListedIgnoreFileDisappearsBeforePolicyRead() {
        File(tempDir, ".html4ignore").writeText("private-*\n")
        File(tempDir, "private-report.txt").writeText("must remain hidden")
        val queue = LinkedList()
        queue.push(LinkedListEntry(tempDir, 0, read_file_identity(tempDir).key))
        var published = false
        var listedIgnorePolicy = false

        crawl_directories(
            queue,
            -1,
            processDirectory = { _, _, _ -> published = true },
            listFiles = { directory ->
                val snapshot = directory.listFiles()
                listedIgnorePolicy = snapshot?.any { it.name == ".html4ignore" } == true
                File(directory, ".html4ignore").delete()
                snapshot
            }
        )

        assertTrue(listedIgnorePolicy)
        assertFalse(
            published,
            "a policy present in the admitted directory snapshot must not become an empty policy when it disappears before read"
        )
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