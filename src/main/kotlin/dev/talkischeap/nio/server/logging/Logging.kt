package dev.talkischeap.nio.server.logging

import java.util.logging.Logger

open class Logging {
    protected val log: Logger = Logger.getLogger(javaClass.name)
}