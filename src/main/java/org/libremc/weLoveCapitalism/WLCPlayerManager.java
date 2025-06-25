package org.libremc.weLoveCapitalism;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.libremc.weLoveCapitalism.datatypes.WLCPlayer;

import javax.annotation.Nullable;
import java.util.HashSet;

public class WLCPlayerManager {

    private static final HashSet<WLCPlayer> WLC_players = new HashSet<>();

    public static HashSet<WLCPlayer> getWLCPlayers(){
        return WLC_players;
    }

    public static void addWLCPlayer(@NotNull WLCPlayer player){
        WLC_players.add(player);
    }

    /* Returns a new WLCPlayer or an existing one if already exists */
    public static WLCPlayer createWLCPlayer(Player player){
        for(WLCPlayer p: getWLCPlayers()){
            if(p.getUUID().compareTo(player.getUniqueId()) == 0){
                return p;
            }
        }

        WLCPlayer wlcplayer = new WLCPlayer(player);
        addWLCPlayer(wlcplayer);
        return wlcplayer;
    }

    public static @Nullable WLCPlayer getWLCPlayer(Player player){
        for(WLCPlayer p: getWLCPlayers()){
            if(p.getUUID().compareTo(player.getUniqueId()) == 0){
                return p;
            }
        }

        return null;
    }
}
