package org.libremc.weLoveCapitalism.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.libremc.weLoveCapitalism.ChestShopManager;

import com.palmergames.bukkit.towny.TownyAPI;
import org.libremc.weLoveCapitalism.datatypes.WLCPlayer;
import org.libremc.weLoveCapitalism.WLCPlayerManager;

public class BlockPlaceListener implements Listener {
    @EventHandler
    public static void onBlockPlace(BlockPlaceEvent event){

        WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(event.getPlayer());

        /* Shops can't be made in the wilderness */
        if(TownyAPI.getInstance().isWilderness(event.getBlockPlaced())){
            return;
        }

        if(event.getBlockAgainst().getType() != Material.CHEST){
            wlcplayer.setChestBlock(event.getBlockAgainst());
            return;
        }

        if(event.getBlockPlaced().getType() != Material.OAK_WALL_SIGN){
            return;
        }

        wlcplayer.setSignBlock(event.getBlockPlaced());
        wlcplayer.setChestBlock(event.getBlockAgainst());

        ChestShopManager.createChestShop(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst());
    }

    @EventHandler
    public static void onSignOpen(PlayerSignOpenEvent event){
        WLCPlayer player = WLCPlayerManager.createWLCPlayer(event.getPlayer());

        if(player.getSignBlock() == null){
            return;
        }

        if(player.getSignBlock().equals(event.getSign().getBlock())){
            event.setCancelled(true);
        }
    }
}
