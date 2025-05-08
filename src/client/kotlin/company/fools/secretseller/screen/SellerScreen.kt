package company.fools.secretseller.screen

import company.fools.secretseller.SecretSeller
import company.fools.secretseller.SecretSeller.merchantry
import company.fools.secretseller.StateSaverAndLoader.Companion.getServerState
import company.fools.secretseller.items.SellerItems
import company.fools.secretseller.screen.SellerScreenHandler.GetMerchantPricesMessage
import company.fools.secretseller.screen.SellerScreenHandler.MerchantStockMessage
import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*


class SellerScreen(handler: SellerScreenHandler, inventory: PlayerInventory, title: Text): BaseOwoHandledScreen<FlowLayout, SellerScreenHandler>(
    handler, inventory, title
) {

    val inventory = inventory;

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, Containers::verticalFlow)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)

        val stack = Containers.stack(Sizing.fixed(176), Sizing.fixed(166))
        stack.child(Components.texture(Identifier("secret-seller","textures/gui/merchant_gui.png"), 0, 0, 176, 166, 176, 166))
        stack.child(Components.item(ItemStack(SellerItems.goldCoin)).positioning(Positioning.absolute(125, 50)))
        stack.child(Components.label(Text.of(inventory.count(SellerItems.goldCoin).toString())).positioning(Positioning.absolute(140, 55)))

        rootComponent.child(stack)
    }

    override fun drawMouseoverTooltip(context: DrawContext, x: Int, y: Int) {
        if (handler.cursorStack.isEmpty && focusedSlot != null && focusedSlot!!.hasStack()) {
            val itemStack = focusedSlot!!.stack
            tooltipWithPrice(context, itemStack, x, y)
        }
    }

    private fun tooltipWithPrice(context: DrawContext, itemStack: ItemStack, x: Int, y: Int) {
        this.handler.sendMessage(GetMerchantPricesMessage())

        val toolTip = this.getTooltipFromItem(itemStack)
        val price = merchantry.getPrice(this.handler.merchantPrices, itemStack.item)
        if(price == 0)
            toolTip.add(1, Text.of("No Price"))
        else
            toolTip.add(1, Text.of("$price Gold"))

        context.drawTooltip(textRenderer, toolTip, itemStack.tooltipData, x, y)
    }
}