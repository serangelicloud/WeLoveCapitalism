package org.libremc.weLoveCapitalism.listener;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.libremc.weLoveCapitalism.Database;
import org.libremc.weLoveCapitalism.datatypes.Tariff;
import org.libremc.weLoveCapitalism.TariffManager;

import java.sql.SQLException;
import java.util.HashSet;

public class GovernmentDeletionListener implements Listener {

    @EventHandler
    void onTownDelete(DeleteTownEvent event) throws SQLException {
        HashSet<Tariff> tariffs;
        tariffs = Database.getTariffsByGovernment(event.getTownUUID());

        for(Tariff tariff : tariffs){
            TariffManager.removeTariff(tariff);
        }

        tariffs = Database.getTariffsToGovernment(event.getTownUUID());

        for(Tariff tariff : tariffs){
            TariffManager.removeTariff(tariff);
        }

    }

    @EventHandler
    void onNationDelete(DeleteNationEvent event) throws SQLException {
        HashSet<Tariff> tariffs;
        tariffs = Database.getTariffsByGovernment(event.getNationUUID());

        for(Tariff tariff : tariffs){
            TariffManager.removeTariff(tariff);
        }

        tariffs = Database.getTariffsToGovernment(event.getNationUUID());

        for(Tariff tariff : tariffs){
            TariffManager.removeTariff(tariff);
        }

    }

}
