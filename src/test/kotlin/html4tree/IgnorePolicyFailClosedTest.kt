package html4tree

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFalse

class IgnorePolicyFailClosedTest {
    @Test
    fun crawlDoesNotPublishDirectoryWhenPolicyReadFails() {
        val root = Files.createTempDirectory("html4tree-ignore-fail-closed-").toFile()
        var processed = false

        try {
            val list = LinkedList()
            list.push(LinkedListEntry(root, 0, read_file_identity(root).key))

            crawl_directories(
                list,
                -1,
                processDirectory = { _: File, _: Set<String>, _: Array<File>? -> processed = true },
                processIgnoreFile = { _, _ -> throw IgnoreFileReadException("policy read failed") }
            )

            assertFalse(processed, "a directory must not be published when its declared ignore policy cannot be read")
        } finally {
            root.deleteRecursively()
        }
    }
}
