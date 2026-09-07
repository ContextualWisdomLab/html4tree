package html4tree

import org.junit.Test
import kotlin.test.assertFailsWith

class SecurityTest {
    @Test
    fun testTopDirLengthLimit() {
        val longString = "a".repeat(4097)
        assertFailsWith<IllegalArgumentException> {
            go(longString, 1)
        }
    }
}
