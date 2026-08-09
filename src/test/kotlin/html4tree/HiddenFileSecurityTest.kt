package html4tree

import org.junit.Test
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
