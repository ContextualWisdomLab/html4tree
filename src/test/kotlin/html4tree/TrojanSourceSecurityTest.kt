package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TrojanSourceSecurityTest {
    @Test
    fun testBidiCharactersAreRemoved() {
        val maliciousStr = "exe.\u202Etad"
        val escaped = maliciousStr.escapeHtml()
        assertFalse(escaped.contains('\u202E'))
        assertEquals("exe.tad", escaped)
    }
}
