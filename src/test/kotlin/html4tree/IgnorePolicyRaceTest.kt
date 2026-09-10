package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFailsWith

class IgnorePolicyRaceTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-ignore-race-").toFile()
    }

    @After
    fun teardown() {
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun failClosedWhenIgnoreFileIsReplacedBySymlinkAfterValidation() {
        val ignoreFile = File(tempDir, ".html4ignore").apply {
            writeText("private.txt\n")
        }
        val replacementPolicy = File(tempDir, "replacement.ignore").apply {
            writeText("public.txt\n")
        }
        val directorySnapshot = arrayOf(
            ".html4ignore",
            "private.txt",
            "public.txt"
        )

        assertFailsWith<IgnoreFileReadException> {
            process_ignore_file(tempDir, directorySnapshot) { file, block ->
                Files.delete(file.toPath())
                Files.createSymbolicLink(file.toPath(), replacementPolicy.toPath())
                file.useLines { lines -> block(lines) }
            }
        }
    }
}
