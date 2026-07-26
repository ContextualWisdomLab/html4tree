package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class GlobIllegalArgumentExceptionTest {
    @Test
    fun testProcessIgnoreFileWithIllegalArgumentExceptionGlobPattern() {
        val tempDir = Files.createTempDirectory("globexc2").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            // A pattern like "a[b" will throw PatternSyntaxException
            // and we rely on IllegalArgumentException to catch it.
            // But how do we test catching exactly IllegalArgumentException that is NOT PatternSyntaxException?
            // Actually, PatternSyntaxException IS an IllegalArgumentException, so the catch block is fully covered
            // when ANY IllegalArgumentException (or subclass) is thrown.
            ignoreFile.writeText("a[b\n")
            val excluded = process_ignore_file(tempDir, null)
            assertTrue(excluded.contains("index.html"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
