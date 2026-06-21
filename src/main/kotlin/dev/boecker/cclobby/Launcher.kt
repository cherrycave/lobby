package dev.boecker.cclobby

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Initializing Lobby Server" }

    CherryCaveLobby().start()
}