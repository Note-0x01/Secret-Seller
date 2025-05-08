package company.fools.secretseller.mob

import company.fools.secretseller.SecretSeller
import company.fools.secretseller.StateSaverAndLoader
import company.fools.secretseller.blocks.SellerBlocks
import company.fools.secretseller.items.SellerItems
import company.fools.secretseller.screen.SellerScreenHandler
import company.fools.secretseller.variant.SellerVariant
import net.minecraft.block.Blocks
import net.minecraft.block.ChestBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityData
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.ai.pathing.MobNavigation
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.screen.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.world.LocalDifficulty
import net.minecraft.world.ServerWorldAccess
import net.minecraft.world.World
import net.minecraft.world.WorldView
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.constant.DefaultAnimations
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.core.animation.AnimatableManager
import software.bernie.geckolib.core.animation.AnimationController
import software.bernie.geckolib.core.`object`.PlayState
import software.bernie.geckolib.util.GeckoLibUtil


class SellerEntity(entityType: EntityType<out PassiveEntity>, world: World) : PassiveEntity(entityType, world), GeoEntity {
    private val cache = GeckoLibUtil.createInstanceCache(this)
    private var spawnTime = world.time
    private val aliveTimer = 12000;
    private var customer: PlayerEntity? = null
    var secretStock: SellerInventory = SellerInventory()

    override fun initialize(
        world: ServerWorldAccess?,
        difficulty: LocalDifficulty?,
        spawnReason: SpawnReason?,
        entityData: EntityData?,
        entityNbt: NbtCompound?
    ): EntityData? {
        val variant = Util.getRandom(SellerVariant.entries, this.random)
        setVariant(variant)
        (this.getNavigation() as MobNavigation).setCanPathThroughDoors(true)
        this.secretStock.addStack(ItemStack(Items.DIAMOND, 1))
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt)
    }

    override fun initGoals() {
        goalSelector.add(0, SwimGoal(this))
        goalSelector.add(1, LookAtEntityGoal(this, PlayerEntity::class.java, 8.0f))
        goalSelector.add(2, TemptGoal(this, 0.4,
            Ingredient.ofItems(*arrayOf<ItemConvertible>(Items.GOLD_INGOT, Items.GOLD_NUGGET, Items.GOLD_BLOCK, Items.RAW_GOLD, Items.RAW_GOLD_BLOCK, SellerItems.goldCoin)), false))
        goalSelector.add(2, WanderAroundFarGoal(this, 0.35))
        goalSelector.add(2, LongDoorInteractGoal(this, true))
        goalSelector.add(3, LookAroundGoal(this))
        goalSelector.add(3, PeekInChestGoal(this))
        goalSelector.add(3, StandOnOutpostGoal(this))
    }

    override fun initDataTracker() {
        super.initDataTracker()
        this.dataTracker.startTracking(VARIANT, 0)
    }

    override fun interactMob(player: PlayerEntity, hand: Hand): ActionResult {
        this.customer = player

        if(!world.isClient) {
            player.openHandledScreen(
                SimpleNamedScreenHandlerFactory({ syncId: Int, playerInventory: PlayerInventory, playerx: PlayerEntity ->
                    SellerScreenHandler(
                        syncId,
                        playerInventory,
                        this.secretStock
                    )
                }, Text.of("Merchant"))
            );
        }

        return ActionResult.success(world.isClient)
    }

    override fun createChild(world: ServerWorld?, entity: PassiveEntity?): PassiveEntity? {
        TODO("Not yet implemented")
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putLong("SpawnTime", this.spawnTime)
        nbt.putInt("Variant", this.getTypeVariant())
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        if (nbt.contains("SpawnTime", 99))
            spawnTime = nbt.getLong("SpawnTime")

        this.dataTracker.set(VARIANT, nbt.getInt("Variant"))
    }

    override fun moveToWorld(destination: ServerWorld?): Entity? {
        this.customer = null;
        return super.moveToWorld(destination)
    }

    override fun onDeath(damageSource: DamageSource?) {
        super.onDeath(damageSource)
        this.customer = null;
    }

    override fun tick() {
        super.tick()
        tickDespawnDelay()
    }

    override fun canBeLeashedBy(player: PlayerEntity): Boolean {
        return false
    }

    fun getVariant() = SellerVariant.byId(this.getTypeVariant() and 255)
    private fun getTypeVariant(): Int = this.dataTracker.get(VARIANT)
    private fun setVariant(variant: SellerVariant) {
        this.dataTracker.set(VARIANT, variant.id and 255)
    }

    override fun registerControllers(controllers: AnimatableManager.ControllerRegistrar) {
        controllers.add(
            AnimationController(this, "Idle", 5
            ) { state ->
                state.setAndContinue(
                    DefaultAnimations.IDLE
                )
            },
        )
        controllers.add(
            AnimationController(this, "Walk", 5
            ) { state ->
                if(state.isMoving)
                    return@AnimationController state.setAndContinue(DefaultAnimations.WALK);

                return@AnimationController PlayState.STOP
        })
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache {
        return this.cache;
    }

    override fun canImmediatelyDespawn(distanceSquared: Double): Boolean {
        return false
    }

    private fun tickDespawnDelay() {
        if(!this.world.isClient)
            // Needs to take into account a customer while here.
            if(world.time - spawnTime > aliveTimer && this.customer == null)
                discard()
    }

    companion object {
        private val VARIANT = DataTracker.registerData(SellerEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
    }

    class PeekInChestGoal(mob: PathAwareEntity): MoveToTargetPosGoal(mob, 0.35, 8) {
        private var chestPos: BlockPos = BlockPos.ORIGIN
        private var chestValid = false
        private var timerPassed = false
        private var isChestOpen = false
        private var chestTimer = 50 + mob.world.random.nextInt(50)
        private var foundChest = false

        override fun stop() {
            super.stop()
            setChestOpen(0)
            chestValid = false
            timerPassed = false
            chestTimer = 50 + mob.world.random.nextInt(50)
            isChestOpen = false
            foundChest = false
            chestPos = BlockPos.ORIGIN
        }

        override fun tick() {
            super.tick()
            if(this.hasReached()) {
                chestTimer--
                if(chestTimer > 0) {
                    if(!isChestOpen) {
                        this.setChestOpen(1)
                        isChestOpen = true
                    }
                    this.mob.lookControl.lookAt(
                        this.chestPos.x.toDouble(), this.chestPos.y.toDouble(), this.chestPos.z.toDouble())
                } else
                    timerPassed = true
            }
        }

        override fun shouldContinue(): Boolean {
            return !timerPassed && super.shouldContinue()
        }

        override fun getDesiredDistanceToTarget(): Double {
            return 2.0
        }

        override fun isTargetPos(world: WorldView, pos: BlockPos): Boolean {
            return if (!world.isAir(pos.up())) {
                false
            } else {
                val blockState = world.getBlockState(pos)
                if(blockState.isOf(Blocks.CHEST)) {
                    this.chestPos = pos.mutableCopy()
                    foundChest = true
                    this.chestValid = !ChestBlock.isChestBlocked(this.mob.world, this.chestPos)
                    false
                } else if(foundChest &&
                    (world.getBlockState(pos.north()).isOf(Blocks.CHEST) ||
                            world.getBlockState(pos.south()).isOf(Blocks.CHEST) ||
                            world.getBlockState(pos.east()).isOf(Blocks.CHEST) ||
                            world.getBlockState(pos.west()).isOf(Blocks.CHEST))) {
                    true
                } else {
                    false
                }
            }
        }

        private fun setChestOpen(type: Int) {
            if(this.chestValid) {
                if(type == 1)
                    this.mob.world.playSound(
                        null,
                        this.chestPos.x.toDouble(),
                        this.chestPos.y.toDouble(),
                        this.chestPos.z.toDouble(),
                        SoundEvents.BLOCK_CHEST_OPEN,
                        SoundCategory.BLOCKS,
                        0.5f,
                        this.mob.world.random.nextFloat() * 0.1f + 0.9f
                    )
                else if (type == 0)
                    this.mob.world.playSound(
                        null,
                        this.chestPos.x.toDouble(),
                        this.chestPos.y.toDouble(),
                        this.chestPos.z.toDouble(),
                        SoundEvents.BLOCK_CHEST_CLOSE,
                        SoundCategory.BLOCKS,
                        0.5f,
                        this.mob.world.random.nextFloat() * 0.1f + 0.9f
                    )
                this.mob.world.addSyncedBlockEvent(
                    this.chestPos,
                    this.mob.world.getBlockState(this.chestPos).block,
                    1,
                    type)
            }
        }
    }
    class StandOnOutpostGoal(mob: PathAwareEntity): MoveToTargetPosGoal(mob, 0.35, 20) {
        private var chestPos: BlockPos = BlockPos.ORIGIN
        private var timerPassed = false
        private var standTimer = 50 + mob.world.random.nextInt(50)

        override fun stop() {
            super.stop()
            standTimer = 50 + mob.world.random.nextInt(50)
            chestPos = BlockPos.ORIGIN
            timerPassed = false
        }

        override fun tick() {
            super.tick()
            if(this.hasReached()) {
                standTimer--
                if(standTimer < 0) {
                    timerPassed = true
                }
            }
        }

        override fun shouldContinue(): Boolean {
            return !timerPassed && super.shouldContinue()
        }

        override fun getDesiredDistanceToTarget(): Double {
            return 5.0
        }

        override fun isTargetPos(world: WorldView, pos: BlockPos): Boolean {
            return if (!world.isAir(pos.up())) {
                false
            } else {
                val blockState = world.getBlockState(pos).isOf(SellerBlocks.outpostBlock)
                return blockState
            }
        }
    }
}

