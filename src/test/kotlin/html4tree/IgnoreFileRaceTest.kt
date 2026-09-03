package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.FileSystemException
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IgnoreFileRaceTest {
    @Test
    fun racedOpenFailureIsContainedAfterPreOpenValidation() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-race").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("*.initial\n")
            File(tempDir, "victim.victim").writeText("candidate")
            File(tempDir, "keep.txt").writeText("candidate")
            val names = arrayOf(".html4ignore", "victim.victim", "keep.txt")
            var openerCalled = false

            val excluded = process_ignore_file(tempDir, names) { path ->
                openerCalled = true
                assertEquals(ignoreFile.toPath(), path)
                // This seam runs only after the production regular-file/readability/size
                // checks. Replace the entry at that point, then model the NOFOLLOW open
                // failing because the checked entry is no longer safely readable.
                ignoreFile.writeText("*.victim\n")
                throw FileSystemException(path.toString(), null, "simulated raced replacement")
            }

            assertTrue(openerCalled, "the injected opener must run after pre-open validation")
            assertFalse("victim.victim" in excluded, "raced replacement patterns must not be applied")
            assertFalse("keep.txt" in excluded)
            assertTrue("index.html" in excluded, "mandatory default exclusions must survive the race")
            assertTrue(".html4ignore" in excluded, "the ignore file itself remains excluded")
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun regularIgnoreFileStillAppliesValidPatterns() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-positive").toFile()
        try {
            File(tempDir, ".html4ignore").writeText("*.artifact\n")
            File(tempDir, "plan.artifact").writeText("candidate")
            File(tempDir, "keep.txt").writeText("candidate")
            val names = arrayOf(".html4ignore", "plan.artifact", "keep.txt")

            val excluded = process_ignore_file(tempDir, names)

            assertTrue("plan.artifact" in excluded)
            assertFalse("keep.txt" in excluded)
            assertTrue("index.html" in excluded)
            assertTrue(".html4ignore" in excluded)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
