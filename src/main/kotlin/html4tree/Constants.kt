package html4tree

import java.security.MessageDigest
import java.util.Base64

internal object Constants {
    const val CSS_CONTENT = """body {
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
  outline: 2px solid #0969da;
  outline-offset: -2px;
}
a:hover span:last-child, a:focus-visible span:last-child {
  text-decoration: underline;
}
@media (prefers-reduced-motion: reduce) {
  a {
    transition: none;
  }
}
li + li {
  border-top: 1px solid #d0d7de;
}
.empty-dir {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.5rem;
  color: #656d76;
  font-style: italic;
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
  li + li {
    border-top-color: #21262d;
  }
  .empty-dir {
    color: #8b949e;
  }
}"""

    @JvmField
    val STYLE_HASH = "sha256-" + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(CSS_CONTENT.toByteArray(Charsets.UTF_8)))

    @JvmField
    val DEFAULT_SENSITIVE_FILES = listOf(".git", ".env", ".ssh", ".htpasswd", ".htaccess", "id_rsa", "id_ed25519", "secrets.yml", ".html4ignore", ".DS_Store", ".aws", ".kube", ".npmrc", ".gnupg", "config.json", "credentials.json")
}
