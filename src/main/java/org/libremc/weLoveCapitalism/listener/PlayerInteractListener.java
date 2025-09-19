package org.libremc.weLoveCapitalism.listener;



import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.libremc.weLoveCapitalism.*;
import org.libremc.weLoveCapitalism.datatypes.ChestShop;
import org.libremc.weLoveCapitalism.datatypes.Embargo;
import org.libremc.weLoveCapitalism.datatypes.WLCPlayer;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class PlayerInteractListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onPlayerInteract(PlayerInteractEvent event) throws SQLException {

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if(block == null){
            return;
        }

        ChestShop shop = ChestShopManager.getChestShopBySign(block);

        if(shop == null){
            return;
        }

        shop.setChest((Chest) Objects.requireNonNull(Bukkit.getWorld("world")).getBlockAt(shop.getChest().getLocation()).getState()); // Update the chest and its contents

        event.setCancelled(true);

        WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);

        /* Is owner */
        if(player.getUniqueId().toString().equalsIgnoreCase(shop.getOwner())){
            player.sendMessage("Your shop");
            player.sendMessage("Selling " + shop.getItemFormatted() + "x" + shop.getItem().getAmount() + " for " + ChatColor.GOLD + shop.getPrice() + "g");

            wlcplayer.setBuyingShop(shop);

            player.sendMessage("You have " + ChatColor.GOLD + shop.getGoldStorage() + "g in earnings");

            TextComponent collect_text = new TextComponent("[Collect Earnings]");
            collect_text.setColor(ChatColor.GREEN);
            collect_text.setBold(true);
            collect_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-collect_earnings"));

            player.spigot().sendMessage(collect_text);

            TextComponent delete_text = new TextComponent("[Delete Shop]");
            delete_text.setColor(ChatColor.DARK_RED);
            delete_text.setBold(true);
            delete_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-delete_shop"));

            player.spigot().sendMessage(delete_text);

            return;
        }
        Town town = TownyAPI.getInstance().getTown(shop.getSign().getLocation());

        if(town == null){
            Bukkit.getServer().getLogger().info("Shop is not in a town, deleting");
            ChestShopManager.removeChestShop(shop);
            event.setCancelled(false);
            return;
        }

        String town_name = town.getName();

        player.sendMessage("");

        /* Check if the player has interacted with a different shop before and reset the multiplier */
        if(wlcplayer.getBuyingShop() != null){
            if(!wlcplayer.getBuyingShop().equals(shop)){
                wlcplayer.setMultiplier(1);
            }
        }

        wlcplayer.setBuyingShop(shop);

        player.sendMessage(ChatColor.AQUA + Bukkit.getOfflinePlayer(UUID.fromString(shop.getOwner())).getName() + "'s shop");
        ItemMeta meta = shop.getItem().getItemMeta();

        if(meta != null){
            if(meta.hasDisplayName()){
                player.sendMessage(ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE + shop.getItem().getItemMeta().getDisplayName() + ChatColor.RESET);
            }
        }

        TextComponent view_text = new TextComponent("[View Item]");
        view_text.setColor(ChatColor.DARK_PURPLE);
        view_text.setBold(true);
        view_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-view_item"));

        player.spigot().sendMessage(view_text);

        Nation nation = TownyAPI.getInstance().getNation(player);

        player.sendMessage(ChatColor.AQUA + "" + shop.getItem().getAmount() + "x " + shop.getItemFormatted());
        player.sendMessage(ChatColor.AQUA + "Buying multiplier: " + ChatColor.DARK_AQUA + wlcplayer.getMultiplier() + "x");
        int tariff;
        if(nation != null){
            tariff = TariffManager.getTariffPercentage(shop, nation);
            if(tariff != 0){
                long price = (long)(Math.ceil(shop.getPrice() * (((double)tariff / 100) + 1)));
                if(wlcplayer.getMultiplier() != 1){
                    player.sendMessage(ChatColor.AQUA + "Tariffed price: " + ChatColor.GOLD + price + "g" + ChatColor.AQUA + " x " + wlcplayer.getMultiplier() + " = " + ChatColor.GOLD + price * wlcplayer.getMultiplier()  + "g" + ChatColor.RED + " (" + tariff + "% tariff by your nation)");
                }else{
                    player.sendMessage(ChatColor.AQUA + "Tariffed price: " + ChatColor.GOLD + price + "g" + ChatColor.RED + " (" + tariff + "% tariff by your nation)");

                }
            }
        }

        if(wlcplayer.getMultiplier() != 1){
            player.sendMessage(ChatColor.AQUA + "Base price: " + ChatColor.GOLD + shop.getPrice() + "g" + ChatColor.AQUA + " x " + wlcplayer.getMultiplier() + " = " + ChatColor.GOLD + shop.getPrice() * wlcplayer.getMultiplier() + "g");
        }else{
            player.sendMessage(ChatColor.AQUA + "Base price: " + ChatColor.GOLD + shop.getPrice() + "g");

        }
        player.sendMessage(ChatColor.AQUA + "Under " + ChatColor.BOLD + town_name + ChatColor.RESET + ChatColor.AQUA + "'s jurisdiction");

        if(nation != null){
            for(Embargo embargo : ChestShopDatabase.getEmbargoesByGovernment(nation.getUUID())){

                if(!embargo.isActive()){
                    continue;
                }

                Nation _nation = town.getNationOrNull();

                if(_nation != null){
                    if(_nation.getUUID().equals(embargo.getEmbargoedNation())){
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Your nation is embargoing this shop!");
                        return;
                    }
                }

                if(town.getUUID().equals(embargo.getEmbargoedNation())){
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Your nation is embargoing this shop!");
                    return;
                }
            }
        }

        TextComponent buy_text = new TextComponent("[Buy]");
        buy_text.setColor(ChatColor.GREEN);
        buy_text.setBold(true);
        buy_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-buy"));


        TextComponent amount_text = new TextComponent("[Set multiplier]");
        amount_text.setColor(ChatColor.AQUA);
        amount_text.setBold(true);
        amount_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-set_amount"));
        amount_text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Current multiplier: ").append(wlcplayer.getMultiplier() + "x").create()));

        player.spigot().sendMessage(buy_text, new TextComponent("   ") , amount_text);



    }
}
