package company.fools.secretseller

import company.fools.secretseller.blocks.SellerBlocks
import company.fools.secretseller.items.SellerItems
import company.fools.secretseller.renderer.SellerRenderer
import company.fools.secretseller.screen.OutpostScreen
import company.fools.secretseller.screen.SellerScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object SecretSellerClient : ClientModInitializer {

	override fun onInitializeClient() {
		EntityRendererRegistry.register(SecretSeller.seller) { context ->
			SellerRenderer(context)
		}
		HandledScreens.register(SecretSeller.outpostScreenHandler, ::OutpostScreen)
		HandledScreens.register(SecretSeller.merchantScreenHandler, ::SellerScreen)
		BlockRenderLayerMap.INSTANCE.putBlock(SellerBlocks.outpostBlock, RenderLayer.getCutout())

		ModelPredicateProviderRegistry.register(SellerItems.goldCoin, Identifier("quantity"))
		{ itemStack: ItemStack, _: ClientWorld?, _: LivingEntity?, _: Int ->
			(itemStack.count.toFloat() / 64)
		}
	}
}