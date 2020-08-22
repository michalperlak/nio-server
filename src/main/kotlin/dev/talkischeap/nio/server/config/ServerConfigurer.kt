package dev.talkischeap.nio.server.config

import dev.talkischeap.nio.server.Server
import dev.talkischeap.nio.server.messages.InitMessage
import dev.talkischeap.nio.server.messages.MessageHandler
import java.util.concurrent.Executor
import java.util.concurrent.Executors

data class ServerConfigurer(
    val messageHandler: MessageHandler,
    val port: Int = DEFAULT_PORT,
    val readBufferSize: Int = 1 * MB,
    val initMessage: InitMessage = InitMessage.empty(),
    val executor: Executor = Executors.newCachedThreadPool()
) {
    fun port(port: Int): ServerConfigurer = copy(port = port)

    fun initMessage(message: InitMessage): ServerConfigurer = copy(initMessage = message)

    fun readBufferSize(size: Int): ServerConfigurer = copy(readBufferSize = size)

    fun executor(executor: Executor): ServerConfigurer = copy(executor = executor)

    fun configure(): Server = Server(
        port = port,
        messageHandler = messageHandler,
        readBufferSize = readBufferSize,
        initMessage = initMessage,
        executor = executor
    )

    companion object {
        const val DEFAULT_PORT = 8080
        const val MB = 1024 * 1024

        fun fromHandler(messageHandler: MessageHandler): ServerConfigurer = ServerConfigurer(messageHandler)
    }
}