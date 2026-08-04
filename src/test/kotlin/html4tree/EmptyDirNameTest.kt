package html4tree

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

class EmptyDirNameTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testProcessDirRootDirectoryMock() {
        val tempDir = tempFolder.newFolder()
        val mockRoot = object : File(tempDir, "mockRoot") {
            override fun getName() = ""
            // Mocking a root directory which has an empty name
        }
        mockRoot.mkdir()

        process_dir(mockRoot, emptySet(), emptyArray())

        val indexHtml = File(mockRoot, "index.html")
        assertTrue(indexHtml.exists())

        val content = indexHtml.readText()
        assertTrue(content.contains("<title>${mockRoot.absolutePath.escapeHtml()}</title>"))
        assertTrue(content.contains("<h1>${mockRoot.absolutePath.escapeHtml()}</h1>"))
    }
}
