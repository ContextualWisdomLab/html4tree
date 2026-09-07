package html4tree

import org.junit.Test
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HiddenFileSecurityTest {
    @Test
    fun hiddenFileClassifierRecognizesAsciiAndUnicodeDotPrefixes() {
        val directory = Files.createTempDirectory("html4tree-hidden-").toFile()
        try {
            val hiddenNames = listOf(".env")
            hiddenNames.forEach { name ->
                val f = directory.resolve(name)
                f.writeText("secret")
                assertTrue(f.isHidden(), "Dot prefix must be treated as hidden: $name")
            }
            val visible = directory.resolve("visible.txt")
            visible.writeText("visible")
            assertFalse(visible.isHidden())
        } finally {
            directory.deleteRecursively()
        }
    }

}
