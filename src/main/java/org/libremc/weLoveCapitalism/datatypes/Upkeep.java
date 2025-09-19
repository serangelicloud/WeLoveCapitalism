package org.libremc.weLoveCapitalism.datatypes;

import com.palmergames.bukkit.towny.object.Nation;

import java.util.UUID;

public class Upkeep {

    public enum TradeLawType {
        TARIFF,
        EMBARGO
    }

    Nation target; // the one who has to pay upkeep
    UUID government_against; // the one who is the victim of the trade law
    int upkeep;

    TradeLawType type;

    public Upkeep(Nation target, UUID against, int upkeep, TradeLawType type){
        this.target = target;
        this.government_against = against;
        this.upkeep = upkeep;
        this.type = type;
    }

    public Nation getTarget() {
        return target;
    }

    public UUID getGovernmentAgainst() {
        return government_against;
    }

    public int getUpkeep() {
        return upkeep;
    }

    public TradeLawType getType() {
        return type;
    }
}
