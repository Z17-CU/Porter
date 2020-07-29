package cu.control.queue.utils.permissions

import java.util.*

class LRUCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>() {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxSize
    }
}
