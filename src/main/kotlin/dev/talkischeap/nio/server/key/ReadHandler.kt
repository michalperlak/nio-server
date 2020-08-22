package dev.talkischeap.nio.server.key

import dev.talkischeap.nio.server.logging.Logging
import dev.talkischeap.nio.server.messages.Message
import dev.talkischeap.nio.server.messages.MessageInbox
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

internal class ReadHandler(
    private val messageInbox: MessageInbox
) : KeyHandler {
    private val buffer: ByteBuffer = ByteBuffer.allocateDirect(1024 * 1024)

    override fun handle(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        buffer.clear()
        val read = socketChannel.read(buffer)
        buffer.rewind()
        when {
            read < 0 -> disconnect(socketChannel)
            read > 0 -> {
                val data = ByteArray(size = read)
                buffer.get(data)
                messageInbox.add(Message(key = key, data = data))
            }
        }
    }

    private fun disconnect(socketChannel: SocketChannel) {
        socketChannel.close()
        log.info("Disconnected from $socketChannel")
    }

    companion object : Logging()
}