package dev.talkischeap.nio.server.key

import dev.talkischeap.nio.server.logging.Logging
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

internal class AcceptHandler : KeyHandler {
    override fun handle(key: SelectionKey) {
        val serverChannel = key.channel() as ServerSocketChannel
        val socketChannel = serverChannel.accept()
        log.info("Connected to: $socketChannel")
        socketChannel.configureBlocking(false)
        socketChannel.register(key.selector(), SelectionKey.OP_READ)
    }

    companion object : Logging()
}