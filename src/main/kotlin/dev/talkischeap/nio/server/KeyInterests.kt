package dev.talkischeap.nio.server

import java.nio.channels.SelectionKey
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue

class KeyInterests {
    private val interestsQueue: Queue<Pair<SelectionKey, Int>> = LinkedBlockingQueue()

    fun interests(key: SelectionKey, ops: Int) {
        interestsQueue.add(key to ops)
    }

    fun process() {
        while (interestsQueue.isNotEmpty()) {
            val (key, ops) = interestsQueue.poll()
            key.interestOps(ops)
        }
    }
}