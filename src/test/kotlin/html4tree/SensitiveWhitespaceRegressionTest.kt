package html4tree

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.rules.TemporaryFolder

/** Regression coverage for padded sensitive names at the listing boundary. */
class SensitiveWhitespaceRegressionTest {
    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `padded sensitive names preserve exact observed identity in exclusions`() {
        val observedNames = arrayOf(
            " .git",
            "config.json ",
            "\tsecrets.yml",
            ".npmrc\r",
            " public.txt ",
            "image.png",
        )

        val excluded = process_ignore_file(temporaryFolder.root, observedNames)

        listOf(" .git", "config.json ", "\tsecrets.yml", ".npmrc\r").forEach { observed ->
            assertTrue(observed in excluded, "the exact observed sensitive name must be excluded: '$observed'")
        }
        listOf(" public.txt ", "image.png").forEach { observed ->
            assertFalse(observed in excluded, "ordinary filenames must remain visible: '$observed'")
        }
    }
}
