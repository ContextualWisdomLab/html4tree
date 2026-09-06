package html4tree

import org.junit.Test
import org.junit.Assert.assertTrue

class BenchmarkTest {
    @Test
    fun benchmarkEscapeHtml() {
        val testString = "This is a <test> string with \"quotes\" & ampersands and 'single quotes' & `backticks`."
        // Warmup
        for (i in 0..10000) {
            testString.escapeHtml()
        }

        val start1 = System.nanoTime()
        for (i in 0..1000000) {
            testString.escapeHtml()
        }
        val time1 = System.nanoTime() - start1

        println("New time: ${time1 / 1000000} ms")
        assertTrue(time1 > 0)
    }
}
