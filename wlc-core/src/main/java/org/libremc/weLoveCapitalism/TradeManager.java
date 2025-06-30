package org.libremc.weLoveCapitalism;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.libremc.weLoveCapitalism.datatypes.Embargo;
import org.libremc.weLoveCapitalism.datatypes.Tariff;
import org.libremc.weLoveCapitalism.datatypes.Trade;
import org.libremc.weLoveCapitalism.datatypes.Upkeep;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

public class TradeManager {

    public static HashSet<Trade> Trades;

    public static void addTrade(Trade trade){
        Trades.add(trade);

        try {
            TradesDatabase.writeTrade(trade);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void removeTrade(Trade trade){
        Trades.remove(trade);

        try {
            TradesDatabase.removeTrade(trade.getTradeId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /* Gets the total foreign trade that a Government with `government_uuid` UUID made in the `time` timeframe in milliseconds */
    public static int getTotalForeignTrade(UUID government_uuid, long time) {
        long current_time = System.currentTimeMillis();
        Town town = TownyAPI.getInstance().getTown(government_uuid);
        Nation nation = TownyAPI.getInstance().getNation(government_uuid);

        int ret = 0;

        if (town != null) {
            for (Trade trade : Trades) {
                if (trade.getTownUuid().equals(town.getUUID())) {
                    if ((current_time - trade.getTimestamp()) > time) {
                        continue;
                    }

                    ret += trade.getPrice();

                }
            }
        } else if (nation != null) {
            for (Trade trade : Trades) {
                if (trade.getNationUuid().equals(nation.getUUID())) {
                    if ((current_time - trade.getTimestamp()) > time) {
                        continue;
                    }

                    ret += trade.getPrice();

                }
            }
        } else {
            return -1;
        }

        return ret;


    }

    /* Returns all the upkeep that `nation` has to pay */
    public static HashSet<Upkeep> getTradeLawUpkeep(Nation nation) {
        HashSet<Upkeep> ret = new HashSet<>();

        HashSet<Tariff> tariffs;

        try {
            tariffs = ChestShopDatabase.getTariffsByGovernment(nation.getUUID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (Tariff tariff : tariffs) {
            int total = getTotalForeignTrade(tariff.getGovernmentTariffed(), 24 * 60 * 1000);

            if (total == -1) {
                return null;
            }

            int upkeep = (total / 10) * tariff.getPercentage() + 15;

            ret.add(new Upkeep(nation, upkeep, Upkeep.TradeLawType.TARIFF));
        }

        HashSet<Embargo> embargoes;

        try {
            embargoes = ChestShopDatabase.getEmbargoesByGovernment(nation.getUUID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (Embargo embargo : embargoes) {
            int total = getTotalForeignTrade(embargo.getEmbargoedNation(), 24 * 60 * 1000);

            if (total == -1) {
                return null;
            }

            int upkeep = total * 5 + 100;

            ret.add(new Upkeep(nation, upkeep, Upkeep.TradeLawType.EMBARGO));
        }

        return ret;
    }

    public static int getTradeLawUpkeep(Nation nation, UUID government_against, Upkeep.TradeLawType type){
        HashSet<Upkeep> upkeeps = getTradeLawUpkeep(nation);

        if(upkeeps == null){
            return 0;
        }

        for(Upkeep upkeep : upkeeps){
            if(!upkeep.getTarget().getUUID().equals(government_against)){
                continue;
            }

            if(!upkeep.getType().equals(type)){
                continue;
            }

            return upkeep.getUpkeep();
        }

        return 0;
    }

}
