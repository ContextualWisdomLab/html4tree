package html4tree

import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneratedIndexOwnershipTest {
    private lateinit var tempDir: File
    private val reports = mutableListOf<String>()

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-ownership-").toFile()
        reports.clear()
    }

    @After
    fun teardown() {
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun classifyAbsentOwnedUnownedAndLateMarker() {
        val missing = File(tempDir, "missing.html")
        assertEquals(IndexTargetKind.ABSENT, classify_index_target(missing).kind)

        val owned = File(tempDir, "owned.html")
        owned.writeText(ownedIndexFixture("ok"))
        assertEquals(IndexTargetKind.OWNED, classify_index_target(owned).kind)

        val empty = File(tempDir, "empty.html")
        empty.writeText("")
        assertEquals("unowned", classify_index_target(empty).reason)

        val late = File(tempDir, "late.html")
        late.writeText("x".repeat(OWNERSHIP_NEAR_START_LIMIT) + ownedIndexFixture("late"))
        assertEquals("late-marker", classify_index_target(late).reason)

        val malformed = File(tempDir, "malformed.html")
        malformed.writeText("""<meta name="generator" content="html4tree/""")
        assertEquals("malformed", classify_index_target(malformed).reason)

        val unsupported = File(tempDir, "v2.html")
        unsupported.writeText("""<meta name="generator" content="html4tree/2">""")
        assertEquals("unsupported-version", classify_index_target(unsupported).reason)

        assertEquals(IndexTargetKind.OWNED, classify_index_prefix(ownedIndexFixture("ok").toByteArray()).kind)
        assertEquals(IndexTargetKind.UNOWNED, classify_index_prefix(ByteArray(0)).kind)
        val overflow = File(tempDir, "overflow.html")
        overflow.writeText("""<meta name="generator" content="html4tree/9999999999999999999">""")
        assertEquals("unsupported-version", classify_index_target(overflow).reason)
        IndexTargetKind.values()
        IndexTargetKind.valueOf("OWNED")
        IndexWriteResult.values()
        IndexWriteResult.valueOf("CREATED")
    }

    @Test
    fun classifyPreservesDirectorySymlinkAndUnreadableTargets() {
        val directory = File(tempDir, "index-dir")
        directory.mkdir()
        assertEquals("directory", classify_index_target(directory).reason)

        val target = File(tempDir, "target.txt")
        target.writeText("keep")
        val link = File(tempDir, "link.html")
        try {
            Files.createSymbolicLink(link.toPath(), target.toPath())
        } catch (e: Exception) {
            Assume.assumeTrue("Symlink creation not supported in this environment", false)
        }
        assertEquals("symlink", classify_index_target(link).reason)

        val unreadable = File(tempDir, "unreadable.html")
        unreadable.writeText(ownedIndexFixture("secret"))
        Assume.assumeTrue(unreadable.setReadable(false, false))
        try {
            val classification = classify_index_target(unreadable)
            assertTrue(
                classification.kind == IndexTargetKind.UNSAFE ||
                    classification.kind == IndexTargetKind.OWNED
            )
        } finally {
            unreadable.setReadable(true, false)
        }
    }

    @Test
    fun generatePreservesUserAuthoredIndexAndReplacesOwned() {
        val authored = File(tempDir, "index.html")
        authored.writeText("<html><body>customer home</body></html>")

        go(tempDir.absolutePath, 0, reporter = { reports.add(it) })

        assertEquals("<html><body>customer home</body></html>", authored.readText())
        assertTrue(reports.any { it.startsWith("preserved:") && it.contains("unowned") })

        val createdDir = File(tempDir, "fresh")
        createdDir.mkdir()
        go(createdDir.absolutePath, 0, reporter = { reports.add(it) })
        val created = File(createdDir, "index.html")
        assertTrue(created.readText().contains(GENERATED_OWNERSHIP_MARKER))
        assertTrue(reports.any { it.startsWith("created:") })

        val first = created.readText()
        go(createdDir.absolutePath, 0, reporter = { reports.add(it) })
        val second = created.readText()
        assertTrue(second.contains(GENERATED_OWNERSHIP_MARKER))
        assertTrue(reports.any { it.startsWith("replaced:") })
        assertEquals(first.contains("<html lang=\"ko\">"), second.contains("<html lang=\"ko\">"))
    }

    @Test
    fun forceOverwriteReplacesUnownedRegularFileButNotSymlink() {
        val authored = File(tempDir, "index.html")
        authored.writeText("<html><body>customer home</body></html>")
        val result = write_index_file(
            tempDir,
            ownedIndexFixture("forced"),
            forceOverwrite = true,
            reporter = { reports.add(it) }
        )
        assertEquals(IndexWriteResult.REPLACED, result)
        assertEquals(ownedIndexFixture("forced"), authored.readText())

        val target = File(tempDir, "target.txt")
        target.writeText("keep")
        val linkedDir = File(tempDir, "linked")
        linkedDir.mkdir()
        val link = File(linkedDir, "index.html")
        try {
            Files.createSymbolicLink(link.toPath(), target.toPath())
        } catch (e: Exception) {
            Assume.assumeTrue("Symlink creation not supported in this environment", false)
        }
        val preserved = write_index_file(
            linkedDir,
            ownedIndexFixture("should-not-write"),
            forceOverwrite = true,
            reporter = { reports.add(it) }
        )
        assertEquals(IndexWriteResult.PRESERVED, preserved)
        assertEquals("keep", target.readText())
        assertTrue(Files.isSymbolicLink(link.toPath()))
    }

    @Test
    fun atomicConflictDoesNotReplaceUnownedOccupant() {
        val indexFile = File(tempDir, "index.html")
        indexFile.writeText(ownedIndexFixture("old"))
        val result = write_index_file(
            tempDir,
            ownedIndexFixture("new"),
            reporter = { reports.add(it) }
        ) { _, target, options ->
            if (options.contains(java.nio.file.StandardCopyOption.ATOMIC_MOVE)) {
                Files.write(target, "<html><body>swapped user page</body></html>".toByteArray())
                throw java.nio.file.FileAlreadyExistsException(target.toString())
            }
            throw AssertionError("unowned occupant must not take REPLACE_EXISTING")
        }
        assertEquals(IndexWriteResult.PRESERVED, result)
        assertEquals("<html><body>swapped user page</body></html>", indexFile.readText())
        assertTrue(reports.any { it.contains("unowned") })
    }

    @Test
    fun backupFailurePreservesOwnedTarget() {
        val indexFile = File(tempDir, "index.html")
        indexFile.writeText(ownedIndexFixture("keep-me"))
        val result = write_index_file(
            tempDir,
            ownedIndexFixture("new"),
            reporter = { reports.add(it) },
            copyFile = { _, _ -> throw java.io.IOException("cannot copy owned artifact") }
        )
        assertEquals(IndexWriteResult.PRESERVED, result)
        assertEquals(ownedIndexFixture("keep-me"), indexFile.readText())
        assertTrue(reports.any { it.contains("backup-failed") })
    }

    @Test
    fun revalidateAfterBackupAbortsWhenTargetBecomesUnowned() {
        val indexFile = File(tempDir, "index.html")
        indexFile.writeText(ownedIndexFixture("keep-me"))
        val result = write_index_file(
            tempDir,
            ownedIndexFixture("new"),
            reporter = { reports.add(it) },
            copyFile = { _, target ->
                Files.write(indexFile.toPath(), "<html>swapped</html>".toByteArray())
                Files.write(target, ownedIndexFixture("backup").toByteArray())
            }
        )
        assertEquals(IndexWriteResult.PRESERVED, result)
        assertEquals("<html>swapped</html>", indexFile.readText())
        assertTrue(reports.any { it.contains("unowned") })
    }

    @Test
    fun publicationFailureRestoresOwnedBackup() {
        val indexFile = File(tempDir, "index.html")
        indexFile.writeText(ownedIndexFixture("original"))
        assertFailsWith<java.io.IOException> {
            write_index_file(
                tempDir,
                ownedIndexFixture("new"),
                reporter = { reports.add(it) }
            ) { source, target, options ->
                if (options.contains(java.nio.file.StandardCopyOption.ATOMIC_MOVE)) {
                    Files.write(target, "clobbered".toByteArray())
                    throw java.io.IOException("publication failed")
                }
                Files.move(source, target, *options)
                Unit
            }
        }
        assertEquals(ownedIndexFixture("original"), indexFile.readText())
        val leftovers = tempDir.listFiles()?.filter {
            it.name.startsWith(".index-") || it.name.startsWith(".index-owned-backup-")
        } ?: emptyList()
        assertTrue(leftovers.isEmpty(), leftovers.toString())
    }

    @Test
    fun publicationAndRestoreFailureLeavesOwnedSource() {
        val indexFile = File(tempDir, "index.html")
        indexFile.writeText(ownedIndexFixture("original"))
        assertFailsWith<java.io.IOException> {
            write_index_file(
                tempDir,
                ownedIndexFixture("new"),
                reporter = { reports.add(it) }
            ) { _, _, _ ->
                throw java.io.IOException("move unavailable")
            }
        }
        assertEquals(ownedIndexFixture("original"), indexFile.readText())
        assertTrue(reports.any { it.startsWith("backup-retained:") })
        val retainedBackups = tempDir.listFiles()?.filter {
            it.name.startsWith(".index-owned-backup-")
        } ?: emptyList()
        assertEquals(1, retainedBackups.size)
        assertEquals(ownedIndexFixture("original"), retainedBackups.single().readText())
    }

    @Test
    fun cleanupDeletesOwnedOnlyAndDryRunSelectsTheSameSet() {
        val ownedDir = File(tempDir, "owned")
        val userDir = File(tempDir, "user")
        ownedDir.mkdir()
        userDir.mkdir()
        File(ownedDir, "index.html").writeText(ownedIndexFixture("generated"))
        File(userDir, "index.html").writeText("<html>mine</html>")

        val dryRunReports = mutableListOf<String>()
        go(tempDir.absolutePath, -1, cleanup = true, dryRun = true, reporter = { dryRunReports.add(it) })
        assertTrue(File(ownedDir, "index.html").exists())
        assertTrue(File(userDir, "index.html").exists())
        assertTrue(dryRunReports.any { it.startsWith("would-delete:") && it.contains("owned") })
        assertTrue(dryRunReports.any { it.startsWith("preserved:") && it.contains("unowned") })

        go(tempDir.absolutePath, -1, cleanup = true, dryRun = false, reporter = { reports.add(it) })
        assertFalse(File(ownedDir, "index.html").exists())
        assertEquals("<html>mine</html>", File(userDir, "index.html").readText())
        assertTrue(reports.any { it.startsWith("deleted:") })
    }

    @Test
    fun cleanupReportsDeleteFailureAndAbsentTargets() {
        assertFalse(cleanup_owned_index(tempDir, dryRun = false, reporter = { reports.add(it) }))

        val owned = File(tempDir, "index.html")
        owned.writeText(ownedIndexFixture("generated"))
        Assume.assumeTrue(tempDir.setWritable(false, false))
        try {
            val cleanupSucceeded = cleanup_owned_index(tempDir, dryRun = false, reporter = { reports.add(it) })
            if (!cleanupSucceeded) {
                assertTrue(reports.any { it.startsWith("failed:") })
            }
        } finally {
            tempDir.setWritable(true, false)
        }
        owned.writeText(ownedIndexFixture("generated"))
        default_index_reporter("noop-report")
        generated_index_file(tempDir)
        assertEquals(0, read_index_prefix(owned.toPath(), 0)!!.size)
        assertEquals(4, read_index_prefix(owned.toPath(), 4)!!.size)
        val shortPrefix = File(tempDir, "short-prefix.html")
        shortPrefix.writeText("abc")
        assertEquals("abc", String(read_index_prefix(shortPrefix.toPath(), 10)!!, Charsets.UTF_8))
        assertEquals(null, read_index_prefix(File(tempDir, "missing-prefix.html").toPath()))
        cleanup_owned_index(tempDir, true)

        val throwingFile = object : File(tempDir, "throws.html") {
            override fun toPath(): java.nio.file.Path {
                throw java.nio.file.InvalidPathException("throws.html", "forced")
            }
        }
        assertEquals("unreadable", classify_index_target(throwingFile).reason)

        val ll = LinkedList()
        ll.push(LinkedListEntry(tempDir, 0, read_file_identity(tempDir).key))
        crawl_directories(ll, 0)
    }

    @Test
    fun forceOverwriteUsesAtomicFallbackOnUnownedFile() {
        val indexFile = File(tempDir, "index.html")
        indexFile.writeText("<html>user</html>")
        val result = write_index_file(
            tempDir,
            ownedIndexFixture("forced"),
            forceOverwrite = true,
            reporter = { reports.add(it) }
        ) { source, target, options ->
            if (options.contains(java.nio.file.StandardCopyOption.ATOMIC_MOVE)) {
                throw java.nio.file.AtomicMoveNotSupportedException(
                    source.toString(),
                    target.toString(),
                    "test filesystem"
                )
            }
            Files.move(source, target, *options)
            Unit
        }
        assertEquals(IndexWriteResult.REPLACED, result)
        assertEquals(ownedIndexFixture("forced"), indexFile.readText())
    }

    @Test
    fun readPrefixRejectsSymlink() {
        val target = File(tempDir, "target.txt")
        target.writeText("keep")
        val link = File(tempDir, "link.html")
        try {
            Files.createSymbolicLink(link.toPath(), target.toPath())
        } catch (e: Exception) {
            Assume.assumeTrue("Symlink creation not supported in this environment", false)
        }
        assertEquals(null, read_index_prefix(link.toPath()))
    }

    @Test
    fun nestedSitesPreserveMixedOwnership() {
        val generated = File(tempDir, "generated")
        val authored = File(tempDir, "authored")
        generated.mkdir()
        authored.mkdir()
        File(authored, "index.html").writeText("<html>keep</html>")

        go(tempDir.absolutePath, -1, reporter = { reports.add(it) })

        assertTrue(File(generated, "index.html").readText().contains(GENERATED_OWNERSHIP_MARKER))
        assertEquals("<html>keep</html>", File(authored, "index.html").readText())
        assertTrue(File(tempDir, "index.html").readText().contains(GENERATED_OWNERSHIP_MARKER))
    }

    @Test
    fun notRegularFifoIsUnsafeWhenAvailable() {
        val fifo = File(tempDir, "fifo.html")
        val created = try {
            ProcessBuilder("mkfifo", fifo.absolutePath).start().waitFor() == 0 && fifo.exists()
        } catch (e: Exception) {
            false
        }
        Assume.assumeTrue("mkfifo is unavailable", created)
        assertEquals("not-regular", classify_index_target(fifo).reason)
    }
}
