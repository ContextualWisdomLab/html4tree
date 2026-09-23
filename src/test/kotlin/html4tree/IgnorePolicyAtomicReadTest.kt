package html4tree

import org.junit.Test
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IgnorePolicyAtomicReadTest {
    @Test
    fun policySwapAfterValidationCannotChangeParsedAuthority() {
        val directory = Files.createTempDirectory("html4tree-policy-swap-")
        try {
            val policy = directory.resolve(".html4ignore")
            val replacement = directory.resolve("replacement.ignore")
            Files.write(policy, "secret.txt\n".toByteArray(Charsets.UTF_8))
            Files.write(replacement, "public.txt\n".toByteArray(Charsets.UTF_8))

            val excluded = process_ignore_file_with_validation_hook(
                directory.toFile(),
                arrayOf("secret.txt", "public.txt")
            ) {
                Files.move(replacement, policy, StandardCopyOption.REPLACE_EXISTING)
            }

            assertTrue("secret.txt" in excluded, "the opened policy must remain the authority")
            assertFalse("public.txt" in excluded, "a replacement policy must not become authoritative")
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
