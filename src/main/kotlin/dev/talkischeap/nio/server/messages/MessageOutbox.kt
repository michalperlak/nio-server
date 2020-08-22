package dev.talkischeap.nio.server.messages

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.LinkedBlockingQueue

class MessageOutbox {
    private val outbox: ConcurrentMap<SelectionKey, Queue<ByteBuffer>> = ConcurrentHashMap()

    fun getAll(key: SelectionKey): Queue<ByteBuffer> = outbox.computeIfAbsent(key) { LinkedBlockingQueue() }

    fun add(key: SelectionKey, data: ByteBuffer) {
        val keyQueue = outbox.computeIfAbsent(key) { LinkedBlockingQueue() }
        keyQueue.add(data)
    }
}