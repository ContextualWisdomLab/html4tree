package html4tree

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.io.UncheckedIOException
import kotlin.test.assertTrue

class IgnoreFileRaceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun readIgnoreMatchersSwallowsIOExceptionFromRacedFileRead() {
        val ignoreFile = temporaryFolder.newFile(".html4ignore")

        val matchers = read_ignore_matchers(ignoreFile) { _, _ ->
            throw IOException("simulated file replacement during read")
        }

        assertTrue(matchers.isEmpty(), "an IOException during the raced read must degrade to no ignore matchers")
    }

    @Test
    fun readIgnoreMatchersSwallowsUncheckedIOExceptionFromLazyIteration() {
        val ignoreFile = temporaryFolder.newFile(".html4ignore")

        val matchers = read_ignore_matchers(ignoreFile) { _, _ ->
            throw UncheckedIOException(IOException("simulated lazy read failure"))
        }

        assertTrue(matchers.isEmpty(), "an UncheckedIOException during lazy iteration must degrade to no ignore matchers")
    }
}
