package org.libremc.weLoveCapitalism;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.libremc.weLoveCapitalism.datatypes.ChestShop;
import org.libremc.weLoveCapitalism.datatypes.Tariff;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.HashSet;

public class TariffManager {
    static HashSet<Tariff> Tariffs = new HashSet<>();


    public static void addTariff(Tariff tariff){
        Tariffs.add(tariff);

        try {
            ChestshopDatabase.writeTariff(tariff);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void removeTariff(Tariff tariff){
        Tariffs.remove(tariff);
        try {
            ChestshopDatabase.removeTariff(tariff);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getTariffPercentage(ChestShop shop, Nation player_nation) throws SQLException {
        Town town = TownyAPI.getInstance().getTown(shop.getSign().getLocation());
        if(town == null){
            return 0;
        }


        /* Check if this town is tariffed by player_nation */
        HashSet<Tariff> town_tariffs = ChestshopDatabase.getTariffsByGovernment(player_nation.getUUID());

        if(!town_tariffs.isEmpty()){

            for(Tariff tariff : town_tariffs) {
                if (tariff.getGovernmentTariffed().equals(town.getUUID())) {
                    return tariff.getPercentage();
                }
            }
        }


        /* If not, check if the town is in a nation and if that nation is tariffed by player_nation */
        Nation nation = town.getNationOrNull();

        if(nation == null){
            return 0;
        }

        for(Tariff tariff : town_tariffs){
            if(tariff.getGovernmentTariffed().equals(nation.getUUID())){
                return tariff.getPercentage();
            }
        }

        return 0;
    }

    public static int getTariffPercentage(Government government, Nation player_nation) {
        HashSet<Tariff> tariffs = null;
        try {
            tariffs = ChestshopDatabase.getTariffsByGovernment(player_nation.getUUID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for(Tariff tariff : tariffs){
            if(government.getUUID().equals(tariff.getGovernmentTariffed())){
                return tariff.getPercentage();
            }
        }

        return 0;
    }

    public static @Nullable Tariff getTariff(Government government, Nation player_nation){
        HashSet<Tariff> tariffs = null;
        try {
            tariffs = ChestshopDatabase.getTariffsByGovernment(player_nation.getUUID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for(Tariff tariff : tariffs){
            if(government.getUUID().equals(tariff.getGovernmentTariffed())){
                return tariff;
            }
        }

        return null;
    }
}
