package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTest {

    @Test
    fun testHelp() {
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))

        try {
            help()
            assertEquals("ERROR: help has not been written yet!\n", outContent.toString())
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun testGoAndProcessDir() {
        val topDir = Files.createTempDirectory("testTree").toFile()
        try {
            val childDir1 = File(topDir, "child1")
            childDir1.mkdir()
            val childDir2 = File(topDir, "child2")
            childDir2.mkdir()
            val grandChild = File(childDir1, "grandChild")
            grandChild.mkdir()
            val file1 = File(topDir, "file1.txt")
            file1.createNewFile()

            // Test go without maxLevel restriction
            go(topDir.absolutePath, -1)

            val indexTop = File(topDir, "index.html")
            assertTrue(indexTop.exists())
            val topContent = indexTop.readText()
            assertTrue(topContent.contains("child1"))
            assertTrue(topContent.contains("child2"))
            assertTrue(topContent.contains("file1.txt"))

            val indexChild1 = File(childDir1, "index.html")
            assertTrue(indexChild1.exists())
            val child1Content = indexChild1.readText()
            assertTrue(child1Content.contains("grandChild"))

            val indexGrandChild = File(grandChild, "index.html")
            assertTrue(indexGrandChild.exists())

        } finally {
            topDir.deleteRecursively()
        }
    }

    @Test
    fun testGoMaxLevel() {
        val topDir = Files.createTempDirectory("testTreeMaxLevel").toFile()
        try {
            val childDir = File(topDir, "child")
            childDir.mkdir()
            val grandChild = File(childDir, "grandChild")
            grandChild.mkdir()

            // Test go with maxLevel = 1 (topDir is level 0, child is level 1, grandChild is level 2)
            go(topDir.absolutePath, 1)

            val indexTop = File(topDir, "index.html")
            assertTrue(indexTop.exists())

            val indexChild = File(childDir, "index.html")
            assertTrue(indexChild.exists())

            val indexGrandChild = File(grandChild, "index.html")
            assertFalse(indexGrandChild.exists()) // Should not be created

        } finally {
            topDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessIgnoreFile() {
        val topDir = Files.createTempDirectory("testIgnore").toFile()
        try {
            val file1 = File(topDir, "file1.txt")
            file1.createNewFile()
            val file2 = File(topDir, "file2.txt")
            file2.createNewFile()

            val ignoreFile = File(topDir, ".html4ignore")
            ignoreFile.writeText("file1.txt")

            go(topDir.absolutePath, -1)

            val indexTop = File(topDir, "index.html")
            assertTrue(indexTop.exists())
            val topContent = indexTop.readText()

            // file2 should be in index
            assertTrue(topContent.contains("file2.txt"))
            // file1 should NOT be in index
            assertFalse(topContent.contains("file1.txt"))
            // ignore file itself should not be in the output unless it's excluded, but by default we exclude index.html.
            // Wait, the logic only excludes what's in .html4ignore and index.html.
            // We just need to make sure file1.txt is excluded.
        } finally {
            topDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessIgnoreRegex() {
        val topDir = Files.createTempDirectory("testIgnoreRegex").toFile()
        try {
            val file1 = File(topDir, "test.log")
            file1.createNewFile()
            val file2 = File(topDir, "test.txt")
            file2.createNewFile()

            val ignoreFile = File(topDir, ".html4ignore")
            ignoreFile.writeText(".*\\.log")

            go(topDir.absolutePath, -1)

            val indexTop = File(topDir, "index.html")
            val topContent = indexTop.readText()

            assertTrue(topContent.contains("test.txt"))
            assertFalse(topContent.contains("test.log"))
        } finally {
            topDir.deleteRecursively()
        }
    }

    @Test
    fun testHtml4treeCli() {
        val topDir = Files.createTempDirectory("testCli").toFile()
        try {
            val childDir = File(topDir, "childCli")
            childDir.mkdir()

            // Test CLI argument parsing
            main(arrayOf("--max-level", "0", topDir.absolutePath))

            val indexTop = File(topDir, "index.html")
            assertTrue(indexTop.exists())

            val indexChild = File(childDir, "index.html")
            assertFalse(indexChild.exists()) // max-level 0 prevents this
        } finally {
            topDir.deleteRecursively()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("some_non_existent_directory_12345", -1)
    }
}
