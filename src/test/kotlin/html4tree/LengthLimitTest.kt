package html4tree

import org.junit.Test
import kotlin.test.assertFailsWith

class LengthLimitTest {
    @Test
    fun testTopDirLengthLimit() {
        val longDir = "a".repeat(4097)
        assertFailsWith<IllegalArgumentException> {
            go(longDir, 1)
        }
    }
}
