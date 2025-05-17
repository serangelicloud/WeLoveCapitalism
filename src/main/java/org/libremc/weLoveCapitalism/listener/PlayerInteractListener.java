package org.libremc.weLoveCapitalism.listener;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.libremc.weLoveCapitalism.ChestShop;
import org.libremc.weLoveCapitalism.ChestShopManager;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent event){

        Block sign = event.getClickedBlock();
        Player player = event.getPlayer();

        ChestShop shop = ChestShopManager.getChestShop(sign);

        if(shop == null){
            return;
        }

        event.setCancelled(true);

        if(player.equals(shop.getOwner())){
            ChestShopManager.removeChestShop(shop);
            ChestShopManager.createChestShop(player, shop.getSign().getBlock(), shop.getChest().getBlock());
            return;
        }

        player.sendMessage("Purchase from this humble shop?");

    }
}
