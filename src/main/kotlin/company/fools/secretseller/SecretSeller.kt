package company.fools.secretseller

import company.fools.secretseller.blocks.OutpostEntity
import company.fools.secretseller.blocks.SellerBlocks
import company.fools.secretseller.config.SellerConfig
import company.fools.secretseller.config.SellerConfigProcessor
import company.fools.secretseller.items.SellerItems
import company.fools.secretseller.mob.SellerEntity
import company.fools.secretseller.mob.SellerManager
import company.fools.secretseller.screen.SellerScreenHandler
import company.fools.secretseller.screen.OutpostScreenHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.datafixer.TypeReferences
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SecretSeller : ModInitializer {
   	val logger: Logger = LoggerFactory.getLogger("secret-seller")

	var sellerManager: SellerManager = SellerManager()
	val merchantry: Merchantry = Merchantry();
	val config: SellerConfig = SellerConfigProcessor.createAndLoad()

	var seller: EntityType<SellerEntity> = Registry.register(Registries.ENTITY_TYPE,
		Identifier.of("secret-seller", "seller"),
		EntityType.Builder.create(
			::SellerEntity, SpawnGroup.CREATURE)
			.setDimensions(0.6f, 1.8f).build("seller"))

	var outpostEntityType: BlockEntityType<OutpostEntity> = Registry.register(Registries.BLOCK_ENTITY_TYPE,
		Identifier.of("secret-seller", "trader_outpost"),
		BlockEntityType.Builder.create(::OutpostEntity, SellerBlocks.outpostBlock).build(
			Util.getChoiceType(
				TypeReferences.BLOCK_ENTITY,
				"outpost"
			))
	)

	var outpostScreenHandler = ExtendedScreenHandlerType(::OutpostScreenHandler)
	var merchantScreenHandler = Registry.register(Registries.SCREEN_HANDLER, Identifier.of("secret-seller","merchant_screen_handler"),
		ScreenHandlerType(::SellerScreenHandler, FeatureFlags.DEFAULT_ENABLED_FEATURES)
	)!!

	override fun onInitialize() {
		logger.info("Hello Fabric world!")
		FabricDefaultAttributeRegistry.register(seller, createGenericEntityAttributes())
		SellerItems.initialize()
		SellerBlocks.initialize()

		outpostScreenHandler = Registry.register(Registries.SCREEN_HANDLER, Identifier("secret-seller", "outpost"), outpostScreenHandler)
		logger.info(config.stock.toString())
	}

	private fun createGenericEntityAttributes(): DefaultAttributeContainer.Builder {
		return PathAwareEntity.createLivingAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.8000000029802322)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0).add(
				EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
			.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.1)
	}
}