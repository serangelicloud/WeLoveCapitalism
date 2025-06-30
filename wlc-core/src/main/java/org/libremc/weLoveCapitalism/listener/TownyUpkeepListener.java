package org.libremc.weLoveCapitalism.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.economy.BankAccount;
import org.bukkit.Bukkit;
import org.libremc.weLoveCapitalism.InsolvencyManager;
import org.libremc.weLoveCapitalism.TariffManager;
import org.libremc.weLoveCapitalism.TradeManager;
import org.libremc.weLoveCapitalism.WeLoveCapitalism;
import org.libremc.weLoveCapitalism.datatypes.Upkeep;

import java.util.HashSet;

public class TownyUpkeepListener {
    void onNewDay(NewDayEvent event) {
        for(Nation nation : TownyAPI.getInstance().getNations()){
            if(event.getFallenNations().contains(nation.getName())){
                continue;
            }

            BankAccount account = nation.getAccount();
            int total_upkeep = 0;

            HashSet<Upkeep> upkeeps = TradeManager.getTradeLawUpkeep(nation);

            if(upkeeps == null){
                continue;
            }

            for(Upkeep upkeep : upkeeps){
                total_upkeep += upkeep.getUpkeep();
            }

            if(total_upkeep <= 0){
                continue;
            }

            double current_balance = account.getHoldingBalance(false);
            double after_balance = current_balance - total_upkeep;

            if(after_balance < 0){
                InsolvencyManager.setInsolvent(nation, Math.abs((int)after_balance));
                Bukkit.broadcastMessage(nation.getName() + " has fallen into insolvency!");
            }
        }
    }
}
