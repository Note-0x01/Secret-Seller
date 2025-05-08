package company.fools.secretseller.blocks

import company.fools.secretseller.StateSaverAndLoader
import company.fools.secretseller.screen.OutpostScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess


class OutpostBlock(settings: Settings) : BlockWithEntity(settings) {
    val shapeNS: VoxelShape = Block.createCuboidShape(-2.0, 0.0, 0.0, 18.0, 10.0, 16.0)
    val shapeEW: VoxelShape = Block.createCuboidShape(0.0, 0.0, -2.0, 16.0, 10.0, 18.0)

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return OutpostEntity(pos, state)
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        if(!world.isClient) {
            val stateSave = StateSaverAndLoader.getServerState(world.server!!)
            stateSave.addToOutposts(pos)
            placer!!.sendMessage(Text.of("Added known outpost at: $pos"))
            for(p in stateSave.outposts) {
                placer.sendMessage(Text.of("Known outpost at: $p"))
            }
        }
    }

    override fun onBroken(world: WorldAccess, pos: BlockPos, state: BlockState) {
        if(!world.isClient) {
            val stateSave = StateSaverAndLoader.getServerState(world.server!!)
            stateSave.removeFromOutposts(pos)
        }
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        val direction = ctx!!.horizontalPlayerFacing.opposite
        return this.defaultState.with(Companion.facing, direction)
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(Companion.facing)
    }

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        val direction = state?.get(facing)
        return when(direction) {
            Direction.NORTH -> shapeNS
            Direction.SOUTH -> shapeNS
            Direction.WEST -> shapeEW
            Direction.EAST -> shapeEW
            else -> shapeNS
        }
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if(!world.isClient) {
            val blockEntity = world.getBlockEntity(pos)
            if(blockEntity is OutpostEntity) {
                player.openHandledScreen(object : ExtendedScreenHandlerFactory {
                    override fun getDisplayName(): Text {
                        return blockEntity.name
                    }

                    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                        buf.writeString(blockEntity.getOutpostName())
                    }

                    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                        return OutpostScreenHandler(
                            syncId,
                            inv,
                            blockEntity,
                            ScreenHandlerContext.create(world, player.blockPos)
                        )
                    }
                })
            }
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    companion object {
        val facing = HorizontalFacingBlock.FACING
    }
}