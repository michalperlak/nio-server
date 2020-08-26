package dev.talkischeap.nio.server.messages

import dev.talkischeap.nio.server.key.KeyInterests
import java.nio.channels.SelectionKey
import java.util.concurrent.Executor

internal class MessageProcessor(
    private val messageHandler: MessageHandler,
    private val executor: Executor,
    private val messageInbox: MessageInbox,
    private val messageOutbox: MessageOutbox,
    private val keyInterests: KeyInterests
) {

    fun start() {
        val processingThread = Thread({ processingLoop() }, "message-processor")
        processingThread.start()
    }

    private fun processingLoop() {
        while (true) {
            val message = messageInbox.next()
            message?.let { process(it) }
        }
    }

    private fun process(message: Message) {
        executor.execute {
            val key = message.key
            val connectionId = key.attachment()
            val responseData = messageHandler.handle(connectionId.toString(), message.data)
            responseData?.let {
                messageOutbox.add(key, it)
                keyInterests.interests(key, SelectionKey.OP_WRITE)
            }
            key.selector().wakeup()
        }
    }
}