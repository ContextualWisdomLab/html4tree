package html4tree

object Benchmark {
    @JvmStatic fun main(args: Array<String>) {
        val testFiles = Array(10000) { "test_file_$it.txt" }
        val testFilesWithHtml = Array(1000) { "test<file>&'$it.txt" }
        val allFiles = testFiles + testFilesWithHtml

        // Warmup
        for (i in 1..100) {
            benchmarkOld(allFiles)
            benchmarkNew(allFiles)
        }

        var oldTime = 0L
        var newTime = 0L

        for (i in 1..10) {
            oldTime += kotlin.system.measureTimeMillis {
                for (j in 1..100) {
                    benchmarkOld(allFiles)
                }
            }

            newTime += kotlin.system.measureTimeMillis {
                for (j in 1..100) {
                    benchmarkNew(allFiles)
                }
            }
        }

        println("Old escapeHtml: $oldTime ms")
        println("New escapeHtml (Array lookup fastpath check first): $newTime ms")
    }

    private fun String.escapeHtmlOld(): String {
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
                    sb = java.lang.StringBuilder(this.length + 16)
                    sb.append(this as CharSequence, 0, i)
                }
                sb.append(replacement)
            } else {
                sb?.append(c)
            }
        }
        return sb?.toString() ?: this
    }

    private fun benchmarkOld(files: Array<String>): Int {
        var len = 0
        files.forEach {
            len += it.escapeHtmlOld().length
        }
        return len
    }

    private fun benchmarkNew(files: Array<String>): Int {
        var len = 0
        files.forEach {
            len += it.escapeHtml().length
        }
        return len
    }
}
