package dev.boecker.cclobby

import dev.boecker.cclobby.c4.LobbyConnectFour
import dev.boecker.cherrycave.permission.minestom.PermissionsAPI
import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.polar.PolarLoader
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.Auth
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSwapItemEvent
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText


class CherryCaveLobby {
    private val logger = KotlinLogging.logger {}

    lateinit var lobbyConnectFour: LobbyConnectFour

    val minimessage = MiniMessage.miniMessage()

    fun start() {
        System.setProperty("minestom.chunk-view-distance", "24");

        val auth = enableAuthentication()
        val minecraftServer = MinecraftServer.init(auth);

        PermissionsAPI.init(System.getenv("LUCKPERMS_REST_URL") ?: "http://localhost:25401")




        val instanceManager = MinecraftServer.getInstanceManager()
        val instanceContainer = instanceManager.createInstanceContainer()

        instanceContainer.chunkLoader =
            PolarLoader(Path("worlds/lobby.polar"));

        val globalEventHandler = MinecraftServer.getGlobalEventHandler()
        globalEventHandler.addListener(
            AsyncPlayerConfigurationEvent::class.java
        ) { event: AsyncPlayerConfigurationEvent? ->
            val player: Player = event!!.player
            event.spawningInstance = instanceContainer
            player.respawnPoint = Pos(0.0, 0.0, 0.0)
            player.gameMode = GameMode.ADVENTURE
        }

        lobbyConnectFour = LobbyConnectFour(this)
        globalEventHandler.addChild(lobbyConnectFour.eventNode)

        globalEventHandler.addListener(InventoryPreClickEvent::class.java) { event ->
            if (event.inventory == event.player.inventory) {
                event.isCancelled = true
            }
        }
        globalEventHandler.addListener(PlayerSwapItemEvent::class.java) { event ->
            event.isCancelled = true
        }

        minecraftServer.start(System.getenv("HOST") ?: "[::1]", System.getenv("PORT")?.toInt() ?: 25565);
    }

    private fun enableAuthentication(): Auth {
        val path = Path("forwarding.secret")
        return if (path.exists()) {
            val secret = path.readText().trim()
            logger.info { "Enabling Velocity Auth: \"$secret\"" }
            Auth.Velocity(secret)
        } else {
            logger.info { "Enabling Mojang Auth" }
            Auth.Online()
        }
    }
}