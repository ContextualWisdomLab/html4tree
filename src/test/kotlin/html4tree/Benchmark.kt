package html4tree

import java.io.File
import java.nio.file.Files

object Benchmark {
    fun String.escapeHtmlOld(): String {
        var sb: StringBuilder? = null
        for (i in 0 until this.length) {
            val c = this[i]
            val replacement = when (c) {
                '&' -> "&amp;"
                '<' -> "&lt;"
                '>' -> "&gt;"
                '"' -> "&quot;"
                '\'' -> "&#x27;"
                '`' -> "&#x60;"
                else -> null
            }
            if (replacement != null) {
                if (sb == null) {
                    sb = StringBuilder(this.length + 16)
                    sb.append(this as CharSequence, 0, i)
                }
                sb.append(replacement)
            } else {
                sb?.append(c)
            }
        }
        return sb?.toString() ?: this
    }

    @JvmStatic fun main(args: Array<String>) {
        val testString = "This is a <test> string with \"quotes\" & ampersands and 'single quotes' & `backticks`."

        println("Measuring CPU cost (Old vs New implementation)...")
        // Warmup
        for (i in 0..100000) {
            testString.escapeHtmlOld()
            testString.escapeHtml()
        }

        val iterations = 5000000
        val timesOld = LongArray(5)
        val timesNew = LongArray(5)

        for (run in 0 until 5) {
            System.gc()
            val startOld = System.nanoTime()
            for (i in 0 until iterations) {
                testString.escapeHtmlOld()
            }
            timesOld[run] = System.nanoTime() - startOld

            System.gc()
            val startNew = System.nanoTime()
            for (i in 0 until iterations) {
                testString.escapeHtml()
            }
            timesNew[run] = System.nanoTime() - startNew
        }

        println("Old implementation (nano seconds per 5M runs): ${timesOld.joinToString()}")
        println("New implementation (nano seconds per 5M runs): ${timesNew.joinToString()}")

        println("\nMeasuring end-to-end directory rendering throughput...")
        val dirIters = 5
        val tmpDir = Files.createTempDirectory("benchmark-html4tree-").toFile()

        try {
            for (i in 0 until 1000) {
                File(tmpDir, "file-$i.txt").writeText("Test file $i")
            }

            println("Warming up directory rendering...")
            for (i in 0 until 2) {
                go(tmpDir.absolutePath, -1)
            }

            System.gc()
            val beforeMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val startDir = System.nanoTime()
            for (i in 0 until dirIters) {
                go(tmpDir.absolutePath, -1)
            }
            val dirElapsed = System.nanoTime() - startDir
            val afterMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

            println("Avg time per end-to-end large dir render run: ${(dirElapsed / 1000000.0) / dirIters} ms")
            println("Estimated memory allocation diff (bytes): ${afterMem - beforeMem}")
        } finally {
            tmpDir.deleteRecursively()
        }
    }
}
