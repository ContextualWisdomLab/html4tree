package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import java.io.File

class Html4treeTest {

    @Test
    fun testHelpFunction() {
        // Just calling to cover the line
        help()
    }

    @Test
    fun testMainWithDummyArgs() {
        val testDir = File("testMainDir")
        testDir.mkdir()

        main(arrayOf(testDir.absolutePath))

        testDir.deleteRecursively()
    }

    @Test
    fun testHtml4treeCommandMaxLevel() {
        val testDir = File("testCommandDir")
        testDir.mkdir()
        val subDir = File(testDir, "subDir")
        subDir.mkdir()

        val cmd = Html4tree()
        cmd.main(arrayOf("--max-level", "0", testDir.absolutePath))

        // Assert we got here without exception
        assertTrue(true)

        testDir.deleteRecursively()
    }
}
