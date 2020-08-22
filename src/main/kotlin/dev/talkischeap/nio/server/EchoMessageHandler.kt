package dev.talkischeap.nio.server

import dev.talkischeap.nio.server.messages.MessageHandler

class EchoMessageHandler : MessageHandler {
    override fun handle(data: ByteArray): ByteArray? = data
}