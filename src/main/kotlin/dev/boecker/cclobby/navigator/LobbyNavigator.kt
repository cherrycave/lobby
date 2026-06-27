package dev.boecker.cclobby.navigator

import dev.boecker.cclobby.CherryCaveLobby
import dev.boecker.cclobby.navigator.LobbyNavigator.Configuration.ItemConfiguration
import dev.boecker.cclobby.serializer.MaterialSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import kotlin.io.path.*

class LobbyNavigator(val lobby: CherryCaveLobby) {

    private val eventNode: EventNode<Event> = EventNode.all("navigator")

    val navigatorItem: ItemStack = ItemStack.of(Material.SPYGLASS).with(
        DataComponents.CUSTOM_NAME, Component.text(
            "Navigator",
            NamedTextColor.BLUE
        )
    )

    @Serializable
    data class Configuration(
        val items: List<ItemConfiguration>,
    ) {
        @Serializable
        data class ItemConfiguration(
            val slot: Int,
            @Serializable(with = MaterialSerializer::class)
            val material: Material,
            val name: String,
            val description: String,
            val action: NavigatorAction
        )

        @OptIn(ExperimentalSerializationApi::class)
        @Serializable
        @JsonClassDiscriminator("actionType")
        sealed class NavigatorAction

        @Serializable
        @SerialName("send-request")
        data class SendRequestAction(val server: String) : NavigatorAction()
    }

    lateinit var navigatorConfig: Configuration

    init {
        loadConfiguration()

        initializeNavInventory(this)

        eventNode.giveItemOnLoadListener(this)
        eventNode.interactNavigatorItemListener(this)
        eventNode.navigatorInvInteractListener(this)

        MinecraftServer.getGlobalEventHandler().addChild(eventNode)
    }

    fun giveNavigatorItem(player: Player) {
        player.inventory.setItemStack(4, navigatorItem)
    }

    fun loadConfiguration() {
        val configPath = Path(System.getenv("NAVIGATOR_CONFIG_PATH") ?: "./navigator.json")

        if (!configPath.exists()) {
            configPath.createParentDirectories()
            configPath.writeText(lobby.json.encodeToString(Configuration(
                items = listOf(
                    ItemConfiguration(
                        slot = 0,
                        material = Material.EMERALD,
                        name = "<red>Example</red>",
                        description = "",
                        action = Configuration.SendRequestAction("lobby")
                    )
                )
            )))
        }

        val navigatorConfigText = configPath.readText()

        navigatorConfig = lobby.json.decodeFromString(navigatorConfigText)
    }
}