package html4tree

import java.io.File

data class LinkedListEntry(val file: File, val level: Int, var fileKey: Any? = null)

class LinkedList {
    // ⚡ Bolt Performance Optimization: Replace custom node-based list with ArrayDeque
    // ArrayDeque avoids allocating Entry wrapper objects for every push/pull operation,
    // significantly reducing Garbage Collection (GC) overhead during BFS traversals.
    private val deque = java.util.ArrayDeque<LinkedListEntry>()

    fun push(lle: LinkedListEntry) {
        deque.addLast(lle)
    }

    fun pull(): LinkedListEntry? {
        return deque.pollFirst()
    }
}
