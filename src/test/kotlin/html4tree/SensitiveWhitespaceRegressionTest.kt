package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression coverage for padded sensitive names at the listing boundary. */
class SensitiveWhitespaceRegressionTest {
    private lateinit var tempDir: File

    @Before
    fun createTemporaryDirectory() {
        tempDir = Files.createTempDirectory("html4tree-sensitive-ws-").toFile()
    }

    @After
    fun removeTemporaryDirectory() {
        tempDir.deleteRecursively()
    }

    @Test
    fun paddedSensitiveNamesPreserveExactObservedIdentityInExclusions() {
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
        }
        listOf(" public.txt ", "image.png").forEach { observed ->
            assertFalse(observed in excluded, "ordinary filenames must remain visible: '$observed'")
        }
    }

    @Test
    fun generatedListingOmitsPaddedSensitiveFilesAndKeepsOrdinaryPaddedNames() {
        val paddedGit = File(tempDir, " .git").apply { writeText("secret") }
        val paddedConfig = File(tempDir, "config.json ").apply { writeText("{}") }
        val ordinaryPadded = File(tempDir, " public.txt ").apply { writeText("ok") }

        process_dir(tempDir, null, arrayOf(paddedGit, paddedConfig, ordinaryPadded))

        val generatedHtml = File(tempDir, "index.html").readText(Charsets.UTF_8)
        assertFalse(generatedHtml.contains("entry-name\" dir=\"auto\"> .git<"))
        assertFalse(generatedHtml.contains("entry-name\" dir=\"auto\">config.json <"))
        assertTrue(generatedHtml.contains("<span class=\"entry-name\" dir=\"auto\"> public.txt </span>"))
        assertTrue(generatedHtml.contains("title=\"${isolate_bidi_plain_text(" public.txt ")} 파일\""))
    }
}
