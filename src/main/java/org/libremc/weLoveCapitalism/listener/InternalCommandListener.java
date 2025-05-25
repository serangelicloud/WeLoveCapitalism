package org.libremc.weLoveCapitalism.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.libremc.weLoveCapitalism.ChestShopManager;
import org.libremc.weLoveCapitalism.WLCPlayer;
import org.libremc.weLoveCapitalism.WLCPlayerManager;
import org.libremc.weLoveCapitalism.WeLoveCapitalism;

public class InternalCommandListener implements Listener {
    @EventHandler
    public static void onCommand(PlayerCommandPreprocessEvent event){
        if(event.getMessage().equalsIgnoreCase("/wlc-internal-buy")){

            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());

            if(wlcplayer == null){
                Bukkit.getLogger().severe("[WLC] WLCPlayer is null for " + event.getPlayer());
            }

            ChestShopManager.buyChestShop(wlcplayer.getBuyingShop(), event.getPlayer(), wlcplayer.getAmount());

            wlcplayer.setAmount(1);

            event.setCancelled(true);
        }else if(event.getMessage().equalsIgnoreCase("/wlc-internal-set_amount")){
            event.getPlayer().sendMessage("Use /wlc amount <multiplier> to set your amount");
            event.setCancelled(true);
        }else if(event.getMessage().equalsIgnoreCase("/wlc-internal-collect_earnings")){
            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());

            long storage = wlcplayer.getBuyingShop().getGoldStorage();

            if(storage < 0){
                wlcplayer.getBuyingShop().setGoldStorage(0);
            }

            if(storage == 0){
                event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "You are broke!");
                return;
            }

            WeLoveCapitalism.getEconomy().depositPlayer(event.getPlayer(), wlcplayer.getBuyingShop().getGoldStorage());

            wlcplayer.getBuyingShop().setGoldStorage(0);

            event.getPlayer().sendMessage(ChatColor.AQUA + "Withdrew " + storage + "g " + "from the chest shop");

            event.setCancelled(true);
        }


    }
}
