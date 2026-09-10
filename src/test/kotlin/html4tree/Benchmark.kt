package html4tree

object Benchmark {
    @JvmStatic fun main(args: Array<String>) {
        val iterations = 10000
        val items = 1000

        val start1 = System.nanoTime()
        for (i in 0 until iterations) {
            val sb = java.lang.StringBuilder()
            for (j in 0 until items) {
                sb.append("          <li><a class=\"dir-link\" href=\"./item.txt\" title=\"item.txt 파일\"><span class=\"icon\" aria-hidden=\"true\">&#128196;</span> <span>item.txt</span> <span class=\"visually-hidden\">파일</span></a></li>\n")
            }
        }
        val end1 = System.nanoTime()

        val start2 = System.nanoTime()
        for (i in 0 until iterations) {
            val sb = java.lang.StringBuilder(if (items > 0) items * 256 else 16)
            for (j in 0 until items) {
                sb.append("          <li><a class=\"dir-link\" href=\"./item.txt\" title=\"item.txt 파일\"><span class=\"icon\" aria-hidden=\"true\">&#128196;</span> <span>item.txt</span> <span class=\"visually-hidden\">파일</span></a></li>\n")
            }
        }
        val end2 = System.nanoTime()

        println("Default StringBuilder: ${(end1 - start1) / 1000000} ms")
        println("Pre-allocated StringBuilder: ${(end2 - start2) / 1000000} ms")
    }
}
