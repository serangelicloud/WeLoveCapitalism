package org.libremc.weLoveCapitalism;

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
    public static final String TRADES_DB_PATH = "trade_history.db";

    static WeLoveCapitalism instance;

    public static ChestshopDatabase chestshopdb;
    public static TradesDatabase tradehistorydb;


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
            chestshopdb = new ChestshopDatabase(CHESTSHOP_DB_PATH);
        } catch (SQLException | IOException e) {
            getLogger().warning("WLC: Failed to open SQL database:" + e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            tradehistorydb = new TradesDatabase(TRADES_DB_PATH);
        } catch (SQLException | IOException e) {
            getLogger().warning("WLC: Failed to open SQL database:" + e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            ChestShopManager.Chestshops = ChestshopDatabase.parseChestShops();
        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            TariffManager.Tariffs = ChestshopDatabase.parseTariffs();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            EmbargoManager.Embargoes = ChestshopDatabase.parseEmbargoes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }



    @Override
    public void onDisable() {
        try {
            chestshopdb.getStatement().close();
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
