package dev.talkischeap.nio.server.messages

interface MessageHandler {
    fun handle(connectionId: String, data: ByteArray): ByteArray?
}