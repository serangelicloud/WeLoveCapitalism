package org.libremc.weLoveCapitalism;

import org.libremc.weLoveCapitalism.datatypes.Trade;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.UUID;

public class TradesDatabaseStandalone {

    private final Connection connection;
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
                nation_buyer_uuid TEXT,
                timestamp INTEGER UNSIGNED NOT NULL,
                is_active BOOLEAN NOT NULL
            );
            """;

    public TradesDatabaseStandalone(String dbFilePath) throws SQLException, IOException {
        File file = new File(dbFilePath);
        if (!file.exists()) {
            file.createNewFile();
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        statement = connection.createStatement();
        statement.executeUpdate(SQL_TRADES_INIT_QUERY);
    }

    public Statement getStatement() {
        return statement;
    }

    public Connection getConnection() {
        return connection;
    }

    public static void removeTrade(int tradeId, TradesDatabaseStandalone db) throws SQLException {
        String query = "DELETE FROM trade_table WHERE trade_id = ?;";
        PreparedStatement statement = db.getConnection().prepareStatement(query);
        statement.setInt(1, tradeId);
        statement.execute();
        statement.close();
    }

    public HashSet<Trade> parseTrades() throws SQLException {
        HashSet<Trade> trades = new HashSet<>();
        String query = "SELECT * FROM trade_table";

        long current_time = System.currentTimeMillis();

        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {

            int tradeId = resultSet.getInt("trade_id");
            long timestamp = resultSet.getLong("timestamp");

            /* Delete this entry if it's older than a week */
            if ((current_time - timestamp) > TradeManager.WEEK_MILLISECONDS) {
                removeTrade(tradeId, this);
                continue;
            }

            int chestShopId = resultSet.getInt("chest_shop_id");
            String item = resultSet.getString("item");
            int amount = resultSet.getInt("amount");
            int price = resultSet.getInt("price");
            boolean is_active = resultSet.getBoolean("is_active");

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
                    nationUuid, townBuyerUuid, nationBuyerUuid,
                    timestamp, is_active
            );

            trades.add(trade);
        }

        resultSet.close();
        statement.close();

        return trades;
    }
}
