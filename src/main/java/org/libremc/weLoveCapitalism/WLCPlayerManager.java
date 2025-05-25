package org.libremc.weLoveCapitalism;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

    public static WLCPlayer createWLCPlayer(Player player){
        for(WLCPlayer p: getWLCPlayers()){
            if(p.getUUID().compareTo(player.getUniqueId()) == 0){
                Bukkit.broadcastMessage("Found");
                return p;
            }
        }

        Bukkit.broadcastMessage("New");
        WLCPlayer wlcplayer = new WLCPlayer(player);
        addWLCPlayer(wlcplayer);
        return wlcplayer;
    }

    public static @Nullable WLCPlayer getWLCPlayer(Player player){
        for(WLCPlayer p: getWLCPlayers()){
            if(p.getUUID().compareTo(player.getUniqueId()) == 0){
                Bukkit.broadcastMessage("Found");
                return p;
            }
        }

        return null;
    }
}
