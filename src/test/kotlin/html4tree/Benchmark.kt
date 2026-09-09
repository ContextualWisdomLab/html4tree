package html4tree

import java.io.File

object Benchmark {
    @JvmStatic fun main(args: Array<String>) {
        val iterations = 50000
        val fileCount = 1000
        val files = Array(fileCount) { File("file${it}.txt") }
        val start = System.nanoTime()
        for (i in 0..iterations) {
            val l = StringBuilder(files.size * 250)
            files.forEach { l.append("<li>${it.name}</li>") }
        }
        val end = System.nanoTime()
        println("With Preallocation: ${(end - start) / 1_000_000} ms")
    }
}
