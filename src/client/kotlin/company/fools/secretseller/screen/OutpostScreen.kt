package company.fools.secretseller.screen

import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import java.text.SimpleDateFormat
import java.util.*

class OutpostScreen(handler: OutpostScreenHandler, inventory: PlayerInventory, title: Text): BaseOwoHandledScreen<FlowLayout, OutpostScreenHandler>(
    handler, inventory, Text.of("")
) {
    private var timeRemaining: LabelComponent? = null;
    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, Containers::verticalFlow)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)

        val container = Containers.verticalFlow(Sizing.content(), Sizing.content())
        val textBox = Components.textBox(Sizing.fill(40), handler.name)
        textBox.onChanged().subscribe {
            this.handler.updateName(it)
        }
        container.child(textBox).padding(Insets.of(10))
        rootComponent.child(container)

        val container2 = Containers.verticalFlow(Sizing.content(), Sizing.content())

        timeRemaining = Components.label(Text.of("Next secret seller arrives in: " + this.handler.timeRemaining.toString()))
        container2.child(timeRemaining).padding(Insets.of(10))

        rootComponent.child(container2)
    }

    override fun handledScreenTick() {
        super.handledScreenTick()
        this.handler.updateRemainingTime()
        val timeInSeconds = this.handler.timeRemaining.toLong() / 20
        val timeString = SimpleDateFormat("mm:ss").format(Date(timeInSeconds * 1000))
        timeRemaining!!.text(Text.of("Next secret seller arrives in: $timeString"))
    }
}