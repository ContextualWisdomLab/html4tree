package html4tree

import kotlin.system.measureTimeMillis

object Benchmark {
    @JvmStatic
    fun main(args: Array<String>) {
        val testString = "This is a <test> string with & some \"special\" 'characters' like `this`."
        // Warm up
        for (i in 0..10000) {
            testString.escapeHtml()
        }

        val time = measureTimeMillis {
            for (i in 0..1000000) {
                testString.escapeHtml()
            }
        }
        println("escapeHtml took ${time}ms")
    }
}
