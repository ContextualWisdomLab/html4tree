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

    /** A stream that fails after open must not leak or misclassify the target. */
    @Test
    fun boundedPrefixReadReturnsNullWhenOpenedStreamFails() {
        val directory = Files.createTempDirectory("html4tree-prefix-read-fail-").toFile()
        val indexFile = File(directory, GENERATED_INDEX_NAME)
        try {
            indexFile.writeText(ownedIndexFixture("readable"))
            assertEquals(
                null,
                read_index_prefix(indexFile.toPath()) {
                    object : java.io.InputStream() {
                        override fun read(): Int {
                            throw java.io.IOException("forced prefix read failure")
                        }
                    }
                }
            )
        } finally {
            directory.deleteRecursively()
        }
    }

    /** A vanished backup skips restore and still leaves the owned source in place. */
    @Test
    fun publicationFailureAfterLostBackupDoesNotRestoreFromMissingFile() {
        val directory = Files.createTempDirectory("html4tree-lost-backup-").toFile()
        val indexFile = File(directory, GENERATED_INDEX_NAME)
        val original = ownedIndexFixture("original")
        try {
            indexFile.writeText(original)
            assertFailsWith<java.io.IOException> {
                write_index_file(directory, ownedIndexFixture("replacement")) { _, _, _ ->
                    directory.listFiles().orEmpty()
                        .filter { it.name.startsWith(".index-owned-backup-") }
                        .forEach { it.delete() }
                    throw java.io.IOException("publication failed after backup vanished")
                }
            }
            assertEquals(original, indexFile.readText())
            assertEquals(
                0,
                directory.listFiles().orEmpty().count { it.name.startsWith(".index-owned-backup-") }
            )
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

    /** A first-time create must not use replace-capable move options. */
    @Test
    fun createRefusesAtomicOrReplacingMoveWhenUserPageAppears() {
        val directory = Files.createTempDirectory("html4tree-create-race-").toFile()
        val indexFile = File(directory, GENERATED_INDEX_NAME)
        val reports = mutableListOf<String>()
        try {
            val result = write_index_file(
                directory,
                ownedIndexFixture("generated"),
                reporter = { reports.add(it) }
            ) { _, target, options ->
                Files.write(target, "<html>customer home</html>".toByteArray())
                if (options.isEmpty()) {
                    throw java.nio.file.FileAlreadyExistsException(target.toString())
                }
                throw AssertionError("create must not use ATOMIC_MOVE or REPLACE_EXISTING: ${options.toList()}")
            }
            assertEquals(IndexWriteResult.PRESERVED, result)
            assertEquals("<html>customer home</html>", indexFile.readText())
            assertTrue(reports.any { it.startsWith("preserved:") && it.contains("unowned") })
        } finally {
            directory.deleteRecursively()
        }
    }

    /** Reclassification after the absent check still preserves a late occupant. */
    @Test
    fun createReclassificationPreservesLateUnownedOccupant() {
        val directory = Files.createTempDirectory("html4tree-create-reclassify-").toFile()
        val indexFile = File(directory, GENERATED_INDEX_NAME)
        val reports = mutableListOf<String>()
        var classifyCalls = 0
        try {
            val result = write_index_file(
                directory,
                ownedIndexFixture("generated"),
                reporter = { reports.add(it) },
                classifyTarget = {
                    classifyCalls += 1
                    if (classifyCalls == 1) {
                        IndexTargetClassification(IndexTargetKind.ABSENT, "absent")
                    } else {
                        IndexTargetClassification(IndexTargetKind.UNOWNED, "unowned")
                    }
                }
            )
            assertEquals(IndexWriteResult.PRESERVED, result)
            assertTrue(!indexFile.exists())
            assertTrue(reports.any { it.startsWith("preserved:") && it.contains("unowned") })
        } finally {
            directory.deleteRecursively()
        }
    }

    /** Cleanup must re-read ownership immediately before deleting. */
    @Test
    fun cleanupReclassificationPreservesTargetThatStoppedBeingOwned() {
        val directory = Files.createTempDirectory("html4tree-cleanup-reclassify-").toFile()
        val indexFile = File(directory, GENERATED_INDEX_NAME)
        val reports = mutableListOf<String>()
        var classifyCalls = 0
        try {
            indexFile.writeText("<html>customer home</html>")
            val deleted = cleanup_owned_index(
                directory,
                dryRun = false,
                reporter = { reports.add(it) },
                classifyTarget = {
                    classifyCalls += 1
                    if (classifyCalls == 1) {
                        IndexTargetClassification(IndexTargetKind.OWNED, "owned")
                    } else {
                        IndexTargetClassification(IndexTargetKind.UNOWNED, "unowned")
                    }
                }
            )
            assertTrue(!deleted)
            assertEquals("<html>customer home</html>", indexFile.readText())
            assertTrue(reports.any { it.startsWith("preserved:") && it.contains("unowned") })
        } finally {
            directory.deleteRecursively()
        }
    }
}
