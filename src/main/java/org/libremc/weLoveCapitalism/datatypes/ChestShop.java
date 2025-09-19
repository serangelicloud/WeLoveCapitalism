package org.libremc.weLoveCapitalism.datatypes;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.UUID;

public class ChestShop {
    private Sign sign;
    private Chest chest;
    private String owner_uuid;
    private ItemStack item;
    private long price;
    private long gold_storage;
    private int id;

    public ChestShop(int id, Sign sign, Chest chest, String uuid, ItemStack item, long price, long gold_storage) {
        this.sign = sign;
        this.chest = chest;
        this.owner_uuid = uuid;
        this.item = item;
        this.price = price;
        this.gold_storage = gold_storage;
        this.id = id;
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

            if(stack.getType().equals(getItem().getType()) && Objects.equals(stack.getItemMeta(), getItem().getItemMeta())){
                stock += stack.getAmount();
            }
        }

        return stock;
    }

    public int getID() {
        return id;
    }

    public @Nullable UUID getTownUUID(){

        return TownyAPI.getInstance().getTownUUID(sign.getLocation());
    }

    public @Nullable UUID getNationUUID(){
        Town town = TownyAPI.getInstance().getTown(sign.getLocation());

        if(town == null){
            return null;
        }

        if(!town.hasNation()){
            return null;
        }

        Nation nation = town.getNationOrNull();

        if(nation == null){
            return null;
        }
        return nation.getUUID();
    }

}
