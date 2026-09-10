package html4tree

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.test.assertEquals
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
        File(tempDir, ".html4ignore").writeText("private.txt\n")
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

    @Test
    fun securePolicyReaderReturnsLinesFromOpenedRegularFile() {
        val policy = File(tempDir, "policy.ignore").apply {
            writeText("private.txt\n*.key\n")
        }

        val lines = policy.useLines { it.toList() }

        assertEquals(listOf("private.txt", "*.key"), lines)
    }

    @Test
    fun securePolicyReaderClosesReaderWhenConsumerFails() {
        val policy = File(tempDir, "consumer-failure.ignore").apply {
            writeText("private.txt\n")
        }

        assertFailsWith<IllegalStateException> {
            policy.useLines {
                throw IllegalStateException("consumer failed")
            }
        }
    }

    @Test
    fun securePolicyReaderRejectsOversizedOpenedFile() {
        val policy = File(tempDir, "oversized.ignore")
        policy.writeBytes(ByteArray(1_048_577) { 'a'.toInt().toByte() })

        assertFailsWith<IOException> {
            policy.useLines { it.toList() }
        }
    }
}
