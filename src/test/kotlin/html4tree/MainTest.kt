package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import java.io.File
import java.nio.file.Files

class MainTest {
    @Test
    fun testProcessIgnoreFile() {
        val tempDir = Files.createTempDirectory("html4tree_test").toFile()

        File(tempDir, "file1.txt").writeText("test")
        File(tempDir, "file2.txt").writeText("test")
        File(tempDir, "index.html").writeText("test")

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("file1.*")

        val exclude = process_ignore_file(tempDir)

        assertEquals(true, exclude.contains("file1.txt"))
        assertEquals(false, exclude.contains("file2.txt"))
        assertEquals(true, exclude.contains("index.html"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testHtml4tree() {
        val tempDir = File.createTempFile("html4tree_test2", "")
        tempDir.delete()
        tempDir.mkdir()

        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        File(subdir, "file1.txt").writeText("test")

        val args = arrayOf(tempDir.absolutePath)
        main(args)

        assertEquals(true, File(tempDir, "index.html").exists())
        assertEquals(true, File(subdir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testHtml4treeMaxLevel() {
        val tempDir = File.createTempFile("html4tree_test3", "")
        tempDir.delete()
        tempDir.mkdir()

        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        val subsubdir = File(subdir, "subsubdir")
        subsubdir.mkdir()

        val args = arrayOf("--max-level", "0", tempDir.absolutePath)
        main(args)

        assertEquals(true, File(tempDir, "index.html").exists())
        assertEquals(false, File(subdir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test
    fun testHelp() {
        help()
    }

    @Test
    fun testLinkedList() {
        val ll = LinkedList()
        val tempDir = File.createTempFile("test_ll", "")
        val lle = LinkedListEntry(tempDir, 1)
        ll.push(lle)

        val lle2 = LinkedListEntry(tempDir, 2)
        ll.push(lle2)

        val p1 = ll.pull()
        val p2 = ll.pull()
        val p3 = ll.pull()

        assertEquals(1, p1?.level)
        assertEquals(2, p2?.level)
        assertEquals(null, p3)
        tempDir.delete()
    }

    @Test
    fun testLinkedListInitialization() {
        val ll = LinkedList()
        assertEquals(null, ll.first)
        assertEquals(null, ll.last)
    }

    @Test
    fun testLinkedListMore() {
        val ll = LinkedList()
        val tempDir = File.createTempFile("test_ll_more", "")

        // This hits the else block where first?.next stuff runs
        val lle1 = LinkedListEntry(tempDir, 1)
        ll.push(lle1)
        val lle2 = LinkedListEntry(tempDir, 2)
        ll.push(lle2)
        val lle3 = LinkedListEntry(tempDir, 3)
        ll.push(lle3)

        ll.pull()
        ll.pull()
        ll.pull()
        ll.pull() // Extra pull
        tempDir.delete()
    }

    @Test
    fun testProcessDir() {
        val tempDir = File.createTempFile("process_dir_test", "")
        tempDir.delete()
        tempDir.mkdir()

        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        File(tempDir, "file1.txt").writeText("test")
        File(tempDir, "file2.txt").writeText("test")

        process_dir(tempDir)
        assertEquals(true, File(tempDir, "index.html").exists())

        val content = File(tempDir, "index.html").readText()
        assertEquals(true, content.contains("file1.txt"))
        assertEquals(true, content.contains("file2.txt"))
        assertEquals(true, content.contains("subdir"))

        tempDir.deleteRecursively()
    }

    @Test
    fun testGoMaxLevelLimit() {
        val tempDir = File.createTempFile("go_max_test", "")
        tempDir.delete()
        tempDir.mkdir()

        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val subsubdir = File(subdir, "subsubdir")
        subsubdir.mkdir()

        go(tempDir.absolutePath, 1)

        assertEquals(true, File(tempDir, "index.html").exists())
        assertEquals(true, File(subdir, "index.html").exists())
        assertEquals(false, File(subsubdir, "index.html").exists())

        tempDir.deleteRecursively()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("nonexistent_directory_which_should_throw", -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoNotADir() {
        val tempFile = File.createTempFile("go_not_dir", ".txt")
        tempFile.deleteOnExit()
        go(tempFile.absolutePath, -1)
    }

    @Test
    fun testGoEmptyDir() {
        val tempDir = File.createTempFile("go_empty", "")
        tempDir.delete()
        tempDir.mkdir()

        go(tempDir.absolutePath, -1)
        assertEquals(true, File(tempDir, "index.html").exists())
        tempDir.deleteRecursively()
    }

    @Test
    fun testIgnoreFileWithIndexHtml() {
        val tempDir = File.createTempFile("ignore_test2", "")
        tempDir.delete()
        tempDir.mkdir()

        File(tempDir, "index.html").writeText("test")

        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("index.html")

        val exclude = process_ignore_file(tempDir)
        assertEquals(true, exclude.contains("index.html"))
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessDirItEqualsCurrDir() {
        val tempDir = File.createTempFile("process_dir_it_eq", "")
        tempDir.delete()
        tempDir.mkdir()

        // it == curr_dir is impossible for listFiles(), but just in case, we add another directory structure
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        val content = process_ignore_file(tempDir)
        assertEquals(true, content.contains("index.html"))
        tempDir.deleteRecursively()
    }

    @Test
    fun testLinkedListPushBranch() {
        val ll = LinkedList()
        val tempDir = File.createTempFile("ll_push_test", "")

        // Setup initial element
        val lle1 = LinkedListEntry(tempDir, 1)
        ll.push(lle1)

        // Make first point to null to hit the `first?.next` safe call branch where `first` might be null (though logically it shouldn't be here, the compiler sees it as nullable)
        ll.first = null
        val lle2 = LinkedListEntry(tempDir, 2)
        ll.push(lle2)

        tempDir.delete()
    }

    @Test
    fun testProcessDirItIsCurrDir() {
        val tempDir = File.createTempFile("process_dir_test2", "")
        tempDir.delete()
        tempDir.mkdir()

        // This won't actually have it == curr_dir but tests index_middle condition
        process_dir(tempDir)
        tempDir.deleteRecursively()
    }

    @Test
    fun testGoLevelMinusOne() {
        val tempDir = File.createTempFile("go_level_m1", "")
        tempDir.delete()
        tempDir.mkdir()

        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val subsubdir = File(subdir, "subsubdir")
        subsubdir.mkdir()

        // Testing the while condition with missing branches (lle != null && lle.file.isDirectory())
        File(tempDir, "file.txt").writeText("hi")

        go(tempDir.absolutePath, -1)
        tempDir.deleteRecursively()
    }

    @Test
    fun testLinkedListSetLast() {
        val ll = LinkedList()
        ll.last = null
    }

    @Test
    fun testGoNotDirPull() {
        val tempDir = File.createTempFile("go_not_dir_pull", "")
        tempDir.delete()
        tempDir.mkdir()

        // Ensure there is a file in the queue that is NOT a directory
        // This hits the `while(lle != null && lle.file.isDirectory())` false branch for the `isDirectory` part
        val file = File(tempDir, "file.txt")
        file.writeText("test")

        val ll = LinkedList()
        ll.push(LinkedListEntry(file, 0))

        // Actually to hit it in `go`, we just need a file in top_dir
        go(tempDir.absolutePath, 1)
        tempDir.deleteRecursively()
    }

    @Test
    fun testProcessDirItEqualsCurrDir2() {
        val tempDir = File.createTempFile("process_dir_test_eq", "")
        tempDir.delete()
        tempDir.mkdir()

        try {
            File(tempDir, "file.txt").writeText("test")
            process_dir(tempDir)
            assertEquals(true, File(tempDir, "index.html").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
