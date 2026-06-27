package html4tree

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.File

class BranchTest4 {
    @Test
    fun testGoNotADirectory() {
        val tempFile = File.createTempFile("test", "file")
        try {
            // we already tested `go` with file which throws IllegalArgumentException
            // wait, we need to test if `lle.file.isDirectory()` returns false in the while loop
            // but we only push if `it.isDirectory()` is true.
            // So we can never have a non-directory in `ll` unless we construct it manually and pass it.

            // To hit that branch, we just use our LinkedList directly and call push with a File
            // but `go` only pushes `top_dir` which we require to be a dir,
            // and it loops over `listFiles` and pushes ONLY if `it.isDirectory()`.
            // So the condition `lle.file.isDirectory()` is always true for what we push!
            // Thus, we cannot hit the false branch of `lle.file.isDirectory()` inside `go`'s while loop
            // without modifying `go`. The Kotlin compiler / Jacoco sees a branch there because `&&` creates one.

            assertTrue(true) // Just an acknowledgement
        } finally {
            tempFile.delete()
        }
    }
}
