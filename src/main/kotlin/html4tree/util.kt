package html4tree

import java.io.File

data class Entry (val data: File, val level: Int, var next: Entry?)

data class LinkedListEntry(val file: File, val level: Int)

class LinkedList {
    var first: Entry? = null
    var last: Entry? = null

    fun push(lle: LinkedListEntry) {
        if(last == null){
            last = Entry(lle.file, lle.level, null)
            first = last
        } else {
            first?.next = Entry(lle.file, lle.level, null)
            first = first?.next
            first?.next = null
        }
    }

    fun pull(): LinkedListEntry? {
        val l: Entry? = last
        if(l != null) {
            last = l.next
        }

        if(l == null){
            return null
        } else {
	        l.next = null
            return LinkedListEntry(l.data, l.level)
        }
    }

}
fun escapeHtml(text: String): String {
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;")
}

fun urlEncode(text: String): String {
    return java.net.URLEncoder.encode(text, "UTF-8").replace("+", "%20")
}
