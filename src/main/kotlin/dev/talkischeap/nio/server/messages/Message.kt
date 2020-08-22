package dev.talkischeap.nio.server.messages

import java.nio.channels.SelectionKey

class Message(
    val key: SelectionKey,
    val data: ByteArray
)