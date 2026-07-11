package html4tree

import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("html4tree-test-").toFile()
    }

    @After
    fun teardown() {
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testEscapeHtml() {
        assertEquals("&amp;", "&".escapeHtml())
        assertEquals("&lt;", "<".escapeHtml())
        assertEquals("&gt;", ">".escapeHtml())
        assertEquals("&quot;", "\"".escapeHtml())
        assertEquals("&#x27;", "'".escapeHtml())
        assertEquals("&#x60;", "`".escapeHtml())
        assertEquals("&amp;&lt;&gt;&quot;&#x27;&#x60;", "&<>\"'`".escapeHtml())
        assertEquals("normal text", "normal text".escapeHtml())
        assertEquals("mix text &amp; and &lt;tag&gt;", "mix text & and <tag>".escapeHtml())
    }

    @Test
    fun testUrlEncodePath() {
        assertEquals("hello%20world", "hello world".urlEncodePath())
        assertEquals("normal_path", "normal_path".urlEncodePath())
        assertEquals("path%2Fwith%2Fslash", "path/with/slash".urlEncodePath())
    }

    @Test
    fun testHelp() {
        val outContent = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outContent))
        try {
            help()
            assertEquals("ERROR: help has not been written yet!\n", outContent.toString().replace("\r\n", "\n"))
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoInvalidDir() {
        go("non_existent_directory", -1)
    }

    @Test
    fun testGoRejectsSymlinkTopDir() {
        val targetDir = Files.createTempDirectory("html4tree-target-").toFile()
        val symlink = File(tempDir, "linked-top")
        try {
            try {
                Files.createSymbolicLink(symlink.toPath(), targetDir.absoluteFile.toPath())
            } catch (e: Exception) {
                Assume.assumeTrue("Symlink creation not supported in this environment", false)
            }

            assertFailsWith<IllegalArgumentException> {
                go(symlink.absolutePath, -1)
            }
        } finally {
            targetDir.deleteRecursively()
        }
    }

    @Test
    fun testGoEmptyDir() {
        go(tempDir.absolutePath, -1)
        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())
        val htmlContent = indexFile.readText()
        assertTrue(htmlContent.contains("<html lang=\"ko\">"))
        assertTrue(htmlContent.contains("이 디렉토리는 비어 있습니다."))
    }

    @Test
    fun testProcessIgnoreFile() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt\n.*\\.log")

        File(tempDir, "test.txt").createNewFile()
        File(tempDir, "test.log").createNewFile()
        File(tempDir, "test.md").createNewFile()

        val excluded = process_ignore_file(tempDir)

        assertTrue(excluded.contains("test.txt"))
        assertTrue(excluded.contains("test.log"))
        assertTrue(excluded.contains("index.html"))
        assertFalse(excluded.contains("test.md"))
    }

    @Test
    fun testProcessIgnoreFileNoIgnore() {
        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("index.html"))
        assertEquals(9, excluded.size) // index.html + 8 default sensitive files
    }

    @Test
    fun testProcessIgnoreFileInvalidRegex() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("[\n.*\\.log")

        File(tempDir, "test.log").createNewFile()
        File(tempDir, "test.txt").createNewFile()

        val excluded = process_ignore_file(tempDir)

        assertTrue(excluded.contains("test.log"))
        assertFalse(excluded.contains("test.txt"))
    }

    @Test
    fun testProcessDir() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        File(tempDir, "file1.txt").createNewFile()
        File(tempDir, "test.ignore").createNewFile()
        File(tempDir, ".html4ignore").writeText(".*\\.ignore")

        process_dir(tempDir)

        val indexFile = File(tempDir, "index.html")
        assertTrue(indexFile.exists())
        val htmlContent = indexFile.readText()
        assertTrue(htmlContent.contains("<html lang=\"ko\">"))
        assertTrue(htmlContent.contains("<nav aria-label=\"디렉토리 목록\">"))
        assertTrue(htmlContent.contains("<main>"))
        assertTrue(htmlContent.contains("</main>"))
        assertTrue(htmlContent.contains("aria-label=\"상위 디렉토리로 이동\""))
        assertTrue(htmlContent.contains("aria-hidden=\"true\""))
        assertTrue(htmlContent.contains("aria-label=\"file1.txt 파일\""))
        assertTrue(htmlContent.contains("aria-label=\"subdir 디렉토리\""))
        assertTrue(htmlContent.contains("file1.txt"))
        assertTrue(htmlContent.contains("subdir/"))
        assertTrue(htmlContent.contains("&#128193;"))
        assertFalse(htmlContent.contains("test.ignore"))
        assertTrue(htmlContent.contains("Content-Security-Policy"))
        assertTrue(htmlContent.contains("default-src 'none'; style-src 'unsafe-inline';"))
        assertTrue(htmlContent.contains("max-width: 800px;"))
        assertTrue(htmlContent.contains("@media (prefers-color-scheme: dark)"))
    }

    @Test
    fun testWriteIndexFileCleansUpTempFileOnFailure() {
        // Files.move cannot replace a non-empty directory, so this drives the
        // exception path through write_index_file's finally block.
        val indexDir = File(tempDir, "index.html")
        indexDir.mkdir()
        File(indexDir, "occupant.txt").writeText("keep")

        assertFailsWith<Exception> {
            write_index_file(tempDir, "content")
        }

        assertTrue(indexDir.isDirectory)
        assertEquals("keep", File(indexDir, "occupant.txt").readText())
        val leftoverTemp = tempDir.listFiles()?.filter { it.name.startsWith(".index-") } ?: emptyList()
        assertTrue(leftoverTemp.isEmpty(), "temporary index file should be cleaned up on failure")
    }

    @Test
    fun testProcessDirReplacesIndexSymlinkWithoutTouchingTarget() {
        val targetFile = File(tempDir, "target.txt")
        targetFile.writeText("original content")

        val indexFile = File(tempDir, "index.html")
        try {
            Files.createSymbolicLink(indexFile.toPath(), targetFile.toPath())
        } catch (e: Exception) {
            Assume.assumeTrue("Symlink creation not supported in this environment", false)
        }

        process_dir(tempDir)

        assertEquals("original content", targetFile.readText())
        assertTrue(indexFile.exists())
        assertFalse(Files.isSymbolicLink(indexFile.toPath()))
        assertTrue(indexFile.readText().contains("<html lang=\"ko\">"))
    }

    @Test
    fun testGoWithSymlink() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()

        val targetDir = Files.createTempDirectory("html4tree-target-").toFile()
        File(targetDir, "secret.txt").writeText("secret")

        try {
            val symlink = File(subdir, "symlink")
            try {
                Files.createSymbolicLink(symlink.toPath(), targetDir.absoluteFile.toPath())
            } catch (e: Exception) {
                Assume.assumeTrue("Symlink creation not supported in this environment", false)
            }

            go(tempDir.absolutePath, -1)

            assertTrue(File(tempDir, "index.html").exists())

            val subdirIndex = File(subdir, "index.html")
            assertTrue(subdirIndex.exists())
            assertFalse(subdirIndex.readText().contains("symlink"), "Symlinked directory should not be listed in index.html")

            val symlinkIndex = File(targetDir, "index.html")
            assertFalse(symlinkIndex.exists(), "Symlink target should not have an index.html generated")
        } finally {
            targetDir.deleteRecursively()
        }
    }

    @Test
    fun testGoWithMaxLevel() {
        val subdir = File(tempDir, "subdir")
        subdir.mkdir()
        val subsubdir = File(subdir, "subsubdir")
        subsubdir.mkdir()

        go(tempDir.absolutePath, 0)

        assertTrue(File(tempDir, "index.html").exists())
        assertFalse(File(subdir, "index.html").exists())
        assertFalse(File(subsubdir, "index.html").exists())
    }

    @Test
    fun testGoWithUnreadableDir() {
        val unreadableDir = File(tempDir, "unreadable")
        unreadableDir.mkdir()
        unreadableDir.setWritable(true)
        unreadableDir.setExecutable(true)

        try {
            Assume.assumeTrue(unreadableDir.setReadable(false, false))
            assertNull(unreadableDir.listFiles())

            go(tempDir.absolutePath, -1)

            assertTrue(File(tempDir, "index.html").exists())
            assertTrue(File(unreadableDir, "index.html").exists())
        } finally {
            unreadableDir.setReadable(true, false)
            unreadableDir.setWritable(true, false)
            unreadableDir.setExecutable(true, false)
        }
    }

    @Test
    fun testCliParsing() {
        val cli = Html4tree()
        cli.parse(arrayOf("--max-level", "2", tempDir.absolutePath))
        assertEquals(2, cli.maxLevel)
        assertEquals(tempDir.absolutePath, cli.topDir)
    }

    @Test
    fun testCliMainParsing() {
        val cli = Html4tree()
        cli.parse(arrayOf(tempDir.absolutePath))
        main(arrayOf(tempDir.absolutePath))
        assertTrue(File(tempDir, "index.html").exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoNotADir() {
        val notADir = File(tempDir, "not_a_dir.txt")
        notADir.writeText("test")
        go(notADir.absolutePath, -1)
    }

    @Test
    fun testGoRejectsRootDirectory() {
        assertFailsWith<IllegalArgumentException> {
            go(File("/").absolutePath, -1)
        }
    }

    @Test
    fun testProcessIgnoreFileWithIndexHtml() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("index\\.html")
        File(tempDir, "index.html").writeText("existing")
        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessDirItEqualsCurrDir() {
        File(tempDir, "tempDir").mkdir()
        process_dir(tempDir)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoBlankDir() {
        go("   ", -1)
    }

    @Test
    fun testUrlEncodePathUnreserved() {
        assertEquals("-._~", "-._~".urlEncodePath())
        assertEquals("A1z", "A1z".urlEncodePath())
    }

    @Test
    fun testUrlEncodePathReservedHexCoverage() {
        // Need characters that produce hex digit > 9 to hit the `else` branch of `if (hex1 < 10)` and `if (hex2 < 10)`.
        // The byte for '가' (EA B0 80) is useful here.
        // EA: E(14)>9, A(10)>9 -> both hex1 and hex2 > 9
        // B0: B(11)>9, 0<10 -> hex1 > 9, hex2 < 10
        // 80: 8<10, 0<10 -> both hex1 and hex2 < 10
        assertEquals("%EA%B0%80", "가".urlEncodePath())
        // And something with <10 for hex1 but >9 for hex2: e.g. ASCII DEL (127 -> 7F)
        assertEquals("%7F", "\u007F".urlEncodePath())
    }

    @Test
    fun testProcessIgnoreFileEmptyLine() {
        val ignoreFile = File(tempDir, ".html4ignore")
        ignoreFile.writeText("\n.*\\.txt\n\n.*\\.log\n")

        File(tempDir, "test.txt").createNewFile()

        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("test.txt"))
    }

    @Test
    fun testIgnoreFileIsDirectory() {
        val ignoreDir = File(tempDir, ".html4ignore")
        ignoreDir.mkdir()

        // This should not crash or parse the directory
        val excluded = process_ignore_file(tempDir)
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileDosProtection() {
        val ignoreFile = File(tempDir, ".html4ignore")
        val longPattern = "a".repeat(101)
        val sb = StringBuilder()

        // Add the long pattern early so it gets evaluated before hitting the 1000 limit
        sb.append(longPattern).append("\n")

        for (i in 0..1005) {
            sb.append("pattern$i\n")
        }
        ignoreFile.writeText(sb.toString())

        File(tempDir, "pattern500").createNewFile()
        File(tempDir, "pattern1005").createNewFile() // Should not be ignored as we stop at 1000
        File(tempDir, longPattern).createNewFile() // Should not be ignored as length > 100

        val excluded = process_ignore_file(tempDir)

        assertTrue(excluded.contains("pattern500"))
        assertFalse(excluded.contains("pattern1005"))
        assertFalse(excluded.contains(longPattern))
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testIgnoreFileIsSymlink() {
        val targetFile = File(tempDir, "target.ignore")
        targetFile.writeText(".*\\.txt")
        val ignoreFile = File(tempDir, ".html4ignore")
        try {
            Files.createSymbolicLink(ignoreFile.toPath(), targetFile.toPath())
        } catch (e: Exception) {
            Assume.assumeTrue("Symlink creation not supported in this environment", false)
        }

        File(tempDir, "test.txt").createNewFile()

        // Should ignore the symlink and NOT parse it
        val excluded = process_ignore_file(tempDir)
        assertFalse(excluded.contains("test.txt"))
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileLargeSize() {
        val ignoreFile = File(tempDir, ".html4ignore")
        // Write slightly more than 1MB
        val largeContent = "a".repeat(1048576 + 10)
        ignoreFile.writeText(largeContent)

        File(tempDir, "test.txt").createNewFile()

        // Should ignore the file because it's too large
        val excluded = process_ignore_file(tempDir)
        assertFalse(excluded.contains("test.txt"))
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileLongRegex() {
        val ignoreFile = File(tempDir, ".html4ignore")
        val longRegex = ".*".repeat(55) // Length 110
        ignoreFile.writeText("$longRegex\n.*\\.log")

        File(tempDir, "test.log").createNewFile()
        File(tempDir, "test.txt").createNewFile()

        val excluded = process_ignore_file(tempDir)
        // .log is excluded because it's valid
        assertTrue(excluded.contains("test.log"))
        // test.txt is not excluded because long regex was ignored
        assertFalse(excluded.contains("test.txt"))
        assertTrue(excluded.contains("index.html"))
    }

    @Test
    fun testProcessIgnoreFileMaxLines() {
        val ignoreFile = File(tempDir, ".html4ignore")
        val content = StringBuilder()
        for (i in 1..1005) {
            content.append(".*\\.txt$i\n")
        }
        ignoreFile.writeText(content.toString())

        File(tempDir, "test.txt1000").createNewFile()
        File(tempDir, "test.txt1001").createNewFile()

        val excluded = process_ignore_file(tempDir)
        // Line 1000 should be processed
        assertTrue(excluded.contains("test.txt1000"))
        // Line 1001 should be ignored due to line limit
        assertFalse(excluded.contains("test.txt1001"))
    }
}
