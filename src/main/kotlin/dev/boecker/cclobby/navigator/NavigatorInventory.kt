package dev.boecker.cclobby.navigator

import dev.boecker.cherrycave.common.proxy.SendRequest
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

val navigatorInventory = Inventory(InventoryType.CHEST_4_ROW, Component.text("Navigator", NamedTextColor.BLUE))

fun initializeNavInventory(navigator: LobbyNavigator) {
    val emptyItem = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).with(DataComponents.CUSTOM_NAME, Component.text(""))

    for (i in 0 until navigatorInventory.size) {
        navigatorInventory.setItemStack(i, emptyItem)
    }

    navigator.navigatorConfig.items.forEach { itemConfig ->
        navigatorInventory.setItemStack(
            itemConfig.slot,
            ItemStack.of(itemConfig.material)
                .with(DataComponents.CUSTOM_NAME, navigator.lobby.minimessage.deserialize(itemConfig.name))
                .with(DataComponents.LORE, listOf(navigator.lobby.minimessage.deserialize(itemConfig.description)))
        )
    }

    navigatorInventory.update()
}

fun Player.openNavigatorInventory() {
    this.openInventory(navigatorInventory)
}

fun EventNode<Event>.navigatorInvInteractListener(navigator: LobbyNavigator) {
    this.addListener(InventoryPreClickEvent::class.java) { event ->
        if (event.inventory != navigatorInventory) {
            return@addListener
        }

        event.isCancelled = true

        navigator.navigatorConfig.items.find { it.slot == event.slot }?.let { itemConfig ->
            when (itemConfig.action) {
                is LobbyNavigator.Configuration.SendRequestAction -> {
                    navigator.lobby.coroutineScope.launch {
                        navigator.lobby.messagingSystem.publishSendRequest(
                            SendRequest(
                                event.player.uuid,
                                itemConfig.action.server
                            )
                        )
                    }
                }
            }
        }
    }
}