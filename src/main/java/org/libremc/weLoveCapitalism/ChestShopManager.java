package org.libremc.weLoveCapitalism;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class ChestShopManager {

    public static HashSet<ChestShop> Chestshops;

    public static boolean createChestShop(Player player, Block sign_block, Block chest_block){
        player.sendMessage("Creating chest shop");
        player.sendMessage("Do '/wlc set <amount>' with the item you want to sell in your hand");

        WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);
        wlcplayer.setCreatingShop(true);
        wlcplayer.setSetItem(null); // Reset any previous item

        // Schedule a task to check after 10 seconds
        Bukkit.getScheduler().runTaskLater(Bukkit.getServer().getPluginManager().getPlugin("WeLoveCapitalism"), () -> {
            if (!wlcplayer.isCreatingShop()) {
                return;
            }

            ItemStack item = wlcplayer.getSetItem();
            if (item == null) {
                player.sendMessage("Timed out - you didn't set an item in time!");
                wlcplayer.setCreatingShop(false);
                return;
            }

            // Process the shop creation
            Sign sign = (Sign) sign_block.getState();
            writeToSign(sign, item);

            player.sendMessage("Done!");

            ChestShop shop = new ChestShop(sign, (Chest)chest_block.getState(), player.getUniqueId().toString(), item, 100, 0);

            ChestShopManager.addChestShop(shop);

            wlcplayer.setCreatingShop(false);
        }, 20 * 10); // 10 seconds

        return true;
    }

    public static void addChestShop(ChestShop chestshop){
        Chestshops.add(chestshop);
        try {
            Database.writeChestShop(chestshop);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeToSign(Sign sign, ItemStack item){
        sign.getSide(Side.FRONT).setLine(0, "Chest shop");
        sign.getSide(Side.FRONT).setLine(1, item.getType().name().replace('_', ' '));
        sign.getSide(Side.FRONT).setLine(3, item.getAmount() + "x");
        sign.update();
    }

    public static void removeChestShop(ChestShop chestshop){
        Chestshops.remove(chestshop);
    }

    public static  @Nullable ChestShop getChestShopBySign(Block sign){
        for(ChestShop chestshop : Chestshops){
            if(chestshop.getSign().getBlock().getLocation().equals(sign.getLocation())){
                return chestshop;
            }
        }

        return null;
    }

    public static  @Nullable ChestShop getChestShopByChest(Block chest){
        for(ChestShop chestshop : Chestshops){
            if(chestshop.getChest().getBlock().getLocation().equals(chest.getLocation())){
                return chestshop;
            }
        }

        return null;
    }

    public static void buyChestShop(ChestShop shop, Player player, int amount){
        Inventory chest_inventory = shop.getChest().getInventory();
        Inventory player_inventory = player.getInventory();
        int stock = 0;
        long required_gold = amount * shop.getPrice();

        for(ItemStack stack : chest_inventory.getContents()){

            if(stack == null){
                continue;
            }
            
            if(stack.getType().equals(shop.getItem().getType())){
                stock += stack.getAmount();
            }
        }

        if(stock < amount * shop.getItem().getAmount()){
            player.sendMessage("Shop doesn't have enough stock. Only has " + stock + " in stock");
            return;
        }

        double player_gold = WeLoveCapitalism.getEconomy().getBalance(player);

        if(player_gold < required_gold){
            player.sendMessage("You don't have enough gold!");
            player.sendMessage("You have " + (int)player_gold + "g, but require " + required_gold + "g!");
            return;
        }

        ItemStack item = new ItemStack(shop.getItem().getType(), shop.getItem().getAmount() * amount);

        item.setItemMeta(shop.getItem().getItemMeta()); // Restore NBT data

        HashMap<Integer, ItemStack> map = player_inventory.addItem(item);

        if(!map.isEmpty()){
            player.sendMessage("You don't have enough space in your inventory!");
            return;
        }

        WeLoveCapitalism.getEconomy().withdrawPlayer(player, required_gold);

        ItemStack stack_to_remove = new ItemStack(shop.getItem().getType(), shop.getItem().getAmount() * amount);
        chest_inventory.removeItem(stack_to_remove);

        player.sendMessage(ChatColor.AQUA + "Bought " + amount * shop.getItem().getAmount() + "x of " + shop.getItemFormatted());

        long received_gold = required_gold;

        shop.addGoldStorage(received_gold);



    }

}
