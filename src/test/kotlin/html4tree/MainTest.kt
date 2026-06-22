package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class MainTest {
    @Test
    fun testXSSVulnerabilityFixed() {
        val testDirName = "build/tmp_test_dir"
        val testDir = File(testDirName)
        if (testDir.exists()) {
            testDir.deleteRecursively()
        }
        testDir.mkdirs()

        // Create a directory with a potentially malicious name
        val maliciousDirName = "\"><script>alert(1)</script>"
        val maliciousDir = File(testDir, maliciousDirName)
        maliciousDir.mkdirs()

        // Create a file with a potentially malicious name
        val maliciousFileName = "bad'file\".txt"
        val maliciousFile = File(testDir, maliciousFileName)
        maliciousFile.createNewFile()

        // Run the html4tree logic
        go(testDirName, -1)

        val indexFile = File(testDir, "index.html")
        assertTrue(indexFile.exists(), "index.html should be created")

        val content = indexFile.readText()

        // Verify HTML escaping for directory name
        assertFalse(content.contains(maliciousDirName), "Should not contain raw malicious directory name")
        assertTrue(content.contains("&quot;&gt;&lt;script&gt;alert(1)&lt;"), "Should contain HTML escaped malicious directory name")

        // Verify HTML escaping for file name
        assertFalse(content.contains(maliciousFileName), "Should not contain raw malicious file name")
        assertTrue(content.contains("bad&#x27;file&quot;.txt"), "Should contain HTML escaped malicious file name")

        // Verify URL encoding for directory href
        assertTrue(content.contains("href=\"./%22%3E%3Cscript%3Ealert%281%29%3C%2Fscript%3E/\"") || content.contains("href=\"./%22%3E%3Cscript%3Ealert%281%29%3C%2Fscript%3E\"") || content.contains("href=\"./%22%3E%3Cscript%3Ealert%281%29%3C/\""), "Should contain URL encoded directory href")

        // Verify URL encoding for file href
        assertTrue(content.contains("href=\"./bad%27file%22.txt\""), "Should contain URL encoded file href")

        // Verify no raw quotes in href
        assertFalse(content.contains("href=\"./$maliciousDirName/\""), "Should not contain raw directory name in href")
        assertFalse(content.contains("href=\"./$maliciousFileName\""), "Should not contain raw file name in href")

        // Clean up
        testDir.deleteRecursively()
    }
}
