package org.libremc.weLoveCapitalism;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

public class ChestShopManager {

    public static HashSet<ChestShop> Chestshops;

    public static boolean createChestShop(Player player, Block sign_block, Block chest_block){
        player.sendMessage("Creating chest shop");
        player.sendMessage("Do '/wlc set <amount>' with the item you want to sell in your hand");

        WLCPlayer wlcplayer = WeLoveCapitalism.createWLCPlayer(player);
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

            ChestShop shop = new ChestShop(sign, (Chest)chest_block.getState(), player, item);

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

    public static  @Nullable ChestShop getChestShop(Block sign){
        for(ChestShop chestshop : Chestshops){
            if(chestshop.getSign().getBlock().getLocation().equals(sign.getLocation())){
                return chestshop;
            }
        }

        return null;
    }
}
