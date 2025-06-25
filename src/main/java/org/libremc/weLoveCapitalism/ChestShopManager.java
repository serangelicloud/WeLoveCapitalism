package org.libremc.weLoveCapitalism;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.libremc.weLoveCapitalism.datatypes.ChestShop;
import org.libremc.weLoveCapitalism.datatypes.WLCPlayer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class ChestShopManager {

    public static HashSet<ChestShop> Chestshops;


    public static boolean createChestShop(Player player, Block sign_block, Block chest_block){
        player.sendMessage("Creating chest shop");
        player.sendMessage("Do '/wlc set <amount> <price>' with the item you want to sell in your hand");

        WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);
        wlcplayer.setCreatingShop(true);
        wlcplayer.setSetItem(null); // Reset any previous item

        // Schedule a task to check after 15 seconds
        BukkitTask task = Bukkit.getScheduler().runTaskLater(WeLoveCapitalism.getInstance(), () -> {
            if (!wlcplayer.isCreatingShop()) {
                return;
            }

            ItemStack item = wlcplayer.getSetItem();
            if (item == null) {
                player.sendMessage("Timed out - you didn't set an item in time!");
                wlcplayer.setCreatingShop(false);
                return;
            }

        }, 20 * 15); // 15 seconds

        wlcplayer.setTask(task);

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

    public static void writeToSign(Sign sign, Player player){
        sign.getSide(Side.FRONT).setLine(0, ChatColor.LIGHT_PURPLE + player.getName() + "'s");
        sign.getSide(Side.FRONT).setLine(2, "Chest shop");
        sign.getSide(Side.FRONT).setGlowingText(true);
        sign.update();
    }

    public static void removeChestShop(ChestShop chestshop){
        try {
            Database.deleteChestShop(chestshop);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    public static void buyChestShop(ChestShop shop, Player player, int amount) throws SQLException {
        Inventory chest_inventory = shop.getChest().getInventory();
        Inventory player_inventory = player.getInventory();
        Nation player_nation = TownyAPI.getInstance().getNation(player);
        long required_gold = amount * shop.getPrice();
        if(player_nation != null){
            int tariff = TariffManager.getTariffPercentage(shop, player_nation);
            if(tariff != 0){
                required_gold = (long)(Math.ceil(shop.getPrice() * (((double)tariff / 100) + 1)));
            }
        }

        long stock = shop.getStock();

        if(stock < (long) amount * shop.getItem().getAmount()){
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

        stack_to_remove.setItemMeta(item.getItemMeta());

        chest_inventory.removeItem(stack_to_remove);

        player.sendMessage(ChatColor.AQUA + "Bought " + amount * shop.getItem().getAmount() + "x of " + shop.getItemFormatted());

        long received_gold = required_gold;

        shop.addGoldStorage(received_gold);



    }

}
