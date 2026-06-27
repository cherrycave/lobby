package dev.boecker.cclobby.listener

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.player.PlayerSwapItemEvent

fun EventNode<Event>.preventItemShifts() {
    this.addListener(InventoryPreClickEvent::class.java) { event ->
        if (event.inventory == event.player.inventory) {
            event.isCancelled = true
        }
    }
    this.addListener(PlayerSwapItemEvent::class.java) { event ->
        event.isCancelled = true
    }
}