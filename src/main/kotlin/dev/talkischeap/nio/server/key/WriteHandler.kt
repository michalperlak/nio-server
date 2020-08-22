package dev.talkischeap.nio.server.key

import dev.talkischeap.nio.server.logging.Logging
import dev.talkischeap.nio.server.messages.MessageOutbox
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

internal class WriteHandler(
    private val messageOutbox: MessageOutbox,
    private val keyInterests: KeyInterests
) : KeyHandler {

    override fun handle(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        val messages = messageOutbox.getAll(key)
        while (!messages.isEmpty()) {
            val buffer = ByteBuffer.wrap(messages.peek())
            val written = socketChannel.write(buffer)
            if (written == -1) {
                socketChannel.close()
                log.info("Disconnected from : $socketChannel")
                return
            }
            if (buffer.hasRemaining()) {
                return
            }
            messages.remove()
        }
        keyInterests.interests(key, SelectionKey.OP_READ)
    }

    companion object : Logging()
}