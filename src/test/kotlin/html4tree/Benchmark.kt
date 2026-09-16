package html4tree

import kotlin.system.measureTimeMillis

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
