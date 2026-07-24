package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.security.MessageDigest
import java.util.Base64

class CspTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testCspHashMatchesStyleContent() {
        val testDir = tempFolder.newFolder("csp_test")
        process_dir(testDir)
        val indexFile = File(testDir, "index.html")
        val content = indexFile.readText()

        val styleRegex = Regex("<style>(.*?)</style>", RegexOption.DOT_MATCHES_ALL)
        val styleMatch = styleRegex.find(content)
        assertNotNull("Could not find style tag", styleMatch)

        val styleContent = styleMatch!!.groupValues[1]
        val actualHash = "sha256-" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(styleContent.toByteArray(Charsets.UTF_8)))

        val cspRegex = Regex("style-src '([^']+)'")
        val cspMatch = cspRegex.find(content)
        assertNotNull("Could not find CSP style-src", cspMatch)

        val expectedHash = cspMatch!!.groupValues[1]

        println("Actual hash based on content: " + actualHash)
        println("Expected hash in CSP header:  " + expectedHash)
        assertEquals("CSP hash does not match style content", expectedHash, actualHash)
    }
}
