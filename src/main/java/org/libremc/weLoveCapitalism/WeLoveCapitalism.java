package org.libremc.weLoveCapitalism;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.libremc.weLoveCapitalism.commands.WLCCommand;
import org.libremc.weLoveCapitalism.listener.*;

import net.milkbowl.vault.economy.Economy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public final class WeLoveCapitalism extends JavaPlugin{

    private static Economy econ = null;

    public static final String CHESTSHOP_DB_PATH = "chestshops.db";
    public static final String TRADES_DB_PATH = "trade_history.db";

    static WeLoveCapitalism instance;

    public static ChestShopDatabase chestshopdb;
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

        Objects.requireNonNull(this.getCommand("wlc")).setExecutor(new WLCCommand());
        Objects.requireNonNull(this.getCommand("wlc")).setTabCompleter(new TabComplete());
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new InternalCommandListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new GovernmentDeletionListener(), this);
        getServer().getPluginManager().registerEvents(new TownyUpkeepListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);


        setupEconomy();

        try {
            chestshopdb = new ChestShopDatabase(CHESTSHOP_DB_PATH);
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
            ChestShopManager.Chestshops = ChestShopDatabase.parseChestShops();
        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            TariffManager.Tariffs = ChestShopDatabase.parseTariffs();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            EmbargoManager.Embargoes = ChestShopDatabase.parseEmbargoes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            TradeManager.Trades = TradesDatabase.parseTrades();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        TradeApi.start();
    }



    @Override
    public void onDisable() {
        try {
            ChestShopManager.saveChestShops();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

        try {
            TradeManager.saveTrades();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            chestshopdb.getStatement().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            tradehistorydb.getStatement().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("[WLC] Vault not found");
            panic();
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("[WLC] Economy not found");
            panic();
            return;
        }
        econ = rsp.getProvider();
    }


    public static Economy getEconomy(){
        return econ;
    }

    void panic(){
        getLogger().severe("[WLC] Panic!");
        this.getPluginLoader().disablePlugin(this);
    }
}
