package html4tree

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import com.github.ajalt.clikt.core.PrintHelpMessage
import kotlin.test.assertFailsWith

class MainTest {
    @Test
    fun testProcessIgnoreFile() {
        val testDir = File("testIgnoreDir")
        testDir.mkdir()

        val ignoreFile = File(testDir, ".html4ignore")
        ignoreFile.writeText(".*\\.txt\nignoreMe\nindex\\.html")

        val file1 = File(testDir, "test.txt")
        file1.createNewFile()
        val file2 = File(testDir, "ignoreMe")
        file2.createNewFile()
        val file3 = File(testDir, "keepMe.log")
        file3.createNewFile()
        val indexFile = File(testDir, "index.html")
        indexFile.createNewFile()

        val excluded = process_ignore_file(testDir)

        assertTrue(excluded.contains("test.txt"))
        assertTrue(excluded.contains("ignoreMe"))
        assertTrue(excluded.contains("index.html"))
        assertFalse(excluded.contains("keepMe.log"))

        // clean up
        file1.delete()
        file2.delete()
        file3.delete()
        indexFile.delete()
        ignoreFile.delete()
        testDir.delete()
    }

    @Test
    fun testProcessIgnoreFileNoIgnoreFile() {
        val testDir = File("testNoIgnoreDir")
        testDir.mkdir()
        val excluded = process_ignore_file(testDir)
        assertTrue(excluded.contains("index.html"))
        assertEquals(1, excluded.size)
        testDir.delete()
    }

    @Test
    fun testProcessDir() {
        val testDir = File("testProcessDir")
        testDir.mkdir()
        val subDir = File(testDir, "sub")
        subDir.mkdir()
        val file = File(testDir, "file.txt")
        file.createNewFile()
        // 커버리지 확보를 위해 exclude 조건에 맞는 파일 추가
        val excludeFile = File(testDir, ".html4ignore")
        excludeFile.writeText(".*\\.tmp\n")
        val tmpFile = File(testDir, "test.tmp")
        tmpFile.createNewFile()

        // 커버리지 확보용으로 빈 디렉토리도 추가
        val emptySubDir = File(testDir, "emptySub")
        emptySubDir.mkdir()

        process_dir(testDir)

        // 커버리지 확보용으로 it == curr_dir (이론상 잘 발생하지 않지만 코드상 존재)
        process_dir(testDir)

        val indexFile = File(testDir, "index.html")
        assertTrue(indexFile.exists())

        val content = indexFile.readText()
        assertTrue(content.contains("<html lang=\"en\">"))
        assertTrue(content.contains("<meta charset=\"utf-8\">"))
        assertTrue(content.contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"))
        assertTrue(content.contains("<main>"))
        assertTrue(content.contains("<nav aria-label=\"Directory navigation\">"))
        assertTrue(content.contains("href=\"./sub/\""))
        assertTrue(content.contains("href=\"./file.txt\""))

        // clean up
        indexFile.delete()
        file.delete()
        subDir.delete()
        emptySubDir.delete()
        excludeFile.delete()
        tmpFile.delete()
        testDir.delete()
    }

    @Test
    fun testGo() {
        val topDir = File("testGoTop")
        topDir.mkdir()
        val subDir = File(topDir, "subDir")
        subDir.mkdir()
        val subSubDir = File(subDir, "subSubDir")
        subSubDir.mkdir()
        val dummyFile = File(topDir, "dummy.txt")
        dummyFile.createNewFile()

        // 커버리지 확보를 위한 빈 디렉토리 생성(listFiles() == null이 안될 수 있지만 empty list 테스트)
        val emptyDir = File(topDir, "emptyTopDir")
        emptyDir.mkdir()

        // Test with maxLevel 0
        go(topDir.absolutePath, 0)
        assertTrue(File(topDir, "index.html").exists())
        assertFalse(File(subDir, "index.html").exists())

        File(topDir, "index.html").delete()

        // Test with default maxLevel -1 (unlimited)
        go(topDir.absolutePath, -1)
        assertTrue(File(topDir, "index.html").exists())
        assertTrue(File(subDir, "index.html").exists())
        assertTrue(File(subSubDir, "index.html").exists())

        // clean up
        File(subSubDir, "index.html").delete()
        File(subDir, "index.html").delete()
        File(emptyDir, "index.html").delete()
        File(topDir, "index.html").delete()
        dummyFile.delete()
        subSubDir.delete()
        subDir.delete()
        emptyDir.delete()
        topDir.delete()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoWithInvalidDir() {
        go("non_existent_directory_for_test", -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGoWithFile() {
        val testFile = File("testGoWithFile.txt")
        testFile.createNewFile()
        try {
            go(testFile.absolutePath, -1)
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun testHtml4treeCommand() {
        val dummyDir = File("dummyDir")
        dummyDir.mkdir()

        try {
            val cmd = Html4tree()
            cmd.main(arrayOf("dummyDir", "--max-level", "2"))

            // 커버리지 확보를 위해 직접 호출
            main(arrayOf("dummyDir", "--max-level", "2"))
        } finally {
            File(dummyDir, "index.html").delete()
            dummyDir.delete()
        }
    }

    @Test
    fun testHelp() {
        // Just calling to get coverage
        help()
    }
}