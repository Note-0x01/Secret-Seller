package company.fools.secretseller.screen

import company.fools.secretseller.SecretSeller
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

class SellerSlot(inventory: Inventory, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    override fun canTakeItems(playerEntity: PlayerEntity?): Boolean {
        return false
    }

    override fun canInsert(stack: ItemStack?): Boolean {
        return false
    }
}