package org.libremc.weLoveCapitalism.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.libremc.weLoveCapitalism.ChestShopManager;

import com.palmergames.bukkit.towny.TownyAPI;

public class BlockPlaceListener implements Listener {
    @EventHandler
    public static void onBlockPlace(BlockPlaceEvent event){

        /* Shops can't be made in the wilderness */
        if(TownyAPI.getInstance().isWilderness(event.getBlockPlaced())){
            return;
        }

        if(event.getBlockAgainst().getType() != Material.CHEST){
            return;
        }

        if(event.getBlockPlaced().getType() != Material.OAK_WALL_SIGN){
            return;
        }


        ChestShopManager.createChestShop(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst());
    }
}
