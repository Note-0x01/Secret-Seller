package company.fools.secretseller.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.serialization.Serializable

@Serializable
data class ShiftingValue(
    var value: Int,
    var max: Int,
    var min: Int) {
    companion object {
        val CODEC: Codec<ShiftingValue> = RecordCodecBuilder.create {
            it.group(
                Codec.INT.fieldOf("value").forGetter(ShiftingValue::value),
                Codec.INT.fieldOf("max").forGetter(ShiftingValue::max),
                Codec.INT.fieldOf("min").forGetter(ShiftingValue::min)
            ).apply(it, ::ShiftingValue)
        }
    }
}