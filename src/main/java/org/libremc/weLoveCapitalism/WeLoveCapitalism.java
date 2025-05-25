package org.libremc.weLoveCapitalism;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.libremc.weLoveCapitalism.commands.WLCCommand;
import org.libremc.weLoveCapitalism.listener.BlockBreakListener;
import org.libremc.weLoveCapitalism.listener.BlockPlaceListener;
import org.libremc.weLoveCapitalism.listener.InternalCommandListener;
import org.libremc.weLoveCapitalism.listener.PlayerInteractListener;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public final class WeLoveCapitalism extends JavaPlugin {

    private static Economy econ = null;

    public static final String CHESTSHOP_DB_PATH = "chestshops.db";

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
        getServer().getPluginManager().registerEvents(new InternalCommandListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);


        setupEconomy();

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

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }


    public static Economy getEconomy(){
        return econ;
    }

}
