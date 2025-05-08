package company.fools.secretseller.screen

import company.fools.secretseller.SecretSeller
import company.fools.secretseller.blocks.OutpostEntity
import io.wispforest.owo.client.screens.ScreenUtils
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext

class OutpostScreenHandler(syncId: Int, inventory: PlayerInventory) : ScreenHandler(SecretSeller.outpostScreenHandler, syncId) {
    var timeRemaining: Int = 0
    var context: ScreenHandlerContext = ScreenHandlerContext.EMPTY
    var name: String = "Test"
    var blockEntity: OutpostEntity? = null

    constructor(syncId: Int, inventory: PlayerInventory, blockEntity: OutpostEntity, context: ScreenHandlerContext) : this(syncId, inventory) {
        this.context = context
        this.name = "Outpost"
        this.blockEntity = blockEntity
        this.addServerboundMessage(RenameMessage::class.java, this::handleRename)
        this.addClientboundMessage(UpdateRemainingTimeMessage::class.java, this::handleUpdateRemainingTimeClient)
        this.addServerboundMessage(GetUpdateRemainingTimeMessage::class.java, this::handleUpdateRemainingTimeServer)
    }

    constructor(syncId: Int, inventory: PlayerInventory, buf: PacketByteBuf): this(syncId, inventory) {
        this.name = buf.readString()
        this.addServerboundMessage(RenameMessage::class.java, this::handleRename)
        this.addClientboundMessage(UpdateRemainingTimeMessage::class.java, this::handleUpdateRemainingTimeClient)
        this.addServerboundMessage(GetUpdateRemainingTimeMessage::class.java, this::handleUpdateRemainingTimeServer)
    }

    private fun handleRename(renameMessage: RenameMessage) {
        blockEntity?.setOutpostName(renameMessage.name)
    }


    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        return ScreenUtils.handleSlotTransfer(this, slot, 4);
    }

    fun updateName(name: String) {
        this.name = name
        this.sendMessage(RenameMessage(this.name))
    }

    fun updateRemainingTime() {
        this.sendMessage(GetUpdateRemainingTimeMessage(0))
    }

    private fun handleUpdateRemainingTimeServer(updateRemainingTimeMessage: GetUpdateRemainingTimeMessage) {
        this.timeRemaining = SecretSeller.sellerManager.spawnTimer
        this.sendMessage(UpdateRemainingTimeMessage(this.timeRemaining))
    }

    private fun handleUpdateRemainingTimeClient(updateRemainingTimeMessage: UpdateRemainingTimeMessage) {
        this.timeRemaining = updateRemainingTimeMessage.remaining
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        return true
    }

    @JvmRecord
    data class RenameMessage(val name: String) {}
    @JvmRecord
    data class UpdateRemainingTimeMessage(val remaining: Int) {}
    @JvmRecord
    data class GetUpdateRemainingTimeMessage(val remaining: Int) {}
}