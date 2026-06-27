package dev.boecker.cclobby.navigator

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerUseItemEvent

fun EventNode<Event>.interactNavigatorItemListener(navigator: LobbyNavigator) {
    this.addListener(PlayerBlockInteractEvent::class.java) { event ->
        if (!event.player.itemInMainHand.isSimilar(navigator.navigatorItem)) {
            return@addListener
        }

        event.isCancelled = true

        event.player.openNavigatorInventory()
    }

    this.addListener(PlayerUseItemEvent::class.java) { event ->
        if (!event.player.itemInMainHand.isSimilar(navigator.navigatorItem)) {
            return@addListener
        }

        event.isCancelled = true

        event.player.openNavigatorInventory()
    }
}