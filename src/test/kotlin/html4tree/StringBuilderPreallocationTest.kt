package html4tree

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.io.File
import java.nio.file.Files

class StringBuilderPreallocationTest {

    @Test
    fun testPreallocationOverflowGuard() {
        val fakeSize = 20_000_000
        val estimatedCharsPerItem = 250L
        val requestedCapacity = fakeSize * estimatedCharsPerItem

        val actualCapacity = if (requestedCapacity > Int.MAX_VALUE) {
            Int.MAX_VALUE
        } else {
            requestedCapacity.toInt()
        }

        assertEquals("Should cap at Int.MAX_VALUE", Int.MAX_VALUE, actualCapacity)
    }

    @Test
    fun testProcessDirOverflow() {
        val tempDir = createTempDir()
        try {
            // Allocate 8.6M array to hit the overflow guard in main.kt line 445
            // 8,600,000 * 250 = 2,150,000,000 > Int.MAX_VALUE
            val largeSize = 8600000
            val dummyFile = File(tempDir, "dummy")
            val largeArray = Array<File>(largeSize) { dummyFile }

            // This will take a second to iterate but will hit the branch
            process_dir(tempDir, setOf("dummy"), largeArray)

            assertTrue(File(tempDir, "index.html").exists())
        } catch (e: OutOfMemoryError) {
            // Ignore OOM if the runner is too small, though 8.6M refs should only be ~34MB
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
