package company.fools.secretseller.renderer

import company.fools.secretseller.mob.SellerEntity
import company.fools.secretseller.model.SellerModel
import company.fools.secretseller.variant.SellerVariant
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import software.bernie.geckolib.renderer.GeoEntityRenderer
import java.util.*

class SellerRenderer(renderManager: EntityRendererFactory.Context?): GeoEntityRenderer<SellerEntity>(renderManager, SellerModel()) {
    private val textureLocationByVariant: Map<SellerVariant, Identifier> = Util.make(EnumMap(SellerVariant::class.java)) { map ->
        map[SellerVariant.DEFAULT] = Identifier("secret-seller","textures/entity/lyn.png")
        map[SellerVariant.ASIMOV] = Identifier("secret-seller","textures/entity/asimov.png")
    }

    override fun getTextureLocation(animatable: SellerEntity): Identifier =
        textureLocationByVariant[animatable.getVariant()] ?: Identifier("secret-seller","lyn")
}