package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class BranchTest5 {
    @Test
    fun testGoWithSymlinkDir() {
        val tempDir = Files.createTempDirectory("go_symlink_dir_main").toFile()
        tempDir.deleteOnExit()

        val subDir = File(tempDir, "sub")
        subDir.mkdir()

        val targetDir = Files.createTempDirectory("go_symlink_dir_outside").toFile()
        targetDir.deleteOnExit()

        val linkFile = File(subDir, "link")
        Files.createSymbolicLink(linkFile.toPath(), targetDir.toPath())

        go(tempDir.absolutePath, -1)

        assert(!File(targetDir, "index.html").exists())
    }

    @Test
    fun testGoWithNullLleFileIsDirectory() {
        val list = LinkedList()
        val file = File("some_file_that_doesnt_exist_so_is_not_dir")
        list.push(LinkedListEntry(file, 0))
        val lle = list.pull()
        assert(lle != null && !lle.file.isDirectory())
    }

    @Test
    fun testProcessIgnoreFileWithIndexHtmlInIt() {
        val tempDir = Files.createTempDirectory("ignore_index").toFile()
        tempDir.deleteOnExit()

        File(tempDir, ".html4ignore").writeText("index\\.html")
        File(tempDir, "index.html").createNewFile()

        val excluded = process_ignore_file(tempDir)
        assert(excluded.contains("index.html"))
    }
}
