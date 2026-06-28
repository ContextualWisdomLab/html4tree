package html4tree

import org.junit.Test
import org.junit.Assume.assumeTrue
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files

class MainTest {
    @Test
    fun testSymlinkIgnored() {
        val rootDir = Files.createTempDirectory("test_root_symlink").toFile()
        val symlinkTargetDir = Files.createTempDirectory("test_symlink_target").toFile()
        try {
            val maliciousDir = File(rootDir, "malicious")
            maliciousDir.mkdir()
            val symlinkFile = File(maliciousDir, "link_to_target")
            Files.createSymbolicLink(symlinkFile.toPath(), symlinkTargetDir.toPath())

            go(rootDir.absolutePath, -1)

            val targetIndex = File(symlinkTargetDir, "index.html")
            assertFalse(targetIndex.exists())
        } finally {
            rootDir.deleteRecursively()
            symlinkTargetDir.deleteRecursively()
        }
    }

    @Test
    fun testUnreadableDirectory() {
        val rootDir = Files.createTempDirectory("test_root_unreadable").toFile()
        val restrictedDir = File(rootDir, "restricted")
        restrictedDir.mkdir()
        try {
            val readableChanged = restrictedDir.setReadable(false, false)
            val executableChanged = restrictedDir.setExecutable(false, false)
            val writableChanged = restrictedDir.setWritable(false, false)

            assumeTrue("Could not set restrictive permissions on test directory", readableChanged && executableChanged && writableChanged)

            go(rootDir.absolutePath, -1)
        } finally {
            restrictedDir.setReadable(true, true)
            restrictedDir.setExecutable(true, true)
            restrictedDir.setWritable(true, true)
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessIgnore() {
        val rootDir = Files.createTempDirectory("test_root_ignore").toFile()
        try {
            val ignoreFile = File(rootDir, ".html4ignore")
            ignoreFile.writeText(".*secret.*\n")
            val secretDir = File(rootDir, "secret_dir")
            secretDir.mkdir()
            val normalDir = File(rootDir, "normal_dir")
            normalDir.mkdir()

            go(rootDir.absolutePath, -1)
            val indexContent = File(rootDir, "index.html").readText()
            assertFalse(indexContent.contains("secret_dir"))
            assertTrue(indexContent.contains("normal_dir"))
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun testEscapeHtmlAndEncode() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
        assertEquals("space%20test", "space test".urlEncodePath())
    }

    @Test
    fun testCliArgsAndHelp() {
        val rootDir = Files.createTempDirectory("test_cli_root").toFile()
        try {
            val f1 = File(rootDir, "file1")
            f1.writeText("test")

            main(arrayOf("--max-level", "2", rootDir.absolutePath))

            val indexFile = File(rootDir, "index.html")
            assertTrue(indexFile.exists())
            val indexContent = indexFile.readText()
            assertTrue(indexContent.contains("file1"))

            val originalOut = System.out
            val output = ByteArrayOutputStream()
            System.setOut(PrintStream(output))
            try {
                help()
            } finally {
                System.setOut(originalOut)
            }
            assertTrue(output.toString().contains("ERROR: help has not been written yet!"))
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun testLinkedList() {
        val ll = LinkedList()
        val entry1 = LinkedListEntry(File("1"), 1)
        val entry2 = LinkedListEntry(File("2"), 2)
        ll.push(entry1)
        assertEquals(entry1, ll.pull())
        ll.push(entry2)
        assertEquals(entry2, ll.pull())
        assertEquals(null, ll.pull())
    }
}