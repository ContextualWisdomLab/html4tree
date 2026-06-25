package html4tree

import java.io.File

data class Entry (val data: File, val level: Int, var next: Entry?)

data class LinkedListEntry(val file: File, val level: Int)

class LinkedList {
    private var first: Entry? = null
    private var last: Entry? = null

    fun push(lle: LinkedListEntry) {
        if(last == null){
            last = Entry(lle.file, lle.level, null)
            first = last
        } else {
            val newEntry = Entry(lle.file, lle.level, null)
            first!!.next = newEntry
            first = newEntry
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
