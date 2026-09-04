package html4tree

import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IgnoreFileRaceSecurityTest {
    private fun directoryAttributes(): BasicFileAttributes {
        return object : BasicFileAttributes {
            override fun lastModifiedTime(): FileTime = FileTime.fromMillis(0)
            override fun lastAccessTime(): FileTime = FileTime.fromMillis(0)
            override fun creationTime(): FileTime = FileTime.fromMillis(0)
            override fun isRegularFile(): Boolean = false
            override fun isDirectory(): Boolean = true
            override fun isSymbolicLink(): Boolean = false
            override fun isOther(): Boolean = false
            override fun size(): Long = 0L
            override fun fileKey(): Any = "stable-key"
        }
    }

    @Test
    fun crawlDirectoriesFailsClosedWhenIgnoreFileChangesDuringOpen() {
        val root = Files.createTempDirectory("html4tree-ignore-race-").toFile()
        val child = File(root, "child").apply { mkdir() }
        val ignorePath = File(root, ".html4ignore").absolutePath
        val openFailures = listOf<IOException>(
            NoSuchFileException(ignorePath),
            FileSystemException(ignorePath, null, "symbolic link replacement rejected"),
            AccessDeniedException(ignorePath)
        )

        try {
            openFailures.forEach { failure ->
                val processed = mutableListOf<File>()
                var childObserved = false
                val queue = LinkedList()
                queue.push(LinkedListEntry(root, 0, "stable-key"))

                crawl_directories(
                    queue,
                    -1,
                    processDirectory = { file, _, _ -> processed.add(file) },
                    processIgnoreFile = { _, _ -> throw failure },
                    listFiles = { file -> if (file == root) arrayOf(child) else emptyArray() },
                    readAttributes = { file ->
                        if (file == child) childObserved = true
                        directoryAttributes()
                    },
                    readIdentity = { FileIdentity("stable-key", true) }
                )

                assertTrue(processed.isEmpty(), "an unreadable ignore file must skip the affected directory")
                assertFalse(childObserved, "children must not be registered after ignore-file open failure")
            }
        } finally {
            root.deleteRecursively()
        }
    }
}
