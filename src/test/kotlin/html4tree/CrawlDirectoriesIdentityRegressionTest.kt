package html4tree

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Regression coverage for fail-closed directory identity acquisition. */
class CrawlDirectoriesIdentityRegressionTest {
    /** Creates deterministic directory attributes without touching the host filesystem. */
    private fun directoryAttributes(): BasicFileAttributes {
        return object : BasicFileAttributes {
            override fun lastModifiedTime(): FileTime = FileTime.fromMillis(0)
            override fun lastAccessTime(): FileTime = FileTime.fromMillis(0)
            override fun creationTime(): FileTime = FileTime.fromMillis(0)
            override fun isRegularFile(): Boolean = false
            override fun isDirectory(): Boolean = true
            override fun isSymbolicLink(): Boolean = false
            override fun isOther(): Boolean = false
            override fun size(): Long = 0
            override fun fileKey(): Any? = null
        }
    }

    /**
     * A child whose identity cannot be read must not enter the queue, even when
     * the same path later resolves to a readable replacement directory.
     */
    @Test
    fun unreadableChildIdentityIsNotEnqueuedBeforePathReplacement() {
        val root = Files.createTempDirectory("html4tree-identity-root-").toFile()
        val child = File(root, "child").apply { mkdir() }
        val processed = mutableListOf<File>()
        val identityCalls = mutableMapOf<File, Int>()
        val queue = LinkedList()
        queue.push(LinkedListEntry(root, 0, "root-key"))

        try {
            crawl_directories(
                queue,
                -1,
                processDirectory = { file, _, _ -> processed.add(file) },
                processIgnoreFile = { _, _ -> emptySet() },
                listFiles = { file -> if (file == root) arrayOf(child) else emptyArray() },
                readAttributes = { directoryAttributes() },
                readIdentity = { file ->
                    val callCount = identityCalls.getOrDefault(file, 0)
                    identityCalls[file] = callCount + 1
                    when (file) {
                        root -> FileIdentity("root-key", true)
                        child -> if (callCount == 0) {
                            FileIdentity(null, false)
                        } else {
                            FileIdentity("replacement-key", true)
                        }
                        else -> FileIdentity(null, false)
                    }
                }
            )

            assertEquals(listOf(root), processed)
            assertEquals(1, identityCalls[child], "unreadable child must never be dequeued")
        } finally {
            root.deleteRecursively()
        }
    }

    /** Expected I/O failures are converted to an absent attribute snapshot. */
    @Test
    fun basicAttributeReaderSkipsIoFailures() {
        val candidate = File("missing")

        assertNull(read_basic_file_attributes(candidate) { throw IOException("missing") })
    }

    /** Expected access-control failures are converted to an absent attribute snapshot. */
    @Test
    fun basicAttributeReaderSkipsSecurityFailures() {
        val candidate = File("restricted")

        assertNull(
            read_basic_file_attributes(candidate) {
                throw SecurityException("restricted")
            }
        )
    }
}
