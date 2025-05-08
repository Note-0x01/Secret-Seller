package company.fools.secretseller.mob

import company.fools.secretseller.SecretSeller
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList
import kotlin.math.min

class SellerInventory: Inventory {
    val stacks: DefaultedList<ItemStack> = DefaultedList.ofSize(12, ItemStack.EMPTY);

    override fun clear() {
        this.stacks.clear()
        this.markDirty()
    }

    override fun size(): Int {
        return 12
    }

    override fun isEmpty(): Boolean {
        for (itemStack in stacks) {
            if (!itemStack.isEmpty) {
                return false
            }
        }

        return true
    }

    override fun getStack(slot: Int): ItemStack {
        return if (slot >= 0 && slot < stacks.size) stacks[slot] else ItemStack.EMPTY
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        val itemStack = Inventories.splitStack(stacks, slot, amount)
        if (!itemStack.isEmpty) {
            markDirty()
        }

        return itemStack
    }

    override fun removeStack(slot: Int): ItemStack {
        val itemStack = stacks[slot]
        return if (itemStack.isEmpty) {
            ItemStack.EMPTY
        } else {
            stacks[slot] = ItemStack.EMPTY
            itemStack
        }
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        stacks[slot] = stack
        if (!stack!!.isEmpty && stack.count > this.maxCountPerStack) {
            stack.count = this.maxCountPerStack
        }

        markDirty()
    }

    override fun markDirty() {
        return
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return false
    }

    fun addStack(stack: ItemStack): ItemStack {
        return if (stack.isEmpty) {
            ItemStack.EMPTY
        } else {
            val itemStack = stack.copy()
            this.addToExistingSlot(itemStack)
            if (itemStack.isEmpty) {
                ItemStack.EMPTY
            } else {
                this.addToNewSlot(itemStack)
                if (itemStack.isEmpty) ItemStack.EMPTY else itemStack
            }
        }
    }

    private fun addToNewSlot(stack: ItemStack) {
        for (i in 0 until 12) {
            val itemStack = getStack(i)
            if (itemStack.isEmpty) {
                setStack(i, stack.copyAndEmpty())
                return
            }
        }
    }

    private fun addToExistingSlot(stack: ItemStack) {
        for (i in 0 until 12) {
            val itemStack = getStack(i)
            if (ItemStack.canCombine(itemStack, stack)) {
                this.transfer(stack, itemStack)
                if (stack.isEmpty) {
                    return
                }
            }
        }
    }

    private fun transfer(source: ItemStack, target: ItemStack) {
        val i = min(this.maxCountPerStack.toDouble(), target.maxCount.toDouble()).toInt()
        val j = min(source.count.toDouble(), (i - target.count).toDouble()).toInt()
        if (j > 0) {
            target.increment(j)
            source.decrement(j)
            markDirty()
        }
    }

}