package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files

class GoFailTest {
    @Test(expected = IllegalArgumentException::class)
    fun testGoRequireDirNotExists() {
        go("not_exists_dir", 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoRequireDirNotDirectory() {
        val f = File.createTempFile("temp", "txt")
        f.deleteOnExit()
        go(f.absolutePath, 0)
    }
}
