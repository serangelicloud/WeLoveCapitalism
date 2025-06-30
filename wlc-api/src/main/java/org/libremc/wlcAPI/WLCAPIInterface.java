package org.libremc.wlcAPI;

import com.palmergames.bukkit.towny.object.Nation;
import org.libremc.wlcAPI.datatypes.Upkeep;

import java.util.HashSet;
import java.util.UUID;

public interface WLCAPIInterface {
    /**
     * @param government_uuid
     * UUID of the government you want to get the total foreign trade of
     *
     * @param time
     * Timeframe withing to get that total
     *
     * @return
     * Returns the total foreign trade in gold
     */
    public int getTotalForeignTrade(UUID government_uuid, long time);

    /**
     * @param nation
     * The nation for which you want get the upkeep for
     *
     * @return
     * Returns a Set of Upkeep types
     */
    public HashSet<Upkeep> getTradeLawUpkeep(Nation nation);
}
