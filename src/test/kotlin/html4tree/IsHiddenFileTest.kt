package html4tree

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsHiddenFileTest {

    @Test
    fun `isHiddenFile should correctly identify hidden files`() {
        assertTrue(".hidden".isHiddenFile())
        assertTrue("\u3002hidden".isHiddenFile())
        assertTrue("\uFF0Ehidden".isHiddenFile())
        assertTrue("\uFF61hidden".isHiddenFile())
        assertFalse("normal.txt".isHiddenFile())
        assertFalse("".isHiddenFile())
    }
}
