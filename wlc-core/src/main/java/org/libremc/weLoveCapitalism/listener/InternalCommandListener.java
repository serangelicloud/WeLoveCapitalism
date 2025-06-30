package org.libremc.weLoveCapitalism.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.libremc.weLoveCapitalism.*;
import org.libremc.weLoveCapitalism.datatypes.WLCPlayer;

import java.sql.SQLException;

public class InternalCommandListener implements Listener {
    @EventHandler
    public static void onCommand(PlayerCommandPreprocessEvent event) throws SQLException {
        if(event.getMessage().equalsIgnoreCase("/wlc-internal-buy")){

            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());

            if(wlcplayer == null){
                Bukkit.getLogger().severe("[WLC] WLCPlayer is null for " + event.getPlayer());
                return;
            }

            ChestShopManager.buyChestShop(wlcplayer.getBuyingShop(), event.getPlayer(), wlcplayer.getMultiplier());

            event.setCancelled(true);
        }else if(event.getMessage().equalsIgnoreCase("/wlc-internal-set_amount")){
            event.getPlayer().sendMessage("Use /wlc multiplier <multiplier> to set your multiplier");
            event.setCancelled(true);
        }else if(event.getMessage().equalsIgnoreCase("/wlc-internal-collect_earnings")){
            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());

            long storage = wlcplayer.getBuyingShop().getGoldStorage();

            if(storage < 0){
                wlcplayer.getBuyingShop().setGoldStorage(0);
            }

            if(storage == 0){
                event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "You are broke!");
                event.setCancelled(true);
                return;
            }

            WeLoveCapitalism.getEconomy().depositPlayer(event.getPlayer(), wlcplayer.getBuyingShop().getGoldStorage());

            wlcplayer.getBuyingShop().setGoldStorage(0);

            Log.playerMessage(event.getPlayer(), "Withdrew " + storage + "g " + "from the chest shop");

            event.setCancelled(true);
        }else if(event.getMessage().equalsIgnoreCase("/wlc-internal-view_item")){
            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());
            ItemGUI inv = new ItemGUI(wlcplayer.getBuyingShop().getItem());
            inv.openInventory(event.getPlayer());

            event.setCancelled(true);
        }else if(event.getMessage().equalsIgnoreCase("/wlc-internal-delete_shop")){
            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());
            ChestShopManager.removeChestShop(wlcplayer.getBuyingShop());

            Log.playerMessage(event.getPlayer(), "Removed your shop");

            event.setCancelled(true);
        }


    }
}
