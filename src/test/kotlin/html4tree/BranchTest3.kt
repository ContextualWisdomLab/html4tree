package html4tree

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File

class BranchTest3 {
    @Test
    fun testProcessIgnoreEmptyDirectory() {
        // Test go with empty directory. The while loop condition `lle != null && lle.file.isDirectory()`
        // should skip if `lle` is null or it's a file, but since we always push a dir, it's about hitting
        // the loop end where we pull and get null.
        // Actually, we've hit these. The missed branches are likely in `process_ignore_file` where we loop over dir files
        // and one branch might be `regex.matches` being false for all, or something like that.

        val tempDir = createTempDir("testNoMatches")
        try {
            File(tempDir, ".html4ignore").writeText(".*\\.exe") // won't match anything
            File(tempDir, "file1.txt").writeText("test")

            val excluded = process_ignore_file(tempDir)
            assertTrue(excluded.contains("index.html"))
            assertFalse(excluded.contains("file1.txt"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
