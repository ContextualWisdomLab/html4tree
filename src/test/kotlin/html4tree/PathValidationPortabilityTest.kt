package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PathValidationPortabilityTest {
    @Test
    fun pathLengthIsNotTreatedAsAPortableFilesystemLimit() {
        val error = assertFailsWith<IllegalArgumentException> {
            go("a".repeat(4097), -1)
        }

        assertEquals(
            "Top directory must be an existing non-symlink directory",
            error.message
        )
    }
}
