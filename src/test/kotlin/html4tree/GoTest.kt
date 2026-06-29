package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class GoTest {
    @Test
    fun testGo() {
        val tempDir = createTempDir("goTestDir")
        try {
            val childDir1 = File(tempDir, "child1")
            childDir1.mkdirs()

            val childDir2 = File(tempDir, "child2")
            childDir2.mkdirs()

            go(tempDir.absolutePath, 0)

            assertTrue(File(tempDir, "index.html").exists())
            assertFalse(File(childDir1, "index.html").exists())
            assertFalse(File(childDir2, "index.html").exists())

            go(tempDir.absolutePath, 1)

            assertTrue(File(tempDir, "index.html").exists())
            assertTrue(File(childDir1, "index.html").exists())
            assertTrue(File(childDir2, "index.html").exists())

            try {
                go(File(tempDir, "non_existent").absolutePath, 0)
                fail("Expected IllegalArgumentException")
            } catch (e: IllegalArgumentException) {
                // pass
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
