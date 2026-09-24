package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IgnoreFileListingTest {
    private class CountingDirectory(
        pathname: String,
        private val snapshots: List<Array<String>?>,
    ) : File(pathname) {
        var listCalls: Int = 0
            private set

        override fun list(): Array<String>? {
            val snapshot = snapshots.getOrElse(listCalls) { snapshots.lastOrNull() }
            listCalls += 1
            return snapshot
        }
    }

    @Test
    fun fallbackReadsDirectoryOnceWhenIgnoreFileExists() {
        val directory = Files.createTempDirectory("html4tree-ignore-list-").toFile()
        try {
            directory.resolve(".html4ignore").writeText("*.tmp\n")
            val countingDirectory = CountingDirectory(
                directory.absolutePath,
                listOf(
                    arrayOf("first.tmp", ".env"),
                    arrayOf("second.txt"),
                ),
            )

            val excluded = process_ignore_file(countingDirectory)

            assertEquals(1, countingDirectory.listCalls)
            assertTrue("first.tmp" in excluded)
            assertTrue(".env" in excluded)
            assertTrue("index.html" in excluded)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun suppliedDirectoryNamesDoNotTriggerFilesystemListing() {
        val directory = Files.createTempDirectory("html4tree-ignore-provided-").toFile()
        try {
            directory.resolve(".html4ignore").writeText("*.tmp\n")
            val countingDirectory = CountingDirectory(
                directory.absolutePath,
                listOf(arrayOf("unexpected.tmp")),
            )

            val excluded = process_ignore_file(
                countingDirectory,
                arrayOf("provided.tmp", ".git"),
            )

            assertEquals(0, countingDirectory.listCalls)
            assertTrue("provided.tmp" in excluded)
            assertTrue(".git" in excluded)
        } finally {
            directory.deleteRecursively()
        }
    }
}
