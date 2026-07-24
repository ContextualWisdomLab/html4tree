package html4tree

import org.junit.Test
import java.io.File
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class AttrExceptionTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testProcessDirException() {
        val subdir = tempFolder.newFolder("unreadable_dir")
        process_dir(subdir, emptySet(), arrayOf(File("nonexistent_file_to_cause_exception")))
    }
}
