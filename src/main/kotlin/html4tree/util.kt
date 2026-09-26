package html4tree

import java.io.File
import java.util.ArrayDeque

data class LinkedListEntry(val file: File, val level: Int, var fileKey: Any? = null)

// ⚡ Bolt Performance Optimization: Replace custom linked list with ArrayDeque
// ArrayDeque provides O(1) amortized queue operations without the overhead of
// allocating a new Entry wrapper object for every pushed item, significantly
// reducing Garbage Collection (GC) pressure when crawling large directory trees.
class LinkedList {
    private val queue = ArrayDeque<LinkedListEntry>()

    fun push(lle: LinkedListEntry) {
        queue.addLast(lle)
    }

    fun pull(): LinkedListEntry? {
        return queue.pollFirst()
    }
}
