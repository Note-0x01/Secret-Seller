package company.fools.secretseller.screen;

import company.fools.secretseller.SecretSeller;
import company.fools.secretseller.StateSaverAndLoader;
import company.fools.secretseller.items.SellerItems;
import company.fools.secretseller.mob.SellerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class SellerScreenHandler extends ScreenHandler {

    private final SellerInventory sellerInventory;
    public HashMap<String, Integer> merchantPrices;
    public record MerchantStockMessage(Map<String, Integer> merchantPrices) {}
    public record GetMerchantPricesMessage() {}
    public record PurchaseItemUpdate(String itemKey) {}

    public SellerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }
    public SellerScreenHandler(int syncId, PlayerInventory playerInventory, SellerInventory inventory) {
        super(SecretSeller.INSTANCE.getMerchantScreenHandler(), syncId);

        this.sellerInventory = Objects.requireNonNullElseGet(inventory, SellerInventory::new);
        this.merchantPrices = new HashMap<>();

        //Buy Slot
        this.addSlot(new Slot(this.sellerInventory, 10, 68, 50));

        //Payment Slot
        this.addSlot(new Slot(this.sellerInventory, 11, 108, 50));

        //Merchant inventory
        int m;
        int l;
        for (m = 0; m < 2; ++m) {
            for (l = 0; l < 5; ++l) {
                this.addSlot(new SellerSlot(this.sellerInventory, m*5 + l, 68 + l * 20, 8 + m * 20));
            }
        }

        // The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        // The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }

        this.addServerboundMessage(GetMerchantPricesMessage.class, this::requestPrices);
        this.addServerboundMessage(PurchaseItemUpdate.class, this::updateItem);
        this.addClientboundMessage(MerchantStockMessage.class, this::updateClientPrices);
    }

    public void updateClientPrices(MerchantStockMessage message) {
        this.merchantPrices = (HashMap<String, Integer>) message.merchantPrices;
    }

    public void requestPrices(GetMerchantPricesMessage message) {
        this.merchantPrices = (HashMap<String, Integer>) SecretSeller.INSTANCE.getMerchantry().getPrices(
                StateSaverAndLoader.getServerState(Objects.requireNonNull(this.player().getServer())));
        sendMessage(new MerchantStockMessage(this.merchantPrices));
    }

    public void updateItem(PurchaseItemUpdate message) {
        SecretSeller.INSTANCE.getMerchantry().purchaseItem(
                StateSaverAndLoader.getServerState(Objects.requireNonNull(this.player().getServer())),
                message.itemKey);

        requestPrices(new GetMerchantPricesMessage());
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if(player.getWorld().isClient)
            sendMessage(new GetMerchantPricesMessage());

        if(this.isValid(slotIndex))
            if(slotIndex == -1 || slotIndex == -999)
                super.onSlotClick(slotIndex, button, actionType, player);
            else if(slotIndex > 11 || slotIndex < 2) {
                super.onSlotClick(slotIndex, button, actionType, player);
            } else if(!this.getSlot(slotIndex).getStack().isEmpty()) {
                ItemStack slot = this.getSlot(slotIndex).getStack().copy();
                Predicate<ItemStack> goldCoins = item -> (item.isOf(SellerItems.Companion.getGoldCoin()));

                int itemCost = merchantPrices.get(Registries.ITEM.getId(slot.getItem()).toString());
                int goldCount = player.getInventory().count(SellerItems.Companion.getGoldCoin());

                if(!(itemCost > goldCount)) {
                    if(player.getWorld().isClient) {
                        sendMessage(new PurchaseItemUpdate(Registries.ITEM.getId(slot.getItem()).toString()));
                        sendMessage(new GetMerchantPricesMessage());
                    }

                    player.getInventory().remove(goldCoins, itemCost, player.getInventory());
                    if(this.getCursorStack().isEmpty())
                        this.setCursorStack(slot);
                    else if(ItemStack.canCombine(slot, this.getCursorStack())) {
                        if(this.getCursorStack().getCount() + slot.getCount() < slot.getMaxCount())
                            this.getCursorStack().increment(slot.getCount());
                    }
                }
            }
    }


    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;

        return newStack;
    }
}