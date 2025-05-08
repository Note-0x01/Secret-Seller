package company.fools.secretseller.blocks

import company.fools.secretseller.SecretSeller.outpostEntityType
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.Nameable
import net.minecraft.util.math.BlockPos

class OutpostEntity(pos: BlockPos, state: BlockState?) : BlockEntity(outpostEntityType, pos,
    state
), Nameable {
    private var outpostName: String = "Outpost"
    override fun getName(): Text {
        return Text.of("Outpost")
    }

    fun setOutpostName(name: String) {
        this.outpostName = name
        this.markDirty()
    }

    fun getOutpostName(): String = this.outpostName

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putString("OutpostName", outpostName)
        super.writeNbt(nbt)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        outpostName = nbt.getString("OutpostName") ?: "Outpost"
    }

    override fun getDisplayName(): Text {
        return Text.translatable(cachedState.block.translationKey);
    }
}