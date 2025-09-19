package org.libremc.weLoveCapitalism;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import org.libremc.weLoveCapitalism.datatypes.Embargo;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.HashSet;

public class EmbargoManager {
    public static HashSet<Embargo> Embargoes;

    public static void addEmbargo(Embargo embargo){
        Embargoes.add(embargo);

        try {
            ChestShopDatabase.writeEmbargo(embargo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeEmbargo(Embargo embargo){
        Embargoes.remove(embargo);

        try {
            ChestShopDatabase.removeEmbargo(embargo);
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

}
