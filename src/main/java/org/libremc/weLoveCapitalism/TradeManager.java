package org.libremc.weLoveCapitalism;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.libremc.weLoveCapitalism.datatypes.Embargo;
import org.libremc.weLoveCapitalism.datatypes.Tariff;
import org.libremc.weLoveCapitalism.datatypes.Trade;
import org.libremc.weLoveCapitalism.datatypes.Upkeep;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

public class TradeManager {

    public static HashSet<Trade> Trades;

    public static final long DAY_MILLISECONDS = 24 * 60 * 60 * 1000;
    public static final long WEEK_MILLISECONDS = 7 * 24 * 60 * 60 * 1000;


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

    /**
     * Gets the total amount of gold revenue that a government made in a timeframe from chestshop trades
     * @param government_uuid The UUID of the government we wish to do this lookup for
     * @param time The timeframe in which we should do this lookup, starting from right now to the specified time
     * @param is_inactive If the lookup should account for trades that are inactive or not. If true, then it will account for them, if false then it won't account for them
     * @return Returns the total foreign trade of a government
     */
    public static int getTotalForeignTrade(UUID government_uuid, long time, boolean is_inactive) {
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

                    /* Check if the trade is active. Every trade becomes inactive every Towny new day */
                    if(!trade.isActive() && is_inactive){
                        continue;
                    }

                    UUID buyer_town = trade.getTownBuyerUuid();
                    UUID buyer_nation = trade.getNationBuyerUuid();

                    /* Prevent inner-town upkeep increase */
                    if(buyer_town != null){
                        if(buyer_town.equals(trade.getTownUuid())){
                            continue;
                        }
                    }

                    /* If it's inner-nation trade then halve the return */
                    if(buyer_nation != null && trade.getNationUuid() != null){
                        if(buyer_nation.equals(trade.getNationUuid())){
                            ret += (trade.getPrice() / 2);
                            continue;
                        }
                    }

                    ret += trade.getPrice();

                }
            }
        } else if (nation != null) {
            for (Trade trade : Trades) {

                if(trade.getNationUuid() == null){
                    continue;
                }

                if (trade.getNationUuid().equals(nation.getUUID())) {
                    //Bukkit.broadcastMessage("yes1");
                    if ((current_time - trade.getTimestamp()) > time) {
                        //Bukkit.broadcastMessage("yes2");
                        //Bukkit.broadcastMessage("current time: " + current_time);
                        //Bukkit.broadcastMessage("trade timestamp: " + trade.getTimestamp());
                        //Bukkit.broadcastMessage("interval: " + (current_time - trade.getTimestamp()));
                        //Bukkit.broadcastMessage("time: " + time);
                        continue;
                    }

                    /* Check if the trade is active. Every trade becomes inactive every Towny new day */
                    if(!trade.isActive() && is_inactive){
                        //Bukkit.broadcastMessage("inactive");
                        continue;
                    }

                    UUID buyer_town = trade.getTownBuyerUuid();
                    UUID buyer_nation = trade.getNationBuyerUuid();

                    /* Prevent inner-town upkeep increase */
                    if(buyer_town != null){
                        if(buyer_town.equals(trade.getTownUuid())){
                            continue;
                        }
                    }

                    /* If it's inner-nation trade then halve the return */
                    if(buyer_nation != null){
                        if(buyer_nation.equals(trade.getNationUuid())){
                            ret += (trade.getPrice() / 2);
                            continue;
                        }
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
            if(!tariff.isActive()){
                continue;
            }

            int total = getTotalForeignTrade(tariff.getGovernmentTariffed(), DAY_MILLISECONDS, true);

            if (total == -1) {
                return null;
            }

            int upkeep = tariffUpkeep(total, tariff.getPercentage());

            ret.add(new Upkeep(nation, tariff.getGovernmentTariffed(), upkeep, Upkeep.TradeLawType.TARIFF));
        }

        HashSet<Embargo> embargoes;

        try {
            embargoes = ChestShopDatabase.getEmbargoesByGovernment(nation.getUUID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (Embargo embargo : embargoes) {
            if(!embargo.isActive()){
                continue;
            }

            int total = getTotalForeignTrade(embargo.getEmbargoedNation(), DAY_MILLISECONDS, true);

            if (total == -1) {
                return null;
            }

            int upkeep = embargoUpkeep(total);

            ret.add(new Upkeep(nation, embargo.getEmbargoedNation(), upkeep, Upkeep.TradeLawType.EMBARGO));
        }



        return ret;
    }

    public static int getTradeLawUpkeep(Nation nation, UUID government_against, Upkeep.TradeLawType type){
        HashSet<Upkeep> upkeeps = getTradeLawUpkeep(nation);

        if(upkeeps == null){
            Log.consoleError("Upkeeps null at getTradeLawUpkeep()!");
            return 0;
        }

        for(Upkeep upkeep : upkeeps){
            if(!upkeep.getGovernmentAgainst().equals(government_against)){
                continue;
            }

            if(!upkeep.getType().equals(type)){
                continue;
            }

            return upkeep.getUpkeep();
        }

        return 0;
    }

    public static void saveTrades() throws SQLException {
        for(Trade trade : Trades){
            TradesDatabase.removeTrade(trade.getTradeId());
            TradesDatabase.writeTrade(trade);
        }
    }

    public static int embargoUpkeep(int total_foreign_trade){
        return total_foreign_trade * 5 + 100;
    }

    public static int tariffUpkeep(int total_foreign_trade, int tariff_percentage){
        return (int) Math.round((double)total_foreign_trade * tariff_percentage / 30) + 15;
    }


}
