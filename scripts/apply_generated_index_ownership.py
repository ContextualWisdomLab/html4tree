from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"expected exactly one {label} match, found {count}")
    return text.replace(old, new, 1)


main_path = Path("src/main/kotlin/html4tree/main.kt")
main = main_path.read_text(encoding="utf-8")
main = replace_once(
    main,
    "import java.nio.file.LinkOption\nimport java.nio.file.StandardCopyOption\n",
    "import java.nio.file.LinkOption\nimport java.nio.file.NoSuchFileException\nimport java.nio.file.Path\nimport java.nio.file.StandardCopyOption\n",
    "NIO imports",
)
main = replace_once(
    main,
    "private val FILE_NAME_COMPARATOR = compareBy<File> { it.name }\n",
    '''private val FILE_NAME_COMPARATOR = compareBy<File> { it.name }
internal const val HTML4TREE_GENERATOR_MARKER = "<meta name=\\"generator\\" content=\\"html4tree/1\\">"
private const val GENERATOR_MARKER_SCAN_LIMIT = 8192

internal enum class GeneratedIndexOwnership {
    ABSENT,
    OWNED,
    UNOWNED
}

internal fun inspect_generated_index_ownership(indexPath: Path): GeneratedIndexOwnership {
    val attributes = try {
        Files.readAttributes(indexPath, BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
    } catch (_: NoSuchFileException) {
        return GeneratedIndexOwnership.ABSENT
    } catch (_: Exception) {
        return GeneratedIndexOwnership.UNOWNED
    }

    if (!attributes.isRegularFile || attributes.isSymbolicLink) {
        return GeneratedIndexOwnership.UNOWNED
    }

    return try {
        val prefixBytes = ByteArray(GENERATOR_MARKER_SCAN_LIMIT)
        val byteCount = Files.newInputStream(indexPath).use { input ->
            var total = 0
            while (total < prefixBytes.size) {
                val read = input.read(prefixBytes, total, prefixBytes.size - total)
                if (read == -1) break
                total += read
            }
            total
        }
        val prefix = String(prefixBytes, 0, byteCount, Charsets.UTF_8)
        if (prefix.contains(HTML4TREE_GENERATOR_MARKER)) {
            GeneratedIndexOwnership.OWNED
        } else {
            GeneratedIndexOwnership.UNOWNED
        }
    } catch (_: Exception) {
        GeneratedIndexOwnership.UNOWNED
    }
}
''',
    "ownership helpers",
)
old_writer = '''fun write_index_file(
    curr_dir: File,
    content: String,
    moveFile: (
        java.nio.file.Path,
        java.nio.file.Path,
        Array<out java.nio.file.CopyOption>
    ) -> Unit = { source, target, options ->
        Files.move(source, target, *options)
        Unit
    }
) {
    val indexPath = curr_dir.toPath().resolve("index.html")
    val tempPath = Files.createTempFile(curr_dir.toPath(), ".index-", ".html")
    try {
        Files.write(tempPath, content.toByteArray(Charsets.UTF_8))
        try {
            // With ATOMIC_MOVE, Java ignores every other copy option and the
            // existing-target policy is provider-specific.
            moveFile(tempPath, indexPath, arrayOf(StandardCopyOption.ATOMIC_MOVE))
        } catch (error: java.io.IOException) {
            if (
                error !is java.nio.file.AtomicMoveNotSupportedException &&
                error !is java.nio.file.FileAlreadyExistsException
            ) {
                throw error
            }
            // This compatibility fallback preserves replacement semantics but
            // is explicitly non-atomic.
            moveFile(tempPath, indexPath, arrayOf(StandardCopyOption.REPLACE_EXISTING))
        }
    } finally {
        Files.deleteIfExists(tempPath)
    }
}
'''
new_writer = '''private fun move_without_replacement(
    source: Path,
    target: Path,
    moveFile: (Path, Path, Array<out java.nio.file.CopyOption>) -> Unit
) {
    try {
        moveFile(source, target, arrayOf(StandardCopyOption.ATOMIC_MOVE))
    } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
        moveFile(source, target, emptyArray<java.nio.file.CopyOption>())
    }
}

fun write_index_file(
    curr_dir: File,
    content: String,
    moveFile: (
        Path,
        Path,
        Array<out java.nio.file.CopyOption>
    ) -> Unit = { source, target, options ->
        Files.move(source, target, *options)
        Unit
    }
) {
    val indexPath = curr_dir.toPath().resolve("index.html")
    val ownership = inspect_generated_index_ownership(indexPath)
    if (ownership == GeneratedIndexOwnership.UNOWNED) {
        throw java.nio.file.FileAlreadyExistsException(
            indexPath.toString(),
            null,
            "Refusing to replace an index.html that is not owned by html4tree"
        )
    }

    val tempPath = Files.createTempFile(curr_dir.toPath(), ".index-", ".html")
    var backupPath: Path? = null
    var published = false
    try {
        Files.write(tempPath, content.toByteArray(Charsets.UTF_8))

        if (ownership == GeneratedIndexOwnership.OWNED) {
            backupPath = Files.createTempFile(curr_dir.toPath(), ".index-backup-", ".html")
            Files.deleteIfExists(backupPath)
            move_without_replacement(indexPath, backupPath, moveFile)

            // Re-check after the move so a concurrently replaced, unowned file
            // is preserved rather than silently consumed as an html4tree artifact.
            if (inspect_generated_index_ownership(backupPath) != GeneratedIndexOwnership.OWNED) {
                move_without_replacement(backupPath, indexPath, moveFile)
                backupPath = null
                throw java.nio.file.FileAlreadyExistsException(
                    indexPath.toString(),
                    null,
                    "The existing index.html changed during ownership verification"
                )
            }
        }

        move_without_replacement(tempPath, indexPath, moveFile)
        published = true
        backupPath?.let { Files.deleteIfExists(it) }
        backupPath = null
    } finally {
        Files.deleteIfExists(tempPath)
        val recoverableBackup = backupPath
        if (!published && recoverableBackup != null &&
            inspect_generated_index_ownership(indexPath) == GeneratedIndexOwnership.ABSENT
        ) {
            try {
                move_without_replacement(recoverableBackup, indexPath, moveFile)
                backupPath = null
            } catch (_: Exception) {
                // Keep the backup in place. Deleting it would turn a publication
                // failure into data loss; operators can recover it explicitly.
            }
        }
    }
}
'''
main = replace_once(main, old_writer, new_writer, "index writer")
main = replace_once(
    main,
    '        <meta charset="UTF-8">\n',
    '        <meta charset="UTF-8">\n        ${HTML4TREE_GENERATOR_MARKER}\n',
    "generator marker",
)
main_path.write_text(main, encoding="utf-8")

main_test_path = Path("src/test/kotlin/html4tree/MainTest.kt")
main_test = main_test_path.read_text(encoding="utf-8")
main_test = replace_once(
    main_test,
    '        indexFile.writeText("old content")\n',
    '        indexFile.writeText("${HTML4TREE_GENERATOR_MARKER}\\nold content")\n',
    "atomic fallback owned fixture",
)
main_test = replace_once(
    main_test,
    '        write_index_file(tempDir, "atomic fallback content") { source, target, options ->\n',
    '        write_index_file(tempDir, "${HTML4TREE_GENERATOR_MARKER}\\natomic fallback content") { source, target, options ->\n',
    "atomic fallback output marker",
)
main_test = replace_once(
    main_test,
    '        assertEquals("atomic fallback content", indexFile.readText())\n',
    '        assertEquals("${HTML4TREE_GENERATOR_MARKER}\\natomic fallback content", indexFile.readText())\n',
    "atomic fallback assertion",
)
old_conflict_test = '''    @Test
    fun testWriteIndexFileFallsBackWhenAtomicReplacementRejectsExistingTarget() {
        val indexFile = File(tempDir, "index.html")
        indexFile.writeText("old content")
        var attemptedFallback = false

        write_index_file(tempDir, "replacement content") { source, target, options ->
            if (options.contains(java.nio.file.StandardCopyOption.ATOMIC_MOVE)) {
                throw java.nio.file.FileAlreadyExistsException(target.toString())
            }
            attemptedFallback = true
            Files.move(source, target, *options)
            Unit
        }

        assertTrue(attemptedFallback, "existing-target atomic conflicts must use the replacement fallback")
        assertEquals("replacement content", indexFile.readText())
    }
'''
new_conflict_test = '''    @Test
    fun testWriteIndexFileDoesNotDowngradeAtomicConflictsToReplacement() {
        val indexFile = File(tempDir, "index.html")
        indexFile.writeText("${HTML4TREE_GENERATOR_MARKER}\\nold content")
        var attemptedFallback = false

        assertFailsWith<java.nio.file.FileAlreadyExistsException> {
            write_index_file(tempDir, "${HTML4TREE_GENERATOR_MARKER}\\nreplacement content") { source, target, options ->
                if (options.contains(java.nio.file.StandardCopyOption.ATOMIC_MOVE)) {
                    throw java.nio.file.FileAlreadyExistsException(target.toString())
                }
                attemptedFallback = true
                Files.move(source, target, *options)
                Unit
            }
        }

        assertFalse(attemptedFallback, "an atomic destination conflict must not become destructive replacement")
        assertEquals("${HTML4TREE_GENERATOR_MARKER}\\nold content", indexFile.readText())
    }
'''
main_test = replace_once(main_test, old_conflict_test, new_conflict_test, "atomic conflict test")
old_symlink_assertions = '''        assertEquals("original content", targetFile.readText())
        assertTrue(indexFile.exists())
        assertFalse(Files.isSymbolicLink(indexFile.toPath()))
        assertTrue(indexFile.readText().contains("<html lang=\\"ko\\">"))
'''
new_symlink_assertions = '''        assertEquals("original content", targetFile.readText())
        assertTrue(indexFile.exists())
        assertTrue(Files.isSymbolicLink(indexFile.toPath()))
'''
main_test = replace_once(main_test, old_symlink_assertions, new_symlink_assertions, "symlink preservation assertions")
main_test_path.write_text(main_test, encoding="utf-8")

Path("src/test/kotlin/html4tree/GeneratedIndexOwnershipTest.kt").write_text('''package html4tree

import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Assume
import org.junit.Test

class GeneratedIndexOwnershipTest {
    @Test
    fun generatedIndexCarriesTheVersionedOwnershipMarker() {
        val directory = Files.createTempDirectory("owned_index_marker").toFile()
        try {
            process_dir(directory)

            val indexFile = File(directory, "index.html")
            assertTrue(indexFile.readText().contains(HTML4TREE_GENERATOR_MARKER))
            assertEquals(
                GeneratedIndexOwnership.OWNED,
                inspect_generated_index_ownership(indexFile.toPath())
            )
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun userAuthoredIndexSurvivesNormalGeneration() {
        val directory = Files.createTempDirectory("unowned_index").toFile()
        val indexFile = File(directory, "index.html").apply {
            writeText("<html><body>customer home page</body></html>")
        }
        try {
            process_dir(directory)

            assertEquals("<html><body>customer home page</body></html>", indexFile.readText())
            assertEquals(
                GeneratedIndexOwnership.UNOWNED,
                inspect_generated_index_ownership(indexFile.toPath())
            )
            assertFalse(directory.listFiles().orEmpty().any { it.name.startsWith(".index-") })
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun markerOutsideTheBoundedPrefixDoesNotClaimOwnership() {
        val directory = Files.createTempDirectory("late_marker").toFile()
        val indexFile = File(directory, "index.html").apply {
            writeText("x".repeat(8192) + HTML4TREE_GENERATOR_MARKER)
        }
        try {
            assertEquals(
                GeneratedIndexOwnership.UNOWNED,
                inspect_generated_index_ownership(indexFile.toPath())
            )
            assertFailsWith<java.nio.file.FileAlreadyExistsException> {
                write_index_file(directory, "${HTML4TREE_GENERATOR_MARKER}\\nreplacement")
            }
            assertTrue(indexFile.readText().startsWith("x".repeat(8192)))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun symbolicLinkIndexIsUnownedAndPreserved() {
        val directory = Files.createTempDirectory("symlink_index_ownership").toFile()
        val target = File(directory, "customer.html").apply { writeText("customer") }
        val indexPath = File(directory, "index.html").toPath()
        try {
            try {
                Files.createSymbolicLink(indexPath, target.toPath())
            } catch (_: Exception) {
                Assume.assumeTrue("Symlink creation not supported in this environment", false)
            }

            assertEquals(GeneratedIndexOwnership.UNOWNED, inspect_generated_index_ownership(indexPath))
            process_dir(directory)
            assertTrue(Files.isSymbolicLink(indexPath))
            assertEquals("customer", target.readText())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun missingIndexIsReportedAsAbsent() {
        val directory = Files.createTempDirectory("absent_index").toFile()
        try {
            assertEquals(
                GeneratedIndexOwnership.ABSENT,
                inspect_generated_index_ownership(File(directory, "index.html").toPath())
            )
        } finally {
            directory.deleteRecursively()
        }
    }
}
''', encoding="utf-8")

readme_path = Path("README.md")
readme = readme_path.read_text(encoding="utf-8")
old_cleanup = '''## Other

To delete all the index.html files generated with one command, do:

`$ find <top directory to crawl> -name index.html -delete`
'''
new_cleanup = '''## Generated-file ownership and cleanup

Every new html4tree page contains the versioned marker
`<meta name="generator" content="html4tree/1">`. Normal generation replaces an
existing `index.html` only when that marker is present near the beginning of a
regular, non-symbolic-link file. An unmarked file, malformed file, directory, or
symbolic link is preserved.

Do not use a blanket command such as `find ... -name index.html -delete`: it can
delete customer-authored home pages. A marker-aware dry-run and cleanup command
is tracked as the next compatibility slice. Until it is released, review and
remove generated files individually after verifying the marker.
'''
readme_path.write_text(replace_once(readme, old_cleanup, new_cleanup, "README cleanup guidance"), encoding="utf-8")

changelog_path = Path("CHANGELOG.md")
changelog = changelog_path.read_text(encoding="utf-8")
changelog = replace_once(
    changelog,
    "### Added\n\n",
    "### Added\n\n- Add a versioned ownership marker to newly generated directory indexes and refuse to replace unowned, malformed, directory, or symbolic-link `index.html` paths.\n\n",
    "changelog added section",
)
changelog = replace_once(
    changelog,
    "### Tests\n\n",
    "### Tests\n\n- Add realistic ownership regressions for customer-authored pages, generated pages, bounded marker scanning, absent targets, and symbolic links.\n\n",
    "changelog tests section",
)
changelog = replace_once(
    changelog,
    "### Documentation\n\n",
    "### Documentation\n\n- Replace the blanket `find ... -name index.html -delete` recommendation with marker-aware preservation guidance and record the artifact-ownership ADR.\n\n",
    "changelog documentation section",
)
changelog_path.write_text(changelog, encoding="utf-8")

Path("docs/adr/0001-generated-index-ownership.md").parent.mkdir(parents=True, exist_ok=True)
Path("docs/adr/0001-generated-index-ownership.md").write_text('''# ADR 0001: Version and verify generated index ownership before replacement

- Status: Accepted
- Date: 2026-08-14

## Context

The filename `index.html` does not prove that html4tree created a file. Replacing
or deleting every file with that name can destroy customer-authored home pages.
Atomic filesystem replacement prevents torn output, but it does not establish
semantic ownership of the destination.

## Decision

New generated pages carry the exact marker
`<meta name="generator" content="html4tree/1">` near the beginning of the HTML
head. Before replacement, html4tree reads a bounded prefix without following
symbolic links and classifies the target as absent, owned, or unowned.

- Absent targets may be created.
- Owned regular files may be replaced through a no-replacement move sequence.
- Unowned, malformed, unreadable, directory, and symbolic-link targets are
  preserved and cause publication for that directory to fail closed.
- Atomic-move unavailability may fall back to a no-replacement move; an
  existing-target conflict never falls back to destructive replacement.
- A marker beyond the bounded prefix does not claim ownership.

The writer temporarily moves an owned artifact to a same-directory backup,
revalidates ownership after the move, publishes the new complete file, and
removes the backup only after success. If publication fails and the destination
is absent, it attempts to restore the backup; a failed restoration keeps the
backup rather than deleting recoverable data.

## Consequences

Pre-marker html4tree pages are treated as unowned. This is intentionally
conservative and requires an explicit future adoption or cleanup workflow rather
than heuristic replacement. A marker-aware dry-run and cleanup command remains
a separate compatibility slice.

## Verification

Tests cover absent, owned, unowned, late-marker, symbolic-link, atomic fallback,
and atomic-conflict paths. Linux, macOS, and Windows CI remain required before a
release can claim cross-platform preservation.

## References

MITRE. (2025). *CWE-73: External control of file name or path*. https://cwe.mitre.org/data/definitions/73.html

Oracle. (2026). *StandardCopyOption*. Java Platform, Standard Edition 21 API Specification. https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/StandardCopyOption.html
''', encoding="utf-8")
