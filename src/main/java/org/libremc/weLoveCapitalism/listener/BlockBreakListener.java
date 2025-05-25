package org.libremc.weLoveCapitalism.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.libremc.weLoveCapitalism.ChestShop;
import org.libremc.weLoveCapitalism.ChestShopManager;


public class BlockBreakListener implements Listener {

    @EventHandler
    void onBlockDestroy(BlockBreakEvent event){

        if(TownyAPI.getInstance().isWilderness(event.getBlock())){
            return;
        }

        if(event.getBlock().getType() != Material.CHEST){
            return;
        }

        if(event.getBlock().getType() != Material.OAK_WALL_SIGN){
            return;
        }

        Location block_location = event.getBlock().getLocation();

        ChestShop by_chest = ChestShopManager.getChestShopByChest(event.getBlock());
        ChestShop by_sign = ChestShopManager.getChestShopBySign(event.getBlock());

        if(by_chest != null){
            event.getPlayer().sendMessage("Removed chest shop");
            ChestShopManager.removeChestShop(by_chest);
        }else if(by_sign != null){
            event.getPlayer().sendMessage("Removed chest shop");
            ChestShopManager.removeChestShop(by_sign);
        }

    }
}
