package html4tree

import java.io.File
import java.util.ArrayDeque

data class Entry (val data: File, val level: Int, var next: Entry?, val fileKey: Any? = null)

data class LinkedListEntry(val file: File, val level: Int, var fileKey: Any? = null)

class LinkedList {
    private val deque = ArrayDeque<LinkedListEntry>()

    var first: Entry? = null
    var last: Entry? = null

    fun push(lle: LinkedListEntry) {
        deque.addLast(lle)
    }

    fun pull(): LinkedListEntry? {
        return if (deque.isEmpty()) null else deque.removeFirst()
    }
}
