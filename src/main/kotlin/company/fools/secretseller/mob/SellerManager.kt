package company.fools.secretseller.mob

import company.fools.secretseller.Merchantry
import company.fools.secretseller.SecretSeller
import company.fools.secretseller.StateSaverAndLoader
import company.fools.secretseller.blocks.OutpostEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.SpawnRestriction
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.GameRules
import net.minecraft.world.Heightmap
import net.minecraft.world.SpawnHelper
import net.minecraft.world.WorldView
import net.minecraft.world.spawner.Spawner

class SellerManager: Spawner {
    var spawnTimer = 100
    private val random = Random.create()

    override fun spawn(world: ServerWorld, spawnMonsters: Boolean, spawnAnimals: Boolean): Int {
        --this.spawnTimer

        if(this.spawnTimer <= 0) {
            return if (!world.gameRules.getBoolean(GameRules.DO_MOB_SPAWNING)) {
                0
            } else {
                this.trySpawn(world)
                this.spawnTimer = 100
                1
            }
        }
        return 0
    }

    private fun trySpawn(world: ServerWorld): Boolean {
        val stateSave = StateSaverAndLoader.getServerState(world.server)

        val playerEntity = world.randomAlivePlayer ?: return false
        var blockPos = playerEntity.blockPos
        val loadedOutposts = stateSave.outposts.filter { world.isChunkLoaded(it) }

        if(loadedOutposts.isNotEmpty()) {
            blockPos = loadedOutposts[random.nextInt(loadedOutposts.size)]
            world.server.playerManager.playerList.forEach {
                it.sendMessage(Text.of("A Secret Seller arrived at ${(world.getBlockEntity(blockPos) as OutpostEntity).getOutpostName()}!")) }
            blockPos = blockPos.up()
        } else {
            world.server.playerManager.playerList.forEach {
                it.sendMessage(Text.of("A Secret Seller arrived somewhere near ${playerEntity.name.string}!")) }
            blockPos = getNearbySpawnPos(world, blockPos, 100)
        }

        SecretSeller.seller.spawn(world, blockPos, SpawnReason.EVENT)
        return true
    }

    private fun getNearbySpawnPos(world: WorldView, pos: BlockPos, range: Int): BlockPos {
        var blockPos: BlockPos = pos
        for (i in 0..9) {
            val j = pos.x + random.nextInt(range * 2) - range
            val k = pos.z + random.nextInt(range * 2) - range
            val l = world.getTopY(Heightmap.Type.WORLD_SURFACE, j, k)
            val blockPos2 = BlockPos(j, l, k)
            if (SpawnHelper.canSpawn(
                    SpawnRestriction.Location.ON_GROUND,
                    world,
                    blockPos2,
                    SecretSeller.seller
                )
            ) {
                blockPos = blockPos2
                break
            }
        }
        return blockPos
    }
}