package company.fools.secretseller.model

import company.fools.secretseller.mob.SellerEntity
import company.fools.secretseller.variant.SellerVariant
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import software.bernie.geckolib.model.DefaultedEntityGeoModel
import java.util.*

class SellerModel(): DefaultedEntityGeoModel<SellerEntity>(Identifier("secret-seller","lyn"), true) {
    private val modelLocationByVariant: Map<SellerVariant, Identifier> = Util.make(EnumMap(SellerVariant::class.java)) { map ->
        map[SellerVariant.DEFAULT] = Identifier("secret-seller","geo/entity/lyn.geo.json")
        map[SellerVariant.ASIMOV] = Identifier("secret-seller","geo/entity/asimov.geo.json")
    }

    override fun getModelResource(animatable: SellerEntity): Identifier =
        modelLocationByVariant[animatable.getVariant()]
        ?: Identifier("secret-seller","geo/entity/lyn.geo.json")
}