package org.libremc.weLoveCapitalism;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Log {
    public static void playerMessage(Player player, String message){
        player.sendMessage(ChatColor.DARK_AQUA + "[WLC] " + ChatColor.AQUA + message);
    }
}
