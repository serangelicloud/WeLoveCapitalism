package org.libremc.weLoveCapitalism;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.UUID;

public class Database {
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

    public Database(String dbFileName) throws SQLException, IOException {
        String dbPath = new File(WeLoveCapitalism.getInstance().getDataFolder(), dbFileName).getAbsolutePath();

        File file = new File(dbPath);
        if(!file.exists()){
            file.createNewFile();
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        statement = connection.createStatement();
        statement.executeUpdate(SQL_CHEST_SHOP_INIT_QUERY);
    }

    public static void writeChestShop(ChestShop shop) throws SQLException, IOException {
        String query = "INSERT INTO chest_shop_table (uuid_owner, chest_block_location, sign_block_location, item, price, gold_storage) VALUES(?, ?, ?, ?, ?, ?);";

        PreparedStatement statement = WeLoveCapitalism.db.getStatement().getConnection().prepareStatement(query);
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

        PreparedStatement statement = WeLoveCapitalism.db.getStatement().getConnection().prepareStatement(query);
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

            ChestShop shop = new ChestShop(sign, chest, set.getString(2), item, price, gold_storage);
            shop_set.add(shop);
        }

        return shop_set;

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

    public static String serializeSign(Sign sign){
        return sign.getSide(Side.FRONT).getLine(0) + "|" + sign.getSide(Side.FRONT).getLine(1) + "|" + sign.getSide(Side.FRONT).getLine(2) + "|" + sign.getSide(Side.FRONT).getLine(3);
    }

    public static void deserializeSign(Sign sign, String data){
        StringTokenizer tok = new StringTokenizer(data, "|");
        for(int i = 0; i < 4; i++){
            sign.getSide(Side.FRONT).setLine(i, tok.nextToken());
        }
    }

    public Statement getStatement() {
        return statement;
    }
}