package html4tree

import kotlin.jvm.JvmStatic
import kotlin.system.measureTimeMillis
import org.junit.Test
import kotlin.test.assertEquals

object Benchmark {
    @JvmStatic
    fun main(args: Array<String>) {
        val testStr = "<script>alert(\"XSS & 'code' \\`here\\`\")</script>".repeat(100)

        // Warmup
        for (i in 1..1000) {
            testStr.escapeHtml()
        }

        val time = measureTimeMillis {
            for (i in 1..100000) {
                testStr.escapeHtml()
            }
        }
        println("escapeHtml time: $time ms")
    }
}

class EscapeHtmlTest {
    @Test
    fun testEscapeHtml() {
        val input = "<script>alert(\"XSS & 'code' \\`here\\`\")</script>한글"
        val expected = "&lt;script&gt;alert(&quot;XSS &amp; &#x27;code&#x27; \\&#x60;here\\&#x60;&quot;)&lt;/script&gt;한글"
        assertEquals(expected, input.escapeHtml())
    }
}
