package dev.talkischeap.nio.server

import dev.talkischeap.nio.server.messages.Message
import dev.talkischeap.nio.server.messages.MessageInbox
import dev.talkischeap.nio.server.messages.MessageOutbox
import dev.talkischeap.nio.server.messages.MessageProcessor
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
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

    interface KeyHandler {
        fun handle(key: SelectionKey)
    }

    class AcceptHandler : KeyHandler {
        override fun handle(key: SelectionKey) {
            val serverChannel = key.channel() as ServerSocketChannel
            val socketChannel = serverChannel.accept()
            println("Connected to: $socketChannel")
            socketChannel.configureBlocking(false)
            socketChannel.register(key.selector(), SelectionKey.OP_READ)
        }
    }

    class ReadHandler(
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
            println("Disconnected from $socketChannel")
        }
    }

    class WriteHandler(
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
                    println("Disconnected from : $socketChannel")
                    return
                }
                if (buffer.hasRemaining()) {
                    return
                }
                messages.remove()
            }
            keyInterests.interests(key, SelectionKey.OP_READ)
        }
    }
}