package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

class GlobTest {
    @Test
    fun testGlobExceptionHandled() {
        val tempDir = Files.createTempDirectory("glob_test").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("[\nvalid.txt\n")

            val validFile = File(tempDir, "valid.txt")
            validFile.createNewFile()

            val excluded = process_ignore_file(tempDir, null)
            assertTrue(excluded.contains("valid.txt"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testDirNameFallbackNormal() {
        val tempDir = Files.createTempDirectory("fallback_test_normal").toFile()
        try {
            process_dir(tempDir, emptySet(), emptyArray())
            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists())
            val content = indexFile.readText()
            val escapedDirName = tempDir.name.escapeHtml()
            assertTrue(content.contains("<title>$escapedDirName</title>"))
            assertTrue(content.contains("<h1>$escapedDirName</h1>"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}

class ExtraTest {
    @Test
    fun testFallbackInlinedEmpty() {
        val tempDir = Files.createTempDirectory("fallback_test_empty").toFile()
        val emptyNameDir = object : File(tempDir.path) {
            override fun getName(): String = ""
        }
        try {
            process_dir(emptyNameDir, emptySet(), emptyArray())
            val indexFile = File(tempDir, "index.html")
            assertTrue(indexFile.exists())
            val content = indexFile.readText()
            assertTrue(content.contains("<title>/</title>"))
            assertTrue(content.contains("<h1>/</h1>"))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
