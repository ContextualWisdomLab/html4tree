package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import java.nio.file.Files

class MainKtTest {

    @Test
    fun testEscapeHtml() {
        val original = "<script>alert('xss & more')</script>\"'"
        val escaped = original.escapeHtml()
        assertEquals("&lt;script&gt;alert(&#x27;xss &amp; more&#x27;)&lt;/script&gt;&quot;&#x27;", escaped)
    }

    @Test
    fun testUrlEncodePath() {
        val path = "my folder/sub+file.txt"
        val encoded = path.urlEncodePath()
        assertEquals("my%20folder%2Fsub%2Bfile.txt", encoded)
    }

    @Test
    fun testHelp() {
        // Just call to cover it
        help()
    }

    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("ignoreTest").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText(".*\\.txt\n^hidden_dir$")

            File(tempDir, "test1.txt").createNewFile()
            File(tempDir, "test2.png").createNewFile()
            File(tempDir, "hidden_dir").mkdir()

            val excluded = process_ignore_file(tempDir)

            assertTrue("test1.txt" in excluded)
            assertFalse("test2.png" in excluded)
            assertTrue("hidden_dir" in excluded)
            assertTrue("index.html" in excluded)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessIgnoreFileNoIgnore() {
        val tempDir = Files.createTempDirectory("ignoreTestNoIgnore").toFile()
        try {
            val excluded = process_ignore_file(tempDir)
            assertTrue("index.html" in excluded)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testGoAndProcessDir() {
        val rootDir = Files.createTempDirectory("goTest").toFile()
        try {
            val subDir1 = File(rootDir, "subDir1").apply { mkdir() }
            val subDir2 = File(rootDir, "subDir2").apply { mkdir() }
            File(rootDir, "file1.txt").apply { createNewFile() }
            File(subDir1, "subFile1.png").apply { createNewFile() }

            // Create a symlink to test prevention
            val symlinkFile = File(rootDir, "symlink")
            Files.createSymbolicLink(symlinkFile.toPath(), subDir1.toPath())

            val symlinkDir = File(rootDir, "symlinkDir")
            Files.createSymbolicLink(symlinkDir.toPath(), rootDir.toPath())

            // Ignore file
            File(rootDir, ".html4ignore").writeText(".*\\.txt")

            go(rootDir.absolutePath, 1) // max level 1

            assertTrue(File(rootDir, "index.html").exists())
            assertTrue(File(subDir1, "index.html").exists()) // level 1
            assertTrue(File(subDir2, "index.html").exists()) // level 1

            val rootIndexHtml = File(rootDir, "index.html").readText()
            assertTrue(rootIndexHtml.contains("subDir1"))
            assertTrue(rootIndexHtml.contains("subDir2"))
            assertFalse(rootIndexHtml.contains("file1.txt")) // ignored
            assertFalse(rootIndexHtml.contains("symlink")) // symlinks are skipped in middle
            assertFalse(rootIndexHtml.contains("symlinkDir")) // symlinks are skipped in middle

        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun testGoNoMaxLevel() {
        val rootDir = Files.createTempDirectory("goTestUnlim").toFile()
        try {
            val subDir = File(rootDir, "sub").apply { mkdir() }
            go(rootDir.absolutePath, -1)
            assertTrue(File(subDir, "index.html").exists())
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test
    fun testMainHtml4Tree() {
        val rootDir = Files.createTempDirectory("mainTest").toFile()
        try {
            main(arrayOf(rootDir.absolutePath, "--max-level", "0"))
            assertTrue(File(rootDir, "index.html").exists())
        } finally {
            rootDir.deleteRecursively()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("/path/does/not/exist/we/hope/12345", -1)
    }

    @Test
    fun testLinkedListBugCompat() {
        // Covering util.kt fully and its known behaviors
        val ll = LinkedList()
        val f1 = File("f1")
        val f2 = File("f2")
        val f3 = File("f3")

        ll.push(LinkedListEntry(f1, 1))
        ll.push(LinkedListEntry(f2, 2))
        ll.push(LinkedListEntry(f3, 3))

        val p1 = ll.pull()
        assertEquals(f1, p1?.file)

        val p2 = ll.pull()
        assertEquals(f2, p2?.file)

        val p3 = ll.pull()
        assertEquals(f3, p3?.file)

        val p4 = ll.pull()
        assertNull(p4)
    }

    @Test
    fun testLinkedListEntryAndEntry() {
        val f1 = File("f1")
        val f2 = File("f2")
        val lle = LinkedListEntry(f1, 0)
        assertEquals(f1, lle.file)
        assertEquals(0, lle.level)

        val entry2 = Entry(f2, 1, null)
        val entry1 = Entry(f1, 0, entry2)

        assertEquals(f1, entry1.data)
        assertEquals(0, entry1.level)
        assertEquals(entry2, entry1.next)

        entry1.next = null
        assertNull(entry1.next)

        // Coverage for getters and setters generated by Kotlin for variables in LinkedList
        val ll = LinkedList()
        ll.first = entry1
        assertEquals(entry1, ll.first)
        ll.last = entry2
        assertEquals(entry2, ll.last)
    }

    @Test
    fun testNonWritableDir() {
        val rootDir = Files.createTempDirectory("nowrite").toFile()
        try {
            rootDir.setWritable(false)
            // It should catch the exception silently in process_dir
            process_dir(rootDir)
        } finally {
            rootDir.setWritable(true)
            rootDir.deleteRecursively()
        }
    }
}
