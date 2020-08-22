package dev.talkischeap.nio.server

import dev.talkischeap.nio.server.key.AcceptHandler
import dev.talkischeap.nio.server.key.KeyInterests
import dev.talkischeap.nio.server.key.ReadHandler
import dev.talkischeap.nio.server.key.WriteHandler
import dev.talkischeap.nio.server.messages.MessageInbox
import dev.talkischeap.nio.server.messages.MessageOutbox
import dev.talkischeap.nio.server.messages.MessageProcessor
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.Executor
import java.util.concurrent.Executors

fun main() {
    val server = Server()
    server.start(25)
}

class Server(
    private val executor: Executor = Executors.newCachedThreadPool()
) {
    fun start(port: Int) {
        val selector = openServerSocketChannel(port)

        val keyInterests = KeyInterests()
        val messageInbox = MessageInbox()
        val messageOutbox = MessageOutbox()
        val echoMessageHandler = EchoMessageHandler()
        val messageProcessor = MessageProcessor(echoMessageHandler, executor, messageInbox, messageOutbox, keyInterests)
        messageProcessor.start()

        val acceptHandler = AcceptHandler()
        val readHandler = ReadHandler(messageInbox)
        val writeHandler = WriteHandler(messageOutbox, keyInterests)

        while (true) {
            selector.select()
            keyInterests.process()
            val keys = selector.selectedKeys()
            val iter = keys.iterator()
            while (iter.hasNext()) {
                val key = iter.next()
                iter.remove()
                if (!key.isValid) {
                    continue
                }
                when {
                    key.isAcceptable -> acceptHandler.handle(key)
                    key.isReadable -> readHandler.handle(key)
                    key.isWritable -> writeHandler.handle(key)
                }
            }
        }
    }

    private fun openServerSocketChannel(port: Int): Selector {
        val socketChannel = ServerSocketChannel.open()
        socketChannel.configureBlocking(false)
        socketChannel.bind(InetSocketAddress(port))
        val selector = Selector.open()
        socketChannel.register(selector, SelectionKey.OP_ACCEPT)
        return selector
    }
}