package dev.boecker.cclobby.c4

import dev.boecker.cclobby.CherryCaveLobby
import dev.boecker.cclobby.util.addFrame
import dev.boecker.cherrycave.connectfour.ConnectFourManager
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerLoadedEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.event.trait.InventoryEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.TransactionOption
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.CustomData
import net.minestom.server.network.player.ResolvableProfile
import java.util.*

class LobbyConnectFour(val lobby: CherryCaveLobby) {

    val connectFourManager: ConnectFourManager = ConnectFourManager()

    val challenges = mutableMapOf<UUID, UUID>()

    val c4Item = ItemStack.of(Material.ENDER_EYE).with(
        DataComponents.CUSTOM_NAME, Component.text(
            "Connect Four",
            NamedTextColor.GREEN
        )
    )


    val eventNode: EventNode<Event> = EventNode.all("c4")

    init {
        MinecraftServer.getCommandManager().register(ConnectFourCommand(this))

        eventNode.addListener(PlayerLoadedEvent::class.java) { event ->
            event.player.inventory.setItemStack(8, c4Item)
        }

        eventNode.addListener(PlayerBlockInteractEvent::class.java) { event ->
            if (!event.player.itemInMainHand.isSimilar(c4Item)) {
                return@addListener
            }

            event.isCancelled = true

            openChallengeInventory(event.player)
        }

        eventNode.addListener(PlayerUseItemEvent::class.java) { event ->
            if (!event.player.itemInMainHand.isSimilar(c4Item)) {
                return@addListener
            }

            event.isCancelled = true

            openChallengeInventory(event.player)
        }
    }

    fun openChallengeInventory(player: Player) {
        val challengeInventory = Inventory(
            InventoryType.CHEST_5_ROW,
            Component.text("Challenge to Connect Four", NamedTextColor.BLUE)
        )

        challengeInventory.addFrame()

        val skullItems = MinecraftServer.getConnectionManager().onlinePlayers.filter { it.uuid != player.uuid }.map {
            ItemStack.of(Material.PLAYER_HEAD).with(DataComponents.PROFILE, ResolvableProfile(it.skin))
                .with(DataComponents.CUSTOM_NAME, it.name).with(
                DataComponents.CUSTOM_DATA,
                CustomData(CompoundBinaryTag.builder().putString("uuid", it.uuid.toString()).build())
            )
        }

        challengeInventory.addItemStacks(skullItems, TransactionOption.ALL)

        val callbackNode: EventNode<InventoryEvent> = EventNode.type("click", EventFilter.INVENTORY) { _, inv ->
            challengeInventory == inv
        }

        callbackNode.addListener(InventoryPreClickEvent::class.java) { event ->
            event.isCancelled = true

            if (event.clickedItem != null && event.clickedItem.material() == Material.PLAYER_HEAD) {
                val uuidTag =
                    event.clickedItem.get(DataComponents.CUSTOM_DATA)?.nbt()?.getString("uuid") ?: return@addListener

                val uuid = UUID.fromString(uuidTag)

                val challengee =
                    MinecraftServer.getConnectionManager().onlinePlayers.find { it.uuid == uuid } ?: return@addListener

                challengePlayer(event.player, challengee)
                player.closeInventory()
                eventNode.removeChild(callbackNode)
            }
        }

        eventNode.addChild(callbackNode)

        player.openInventory(challengeInventory)
    }

    fun challengePlayer(challenger: Player, challengee: Player) {
        if (challenges[challenger.uuid] == challengee.uuid) {
            challenger.sendMessage(lobby.minimessage.deserialize("<red>You already challenged this player</red>"))
            return
        }

        challenges[challenger.uuid] = challengee.uuid

        challengee.sendMessage(lobby.minimessage.deserialize("<blue>${challenger.username}</blue> <gray>challenged you to</gray> <blue>Connect Four!</blue> <green><click:run_command:/connect-four accept ${challenger.uuid}>Accept</click><green>"))
    }

    fun acceptChallenge(challengee: Player, challenger: Player) {
        challenges.remove(challenger.uuid)
        connectFourManager.createGame(challenger, challengee)
    }
}