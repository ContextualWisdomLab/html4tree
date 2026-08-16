package html4tree

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

/** Regression coverage for padded sensitive names at the listing boundary. */
class SensitiveWhitespaceRegressionTest {
    @TempDir
    lateinit var tempDir: File

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

        val excluded = process_ignore_file(tempDir, observedNames)

        listOf(" .git", "config.json ", "\tsecrets.yml", ".npmrc\r").forEach { observed ->
            assertTrue(observed in excluded, "the exact observed sensitive name must be excluded: '$observed'")
            assertFalse(
                observed.trim() in excluded && observed.trim() != observed,
                "a normalized alias must not replace the exact filesystem entry identity: '$observed'",
            )
        }
        listOf(" public.txt ", "image.png").forEach { observed ->
            assertFalse(observed in excluded, "ordinary filenames must remain visible: '$observed'")
        }
    }
}
