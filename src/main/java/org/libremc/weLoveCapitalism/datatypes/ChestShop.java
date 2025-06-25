package org.libremc.weLoveCapitalism.datatypes;

import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.util.StringTokenizer;

public class ChestShop {
    private Sign sign;
    private Chest chest;
    private String owner_uuid;
    private ItemStack item;
    private long price;
    private long gold_storage;

    public ChestShop(Sign sign, Chest chest, String uuid, ItemStack item, long price, long gold_storage) {
        this.sign = sign;
        this.chest = chest;
        this.owner_uuid = uuid;
        this.item = item;
        this.price = price;
        this.gold_storage = gold_storage;
    }

    public ChestShop(Sign sign, Chest chest, String uuid, ItemStack item, long price) {
        this.sign = sign;
        this.chest = chest;
        this.owner_uuid = uuid;
        this.item = item;
        this.price = price;
        this.gold_storage = 0;
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

    public String getOwner() {
        return owner_uuid;
    }

    public void setOwner(String owner_uuid) {
        this.owner_uuid = owner_uuid;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public String getItemFormatted(){
       String base = this.item.getType().name().replace('_', ' ');
       StringTokenizer tok = new StringTokenizer(base, " ");

       String ret = "";

       while(tok.hasMoreElements()){
           ret = ret.concat(tok.nextToken() + " ");
       }

       return ret;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long p) {
        this.price = p;
    }

    public long getGoldStorage() {
        return gold_storage;
    }

    public void addGoldStorage(long gold_storage) {
        this.gold_storage += gold_storage;
    }

    public void removeGoldStorage(long gold_storage) {
        this.gold_storage -= gold_storage;
    }

    public void setGoldStorage(long gold_storage) {
        this.gold_storage = gold_storage;
    }

    public long getStock(){
        long stock = 0;

        for(ItemStack stack : getChest().getInventory().getContents()){

            if(stack == null){
                continue;
            }

            if(stack.getType().equals(getItem().getType()) && stack.getItemMeta().equals(getItem().getItemMeta())){
                stock += stack.getAmount();
            }
        }

        return stock;
    }
}
