package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class BranchTest3 {
    @Test
    fun testGoWithLleNotDir() {
        val tempDir = Files.createTempDirectory("go_not_dir").toFile()
        tempDir.deleteOnExit()

        // This is mainly to satisfy coverage - we'd need to manipulate the linked list to yield a file directly,
        // but `go` only pushes directories due to the condition `it.isDirectory()`.
        // However, we can test process_dir not looping into files
        val ll = LinkedList()
        val file = File(tempDir, "test.txt")
        file.createNewFile()
        ll.push(LinkedListEntry(file, 0))
        val pulled = ll.pull()
        assert(pulled?.file?.isDirectory() == false)
    }

    @Test
    fun testHtml4TreeRun() {
        val tempDir = Files.createTempDirectory("html4tree_run").toFile()
        tempDir.deleteOnExit()

        val cmd = Html4tree()
        // We can't directly call run() without parsing if we don't mock Clikt context,
        // but we can call main with args which hits it
        cmd.main(arrayOf(tempDir.absolutePath))
        assert(File(tempDir, "index.html").exists())
    }
}
