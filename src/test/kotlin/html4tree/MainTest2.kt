package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class MainTest2 {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-test2-").toFile()
    }

    @After
    fun teardown() {
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessDirListFilesNull() {
        val unreadableDir = File(tempDir, "unreadable2")
        unreadableDir.mkdir()
        unreadableDir.setReadable(false, false)
        try {
            process_dir(unreadableDir)
            val indexFile = File(unreadableDir, "index.html")
            assertTrue(indexFile.exists())
        } finally {
            unreadableDir.setReadable(true, false)
            unreadableDir.setWritable(true, false)
            unreadableDir.setExecutable(true, false)
        }
    }
}
