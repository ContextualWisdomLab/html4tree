package html4tree

import java.io.File
import java.util.ArrayDeque

data class LinkedListEntry(val file: File, val level: Int, var fileKey: Any? = null)

class LinkedList {
    private val queue = ArrayDeque<LinkedListEntry>()

    fun push(lle: LinkedListEntry) {
        queue.addLast(lle)
    }

    fun pull(): LinkedListEntry? {
        return queue.pollFirst()
    }
}
