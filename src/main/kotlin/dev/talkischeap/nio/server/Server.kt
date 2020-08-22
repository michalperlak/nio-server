package dev.talkischeap.nio.server

import dev.talkischeap.nio.server.config.ServerConfigurer
import dev.talkischeap.nio.server.key.*
import dev.talkischeap.nio.server.key.AcceptHandler
import dev.talkischeap.nio.server.key.KeyInterests
import dev.talkischeap.nio.server.key.ReadHandler
import dev.talkischeap.nio.server.key.WriteHandler
import dev.talkischeap.nio.server.logging.Logging
import dev.talkischeap.nio.server.messages.MessageHandler
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
    val server = ServerConfigurer
        .fromHandler(EchoMessageHandler())
        .readBufferSize(256)
        .executor(Executors.newSingleThreadExecutor())
        .port(25)
        .configure()
    server.start()
}

class Server(
    private val port: Int,
    private val messageHandler: MessageHandler,
    private val readBufferSize: Int,
    private val executor: Executor
) {
    fun start() {
        val selector = openServerSocketChannel(port)
        val keyInterests = KeyInterests()
        val (inbox, outbox) = configureMessageProcessing(keyInterests)
        val keyHandler = configureKeyHandler(inbox, outbox, keyInterests)
        while (true) {
            processMessages(selector, keyInterests, keyHandler)
        }
    }

    private fun processMessages(selector: Selector, keyInterests: KeyInterests, handler: KeyHandler) {
        selector.select()
        keyInterests.process()
        val keys = selector.selectedKeys()
        val iter = keys.iterator()
        while (iter.hasNext()) {
            val key = iter.next()
            iter.remove()
            handler.handle(key)
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

    private fun configureMessageProcessing(keyInterests: KeyInterests): Pair<MessageInbox, MessageOutbox> {
        val messageInbox = MessageInbox()
        val messageOutbox = MessageOutbox()
        val messageProcessor = MessageProcessor(messageHandler, executor, messageInbox, messageOutbox, keyInterests)
        messageProcessor.start()
        return messageInbox to messageOutbox
    }

    private fun configureKeyHandler(
        inbox: MessageInbox,
        outbox: MessageOutbox,
        keyInterests: KeyInterests
    ): KeyHandler {
        val acceptHandler = AcceptHandler()
        val readHandler = ReadHandler(readBufferSize, inbox)
        val writeHandler = WriteHandler(outbox, keyInterests)

        return object : KeyHandler {
            override fun handle(key: SelectionKey) {
                if (!key.isValid) {
                    return
                }

                when {
                    key.isAcceptable -> acceptHandler.handle(key)
                    key.isReadable -> readHandler.handle(key)
                    key.isWritable -> writeHandler.handle(key)
                }
            }
        }
    }

    companion object : Logging()
}