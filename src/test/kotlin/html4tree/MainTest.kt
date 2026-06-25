package html4tree

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import java.io.File
import java.nio.file.Files

class MainTest {

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("testProcessIgnoreFile").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("secret\\.txt\n.*hidden.*")

            File(tempDir, "secret.txt").createNewFile()
            File(tempDir, "hidden_folder").mkdir()
            File(tempDir, "public.txt").createNewFile()

            val excludes = process_ignore_file(tempDir)

            assertTrue("secret.txt" in excludes)
            assertTrue("hidden_folder" in excludes)
            assertFalse("public.txt" in excludes)
            assertTrue("index.html" in excludes) // 항상 포함되어야 함
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessIgnoreFileNoIgnoreFile() {
        val tempDir = Files.createTempDirectory("testProcessIgnoreFileNoIgnoreFile").toFile()
        try {
            val excludes = process_ignore_file(tempDir)
            assertTrue("index.html" in excludes)
            assertEquals(1, excludes.size)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessDir() {
        val tempDir = Files.createTempDirectory("testProcessDir").toFile()
        try {
            val childFile = File(tempDir, "test.txt")
            childFile.writeText("test")
            val childDir = File(tempDir, "subDir")
            childDir.mkdir()
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("ignore\\.txt")
            File(tempDir, "ignore.txt").createNewFile()

            process_dir(tempDir)

            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists())

            val indexContent = indexFile.readText()
            assertTrue(indexContent.contains("<html lang=\"en\">"))
            assertTrue(indexContent.contains("<meta charset=\"utf-8\">"))
            assertTrue(indexContent.contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"))
            assertTrue(indexContent.contains(tempDir.getName().escapeHtml()))
            assertTrue(indexContent.contains("test.txt"))
            assertTrue(indexContent.contains("subDir"))
            assertFalse(indexContent.contains("ignore.txt"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testGo() {
        val tempDir = Files.createTempDirectory("testGo").toFile()
        try {
            val subDir1 = File(tempDir, "sub1")
            subDir1.mkdir()
            val subDir2 = File(subDir1, "sub2")
            subDir2.mkdir()
            val subDir3 = File(subDir2, "sub3")
            subDir3.mkdir()

            go(tempDir.absolutePath, 1)

            assertTrue(File(tempDir, "index.html").exists())
            assertTrue(File(subDir1, "index.html").exists())
            // maxLevel이 1이므로 sub2, sub3에는 index.html이 생성되지 않아야 함
            assertFalse(File(subDir2, "index.html").exists())
            assertFalse(File(subDir3, "index.html").exists())

        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("invalid_path_that_does_not_exist", -1)
    }

    @Test
    fun testHelp() {
        // help() is currently print only, just call it to cover
        help()
    }

    @Test
    fun testMain() {
        val tempDir = Files.createTempDirectory("testMain").toFile()
        try {
            main(arrayOf("--max-level", "0", tempDir.absolutePath))
            assertTrue(File(tempDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
