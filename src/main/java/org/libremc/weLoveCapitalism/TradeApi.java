package org.libremc.weLoveCapitalism;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.sql.SQLException;
import java.io.IOException;

public class TradeApi {

    private static final Gson gson = new Gson();
    private static TradesDatabaseStandalone db;

    public static void start() {
        // Use port 8080 (can change if needed)
        port(8080);

        get("/trades", (req, res) -> {
            res.type("application/json");

            try {
                // Fetch trades from the standalone database
                return gson.toJson(db.parseTrades());
            } catch (SQLException e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson("{\"error\": \"Failed to fetch trades\"}");
            }
        });

        System.out.println("[WLC] Trade API running on port 8080");
    }

    // --- Standalone entry point ---
    public static void main(String[] args) {
        System.out.println("[WLC] Starting Trade API in standalone mode...");

        try {
            // Use the same DB file your plugin uses
            db = new TradesDatabaseStandalone("trade_history.db");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.err.println("[WLC] Failed to open database. Exiting.");
            System.exit(1);
        }

        // Start the API
        start();
    }
}
