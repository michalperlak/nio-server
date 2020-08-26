package dev.talkischeap.nio.server.key

import dev.talkischeap.nio.server.logging.Logging
import dev.talkischeap.nio.server.messages.InitMessage
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.util.UUID

internal class AcceptHandler(
    private val initMessage: InitMessage
) : KeyHandler {
    override fun handle(key: SelectionKey) {
        val serverChannel = key.channel() as ServerSocketChannel
        val socketChannel = serverChannel.accept()
        log.info("Connected to: $socketChannel")
        socketChannel.configureBlocking(false)
        val initMessageData = initMessage.data
        if (initMessageData.isNotEmpty()) {
            socketChannel.write(ByteBuffer.wrap(initMessageData))
        }
        val connectionId = UUID.randomUUID()
        key.attach(connectionId.toString())
        socketChannel.register(key.selector(), SelectionKey.OP_READ)
    }

    companion object : Logging()
}