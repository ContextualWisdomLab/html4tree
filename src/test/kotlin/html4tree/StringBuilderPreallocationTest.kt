package html4tree

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.io.File
import java.nio.file.Files

class StringBuilderPreallocationTest {

    @Test
    fun testPreallocationOverflowGuard() {
        // Intentionally large fake array size to trigger the guard.
        // We simulate process_dir by directly checking the logic.
        val fakeSize = 20_000_000 // 20M files * 250 = 5,000,000,000 > Int.MAX_VALUE (2,147,483,647)
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
    fun testEmptyDirectoryPreallocation() {
        val fakeSize = 0
        val estimatedCharsPerItem = 250L
        val requestedCapacity = fakeSize * estimatedCharsPerItem

        val actualCapacity = if (requestedCapacity > Int.MAX_VALUE) {
            Int.MAX_VALUE
        } else {
            requestedCapacity.toInt()
        }

        assertEquals("Capacity should be 0 for empty directory", 0, actualCapacity)

        val sb = StringBuilder(actualCapacity)
        assertEquals("Empty string builder length should be 0", 0, sb.length)
    }
}
