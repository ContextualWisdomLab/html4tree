package html4tree

object Benchmark {
    @JvmStatic
    fun main(args: Array<String>) {
        val iterations = 10_000

        // Mocking a large directory logic
        fun generateHTML(numItems: Int, preallocate: Boolean): String {
            val l = if (preallocate) StringBuilder(numItems * 200) else StringBuilder()
            for (i in 0 until numItems) {
                l.append("""          <li><a class="dir-link" href="test" title="test"><span class="icon" aria-hidden="true">&#128196;</span> <span>test</span> <span class="visually-hidden">파일</span></a></li>""")
                l.append('\n')
            }
            return l.toString()
        }

        // Warmup
        for (i in 0 until 100) {
            generateHTML(1000, false)
            generateHTML(1000, true)
        }

        val startOld = System.nanoTime()
        for (i in 0 until iterations) {
            generateHTML(1000, false)
        }
        val endOld = System.nanoTime()

        val startNew = System.nanoTime()
        for (i in 0 until iterations) {
            generateHTML(1000, true)
        }
        val endNew = System.nanoTime()

        println("StringBuilder() Time taken: ${(endOld - startOld) / 1_000_000} ms")
        println("StringBuilder(expectedItems * estimatedCharsPerItem) Time taken: ${(endNew - startNew) / 1_000_000} ms")
    }
}
