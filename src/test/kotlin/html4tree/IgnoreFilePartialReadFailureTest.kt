package html4tree

import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IgnoreFilePartialReadFailureTest {
    @Test
    fun partialPatternsAreDiscardedWhenIgnoreReadFails() {
        val tempDir = Files.createTempDirectory("html4tree-ignore-partial-read").toFile()
        try {
            File(tempDir, ".html4ignore").writeText("*.secret\n*.later\n")
            File(tempDir, "leak.secret").writeText("candidate")
            File(tempDir, "keep.txt").writeText("candidate")
            val names = arrayOf(".html4ignore", "leak.secret", "keep.txt")

            val excluded = process_ignore_file(tempDir, names) {
                object : BufferedReader(StringReader("")) {
                    private var calls = 0

                    override fun readLine(): String? {
                        calls += 1
                        return when (calls) {
                            1 -> "*.secret"
                            else -> throw IOException("simulated failure after one parsed policy line")
                        }
                    }
                }
            }

            assertFalse(
                "leak.secret" in excluded,
                "a failed read must not leave a partially parsed ignore policy active"
            )
            assertFalse("keep.txt" in excluded)
            assertTrue("index.html" in excluded)
            assertTrue(".html4ignore" in excluded)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
