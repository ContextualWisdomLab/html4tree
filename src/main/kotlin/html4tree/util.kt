package html4tree

import java.io.File
import java.util.ArrayDeque

data class Entry (val data: File, val level: Int, var next: Entry?, val fileKey: Any? = null)

data class LinkedListEntry(val file: File, val level: Int, var fileKey: Any? = null)

class LinkedList {
    private val deque = ArrayDeque<LinkedListEntry>()

    private var explicitFirst: Entry? = null
    private var explicitLast: Entry? = null
    private var explicitMode = false

    var first: Entry? = null
        get() = if (explicitMode) explicitFirst else deque.peekFirst()?.let { Entry(it.file, it.level, null, it.fileKey) }
        set(value) {
            field = value
            explicitFirst = value
            explicitMode = true
        }

    var last: Entry? = null
        get() = if (explicitMode) explicitLast else deque.peekLast()?.let { Entry(it.file, it.level, null, it.fileKey) }
        set(value) {
            field = value
            explicitLast = value
            explicitMode = true
        }

    fun push(lle: LinkedListEntry) {
        if (explicitMode) {
            val nextEntry = Entry(lle.file, lle.level, null, lle.fileKey)
            val currentFirst = explicitFirst
            if (currentFirst == null) {
                if (explicitLast != null) {
                    var currentLast = explicitLast!!
                    while (currentLast.next != null) {
                        currentLast = currentLast.next!!
                    }
                    currentLast.next = nextEntry
                } else {
                    explicitLast = nextEntry
                }
            } else {
                currentFirst.next = nextEntry
            }
            explicitFirst = nextEntry
            return
        }
        deque.addLast(lle)
    }

    fun pull(): LinkedListEntry? {
        if (explicitMode) {
            val l = explicitLast
            if (l != null) {
                explicitLast = l.next
                l.next = null
                return LinkedListEntry(l.data, l.level, l.fileKey)
            }
            return null
        }
        return deque.pollFirst()
    }
}
