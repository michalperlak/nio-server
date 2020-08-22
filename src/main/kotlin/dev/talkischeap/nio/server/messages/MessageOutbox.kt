package dev.talkischeap.nio.server.messages

import java.nio.channels.SelectionKey
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.LinkedBlockingQueue

class MessageOutbox {
    private val outbox: ConcurrentMap<SelectionKey, Queue<ByteArray>> = ConcurrentHashMap()

    fun getAll(key: SelectionKey): Queue<ByteArray> = outbox.computeIfAbsent(key) { LinkedBlockingQueue() }

    fun add(key: SelectionKey, data: ByteArray) {
        val keyQueue = outbox.computeIfAbsent(key) { LinkedBlockingQueue() }
        keyQueue.add(data)
    }
}