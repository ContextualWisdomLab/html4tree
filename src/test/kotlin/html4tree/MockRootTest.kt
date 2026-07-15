package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class MockRootTest {
    @Test
    fun testMockRoot() {
        val mockRoot = object : File("/tmp", "fakeRoot") {
            override fun getName(): String = ""
        }
        assertEquals("", mockRoot.name)
        assertEquals("/tmp/fakeRoot", mockRoot.absolutePath)
    }
}
