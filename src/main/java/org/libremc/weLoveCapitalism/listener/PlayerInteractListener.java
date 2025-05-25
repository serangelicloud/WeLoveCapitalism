package org.libremc.weLoveCapitalism.listener;


import com.palmergames.bukkit.towny.TownyAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.libremc.weLoveCapitalism.ChestShop;
import org.libremc.weLoveCapitalism.ChestShopManager;
import org.libremc.weLoveCapitalism.WLCPlayer;
import org.libremc.weLoveCapitalism.WLCPlayerManager;

import java.util.UUID;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent event){

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();


        ChestShop shop = ChestShopManager.getChestShopBySign(block);

        if(shop == null){
            return;
        }

        shop.setChest((Chest) Bukkit.getWorld("world").getBlockAt(shop.getChest().getLocation()).getState()); // Update the chest and its contents

        event.setCancelled(true);

        WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);

        /* Is owner */
        if(player.getUniqueId().toString().equalsIgnoreCase(shop.getOwner().toString())){
            player.sendMessage("Your shop");

            wlcplayer.setBuyingShop(shop);

            player.sendMessage("You have " + ChatColor.GOLD + shop.getGoldStorage() + "g in earnings");

            TextComponent collect_text = new TextComponent("[Collect Earnings]");
            collect_text.setColor(ChatColor.GREEN);
            collect_text.setBold(true);
            collect_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-collect_earnings"));

            player.spigot().sendMessage(collect_text);

            return;
        }

        String town_name = TownyAPI.getInstance().getTownName(shop.getSign().getLocation());

        if(town_name == null){
            player.sendMessage("Shop is not in a town");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + Bukkit.getPlayer(UUID.fromString(shop.getOwner())).getName() + "'s shop");
        player.sendMessage(ChatColor.AQUA + "" + shop.getItem().getAmount() + "x " + shop.getItemFormatted());
        player.sendMessage(ChatColor.AQUA + "Price: " + ChatColor.GOLD + "" + shop.getPrice() + "g");
        player.sendMessage(ChatColor.AQUA + "Under " + ChatColor.BOLD + town_name + ChatColor.RESET + "" + ChatColor.AQUA + "'s jurisdiction");

        wlcplayer.setBuyingShop(shop);

        TextComponent buy_text = new TextComponent("[Buy]");
        buy_text.setColor(ChatColor.GREEN);
        buy_text.setBold(true);
        buy_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-buy"));


        TextComponent amount_text = new TextComponent("[Amount]");
        amount_text.setColor(ChatColor.AQUA);
        amount_text.setBold(true);
        amount_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-set_amount"));


        player.spigot().sendMessage(buy_text, new TextComponent("   ") , amount_text);



    }
}
