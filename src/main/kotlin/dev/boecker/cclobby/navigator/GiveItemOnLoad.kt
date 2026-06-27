package dev.boecker.cclobby.navigator

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerLoadedEvent



fun EventNode<Event>.giveItemOnLoadListener(navigator: LobbyNavigator) {
    this.addListener(PlayerLoadedEvent::class.java) { event: PlayerLoadedEvent ->
        navigator.giveNavigatorItem(event.player)
    }
}