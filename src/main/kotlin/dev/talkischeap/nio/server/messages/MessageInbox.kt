package dev.talkischeap.nio.server.messages

import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue

class MessageInbox {
    private val queue: Queue<Message> = LinkedBlockingQueue()

    fun add(message: Message) {
        queue.add(message)
    }

    fun next(): Message? = queue.poll()
}