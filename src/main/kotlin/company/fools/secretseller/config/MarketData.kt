package company.fools.secretseller.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.serialization.Serializable

@Serializable
data class MarketData(
    var lockedPrice: Boolean,
    var basePrice: Int,
    var purchaseStack: Int,
    var supply: ShiftingValue,
    var demand: ShiftingValue,
    var enabled: Boolean = true
) {
    companion object {
        val CODEC: Codec<MarketData> = RecordCodecBuilder.create {
            it.group(
                Codec.BOOL.fieldOf("lockedPrice").forGetter(MarketData::lockedPrice),
                Codec.INT.fieldOf("basePrice").forGetter(MarketData::basePrice),
                Codec.INT.fieldOf("purchaseStack").forGetter(MarketData::purchaseStack),
                ShiftingValue.CODEC.fieldOf("supply").forGetter(MarketData::supply),
                ShiftingValue.CODEC.fieldOf("demand").forGetter(MarketData::demand),
                Codec.BOOL.fieldOf("enabled").forGetter(MarketData::enabled)
            ).apply(it, ::MarketData)
        }
    }
}