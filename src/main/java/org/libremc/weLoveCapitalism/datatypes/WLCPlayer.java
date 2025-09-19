package org.libremc.weLoveCapitalism.datatypes;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.UUID;

public class WLCPlayer {
    private final UUID uuid;
    private boolean creating_shop;

    private Block sign_block; // Sign block of creating shop
    private Block chest_block; // Chest block of creating shop

    private ItemStack set_item;

    private BukkitTask task;

    private ChestShop buying_shop = null; // Shop the player is buying from OR the shop the player is managing if they own it
    private int multiplier = 1;                   // The amount the player is paying for, a multiplier for the shops amount. Base is 1x

    private boolean creation_enabled = true; // If a player wants to create a shop everytime they place a sign on a chest. Can be disabled with /wlc disable on/off

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

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public BukkitTask getTask() {
        return task;
    }

    public @Nullable Block getChestBlock() {
        return chest_block;
    }

    public void setChestBlock(@Nullable Block chest_block) {
        this.chest_block = chest_block;
    }

    public @Nullable Block getSignBlock() {
        return sign_block;
    }

    public void setSignBlock(@Nullable Block sign_block) {
        this.sign_block = sign_block;
    }

    public boolean isCreationEnabled() {
        return creation_enabled;
    }

    public void setCreationEnabled(boolean creation_enabled) {
        this.creation_enabled = creation_enabled;
    }

    /**
     * Sets the chest and sign block to null to make sure that the player
     * cannot do `/wlc set` after placing a sign on a non-chest block
     */
    public void resetShopCreation(){
        setChestBlock(null);
        setSignBlock(null); // maybe problematic
        setCreatingShop(false);
    }
}
