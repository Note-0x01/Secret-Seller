package company.fools.secretseller.items

import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

class SellerItems {
    companion object {
        private fun register(item: Item, id: String): Item {
            val itemId = Identifier.of("secret-seller", id)
            return Registry.register(Registries.ITEM, itemId, item)
        }

        val goldCoin = register(Item(Item.Settings()), "gold_coin")
        val escapeRope = register(EscapeRopeItem(Item.Settings().maxCount(1)), "escape-rope")
        fun initialize() {}
    }
}