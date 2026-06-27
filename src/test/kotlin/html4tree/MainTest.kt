package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import org.junit.After
import org.junit.Before

class MainTest {

    private val testDir = File("test_run_dir")

    @Before
    fun setUp() {
        testDir.mkdirs()
    }

    @After
    fun tearDown() {
        testDir.deleteRecursively()
    }

    @Test
    fun testSymlinkDirTraversal() {
        val targetDir = File("test_target_dir")
        targetDir.mkdirs()

        val symlinkDir = File(testDir, "symlink_dir")
        Files.createSymbolicLink(symlinkDir.toPath(), targetDir.absoluteFile.toPath())

        go(testDir.absolutePath, -1)

        val indexInSymlinkTarget = File(targetDir, "index.html")
        assertFalse(indexInSymlinkTarget.exists(), "Should not traverse into symlink directory and create index.html there")

        targetDir.deleteRecursively()
    }

    @Test
    fun testSymlinkFileOverwrite() {
        val targetFile = File("test_target.txt")
        targetFile.writeText("Original Content")

        val symlinkFile = File(testDir, "index.html")
        Files.createSymbolicLink(symlinkFile.toPath(), targetFile.absoluteFile.toPath())

        go(testDir.absolutePath, -1)

        assertEquals("Original Content", targetFile.readText(), "Should not overwrite the target of a symlink index.html")

        targetFile.delete()
    }

    @Test
    fun testNormalDirProcessing() {
        val subDir = File(testDir, "subdir")
        subDir.mkdirs()

        go(testDir.absolutePath, -1)

        val topIndex = File(testDir, "index.html")
        assertTrue(topIndex.exists(), "Should create index.html in top directory")

        val subIndex = File(subDir, "index.html")
        assertTrue(subIndex.exists(), "Should create index.html in sub directory")
    }
}
