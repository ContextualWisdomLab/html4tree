package html4tree

import org.junit.Test
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.File

class HiddenFileSecurityTest {
    @Test
    fun hiddenFileClassifierRecognizesAsciiAndUnicodeDotPrefixes() {
        val hiddenNames = listOf(".env", "\u3002env", "\uFF0Egit", "\uFF61ssh")

        hiddenNames.forEach { name ->
            assertTrue(name.isHiddenFile(), "Dot-like prefix must be treated as hidden: $name")
        }
        assertFalse("visible.txt".isHiddenFile())
        assertFalse("".isHiddenFile())
    }

    @Test
    fun failClosedOnInaccessibleIgnoreFile() {
        val directory = Files.createTempDirectory("html4tree-fail-closed-").toFile()
        try {
            val ignoreFile = directory.resolve(".html4ignore")
            ignoreFile.writeText("secret")

            org.junit.Assume.assumeTrue("Test requires ability to make file unreadable", ignoreFile.setReadable(false))

            var thrown = false
            try {
                process_ignore_file(directory, arrayOf(".html4ignore"))
            } catch (e: IgnoreFileReadException) {
                thrown = true
            }
            assertTrue(thrown, "Expected IgnoreFileReadException to be thrown when .html4ignore is inaccessible")
        } finally {
            directory.resolve(".html4ignore").setReadable(true)
            directory.deleteRecursively()
        }
    }

    @Test
    fun failClosedOnToctouIgnoreFile() {
        val directory = Files.createTempDirectory("html4tree-fail-closed-toctou-").toFile()
        try {
            var thrown = false
            try {
                process_ignore_file(directory, arrayOf(".html4ignore"))
            } catch (e: IgnoreFileReadException) {
                thrown = true
            }
            assertTrue(thrown, "Expected IgnoreFileReadException to be thrown when .html4ignore is in snapshot but missing on disk")

            // Add coverage for crawl_directories catching the exception
            val ll = LinkedList()
            ll.push(LinkedListEntry(directory, 0, read_file_identity(directory).key))
            crawl_directories(
                ll,
                -1,
                processDirectory = { _, _, _ -> },
                processIgnoreFile = { _, _ -> throw IgnoreFileReadException("mock") },
                listFiles = { arrayOf(File(".")) },
                readIdentity = { read_file_identity(it) }
            )
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun failClosedOnToctouIgnoreFileCaseInsensitive() {
        val directory = Files.createTempDirectory("html4tree-fail-closed-toctou-case-").toFile()
        try {
            var thrown = false
            try {
                process_ignore_file(directory, arrayOf(".HTML4IGNORE"))
            } catch (e: IgnoreFileReadException) {
                thrown = true
            }
            assertTrue(thrown, "Expected IgnoreFileReadException to be thrown when .HTML4IGNORE is in snapshot but missing on disk")
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun unicodeDotHomoglyphsAreExcludedFromDirectoryIndexes() {
        val directory = Files.createTempDirectory("html4tree-homoglyph-").toFile()
        try {
            val hiddenNames = listOf("\u3002env", "\uFF0Egit", "\uFF61ssh")
            hiddenNames.forEach { name -> directory.resolve(name).writeText("secret") }

            val excluded = process_ignore_file(directory)

            hiddenNames.forEach { name ->
                assertTrue(name in excluded, "Unicode dot homoglyph must be treated as hidden: $name")
            }
        } finally {
            directory.deleteRecursively()
        }
    }
}
