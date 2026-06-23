package dev.boecker.cclobby.util

import net.kyori.adventure.text.Component
import net.minestom.server.component.DataComponents
import net.minestom.server.inventory.Inventory
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

fun Inventory.addFrame() {
    val rows = this.size / 9

    val frameItem = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).with(DataComponents.CUSTOM_NAME, Component.text(""))

    for (i in 0 until this.size) {
        if (i < 9 || i % 9 == 0 || i % 9 == 8 || i > (9 * (rows - 1))) {
            this.setItemStack(i, frameItem)
        }
    }
}