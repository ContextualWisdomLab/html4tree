package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class MainTest {
    @Test
    fun testSymlinkIgnored() {
        val rootDir = File("test_root_symlink")
        rootDir.mkdir()
        val maliciousDir = File(rootDir, "malicious")
        maliciousDir.mkdir()
        val symlinkFile = File(maliciousDir, "link_to_tmp")
        Files.createSymbolicLink(symlinkFile.toPath(), Paths.get("/tmp"))

        go(rootDir.absolutePath, -1)

        val tmpIndex = File("/tmp", "index.html")
        assertFalse(tmpIndex.exists())

        rootDir.deleteRecursively()
    }

    @Test
    fun testUnreadableDirectory() {
        val rootDir = File("test_root_unreadable")
        rootDir.mkdir()
        val restrictedDir = File(rootDir, "restricted")
        restrictedDir.mkdir()
        restrictedDir.setReadable(false, false)
        restrictedDir.setExecutable(false, false)
        restrictedDir.setWritable(false, false)

        go(rootDir.absolutePath, -1)

        restrictedDir.setReadable(true, true)
        restrictedDir.setExecutable(true, true)
        restrictedDir.setWritable(true, true)
        rootDir.deleteRecursively()
    }

    @Test
    fun testProcessIgnore() {
        val rootDir = File("test_root_ignore")
        rootDir.mkdir()
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

        rootDir.deleteRecursively()
    }

    @Test
    fun testEscapeHtmlAndEncode() {
        assertEquals("&amp;&lt;&gt;&quot;&#x27;", "&<>\"'".escapeHtml())
        assertEquals("space%20test", "space test".urlEncodePath())
    }

    @Test
    fun testCliArgsAndHelp() {
        val rootDir = File("test_cli_root")
        rootDir.mkdir()
        val f1 = File(rootDir, "file1")
        f1.writeText("test")

        main(arrayOf("--max-level", "2", rootDir.absolutePath))

        val indexFile = File(rootDir, "index.html")
        assertTrue(indexFile.exists())
        val indexContent = indexFile.readText()
        assertTrue(indexContent.contains("file1"))

        rootDir.deleteRecursively()

        help()
    }

    @Test
    fun testLinkedList() {
        val ll = LinkedList()
        val entry1 = LinkedListEntry(File("1"), 1)
        val entry2 = LinkedListEntry(File("2"), 2)
        ll.push(entry1)
        ll.pull()
        ll.push(entry2)
        ll.pull()
        ll.pull()
    }
}