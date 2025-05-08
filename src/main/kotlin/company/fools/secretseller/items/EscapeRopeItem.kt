package company.fools.secretseller.items

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

class EscapeRopeItem(settings: Settings) : Item(settings) {
    private var charged = false
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        user.setCurrentHand(hand)
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        if(!world.isClient) {
            if(remainingUseTicks == 1) {
                for(x in 1..200)
                    world.addParticle(ParticleTypes.LARGE_SMOKE, user.x + world.random.nextDouble(), user.y + world.random.nextDouble() * world.random.nextInt(5), user.z + world.random.nextDouble(), 0.0, 0.0, 0.0);

                val serverPlayerEntity = user as ServerPlayerEntity
                val serverWorld = world as ServerWorld

                val spawnPointPosition = serverPlayerEntity.spawnPointPosition ?: serverWorld.spawnPos

                var position = PlayerEntity.findRespawnPosition(
                    serverWorld.server.getWorld(serverPlayerEntity.spawnPointDimension),
                    spawnPointPosition, 0F, true, true)

                if(position.isEmpty)
                    position = Optional.of(Vec3d(spawnPointPosition.x.toDouble(), spawnPointPosition.y.toDouble(), spawnPointPosition.z.toDouble()))

                if (!user.abilities.creativeMode) {
                    user.getStackInHand(user.activeHand).decrement(1)
                }

                serverPlayerEntity.teleport(
                    serverWorld.server.getWorld(serverPlayerEntity.spawnPointDimension),
                    position.get().x, position.get().y, position.get().z,
                    user.yaw, user.pitch)
            } else {
                world.addParticle(ParticleTypes.LARGE_SMOKE, user.x + world.random.nextDouble(), user.y + world.random.nextDouble() * world.random.nextInt(5), user.z + world.random.nextDouble(), 0.0, 0.0, 0.0);
            }
        }
        if(world.isClient) {
            if(remainingUseTicks == 1) {
                for(x in 1..200)
                    world.addParticle(ParticleTypes.LARGE_SMOKE, user.x + world.random.nextDouble(), user.y + world.random.nextDouble() * world.random.nextInt(5), user.z + world.random.nextDouble(), 0.0, 0.0, 0.0);
            } else {
                world.addParticle(ParticleTypes.LARGE_SMOKE, user.x + world.random.nextDouble(), user.y + world.random.nextDouble() * world.random.nextInt(5), user.z + world.random.nextDouble(), 0.0, 0.0, 0.0);
            }
        }
    }

    override fun getUseAction(stack: ItemStack?): UseAction {
        return UseAction.CROSSBOW
    }

    override fun onStoppedUsing(stack: ItemStack?, world: World?, user: LivingEntity?, remainingUseTicks: Int) {
        charged = false
    }

    override fun getMaxUseTime(stack: ItemStack): Int {
        return 25
    }
}