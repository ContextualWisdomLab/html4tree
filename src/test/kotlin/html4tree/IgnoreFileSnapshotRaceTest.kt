package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IgnoreFileSnapshotRaceTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-ignore-snapshot-race-").toFile()
    }

    @After
    fun teardown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun observedPolicyDisappearanceBeforeSecureOpenFailsClosed() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("*.txt")
        File(tempDir, "secret.txt").writeText("secret")

        val directorySnapshot = arrayOf(".html4ignore", "secret.txt")
        assertTrue(ignoreFile.delete(), "test precondition: observed policy must disappear before the secure open")

        assertFailsWith<IgnoreFileReadException> {
            process_ignore_file(tempDir, directorySnapshot)
        }
    }
}
