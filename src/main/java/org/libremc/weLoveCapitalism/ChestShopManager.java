package org.libremc.weLoveCapitalism;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.libremc.weLoveCapitalism.datatypes.ChestShop;
import org.libremc.weLoveCapitalism.datatypes.Trade;
import org.libremc.weLoveCapitalism.datatypes.WLCPlayer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class ChestShopManager {

    public static HashSet<ChestShop> Chestshops;


    public static boolean createChestShop(Player player, Block sign_block, Block chest_block){
        Log.playerMessage(player, "Creating chestshop");
        Log.playerMessage(player, "Do '/wlc set <amount> <price>' with the item you want to sell in your hand");

        WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);
        wlcplayer.setCreatingShop(true);
        wlcplayer.setSetItem(null); // Reset any previous item

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

        }, 20 * 30); // 30 seconds

        wlcplayer.setTask(task);

        return true;
    }

    public static void addChestShop(ChestShop chestshop){
        Chestshops.add(chestshop);
        try {
            ChestShopDatabase.writeChestShop(chestshop);
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
            ChestShopDatabase.deleteChestShop(chestshop);
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
        long base_gold = required_gold;
        if(player_nation != null){
            int tariff = TariffManager.getTariffPercentage(shop, player_nation);
            if(tariff != 0){
                required_gold = (long)(Math.ceil(shop.getPrice() * (((double)tariff / 100) + 1)));
            }
        }

        long stock = shop.getStock();

        if(stock < (long) amount * shop.getItem().getAmount()){
            Log.playerMessage(player, "Shop doesn't have enough stock. Only has " + stock + " in stock");
            return;
        }

        double player_gold = WeLoveCapitalism.getEconomy().getBalance(player);

        if(player_gold < required_gold){
            Log.playerMessage(player,"You don't have enough gold!");
            Log.playerMessage(player,"You have " + (int)player_gold + "g, but require " + required_gold + "g!");
            return;
        }

        if(!shop.getChest().getInventory().getViewers().isEmpty()){
            Log.playerMessage(player,"You can't buy from an open chestshop!");
            return;
        }

        ItemStack item = new ItemStack(shop.getItem().getType(), shop.getItem().getAmount() * amount);

        item.setItemMeta(shop.getItem().getItemMeta()); // Restore NBT data

        HashMap<Integer, ItemStack> map = player_inventory.addItem(item);

        if(!map.isEmpty()){
            for(var stack : map.entrySet()){
                Item _item = player.getWorld().dropItem(player.getLocation(), stack.getValue());
                _item.setOwner(player.getUniqueId());
            }

            Log.playerMessage(player,"You didn't have enough space, so some items have been dropped on the ground!");
            Log.playerMessage(player, "(Don't worry, only you can pick them up)");
        }

        ItemStack stack_to_remove = new ItemStack(shop.getItem().getType(), shop.getItem().getAmount() * amount);

        stack_to_remove.setItemMeta(item.getItemMeta());

        chest_inventory.removeItem(stack_to_remove);

        WeLoveCapitalism.getEconomy().withdrawPlayer(player, required_gold);

        Log.playerMessage(player,ChatColor.AQUA + "Bought " + amount * shop.getItem().getAmount() + "x of " + shop.getItemFormatted());

        shop.addGoldStorage(base_gold);

        Town player_town = TownyAPI.getInstance().getTown(player);
        UUID player_town_uuid = player_town == null ? null : player_town.getUUID();
        UUID player_nation_uuid = player_nation == null ? null : player_nation.getUUID();

        if(player_nation != null){
            int to_pay = Math.toIntExact(required_gold - base_gold);

            if(to_pay > 0){
                int balance_after = (int) player_nation.getAccount().getHoldingBalance(false) + to_pay;
                player_nation.getAccount().setBalance(balance_after, "Tariff paid");
            }


        }

        Trade trade;
        try {
            trade = new Trade(shop.getID(), ChestShopDatabase.serializeItemStack(item), amount, (int) base_gold, UUID.fromString(shop.getOwner()), player.getUniqueId(), shop.getTownUUID(), shop.getNationUUID(),  player_town_uuid, player_nation_uuid, System.currentTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TradeManager.addTrade(trade);

    }

    public static void saveChestShops() throws SQLException, IOException {
        /* Rewrite every chestshop to the database if anything has changed */
        for(ChestShop shop : Chestshops){
            ChestShopDatabase.deleteChestShop(shop);
            ChestShopDatabase.writeChestShop(shop);
        }
    }

}
