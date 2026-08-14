package html4tree

import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
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

            process_ignore_file(countingDirectory)

            assertEquals(1, countingDirectory.listCallCount)
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

            process_ignore_file(
                countingDirectory,
                arrayOf("visible.txt", "excluded.tmp")
            )

            assertEquals(0, countingDirectory.listCallCount)
        } finally {
            temporaryDirectory.deleteRecursively()
        }
    }
}
