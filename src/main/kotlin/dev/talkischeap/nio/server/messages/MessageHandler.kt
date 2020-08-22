package dev.talkischeap.nio.server.messages

interface MessageHandler {
    fun handle(data: ByteArray): ByteArray?
}