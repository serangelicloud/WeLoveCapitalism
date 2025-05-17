package org.libremc.weLoveCapitalism;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.libremc.weLoveCapitalism.commands.WLCCommand;
import org.libremc.weLoveCapitalism.listener.BlockPlaceListener;
import org.libremc.weLoveCapitalism.listener.PlayerInteractListener;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public final class WeLoveCapitalism extends JavaPlugin {

    public static final String CHESTSHOP_DB_PATH = "chestshops.db";

    private static HashSet<WLCPlayer> WLC_players = new HashSet<>();

    static WeLoveCapitalism instance;

    public static Database db;

    public static WeLoveCapitalism getInstance(){
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        this.getCommand("wlc").setExecutor(new WLCCommand());
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);

        try {
            db = new Database(CHESTSHOP_DB_PATH);
        } catch (SQLException | IOException e) {
            getLogger().warning("LibreMC Core: Failed to open SQL database:" + e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            ChestShopManager.Chestshops = Database.parseChestShops();
        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        try {
            db.getStatement().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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
}
