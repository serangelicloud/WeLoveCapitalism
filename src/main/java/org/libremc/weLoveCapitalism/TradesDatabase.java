package org.libremc.weLoveCapitalism;

import org.libremc.weLoveCapitalism.datatypes.Trade;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.UUID;

public class TradesDatabase {

    private final Statement statement;

    private static final String SQL_TRADES_INIT_QUERY = """
            CREATE TABLE IF NOT EXISTS trade_table(
                trade_id INTEGER PRIMARY KEY AUTOINCREMENT,
                chest_shop_id INTEGER NOT NULL,
                item TEXT NOT NULL,
                amount INTEGER UNSIGNED NOT NULL,
                price INTEGER UNSIGNED NOT NULL,
                uuid_owner TEXT NOT NULL,
                uuid_buyer TEXT NOT NULL,
                town_uuid TEXT NOT NULL,
                nation_uuid TEXT,
                town_buyer_uuid TEXT,
                nation_buyer_uuid TEXT
            );
            """;

    public TradesDatabase(String dbFileName) throws SQLException, IOException {
        String dbPath = new File(WeLoveCapitalism.getInstance().getDataFolder(), dbFileName).getAbsolutePath();

        File file = new File(dbPath);
        if(!file.exists()){
            file.createNewFile();
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        statement = connection.createStatement();
        statement.executeUpdate(SQL_TRADES_INIT_QUERY);
        statement.close();
    }

    public static void writeTrade(Trade trade) throws SQLException {
        String query = """
        INSERT INTO trade_table (
            chest_shop_id, item, amount, price, 
            uuid_owner, uuid_buyer, town_uuid, 
            nation_uuid, town_buyer_uuid, nation_buyer_uuid
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);

        statement.setInt(1, trade.getChestShopId());
        statement.setString(2, trade.getItem());
        statement.setInt(3, trade.getAmount());
        statement.setInt(4, trade.getPrice());
        statement.setString(5, trade.getOwnerUuid().toString());
        statement.setString(6, trade.getBuyerUuid().toString());
        statement.setString(7, trade.getTownUuid().toString());

        if (trade.getNationUuid() != null) {
            statement.setString(8, trade.getNationUuid().toString());
        } else {
            statement.setNull(8, Types.VARCHAR);
        }

        if (trade.getTownBuyerUuid() != null) {
            statement.setString(9, trade.getTownBuyerUuid().toString());
        } else {
            statement.setNull(9, Types.VARCHAR);
        }

        if (trade.getNationBuyerUuid() != null) {
            statement.setString(10, trade.getNationBuyerUuid().toString());
        } else {
            statement.setNull(10, Types.VARCHAR);
        }

        statement.execute();
        statement.close();
    }

    public static void removeTrade(int tradeId) throws SQLException {
        String query = "DELETE FROM trade_table WHERE trade_id = ?;";

        PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
        statement.setInt(1, tradeId);
        statement.execute();
        statement.close();
    }

    public static HashSet<Trade> parseTrades() throws SQLException {
        HashSet<Trade> trades = new HashSet<>();
        String query = "SELECT * FROM trade_table;";

        try (PreparedStatement statement = WeLoveCapitalism.chestshopdb.getStatement().getConnection().prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Integer tradeId = resultSet.getInt("trade_id");
                int chestShopId = resultSet.getInt("chest_shop_id");
                String item = resultSet.getString("item");
                int amount = resultSet.getInt("amount");
                int price = resultSet.getInt("price");

                // Parse UUIDs - handle potential null values for optional fields
                UUID ownerUuid = UUID.fromString(resultSet.getString("uuid_owner"));
                UUID buyerUuid = UUID.fromString(resultSet.getString("uuid_buyer"));
                UUID townUuid = UUID.fromString(resultSet.getString("town_uuid"));

                UUID nationUuid = resultSet.getString("nation_uuid") != null ?
                        UUID.fromString(resultSet.getString("nation_uuid")) : null;
                UUID townBuyerUuid = resultSet.getString("town_buyer_uuid") != null ?
                        UUID.fromString(resultSet.getString("town_buyer_uuid")) : null;
                UUID nationBuyerUuid = resultSet.getString("nation_buyer_uuid") != null ?
                        UUID.fromString(resultSet.getString("nation_buyer_uuid")) : null;

                Trade trade = new Trade(
                        tradeId, chestShopId, item, amount, price,
                        ownerUuid, buyerUuid, townUuid,
                        nationUuid, townBuyerUuid, nationBuyerUuid
                );

                trades.add(trade);
            }
        }

        return trades;
    }


}
