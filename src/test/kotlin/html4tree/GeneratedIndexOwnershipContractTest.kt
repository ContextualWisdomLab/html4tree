package html4tree

import com.github.ajalt.clikt.core.UsageError
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.Test

/** Regression tests for the public generated-index ownership safety contract. */
class GeneratedIndexOwnershipContractTest {
    /** `--dry-run` is meaningful only for cleanup and must fail before generation starts. */
    @Test
    fun dryRunRequiresCleanup() {
        val directory = Files.createTempDirectory("html4tree-dry-run-").toFile()
        try {
            assertFailsWith<UsageError> {
                Html4tree().parse(arrayOf("--dry-run", directory.absolutePath))
            }
            assertTrue(!File(directory, GENERATED_INDEX_NAME).exists())
        } finally {
            directory.deleteRecursively()
        }
    }

    /** Prefix inspection stops cleanly at EOF instead of requiring a full-size buffer. */
    @Test
    fun boundedPrefixReadReturnsShortRegularFileAtEof() {
        val directory = Files.createTempDirectory("html4tree-short-prefix-").toFile()
        val indexFile = File(directory, "short.html")
        try {
            indexFile.writeBytes(byteArrayOf(1, 2, 3))
            assertEquals(listOf<Byte>(1, 2, 3), read_index_prefix(indexFile.toPath(), 32)!!.toList())
        } finally {
            directory.deleteRecursively()
        }
    }

    /** A failed restore retains a recoverable backup and reports its exact path. */
    @Test
    fun failedRestoreRetainsAndReportsOwnedBackup() {
        val directory = Files.createTempDirectory("html4tree-retained-backup-").toFile()
        val indexFile = File(directory, GENERATED_INDEX_NAME)
        val reports = mutableListOf<String>()
        try {
            val original = ownedIndexFixture("original")
            indexFile.writeText(original)

            assertFailsWith<java.io.IOException> {
                write_index_file(
                    directory,
                    ownedIndexFixture("replacement"),
                    reporter = { reports.add(it) }
                ) { _, _, _ ->
                    throw java.io.IOException("publication and restore unavailable")
                }
            }

            assertEquals(original, indexFile.readText())
            val backups = directory.listFiles().orEmpty().filter {
                it.name.startsWith(".index-owned-backup-")
            }
            assertEquals(1, backups.size)
            assertEquals(original, backups.single().readText())
            assertTrue(reports.contains("backup-retained: ${backups.single().toPath().toAbsolutePath()}"))
        } finally {
            directory.deleteRecursively()
        }
    }
}
