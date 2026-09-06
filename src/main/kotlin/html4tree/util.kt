package html4tree

import java.io.File
import java.util.ArrayDeque

data class LinkedListEntry(val file: File, val level: Int, var fileKey: Any? = null)

class LinkedList {
    private val deque = ArrayDeque<LinkedListEntry>()

    fun push(lle: LinkedListEntry) {
        // Performance optimization: Using ArrayDeque avoids allocating Entry wrapper nodes
        deque.addLast(lle)
    }

    fun pull(): LinkedListEntry? {
        return deque.pollFirst()
    }
}
