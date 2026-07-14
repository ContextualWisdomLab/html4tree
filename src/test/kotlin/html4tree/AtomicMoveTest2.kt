package html4tree

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class AtomicMoveTest2 {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testAtomicMoveFallback() {
        val curr_dir = tempFolder.newFolder("test_dir")
        var fallbackCalled = false
        val moveFile: (Path, Path) -> Unit = { src, dest ->
            fallbackCalled = true
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING)
        }
        write_index_file(curr_dir, "content", moveFile)
        assertTrue(File(curr_dir, "index.html").exists())
        assertEquals("content", File(curr_dir, "index.html").readText())
        assertTrue(fallbackCalled)
    }

    @Test
    fun testAtomicMoveDefaultNotSupported() {
        val curr_dir = tempFolder.newFolder("test_dir_2")
        val content = "atomic content"

        // This will call the default function
        write_index_file(curr_dir, content)

        assertTrue(File(curr_dir, "index.html").exists())
        assertEquals(content, File(curr_dir, "index.html").readText())
    }

    @Test
    fun testAtomicMoveCoverage() {
        val src = tempFolder.newFile("src.txt").toPath()
        val dest = tempFolder.newFolder("dest_dir").toPath().resolve("dest.txt")
        Files.write(src, "content".toByteArray())
        atomic_move(src, dest)
        assertTrue(Files.exists(dest))
    }

    @Test
    fun testAtomicMoveFallbackException() {
        val src = tempFolder.newFile("src2.txt").toPath()
        val dest = tempFolder.newFolder("dest_dir2").toPath().resolve("dest2.txt")
        Files.write(src, "content".toByteArray())

        var fallbackHit = false
        val mockMove: (Path, Path, Array<StandardCopyOption>) -> Unit = { s, d, opts ->
            if (opts.contains(StandardCopyOption.ATOMIC_MOVE)) {
                throw java.nio.file.AtomicMoveNotSupportedException(s.toString(), d.toString(), "Not supported")
            } else {
                fallbackHit = true
                Files.move(s, d, *opts)
            }
        }

        atomic_move(src, dest, mockMove)
        assertTrue(fallbackHit)
        assertTrue(Files.exists(dest))
    }
}
