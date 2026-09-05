package html4tree

fun String.oldEscapeHtml(): String {
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

fun main() {
    val testStrings = listOf(
        "normal text without any escapes",
        "some text with & and < and > and \" and ' and `",
        "가나다라 Hello & World",
        "👨‍👩‍👧‍👦 emoji and symbols < >",
        "mix text &amp; and &lt;tag&gt;",
        "",
        "A B C !@# 123",
        "A".repeat(100) + "&" + "B".repeat(100)
    )

    var dummy1 = 0
    // Warm-up: 10000 iterations
    for (i in 0..10000) {
        for (s in testStrings) {
            dummy1 += s.oldEscapeHtml().length
            dummy1 += s.escapeHtml().length
        }
    }

    var dummy2 = 0
    // Benchmark old: 500000 iterations
    val start1 = System.nanoTime()
    for (i in 0..500000) {
        for (s in testStrings) {
            dummy2 += s.oldEscapeHtml().length
        }
    }
    val oldTime = System.nanoTime() - start1

    var dummy3 = 0
    // Benchmark new: 500000 iterations
    val start2 = System.nanoTime()
    for (i in 0..500000) {
        for (s in testStrings) {
            dummy3 += s.escapeHtml().length
        }
    }
    val newTime = System.nanoTime() - start2

    println("Dummy: " + (dummy1 + dummy2 + dummy3))
    println("Old Escape Time: " + (oldTime / 1000000) + " ms")
    println("New Escape Time: " + (newTime / 1000000) + " ms")
    println("Improvement: " + ((1.0 - (newTime.toDouble() / oldTime.toDouble())) * 100) + "%")
}
