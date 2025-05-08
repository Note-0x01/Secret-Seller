package company.fools.secretseller

import company.fools.secretseller.config.MarketData
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.util.math.random.Random
import kotlin.collections.HashMap

class Merchantry {
    private val random = Random.create()

    fun worldInit(state: StateSaverAndLoader) {
        state.disableAllItems()
        state.enableOrAddFromConfig()
    }

    fun getStock(state: StateSaverAndLoader): HashMap<String, MarketData> {
        return state.merchantStock
    }

    fun getPrices(state: StateSaverAndLoader): Map<String, Int> {
        return state.merchantStock.mapValues { item ->
            var price = item.value.basePrice - (item.value.supply.value - item.value.demand.value)
            if(price < 1)
                price = 1
            price
        }
    }

    fun getPrice(merchantStock: HashMap<String, Int>, item: Item): Int {
        val merchantItem = merchantStock[Registries.ITEM.getId(item).toString()]
        if (merchantItem != null) {
            return merchantItem
        }
        return 0
    }

    fun purchaseItem(state: StateSaverAndLoader, item: String) {
        SecretSeller.logger.info("Item Purchased: $item")
        SecretSeller.logger.info(state.merchantStock.toString())
        val merchantItem = state.merchantStock[item]
        if(merchantItem != null) {
            if(merchantItem.demand.value < merchantItem.demand.max)
                state.increaseDemand(item, 1)

            if(merchantItem.supply.value > merchantItem.supply.min) //&& random.nextInt(100) < 25
                state.reduceSupply(item, 1)

            state.markDirty()
            SecretSeller.logger.info((state.merchantStock[item]!!.supply.value).toString())
            SecretSeller.logger.info((state.merchantStock[item]!!.demand.value).toString())
            var price = state.merchantStock[item]!!.basePrice + (state.merchantStock[item]!!.supply.value + state.merchantStock[item]!!.demand.value)
            SecretSeller.logger.info("$price")
        }
    }

    fun sellItem(state: StateSaverAndLoader, item: String) {
        val merchantItem = state.merchantStock[item]
        if(merchantItem != null) {
            if(merchantItem.demand.value != merchantItem.demand.min)
                state.merchantStock[item]!!.demand.value--

            if(merchantItem.supply.value != merchantItem.supply.max && random.nextInt(100) < 25)
                state.merchantStock[item]!!.supply.value++
        }
    }
}