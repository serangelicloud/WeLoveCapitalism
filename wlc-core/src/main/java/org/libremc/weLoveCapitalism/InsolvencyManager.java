package org.libremc.weLoveCapitalism;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;

import java.util.Objects;

public class InsolvencyManager {
    public static final String INSOLVENCY_METADATA_KEY = "INSOLVENCY_KEY";

    /*
        - IntegerDataField(INSOLVENCY_METADATA_KEY, amount) -
        This is the key used to store insolvency. `amount` is an integer which
        stores the amount of money that a nation owes for its trade law upkeep
        insolvency

        A nation goes into insolvency by not having enough gold in their bank at a Towny new
        day, which also acts as the time that trade law upkeep is calculated and removed from
        nation banks. During insolvency a nation cannot add new trade laws and all their
        current trade laws are set to inactive
     */

    public static void setInsolvent(Nation nation, int amount){
        /* We set this up using Towny's metadata field in the Nation class */

        IntegerDataField data = new IntegerDataField(INSOLVENCY_METADATA_KEY, amount);

        if(nation.hasMeta(INSOLVENCY_METADATA_KEY)){
            nation.removeMetaData(INSOLVENCY_METADATA_KEY);
        }

        nation.addMetaData(data);
    }

    public static int getInsolvency(Nation nation){
        if(!nation.hasMeta(INSOLVENCY_METADATA_KEY)){
            return 0;
        }

        return (int)Objects.requireNonNull(nation.getMetadata(INSOLVENCY_METADATA_KEY)).getValue();
    }

    public static void removeInsolvency(Nation nation){
        if(!nation.hasMeta(INSOLVENCY_METADATA_KEY)){
            return;
        }

        nation.removeMetaData(INSOLVENCY_METADATA_KEY);
    }
}
