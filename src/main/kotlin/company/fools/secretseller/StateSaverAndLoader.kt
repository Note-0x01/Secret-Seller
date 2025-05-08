package company.fools.secretseller

import com.mojang.serialization.Codec
import company.fools.secretseller.config.MarketData
import company.fools.secretseller.config.MerchantItem
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.World

class StateSaverAndLoader :  PersistentState() {
    var outposts: ArrayList<BlockPos> = ArrayList()
    var merchantStock: HashMap<String, MarketData> = HashMap()

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val encoded = blockPosListCodec.encodeStart(NbtOps.INSTANCE, outposts)
        nbt.put("outposts", encoded.getOrThrow(true ) {})

        val stockList = ArrayList<MerchantItem>()
        this.merchantStock.forEach {
            stockList.add(
                MerchantItem(it.key, it.value)
            )
        }

        val itemsEncoded = merchantItemListCodec.encodeStart(NbtOps.INSTANCE, stockList)
        nbt.put("merchantStock", itemsEncoded.getOrThrow(true) {})

        return nbt
    }

    fun increaseDemand(item: String, amount: Int) {
        SecretSeller.logger.info("Before: ${this.merchantStock[item]!!.demand.value}")
        this.merchantStock[item]!!.demand.value += amount
        SecretSeller.logger.info("After: ${this.merchantStock[item]!!.demand.value}")
        this.markDirty()
    }

    fun reduceSupply(item: String, amount: Int) {
        SecretSeller.logger.info("Before: ${this.merchantStock[item]!!.supply.value}")
        this.merchantStock[item]!!.supply.value -= amount
        SecretSeller.logger.info("After: ${this.merchantStock[item]!!.supply.value}")
        this.markDirty()
    }

    fun addToOutposts(pos: BlockPos) {
        this.outposts.add(pos)
        this.markDirty()
    }

    fun removeFromOutposts(pos: BlockPos) {
        this.outposts.remove(pos)
        this.markDirty()
    }

    fun disableAllItems() {
        this.merchantStock.forEach { it.value.enabled = false }
    }

    fun enableOrAddFromConfig() {
        SecretSeller.config.stock.forEach {
            if(this.merchantStock[it.itemIdentifier] != null) {
                this.merchantStock[it.itemIdentifier]!!.enabled = true

                this.merchantStock[it.itemIdentifier]!!.lockedPrice = it.marketData.lockedPrice
                this.merchantStock[it.itemIdentifier]!!.basePrice = it.marketData.basePrice

                this.merchantStock[it.itemIdentifier]!!.purchaseStack = it.marketData.purchaseStack

                this.merchantStock[it.itemIdentifier]!!.supply.max = it.marketData.supply.max
                this.merchantStock[it.itemIdentifier]!!.supply.min = it.marketData.supply.min

                this.merchantStock[it.itemIdentifier]!!.supply.value =
                    restrainValue(this.merchantStock[it.itemIdentifier]!!.supply.value,
                        it.marketData.supply.max,
                        it.marketData.supply.min)

                this.merchantStock[it.itemIdentifier]!!.demand.max = it.marketData.demand.max
                this.merchantStock[it.itemIdentifier]!!.demand.min = it.marketData.demand.min

                this.merchantStock[it.itemIdentifier]!!.demand.value =
                    restrainValue(this.merchantStock[it.itemIdentifier]!!.demand.value,
                        it.marketData.demand.max,
                        it.marketData.demand.min)

                this.markDirty()
            } else {
                this.merchantStock[it.itemIdentifier] = it.marketData
                this.markDirty()
            }
        }
    }

    private fun restrainValue(value: Int, min: Int, max: Int) =
        if (value > max)
            max
        else if (value < min)
            min
        else
            value

    companion object {
        var blockPosListCodec: Codec<List<BlockPos>> = BlockPos.CODEC.listOf()
        var merchantItemListCodec: Codec<List<MerchantItem>> = MerchantItem.CODEC.listOf()
        private fun createFromNbt(tag: NbtCompound): StateSaverAndLoader {
            val state = StateSaverAndLoader()

            val decode = blockPosListCodec.decode(NbtOps.INSTANCE, tag.get("outposts")).getOrThrow(true) {}.first
            @Suppress("UNCHECKED_CAST")
            state.outposts = decode.toMutableList() as ArrayList<BlockPos>

            val itemsDecode =  merchantItemListCodec.decode(NbtOps.INSTANCE, tag.get("merchantStock")).getOrThrow(true) {}.first
            @Suppress("UNCHECKED_CAST")
            val listOfItems = itemsDecode.toMutableList() as ArrayList<MerchantItem>
            state.merchantStock = HashMap()
            listOfItems.forEach {
                state.merchantStock[it.itemIdentifier] = it.marketData
            }

            return state
        }

        @JvmStatic
        fun getServerState(server: MinecraftServer): StateSaverAndLoader {
            val persistentStateManager = server.getWorld(World.OVERWORLD)!!.persistentStateManager

            val state = persistentStateManager.getOrCreate(
                StateSaverAndLoader::createFromNbt,
                ::StateSaverAndLoader,
                "secret-seller"
            )

            SecretSeller.merchantry.worldInit(state)

            state.markDirty()
            return state
        }
    }
}