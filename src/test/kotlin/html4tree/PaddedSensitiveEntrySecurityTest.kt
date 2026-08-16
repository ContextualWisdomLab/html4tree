package html4tree

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

/** Security regression for whitespace-padded sensitive filesystem entries. */
class PaddedSensitiveEntrySecurityTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `sensitive matching preserves exact observed filesystem identity`() {
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
            assertTrue(observed in excluded, "exact sensitive entry must be excluded: '$observed'")
            if (observed.trim() != observed) {
                assertFalse(
                    observed.trim() in excluded,
                    "normalization must not replace the exact downstream File.name identity: '$observed'",
                )
            }
        }
        listOf(" public.txt ", "image.png").forEach { observed ->
            assertFalse(observed in excluded, "ordinary entry must remain visible: '$observed'")
        }
    }
}
