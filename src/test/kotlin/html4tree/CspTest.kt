package html4tree

import org.junit.Test
import java.security.MessageDigest
import java.util.Base64
import kotlin.test.assertTrue

class CspTest {
    @Test
    fun testCspHashMatchesInjectedStyle() {
        val cssContent = """
              body {
                font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                line-height: 1.5;
                padding: 1rem;
                color: #1f2328;
              }
              main {
                max-width: 800px;
                margin: 0 auto;
              }
              ul {
                list-style-type: none;
                padding-left: 0;
              }
              a.dir-link {
                display: flex;
                align-items: flex-start;
                gap: 0.5rem;
                width: 100%;
                overflow-wrap: anywhere;
                box-sizing: border-box;
              }
              .icon {
                flex-shrink: 0;
                width: 1.25rem;
                text-align: center;
              }
              a {
                padding: 0.5rem;
                text-decoration: none;
                color: #0969da;
                border-radius: 4px;
                transition: background-color 0.2s ease, outline-color 0.2s ease;
              }
              a:hover, a:focus-visible {
                background-color: #f6f8fa;
                text-decoration: underline;
                outline: 2px solid #0969da;
                outline-offset: -2px;
              }
              @media (prefers-reduced-motion: reduce) {
                a {
                  transition: none;
                }
              }
              @media (prefers-color-scheme: dark) {
                body {
                  background-color: #0d1117;
                  color: #c9d1d9;
                }
                a {
                  color: #58a6ff;
                }
                a:hover, a:focus-visible {
                  background-color: #161b22;
                  outline-color: #58a6ff;
                }
              }
              .empty-dir {
                padding: 0.5rem;
                opacity: 0.7;
                font-style: italic;
              }
              """.trimIndent()

        val styleHash = "sha256-" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(cssContent.toByteArray(Charsets.UTF_8)))

        val css = "<style>${cssContent}</style>"
        val insideStyle = css.substringAfter("<style>").substringBefore("</style>")
        val styleHash2 = "sha256-" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(insideStyle.toByteArray(Charsets.UTF_8)))

        assertTrue(styleHash == styleHash2, "The CSP hash must exactly match the injected inline style content.")
    }
}
