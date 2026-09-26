package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class CatchTest {
    @Test
    fun testProcessIgnoreFileException() {
        val testDir = Files.createTempDirectory("test_catch_race").toFile()
        for (i in 0..100) {
            val dir = Files.createTempDirectory("test_race_$i").toFile()
            val f = File(dir, ".html4ignore")
            f.writeText("test")
            val t = kotlin.concurrent.thread {
                f.delete()
                f.mkdir()
            }
            process_ignore_file(dir)
            t.join()
        }

        process_ignore_file(testDir, null) { _, _ ->
            throw RuntimeException("Injected Exception")
        }

        process_ignore_file(testDir, null) { _, _ ->
            object : java.io.InputStream() {
                var first = true
                override fun read(): Int {
                    if (first) {
                        first = false
                        return 'a'.toInt()
                    }
                    throw RuntimeException("Exception from read!")
                }
                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    if (first && len > 0) {
                        first = false
                        b[off] = 'a'.toByte()
                        return 1
                    }
                    throw RuntimeException("Exception from read!")
                }
            }
        }

        // This is to cover the default fallback explicitly
        val testDir2 = Files.createTempDirectory("test_catch_default").toFile()
        val ignoreFile = File(testDir2, ".html4ignore")
        ignoreFile.writeText("test")
        process_ignore_file(testDir2, null)
    }
}
