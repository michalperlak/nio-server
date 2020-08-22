package dev.talkischeap.nio.server.messages

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class InitMessage internal constructor(
    val data: ByteArray
) {
    companion object {
        private val EMPTY = InitMessage(ByteArray(0))

        fun empty(): InitMessage = EMPTY

        fun from(data: ByteArray) = InitMessage(data)

        fun fromString(data: String, charset: Charset = StandardCharsets.UTF_8) =
            InitMessage(data.toByteArray(charset))
    }
}