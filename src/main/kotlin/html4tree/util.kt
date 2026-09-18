package html4tree

import java.io.File

data class Entry (val data: File, val level: Int, var next: Entry?, val fileKey: Any? = null)

data class LinkedListEntry(val file: File, val level: Int, var fileKey: Any? = null)

class LinkedList {
    var first: Entry? = null
    var last: Entry? = null

    private val deque = java.util.ArrayDeque<LinkedListEntry>()

    fun push(lle: LinkedListEntry) {
        deque.addLast(lle)
    }

    fun pull(): LinkedListEntry? {
        return deque.pollFirst()
    }
}
