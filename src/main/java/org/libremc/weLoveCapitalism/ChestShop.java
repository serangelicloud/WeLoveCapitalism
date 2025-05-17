package org.libremc.weLoveCapitalism;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChestShop {
    private Sign sign;
    private Chest chest;
    private Player owner;
    private ItemStack item;

    public ChestShop(Sign sign, Chest chest, Player owner, ItemStack item) {
        this.sign = sign;
        this.chest = chest;
        this.owner = owner;
        this.item = item;
    }

    public Sign getSign() {
        return sign;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
    }

    public Chest getChest() {
        return chest;
    }

    public void setChest(Chest chest) {
        this.chest = chest;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
