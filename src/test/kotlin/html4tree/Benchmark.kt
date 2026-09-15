package html4tree

object Benchmark {
    @JvmStatic fun main(args: Array<String>) {
        val testStrings = arrayOf(".hidden", "normal", "\u3002hidden", "file.txt", "")

        // Warmup
        for (i in 1..10000) {
            for (s in testStrings) {
                s.isHiddenFile()
            }
        }

        val start2 = System.nanoTime()
        for (i in 1..10000000) {
            for (s in testStrings) {
                s.isHiddenFile()
            }
        }
        val end2 = System.nanoTime()

        println("Optimized Time: ${(end2 - start2) / 1000000} ms")
    }
}
