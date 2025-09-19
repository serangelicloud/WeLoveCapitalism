package org.libremc.weLoveCapitalism.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.libremc.weLoveCapitalism.TradeManager;
import org.libremc.weLoveCapitalism.datatypes.Upkeep;

import java.util.HashSet;

public class JoinListener implements Listener {
    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Nation nation;

        if((nation = TownyAPI.getInstance().getNation(player)) == null){
            return;
        }

        HashSet<Upkeep> upkeeps = TradeManager.getTradeLawUpkeep(nation);

        if(upkeeps == null){
            return;
        }

        if(upkeeps.isEmpty()){
            return;
        }
        int total = 0;

        for(Upkeep upkeep : upkeeps){
            total += upkeep.getUpkeep();
        }

        if(total > nation.getAccount().getHoldingBalance()){
            player.sendMessage(ChatColor.RED + "Your nation will go into insolvency the next day, as it does not have enough money to pay for it's trade law upkeep!");
            player.sendMessage(ChatColor.RED + "You can pay for your nation's insolvency by doing /wlc insolvency pay");
        }
    }
}
