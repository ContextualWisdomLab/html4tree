package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.BasicFileAttributes

class TrackKeyFallbackTest {
    @Test
    fun testTrackKeyFallback() {
        val rootDir = File("build/test-track-key").apply { mkdirs() }

        val ll = LinkedList()
        val topEntry = LinkedListEntry(rootDir, 0, null) // Intentionally null fileKey
        ll.push(topEntry)

        var processedCount = 0
        crawl_directories(
            ll,
            -1,
            processDirectory = { _, _, _ -> processedCount++ },
            processIgnoreFile = { _, _ -> emptySet() },
            listFiles = { emptyArray() },
            readAttributes = {
                try {
                    Files.readAttributes(it.toPath(), BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                } catch (e: Exception) {
                    null
                }
            },
            readIdentity = { FileIdentity(null, true) } // Force identity to return null key but readable
        )

        assertTrue(processedCount == 1, "Should process the root directory exactly once using absolutePath fallback")
    }
}
