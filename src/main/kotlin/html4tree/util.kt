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
        val nextEntry = Entry(lle.file, lle.level, null, lle.fileKey)

        // Rebuild deque from externally mutated 'last' chain if needed when deque is empty
        if (deque.isEmpty() && last != null) {
            var current = last
            while (current != null) {
                deque.addLast(LinkedListEntry(current.data, current.level, current.fileKey))
                current = current.next
            }
        }

        if(last == null){
            last = nextEntry
            first = last
        } else {
            val currentFirst = first
            if (currentFirst == null) {
                var currentLast = last!!
                while (currentLast.next != null) {
                    currentLast = currentLast.next!!
                }
                currentLast.next = nextEntry
            } else {
                currentFirst.next = nextEntry
            }
            first = nextEntry
        }
        deque.addLast(lle)
    }

    fun pull(): LinkedListEntry? {
        val l: Entry? = last
        if(l != null) {
            last = l.next
        }

        if(l == null){
            if (deque.isEmpty()) {
                return null
            } else {
                return deque.removeFirst()
            }
        } else {
            l.next = null
            if (deque.isEmpty()) {
                return LinkedListEntry(l.data, l.level, l.fileKey)
            } else {
                return deque.removeFirst()
            }
        }
    }
}
