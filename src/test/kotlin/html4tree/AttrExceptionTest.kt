package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files

class AttrExceptionTest {

    @Test
    fun testProcessDirExceptionHandling() {
        val tempDir = createTempDir()
        try {
            val files = Array(1) { File(tempDir, "doesnotexist") }
            process_dir(tempDir, setOf(), files)
            assertTrue(File(tempDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testDefaultReadAttributesException() {
        val file = File("doesnotexist")
        val defaultReadAttributes: (File) -> java.nio.file.attribute.BasicFileAttributes? = {
            try {
                java.nio.file.Files.readAttributes(it.toPath(), java.nio.file.attribute.BasicFileAttributes::class.java, java.nio.file.LinkOption.NOFOLLOW_LINKS)
            } catch (e: Exception) {
                null
            }
        }
        val result = defaultReadAttributes(file)
        assertNull(result)

        val ll = LinkedList()
        val tempDir = createTempDir()
        try {
            val nonExistent = File(tempDir, "nonexistent")
            ll.push(LinkedListEntry(nonExistent, 0))
            crawl_directories(ll, 0)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
