package company.fools.secretseller.config

import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.serialization.Serializable
import net.minecraft.util.math.Vec3d
import java.util.function.Function


@Serializable
data class MerchantItem(
    val itemIdentifier: String,
    val marketData: MarketData
) {
    companion object {
        val CODEC: Codec<MerchantItem> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("itemIdentifier").forGetter(MerchantItem::itemIdentifier),
                MarketData.CODEC.fieldOf("marketData").forGetter(MerchantItem::marketData)
            ).apply(it, ::MerchantItem)
        }
    }
}