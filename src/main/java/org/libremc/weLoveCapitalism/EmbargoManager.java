package org.libremc.weLoveCapitalism;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.libremc.weLoveCapitalism.datatypes.Embargo;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

public class EmbargoManager {
    public static HashSet<Embargo> Embargoes;

    public static void addEmbargo(Embargo embargo){
        Embargoes.add(embargo);

        try {
            ChestshopDatabase.writeEmbargo(embargo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeEmbargo(Embargo embargo){
        Embargoes.remove(embargo);

        try {
            ChestshopDatabase.removeEmbargo(embargo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable Embargo getEmbargo(Government embargoed, Nation embargoer){
        for(Embargo embargo : Embargoes){
            if(embargo.getEmbargoedNation().equals(embargoed.getUUID()) && embargo.getEmbargoingNation().equals(embargoer.getUUID())){
                return embargo;
            }
        }

        return null;
    }


    public static boolean isEmbargoed(Government embargoed, Nation embargoer){

        TownyAPI api = TownyAPI.getInstance();

        HashSet<UUID> embargoes;
        try {
            embargoes = ChestshopDatabase.getEmbargoesByGovernment(embargoer.getUUID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for(UUID uuid : embargoes){
            Town town = api.getTown(uuid);
            Nation nation = api.getNation(uuid);

            if(town == null){
                return true;
            }else if(nation == null){
                return true;
            }else{
                /* If doesn't exist then remove the embargo */
                Embargo embargo = getEmbargo(embargoed, embargoer);

            }
        }

        return true;
    }
}
