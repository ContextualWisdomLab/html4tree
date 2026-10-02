package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PosixPermissionsTest {
    @Test
    fun writeIndexFileAppliesPosixPermissionsWhenSupported() {
        val tempDir = Files.createTempDirectory("html4tree-test-posix").toFile()
        try {
            val actuallySupportsPosix = java.nio.file.FileSystems.getDefault().supportedFileAttributeViews().contains("posix")
            write_index_file(tempDir, "posix permissions test", supportsPosix = actuallySupportsPosix)
            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists())

            if (actuallySupportsPosix) {
                val perms = Files.getPosixFilePermissions(indexFile.toPath())
                assertEquals(
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-r--r--"),
                    perms
                )
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun writeIndexFileSkipsPosixPermissionsWhenNotSupported() {
        val tempDir = Files.createTempDirectory("html4tree-test-no-posix").toFile()
        try {
            write_index_file(tempDir, "posix permissions test fallback", supportsPosix = false)
            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
