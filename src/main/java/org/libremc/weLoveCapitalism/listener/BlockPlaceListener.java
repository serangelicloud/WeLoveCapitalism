package org.libremc.weLoveCapitalism.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.libremc.weLoveCapitalism.ChestShopManager;

import com.palmergames.bukkit.towny.TownyAPI;
import org.libremc.weLoveCapitalism.datatypes.WLCPlayer;
import org.libremc.weLoveCapitalism.WLCPlayerManager;

public class BlockPlaceListener implements Listener {
    @EventHandler
    public static void onBlockPlace(BlockPlaceEvent event){

        WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(event.getPlayer());

        if(!wlcplayer.isCreationEnabled()){
            wlcplayer.resetShopCreation();
            return;
        }

        /* Shops can't be made in the wilderness */
        if(TownyAPI.getInstance().isWilderness(event.getBlockPlaced())){
            wlcplayer.resetShopCreation();
            return;
        }

        if(event.getBlockAgainst().getType() != Material.CHEST){
            wlcplayer.resetShopCreation();
            return;
        }

        if(event.getBlockPlaced().getType() != Material.OAK_WALL_SIGN){
            wlcplayer.resetShopCreation();
            return;
        }

        if(ChestShopManager.getChestShopByChest(event.getBlockAgainst()) != null){
            wlcplayer.resetShopCreation();
            return;
        }

        wlcplayer.setSignBlock(event.getBlockPlaced());
        wlcplayer.setChestBlock(event.getBlockAgainst());

        /* Check if this is a double chest and the player is trying to create another shop in an already existing one */
        Block chestBlock = event.getBlockAgainst();
        InventoryHolder holder = ((InventoryHolder)chestBlock.getState()).getInventory().getHolder();

        if (holder instanceof DoubleChest double_chest) {
            Chest left = (Chest)double_chest.getLeftSide();
            Chest right = (Chest)double_chest.getRightSide();

            assert left != null;
            assert right != null;

            if (ChestShopManager.getChestShopByChest(left.getBlock()) != null ||
                    ChestShopManager.getChestShopByChest(right.getBlock()) != null) {
                wlcplayer.resetShopCreation();
                return;
            }

            for(WLCPlayer player : WLCPlayerManager.getWLCPlayers()){
                if(player.isCreatingShop()){

                    if(player.getChestBlock() == null){
                        continue;
                    }

                    if(player.getChestBlock().equals(left.getBlock()) || player.getChestBlock().equals(right.getBlock())){
                        wlcplayer.resetShopCreation();
                        return;
                    }
                }
            }
        }

        for(WLCPlayer player : WLCPlayerManager.getWLCPlayers()){
            if(player.isCreatingShop()){

                if(player.getChestBlock() == null){
                    continue;
                }

                if(chestBlock.equals(player.getChestBlock())){
                    wlcplayer.resetShopCreation();
                    return;
                }
            }
        }

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
