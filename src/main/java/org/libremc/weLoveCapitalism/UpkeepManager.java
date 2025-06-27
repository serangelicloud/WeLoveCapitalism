package org.libremc.weLoveCapitalism;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import org.libremc.weLoveCapitalism.datatypes.Embargo;
import org.libremc.weLoveCapitalism.datatypes.Tariff;
import org.libremc.weLoveCapitalism.datatypes.Upkeep;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class UpkeepManager {
   /* Returns all the upkeeps that `nation` has to pay */
   public static HashSet<Upkeep> getTradeLawUpkeep(Nation nation){
      HashSet<Upkeep> ret = new HashSet<>();

      HashSet<Tariff> tariffs;

       try {
           tariffs = ChestshopDatabase.getTariffsByGovernment(nation.getUUID());
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }

      HashSet<UUID> embargoes;

      try {
         embargoes = ChestshopDatabase.getEmbargoesByGovernment(nation.getUUID());
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }

   }
}
