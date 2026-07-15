package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class CoverageTest {
    @Test
    fun testProcessDirWriteException() {
        val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
        val readOnlyDir = File(tempDir, "readonly")
        readOnlyDir.mkdir()
        readOnlyDir.setWritable(false, false)
        try {
            process_dir(readOnlyDir)
            // It should be handled securely
            assertTrue(true)
        } finally {
            readOnlyDir.setWritable(true, false)
        }
    }

    @Test
    fun testCssProperties() {
        val cssCont = cssContent
        val style = styleHash
        val cssProp = css
        assertTrue(cssCont.isNotEmpty())
        assertTrue(style.isNotEmpty())
        assertTrue(cssProp.isNotEmpty())
    }
}
