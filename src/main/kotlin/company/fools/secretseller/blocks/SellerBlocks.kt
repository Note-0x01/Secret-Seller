package company.fools.secretseller.blocks

import company.fools.secretseller.items.EscapeRopeItem
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier

class SellerBlocks {
    companion object {
        private fun register(block: Block, id: String, shouldRegisterItem: Boolean): Block {
            val itemId = Identifier.of("secret-seller", id)

            if(shouldRegisterItem) {
                val blockItem = BlockItem(block, Item.Settings())
                Registry.register(Registries.ITEM, itemId, blockItem)
            }

            return Registry.register(Registries.BLOCK, itemId, block)
        }

        val outpostBlock = register(OutpostBlock(AbstractBlock.Settings.create().sounds(BlockSoundGroup.WOOD).nonOpaque()),
            "trader_outpost",
            true)
        fun initialize() {}
    }
}