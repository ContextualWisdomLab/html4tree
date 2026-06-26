package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class BranchTest {

    @Test
    fun testProcessIgnoreFileWithMiss() {
        val tempDir = Files.createTempDirectory("ignore_miss").toFile()
        tempDir.deleteOnExit()

        File(tempDir, ".html4ignore").writeText("secret.*")
        File(tempDir, "public.txt").createNewFile()

        val excluded = process_ignore_file(tempDir)
        assert(!excluded.contains("public.txt"))
    }

    @Test
    fun testProcessDirWithIgnoreFile() {
        val tempDir = Files.createTempDirectory("process_dir").toFile()
        tempDir.deleteOnExit()

        File(tempDir, ".html4ignore").writeText("secret.*")
        File(tempDir, "secret.txt").createNewFile()
        File(tempDir, "public.txt").createNewFile()

        process_dir(tempDir)

        val indexHtml = File(tempDir, "index.html").readText()
        assert(!indexHtml.contains("secret.txt"))
        assert(indexHtml.contains("public.txt"))
    }

    @Test
    fun testGoWithDeepDirectory() {
        val tempDir = Files.createTempDirectory("go_deep").toFile()
        tempDir.deleteOnExit()

        val subDir = File(tempDir, "sub")
        subDir.mkdir()
        File(subDir, "file.txt").createNewFile()

        go(tempDir.absolutePath, 0)

        // index.html in subDir should not be created if maxLevel is 0
        assert(!File(subDir, "index.html").exists())
    }
}
