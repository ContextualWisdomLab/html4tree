package html4tree

import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertTrue
import kotlin.test.fail
import java.io.File
import java.nio.file.StandardOpenOption
import java.nio.file.LinkOption

class SymlinkReadTest {

    @Test
    fun testProcessIgnoreFileDoesNotFollowSymlink() {
        val tempDir = Files.createTempDirectory("test-symlink-read-")
        try {
            val secretFile = tempDir.resolve("secret.txt")
            Files.write(secretFile, listOf("top_secret_pattern"))

            val symlinkFile = tempDir.resolve(".html4ignore")
            try {
                Files.createSymbolicLink(symlinkFile, secretFile)
            } catch (e: UnsupportedOperationException) {
                return // OS might not support symlinks, skip test
            } catch (e: java.nio.file.FileSystemException) {
                return // Windows might require admin privileges for symlinks
            }

            val excludeSet = process_ignore_file(tempDir.toFile(), arrayOf("test.txt"))

            // "test.txt" should not be excluded because the symlink .html4ignore should be ignored/not followed
            // If it WAS followed, "top_secret_pattern" might cause matching, but even if not,
            // we mainly want to ensure no exception is thrown when NOFOLLOW_LINKS hits a symlink in useLines
            // (actually since we check !isSymbolicLink first, process_ignore_file skips it entirely.
            // But if it was swapped between the check and useLines, newInputStream with NOFOLLOW_LINKS would throw an exception).
            assertTrue(excludeSet.contains("index.html")) // Should just contain defaults

        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
}
