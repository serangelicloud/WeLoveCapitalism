package org.libremc.weLoveCapitalism;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.sql.SQLException;
import java.io.IOException;

public class TradeApi {

    private static final Gson gson = new Gson();
    private static TradesDatabase db;

    public static void start() {
        // Use port 8080 (can change if needed)
        port(8080);

        get("/trades", (req, res) -> {
            res.type("application/json");

            try {
                // Fetch trades from the database
                return gson.toJson(db.parseTrades());
            } catch (SQLException e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson("{\"error\": \"Failed to fetch trades\"}");
            }
        });

        System.out.println("[WLC] Trade API running on port 8080");
    }
    
}
