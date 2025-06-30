package org.libremc.wlcAPI.datatypes;

import com.palmergames.bukkit.towny.object.Government;

public class Upkeep {

    public static enum TradeLawType {
        TARIFF,
        EMBARGO
    }

    Government target;
    int upkeep;

    TradeLawType type;

    public Upkeep(Government target, int upkeep, TradeLawType type){
        this.target = target;
        this.upkeep = upkeep;
        this.type = type;
    }

    public Government getTarget() {
        return target;
    }

    public int getUpkeep() {
        return upkeep;
    }
}
