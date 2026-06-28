package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import java.io.File
import org.junit.After
import org.junit.Before

class MainKtTest {

    lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = createTempDir(prefix = "html4tree-mainkt-test-")
    }

    @After
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testGoWithNonExistentDir() {
        assertFailsWith<IllegalArgumentException> {
            go("non_existent_dir", -1)
        }
    }

    @Test
    fun testGoWithFileInsteadOfDir() {
        val file = File(tempDir, "test.txt")
        file.createNewFile()
        assertFailsWith<IllegalArgumentException> {
            go(file.absolutePath, -1)
        }
    }

    @Test
    fun testGoWithEmptyDir() {
        go(tempDir.absolutePath, -1)
        assertTrue(File(tempDir, "index.html").exists())
    }

    @Test
    fun testProcessIgnoreFileWithNonExistentIgnoreFile() {
        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("index.html"))
        assertEquals(1, excluded.size)
    }
}
