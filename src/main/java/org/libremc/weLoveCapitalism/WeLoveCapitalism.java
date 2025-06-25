package org.libremc.weLoveCapitalism;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.libremc.weLoveCapitalism.commands.WLCCommand;
import org.libremc.weLoveCapitalism.listener.*;

import net.milkbowl.vault.economy.Economy;

import java.io.IOException;
import java.sql.SQLException;

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
        this.getCommand("wlc").setTabCompleter(new TabComplete());
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new InternalCommandListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new GovernmentDeletionListener(), this);

        setupEconomy();

        try {
            db = new Database(CHESTSHOP_DB_PATH);
        } catch (SQLException | IOException e) {
            getLogger().warning("WLC: Failed to open SQL database:" + e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            ChestShopManager.Chestshops = Database.parseChestShops();
        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            TariffManager.Tariffs = Database.parseTariffs();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            EmbargoManager.Embargoes = Database.parseEmbargoes();
        } catch (SQLException e) {
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
