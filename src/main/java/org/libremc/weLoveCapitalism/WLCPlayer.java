package org.libremc.weLoveCapitalism;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public class WLCPlayer {
    private final UUID uuid;
    private boolean creating_shop;
    private ItemStack set_item;

    private ChestShop buying_shop = null; // Shop the player is buying from
    int amount = 1;                       // The amount the player is paying for, a multiplier for the shops amount. Base is 1x

    public WLCPlayer(Player player){
        uuid = player.getUniqueId();
        creating_shop = false;
    }

    public boolean isCreatingShop(){
        return creating_shop;
    }

    public void setCreatingShop(boolean creating_shop) {
        this.creating_shop = creating_shop;
    }

    public UUID  getUUID(){
        return uuid;
    }

    public @Nullable ItemStack getSetItem(){
        return set_item;
    }

    public void setSetItem(ItemStack item){
        this.set_item = item;
    }

    public void setBuyingShop(ChestShop buying_shop) {
        this.buying_shop = buying_shop;
    }

    public ChestShop getBuyingShop() {
        return buying_shop;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
