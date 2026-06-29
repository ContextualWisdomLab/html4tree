package html4tree

import org.junit.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class HelpTest {
    @Test
    fun testHelp() {
        val originalOut = System.out
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)
        System.setOut(ps)

        try {
            help()
            assertEquals("ERROR: help has not been written yet!\n", baos.toString().replace("\r\n", "\n"))
        } finally {
            System.setOut(originalOut)
        }
    }
}
