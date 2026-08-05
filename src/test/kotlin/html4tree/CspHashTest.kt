package html4tree

import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CspHashTest {
    @Test
    fun emittedStyleBytesMatchTheDeclaredCspHash() {
        val directory = Files.createTempDirectory("html4tree-csp-").toFile()

        try {
            process_dir(directory, setOf("index.html"), emptyArray<File>())

            val html = File(directory, "index.html").readText(Charsets.UTF_8)
            val styleContent = Regex("""<style>([\s\S]*?)</style>""")
                .find(html)
                ?.groupValues
                ?.get(1)
            val declaredHash = Regex("""style-src 'sha256-([^']+)'""")
                .find(html)
                ?.groupValues
                ?.get(1)

            assertNotNull(styleContent, "Generated HTML must contain one inline style block")
            assertNotNull(declaredHash, "Generated HTML must declare a SHA-256 style source")
            assertEquals(styleContent.trim(), styleContent, "Hashed style bytes must not gain template padding")

            val actualHash = Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-256")
                    .digest(styleContent.toByteArray(Charsets.UTF_8))
            )
            assertEquals(declaredHash, actualHash)
            assertTrue(styleContent.startsWith("body {"))
            assertTrue(styleContent.endsWith("}"))
        } finally {
            directory.listFiles()?.forEach { it.delete() }
            directory.delete()
        }
    }
}
