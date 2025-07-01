package org.libremc.weLoveCapitalism;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.libremc.weLoveCapitalism.datatypes.ChestShop;
import org.libremc.weLoveCapitalism.datatypes.Embargo;
import org.libremc.weLoveCapitalism.datatypes.Tariff;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.UUID;


public class ChestShopDatabase {
    private final Statement statement;

    private static final String SQL_CHEST_SHOP_INIT_QUERY = """
            CREATE TABLE IF NOT EXISTS chest_shop_table(
                chest_shop_id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid_owner TEXT NOT NULL,
                chest_block_location TEXT NOT NULL,
                sign_block_location TEXT NOT NULL,
                item TEXT NOT NULL,
                price INTEGER UNSIGNED NOT NULL,
                gold_storage INTEGER UNSIGNED NOT NULL
            );
            """;

    private static final String SQL_TARIFF_INIT_QUERY = """
            CREATE TABLE IF NOT EXISTS tariff_table(
                tariffing_nation_uuid STRING NOT NULL,
                tariffed_nation_uuid STRING NOT NULL,
                tariff_percentage INTEGER NOT NULL,
                is_active BOOLEAN NOT NULL
            );
            """;

    private static final String SQL_EMBARGO_INIT_QUERY = """
            CREATE TABLE IF NOT EXISTS embargo_table(
                embargoing_nation_uuid STRING NOT NULL,
                embargoed_nation_uuid STRING NOT NULL,
                is_active BOOLEAN NOT NULL
            );
            """;


    public ChestShopDatabase(String dbFileName) throws SQLException, IOException {
        String dbPath = new File(WeLoveCapitalism.getInstance().getDataFolder(), dbFileName).getAbsolutePath();

        File file = new File(dbPath);
        if(!file.exists()){
            file.createNewFile();
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        statement = connection.createStatement();
        statement.executeUpdate(SQL_CHEST_SHOP_INIT_QUERY);
        statement.executeUpdate(SQL_TARIFF_INIT_QUERY);
        statement.executeUpdate(SQL_EMBARGO_INIT_QUERY);
    }

    public static void deleteChestShop(ChestShop shop) throws SQLException {
        String query = "DELETE FROM chest_shop_table WHERE uuid_owner = ? AND chest_block_location = ? AND sign_block_location = ?;";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        statement.setString(1, shop.getOwner().toString());
        statement.setString(2, serializeLocation(shop.getChest().getLocation()));
        statement.setString(3, serializeLocation(shop.getSign().getLocation()));

        statement.execute();

        statement.close();
    }

    public static void writeChestShop(ChestShop shop) throws SQLException, IOException {
        String query = "INSERT INTO chest_shop_table (uuid_owner, chest_block_location, sign_block_location, item, price, gold_storage) VALUES(?, ?, ?, ?, ?, ?);";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        statement.setString(1, shop.getOwner().toString());
        statement.setString(2, serializeLocation(shop.getChest().getLocation()));
        statement.setString(3, serializeLocation(shop.getSign().getLocation()));
        statement.setString(4, serializeItemStack(shop.getItem()));
        statement.setLong(5, shop.getPrice());
        statement.setLong(6, shop.getGoldStorage());
        statement.execute();

        statement.close();
    }

    public static HashSet<ChestShop> parseChestShops() throws SQLException, IOException, ClassNotFoundException {
        HashSet<ChestShop> shop_set = new HashSet<>();
        String query = "SELECT chest_shop_id, uuid_owner, chest_block_location, sign_block_location, item, price, gold_storage from chest_shop_table";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        ResultSet set = statement.executeQuery();

        while(set.next()){
            Location chest_loc = deserializeLocation(set.getString(3));
            Location sign_loc = deserializeLocation(set.getString(4));

            Bukkit.getLogger().info("chest x: " + chest_loc.getBlockX() + " y: " + chest_loc.getBlockY() + " z: " + chest_loc.getBlockZ());

            if(Bukkit.getWorld("world").getBlockAt(chest_loc).getType() != Material.CHEST){
                continue;
            }

            if(Bukkit.getWorld("world").getBlockAt(sign_loc).getType() != Material.OAK_WALL_SIGN){
                continue;
            }

            Chest chest = (Chest)Bukkit.getWorld("world").getBlockAt(chest_loc).getState();

            Sign sign = (Sign) Bukkit.getWorld("world").getBlockAt(sign_loc).getState();

            ItemStack item = deserializeItemStack(set.getString(5));

            long price = set.getLong(6);

            long gold_storage = set.getLong(7);

            int id = set.getInt(1);

            ChestShop shop = new ChestShop(id, sign, chest, set.getString(2), item, price, gold_storage);
            shop_set.add(shop);
        }

        statement.close();
        set.close();

        return shop_set;

    }

    public static void writeTariff(Tariff tariff) throws SQLException {
        String query = "INSERT INTO tariff_table (tariffing_nation_uuid, tariffed_nation_uuid, tariff_percentage, is_active) VALUES(?, ?, ?, ?);";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        statement.setString(1, tariff.getGovernmentTariffing().toString());
        statement.setString(2, tariff.getGovernmentTariffed().toString());
        statement.setInt(3, tariff.getPercentage());
        statement.setBoolean(4, tariff.isActive());

        statement.execute();

        statement.close();
    }

    public static void removeTariff(Tariff tariff) throws SQLException {
        String query = "DELETE FROM tariff_table WHERE tariffing_nation_uuid = ? AND tariffed_nation_uuid = ?;";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        statement.setString(1, tariff.getGovernmentTariffing().toString());
        statement.setString(2, tariff.getGovernmentTariffed().toString());

        statement.execute();

        statement.close();
    }


    public static HashSet<Tariff> parseTariffs() throws SQLException {

        HashSet<Tariff> tariff_set = new HashSet<>();

        String query = "SELECT tariffing_nation_uuid, tariffed_nation_uuid, tariff_percentage, is_active FROM tariff_table";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        ResultSet set = statement.executeQuery();

        while(set.next()){
            tariff_set.add(new Tariff(UUID.fromString(set.getString(1)), UUID.fromString(set.getString(2)), set.getInt(3), set.getBoolean(4)));
        }

        statement.close();
        set.close();

        return tariff_set;
    }

    public static HashSet<Tariff> getTariffsByGovernment(UUID government_uuid) throws SQLException {

        HashSet<Tariff> tariff_set = new HashSet<>();

        String query = "SELECT tariffing_nation_uuid, tariffed_nation_uuid, tariff_percentage, is_active FROM tariff_table WHERE tariffing_nation_uuid = ?";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);

        statement.setString(1, government_uuid.toString());

        ResultSet set = statement.executeQuery();

        while(set.next()){
            tariff_set.add(new Tariff(UUID.fromString(set.getString(1)), UUID.fromString(set.getString(2)), set.getInt(3), set.getBoolean(4)));
        }

        statement.close();
        set.close();

        return tariff_set;
    }

    public static HashSet<Tariff> getTariffsToGovernment(UUID government_uuid) throws SQLException {

        HashSet<Tariff> tariff_set = new HashSet<>();

        String query = "SELECT tariffing_nation_uuid, tariffed_nation_uuid, tariff_percentage, is_active FROM tariff_table WHERE tariffed_nation_uuid = ?";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);

        statement.setString(1, government_uuid.toString());

        ResultSet set = statement.executeQuery();

        while(set.next()){
            tariff_set.add(new Tariff(UUID.fromString(set.getString(1)), UUID.fromString(set.getString(2)), set.getInt(3), set.getBoolean(4)));
        }

        statement.close();
        set.close();

        return tariff_set;
    }

    public static void writeEmbargo(Embargo embargo) throws SQLException {
        String query = "INSERT INTO embargo_table (embargoing_nation_uuid, embargoed_nation_uuid, is_active) VALUES(?, ?);";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        statement.setString(1, embargo.getEmbargoingNation().toString());
        statement.setString(2, embargo.getEmbargoedNation().toString());
        statement.setBoolean(3, embargo.isActive());

        statement.execute();
        statement.close();
    }

    public static void removeEmbargo(Embargo embargo) throws SQLException {
        String query = "DELETE FROM embargo_table WHERE embargoing_nation_uuid = ? AND embargoed_nation_uuid = ?;";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        statement.setString(1, embargo.getEmbargoingNation().toString());
        statement.setString(2, embargo.getEmbargoedNation().toString());

        statement.execute();
        statement.close();
    }

    public static HashSet<Embargo> parseEmbargoes() throws SQLException {
        HashSet<Embargo> embargoSet = new HashSet<>();
        String query = "SELECT embargoing_nation_uuid, embargoed_nation_uuid, is_active FROM embargo_table";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        ResultSet set = statement.executeQuery();

        while(set.next()) {
            embargoSet.add(new Embargo(UUID.fromString(set.getString(1)), UUID.fromString(set.getString(2)), set.getBoolean(3)));
        }

        statement.close();
        set.close();

        return embargoSet;
    }

    public static HashSet<Embargo> getEmbargoesByGovernment(UUID governmentUuid) throws SQLException {
        HashSet<Embargo> embargoedNations = new HashSet<>();
        String query = "SELECT embargoed_nation_uuid, is_active FROM embargo_table WHERE embargoing_nation_uuid = ?";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        statement.setString(1, governmentUuid.toString());
        ResultSet set = statement.executeQuery();

        while(set.next()) {
            embargoedNations.add(new Embargo(governmentUuid, UUID.fromString(set.getString(1)), set.getBoolean(2)));

        }

        statement.close();
        set.close();

        return embargoedNations;
    }

    public static HashSet<Embargo> getEmbargoesToGovernment(UUID governmentUuid) throws SQLException {
        HashSet<Embargo> embargoingNations = new HashSet<>();
        String query = "SELECT embargoing_nation_uuid, is_active FROM embargo_table WHERE embargoed_nation_uuid = ?";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        statement.setString(1, governmentUuid.toString());
        ResultSet set = statement.executeQuery();

        while(set.next()) {
            embargoingNations.add(new Embargo(UUID.fromString(set.getString(1)), governmentUuid , set.getBoolean(2)));
        }

        statement.close();
        set.close();

        return embargoingNations;
    }


    public static String serializeItemStack(ItemStack item) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeObject(item);
        dataOutput.close();

        return Base64Coder.encodeLines(outputStream.toByteArray());

    }

    public static ItemStack deserializeItemStack(String data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();

        return item;
    }

    public static String serializeLocation(Location loc){
        return loc.getX() + "|" + loc.getY() + "|" + loc.getZ();
    }

    public static Location deserializeLocation(String data){
        StringTokenizer tok = new StringTokenizer(data, "|");
        double x = Double.parseDouble(tok.nextToken());
        double y = Double.parseDouble(tok.nextToken());
        double z = Double.parseDouble(tok.nextToken());
        return new Location(Bukkit.getWorld("world"), x, y, z);
    }

    public Statement getStatement() {
        return statement;
    }
}