package dev.talkischeap.nio.server.key

import java.nio.channels.SelectionKey

internal interface KeyHandler {
    fun handle(key: SelectionKey)
}