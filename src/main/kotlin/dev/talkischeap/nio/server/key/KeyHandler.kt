package dev.talkischeap.nio.server.key

import java.nio.channels.SelectionKey

interface KeyHandler {
    fun handle(key: SelectionKey)
}