package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.nio.file.Files

class UseLinesExceptionTest {

    @Test
    fun testProcessIgnoreFileThrowsException() {
        val tempDir = Files.createTempDirectory("ignore_test").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("pattern1")

            // badNames에 정상적인 이름과 널 바이트가 포함된 잘못된 이름을 같이 넣습니다.
            val badNames = arrayOf("valid.txt", "bad\u0000name.txt")

            val excluded = process_ignore_file(tempDir, badNames)
            assertTrue(excluded.contains("index.html"))
            assertTrue(excluded.contains(".git"))

        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testProcessIgnoreFileOuterException() {
        val tempDir = Files.createTempDirectory("ignore_test2").toFile()
        try {
            val ignoreFile = File(tempDir, ".html4ignore")
            ignoreFile.writeText("pattern1")

            var listCallCount = 0
            val badDir = object : File(tempDir.absolutePath) {
                override fun list(): Array<String>? {
                    listCallCount++
                    if (listCallCount == 1) {
                        // 첫 번째 호출(204줄, try 내부)에서 예외 발생
                        throw RuntimeException("Forced exception for outer try-catch coverage")
                    } else {
                        // 두 번째 호출(232줄, try 외부)에서는 정상 반환
                        return arrayOf("somefile.txt")
                    }
                }
            }

            val excluded = process_ignore_file(badDir, null)
            assertTrue(excluded.contains("index.html"))
            assertTrue(excluded.contains(".git"))

        } finally {
            tempDir.deleteRecursively()
        }
    }
}
