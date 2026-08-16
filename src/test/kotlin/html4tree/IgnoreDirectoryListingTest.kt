package html4tree

import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class IgnoreDirectoryListingTest {
    private class CountingDirectory(path: String) : File(path) {
        var listCallCount: Int = 0
            private set

        override fun list(): Array<String>? {
            listCallCount += 1
            return super.list()
        }
    }

    @Test
    fun ignoreProcessingListsTheDirectoryOnlyOnceWhenNamesAreNotSupplied() {
        val temporaryDirectory = Files.createTempDirectory("ignore_listing").toFile()
        try {
            File(temporaryDirectory, ".html4ignore").writeText("*.tmp\n")
            File(temporaryDirectory, "visible.txt").writeText("visible")
            File(temporaryDirectory, "excluded.tmp").writeText("excluded")
            val countingDirectory = CountingDirectory(temporaryDirectory.absolutePath)

            val excluded = process_ignore_file(countingDirectory)

            assertEquals(1, countingDirectory.listCallCount)
            assertTrue("excluded.tmp" in excluded)
            assertFalse("visible.txt" in excluded)
            assertTrue("index.html" in excluded)
            assertTrue(".html4ignore" in excluded)
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }

    @Test
    fun callerSuppliedNamesAvoidDirectoryListingEntirely() {
        val temporaryDirectory = Files.createTempDirectory("ignore_supplied_names").toFile()
        try {
            File(temporaryDirectory, ".html4ignore").writeText("*.tmp\n")
            val countingDirectory = CountingDirectory(temporaryDirectory.absolutePath)

            val excluded = process_ignore_file(
                countingDirectory,
                arrayOf("visible.txt", "excluded.tmp", ".env")
            )

            assertEquals(0, countingDirectory.listCallCount)
            assertTrue("excluded.tmp" in excluded)
            assertFalse("visible.txt" in excluded)
            assertTrue(".env" in excluded)
            assertTrue("index.html" in excluded)
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }

    @Test
    fun ignoreProcessingListsOnceAndHidesSecretsWhenIgnoreFileIsAbsent() {
        val temporaryDirectory = Files.createTempDirectory("ignore_no_file").toFile()
        try {
            File(temporaryDirectory, "minutes.txt").writeText("meeting notes")
            File(temporaryDirectory, ".env").writeText("SECRET=1")
            val countingDirectory = CountingDirectory(temporaryDirectory.absolutePath)

            val excluded = process_ignore_file(countingDirectory)

            assertEquals(1, countingDirectory.listCallCount)
            assertTrue(".env" in excluded)
            assertFalse("minutes.txt" in excluded)
            assertTrue("index.html" in excluded)
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }

    @Test
    fun unreadableDirectoryListingStillReturnsDefaultExclusions() {
        val temporaryDirectory = Files.createTempDirectory("ignore_null_list").toFile()
        try {
            File(temporaryDirectory, ".html4ignore").writeText("*.tmp\n")
            File(temporaryDirectory, "scratch.tmp").writeText("scratch")
            val unreadable = object : File(temporaryDirectory.absolutePath) {
                override fun list(): Array<String>? = null
            }

            val excluded = process_ignore_file(unreadable)

            assertTrue("index.html" in excluded)
            assertTrue(".env" in excluded)
            assertFalse("scratch.tmp" in excluded)
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }

    @Test
    fun fallbackIgnorePathHidesSecretsAndIgnoredTempFilesInGeneratedListing() {
        val temporaryDirectory = Files.createTempDirectory("ignore_listing_html").toFile()
        try {
            File(temporaryDirectory, ".html4ignore").writeText("*.tmp\n")
            File(temporaryDirectory, "minutes.txt").writeText("meeting notes")
            File(temporaryDirectory, "scratch.tmp").writeText("scratch")
            File(temporaryDirectory, ".env").writeText("SECRET=1")

            process_dir(temporaryDirectory)

            val html = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)
            assertTrue(html.contains("minutes.txt"))
            assertFalse(html.contains("scratch.tmp"))
            assertFalse(html.contains(".env"))
            assertFalse(html.contains(".html4ignore"))
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }
}
