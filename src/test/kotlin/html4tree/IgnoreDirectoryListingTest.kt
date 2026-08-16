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
        var listFilesCallCount: Int = 0
            private set

        override fun list(): Array<String>? {
            listCallCount += 1
            return super.list()
        }

        override fun listFiles(): Array<File>? {
            listFilesCallCount += 1
            val names = super.list() ?: return null
            return Array(names.size) { index -> File(this, names[index]) }
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

            val countingDirectory = CountingDirectory(temporaryDirectory.absolutePath)
            process_dir(countingDirectory)

            val html = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)
            assertEquals(0, countingDirectory.listCallCount)
            assertEquals(1, countingDirectory.listFilesCallCount)
            assertTrue(html.contains("href=\"./minutes.txt\""))
            assertTrue(html.contains("title=\"minutes.txt 파일\""))
            assertFalse(html.contains("scratch.tmp"))
            assertFalse(html.contains(".env"))
            assertFalse(html.contains(".html4ignore"))
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }

    @Test
    fun fallbackProcessDirWithUnreadableListingWritesEmptyPageWithoutCallingList() {
        val temporaryDirectory = Files.createTempDirectory("ignore_null_listfiles").toFile()
        try {
            File(temporaryDirectory, "minutes.txt").writeText("meeting notes")
            val unreadable = object : File(temporaryDirectory.absolutePath) {
                override fun list(): Array<String>? {
                    throw AssertionError("list() must not run after listFiles() returned null")
                }

                override fun listFiles(): Array<File>? = null
            }

            process_dir(unreadable)

            val html = File(temporaryDirectory, "index.html").readText(Charsets.UTF_8)
            assertTrue(html.contains("이 디렉토리는 비어 있습니다."))
            assertFalse(html.contains("href=\"./minutes.txt\""))
            assertFalse(html.contains("title=\"minutes.txt 파일\""))
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }
}
