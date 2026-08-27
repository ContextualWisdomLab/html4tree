package html4tree

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression coverage for whitespace-padded filesystem identities. */
class SensitiveWhitespaceRegressionTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun paddedSensitiveNameIsMatchedByNormalizedPolicyButExcludedByExactIdentity() {
        val root = temporaryFolder.newFolder("root")
        val sensitiveName = " .git"
        val ordinaryName = " report.txt "
        val sensitiveFile = File(root, sensitiveName).apply { writeText("secret") }
        val ordinaryFile = File(root, ordinaryName).apply { writeText("safe") }

        val excluded = process_ignore_file(root, arrayOf(sensitiveName, ordinaryName))

        assertTrue(
            sensitiveName in excluded,
            "the exact observed filesystem identity must be excluded after normalized matching"
        )
        assertFalse(
            ordinaryName in excluded,
            "normal whitespace padding must not make an ordinary file sensitive"
        )

        process_dir(root, excluded, arrayOf(sensitiveFile, ordinaryFile))
        val generatedHtml = File(root, "index.html").readText(Charsets.UTF_8)

        assertFalse(
            generatedHtml.contains(sensitiveName.escapeHtml()),
            "a padded sensitive filesystem entry must never appear in generated output"
        )
        assertTrue(
            generatedHtml.contains(ordinaryName.escapeHtml()),
            "an ordinary padded filename remains buyer-visible"
        )
    }
}
