package html4tree

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.security.Permission
import kotlin.test.assertTrue

class IgnoreFileRaceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun withReadFault(
        target: File,
        fault: (File) -> Unit,
        block: () -> Unit
    ) {
        val previous = System.getSecurityManager()
        val targetPath = target.absolutePath
        System.setSecurityManager(object : SecurityManager() {
            override fun checkPermission(permission: Permission?) {
                // This test manager is a deterministic read-race hook, not a sandbox.
            }

            override fun checkRead(file: String?) {
                val openingStream = Thread.currentThread().stackTrace.any {
                    it.className == "java.io.FileInputStream"
                }
                if (openingStream && file != null && File(file).absolutePath == targetPath) {
                    fault(target)
                }
            }
        })
        try {
            block()
        } finally {
            System.setSecurityManager(previous)
        }
    }

    @Test
    fun processIgnoreFileSwallowsIOExceptionWhenFileDisappearsBeforeOpen() {
        val ignoreFile = temporaryFolder.newFile(".html4ignore")
        ignoreFile.writeText("*.tmp\n")

        withReadFault(ignoreFile, { racedFile ->
            assertTrue(racedFile.delete(), "the test must remove the file between metadata checks and open")
        }) {
            val excluded = process_ignore_file(temporaryFolder.root, arrayOf("safe.txt"))
            assertTrue("index.html" in excluded)
        }
    }

    @Test
    fun processIgnoreFileSwallowsUncheckedIOExceptionFromOpen() {
        val ignoreFile = temporaryFolder.newFile(".html4ignore")
        ignoreFile.writeText("*.tmp\n")

        withReadFault(ignoreFile, {
            throw UncheckedIOException(IOException("simulated lazy read failure"))
        }) {
            val excluded = process_ignore_file(temporaryFolder.root, arrayOf("safe.txt"))
            assertTrue("index.html" in excluded)
        }
    }
}
