package html4tree

import java.io.File
import java.nio.file.Files

/**
 * Benchmark evidence to validate StringBuilder pre-allocation logic.
 * Retained in the repository per PR review requirements.
 */
object StringBuilderPreallocationBenchmark {
    @JvmStatic
    fun main(args: Array<String>) {
        val iterations = 20000
        val fileCounts = listOf(10, 100, 1000)

        println("=== StringBuilder Preallocation Benchmark ===")
        println("Iterations: \$iterations")

        fileCounts.forEach { count ->
            val files = Array(count) { File("file\${it}.txt") }

            // Warmup
            for (i in 0..5000) {
                runOriginal(files)
                runPreallocated(files)
            }

            // Measurement - Original
            val startOriginal = System.nanoTime()
            for (i in 0..iterations) {
                runOriginal(files)
            }
            val timeOriginal = (System.nanoTime() - startOriginal) / 1_000_000

            // Measurement - Preallocated
            val startPreallocated = System.nanoTime()
            for (i in 0..iterations) {
                runPreallocated(files)
            }
            val timePreallocated = (System.nanoTime() - startPreallocated) / 1_000_000

            println("\\nFile count: \$count")
            println("Original: \${timeOriginal}ms")
            println("Preallocated: \${timePreallocated}ms")
            println("Improvement: \${(timeOriginal - timePreallocated) * 100.0 / timeOriginal}%")
        }
    }

    private fun runOriginal(files: Array<File>): String {
        val l = StringBuilder()
        files.forEach {
            l.append("<li><a href=\"./\${it.name}\">\${it.name}</a></li>\\n")
        }
        return l.toString()
    }

    private fun runPreallocated(files: Array<File>): String {
        // Safe pre-allocation logic with overflow guard
        val estimatedCharsPerItem = 250L
        val requestedCapacity = files.size * estimatedCharsPerItem
        val actualCapacity = if (requestedCapacity > Int.MAX_VALUE) Int.MAX_VALUE else requestedCapacity.toInt()

        val l = StringBuilder(actualCapacity)
        files.forEach {
            l.append("<li><a href=\"./\${it.name}\">\${it.name}</a></li>\\n")
        }
        return l.toString()
    }
}
