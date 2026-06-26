package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class BranchTest2 {
    @Test
    fun testProcessIgnoreFileWithMissRegex() {
        val tempDir = Files.createTempDirectory("ignore_regex").toFile()
        tempDir.deleteOnExit()

        File(tempDir, ".html4ignore").writeText("secret.*")
        File(tempDir, "public.txt").createNewFile()

        val excluded = process_ignore_file(tempDir)
        assert(!excluded.contains("public.txt"))
    }

    @Test
    fun testGoWithEmptySubdirs() {
        val tempDir = Files.createTempDirectory("go_empty").toFile()
        tempDir.deleteOnExit()

        val subDir = File(tempDir, "sub")
        subDir.mkdir()

        go(tempDir.absolutePath, -1)

        assert(File(subDir, "index.html").exists())
    }
}
