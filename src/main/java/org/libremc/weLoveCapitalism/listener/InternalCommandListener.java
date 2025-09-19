package org.libremc.weLoveCapitalism.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.libremc.weLoveCapitalism.*;
import org.libremc.weLoveCapitalism.datatypes.Embargo;
import org.libremc.weLoveCapitalism.datatypes.Tariff;
import org.libremc.weLoveCapitalism.datatypes.WLCPlayer;

import com.palmergames.bukkit.towny.object.Nation;


import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class InternalCommandListener implements Listener {
    @EventHandler
    public static void onCommand(PlayerCommandPreprocessEvent event) throws SQLException {

        String[] args = event.getMessage().split("\\s+");

        if(args[0].equalsIgnoreCase("/wlc-internal-buy")){

            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());

            if(wlcplayer == null){
                Bukkit.getLogger().severe("[WLC] WLCPlayer is null for " + event.getPlayer());
                Log.playerMessage(event.getPlayer(), "This is a bug! Report it on the discord through the ticket system");
                return;
            }

            /* Check if the chest shop has been deleted */
            if(!ChestShopManager.Chestshops.contains(wlcplayer.getBuyingShop())){
                Log.playerMessage(event.getPlayer(), "Chestshop doesn't exist anymore!");
                event.setCancelled(true);
                return;
            }

            ChestShopManager.buyChestShop(wlcplayer.getBuyingShop(), event.getPlayer(), wlcplayer.getMultiplier());

            event.setCancelled(true);
        }else if(args[0].equalsIgnoreCase("/wlc-internal-set_amount")){
            event.getPlayer().sendMessage("Use /wlc multiplier <multiplier> to set your multiplier");
            event.setCancelled(true);
        }else if(args[0].equalsIgnoreCase("/wlc-internal-collect_earnings")){
            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());

            assert wlcplayer != null;

            if(!ChestShopManager.Chestshops.contains(wlcplayer.getBuyingShop())){
                Log.playerMessage(event.getPlayer(), "Chestshop doesn't exist anymore!");
                event.setCancelled(true);
                return;
            }


            long storage = wlcplayer.getBuyingShop().getGoldStorage();

            if(storage < 0){
                wlcplayer.getBuyingShop().setGoldStorage(0);
            }

            if(storage == 0){
                event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "You are broke!");
                event.setCancelled(true);
                return;
            }

            WeLoveCapitalism.getEconomy().depositPlayer(event.getPlayer(), wlcplayer.getBuyingShop().getGoldStorage());

            wlcplayer.getBuyingShop().setGoldStorage(0);

            Log.playerMessage(event.getPlayer(), "Withdrew " + ChatColor.GOLD + storage + "g " + ChatColor.AQUA + "from the chest shop");

            event.setCancelled(true);
        }else if(args[0].equalsIgnoreCase("/wlc-internal-view_item")){
            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());

            assert wlcplayer != null;

            ItemGUI inv = new ItemGUI(wlcplayer.getBuyingShop().getItem());

            inv.openInventory(event.getPlayer());

            event.setCancelled(true);
        }else if(args[0].equalsIgnoreCase("/wlc-internal-delete_shop")){
            WLCPlayer wlcplayer = WLCPlayerManager.getWLCPlayer(event.getPlayer());

            assert wlcplayer != null;

            if(wlcplayer.getBuyingShop().getGoldStorage() != 0){
                Log.playerMessage(event.getPlayer(), "Cannot remove your shop as it still has gold storage!");
                event.setCancelled(true);
                return;
            }

            ChestShopManager.removeChestShop(wlcplayer.getBuyingShop());

            Log.playerMessage(event.getPlayer(), "Removed your shop");

            event.setCancelled(true);
        }else if(args[0].equalsIgnoreCase("/wlc-internal-confirm_tariffs")){
            if(args.length != 5){
                Log.consoleError("args.length != 5");
                event.setCancelled(true);
                return;
            }

            Nation player_nation = TownyAPI.getInstance().getNation(UUID.fromString(args[2]));

            if(player_nation == null){
                Log.consoleError("player_nation == null @ wlc-internal-confirm_tariffs");
                event.setCancelled(true);
                return;
            }

            if(args[1].equalsIgnoreCase("nation")){
                Nation nation = TownyAPI.getInstance().getNation(UUID.fromString(args[3]));
                int percentage = Integer.parseInt(args[4]);

                Tariff tariff = new Tariff(player_nation.getUUID(), Objects.requireNonNull(nation).getUUID(), percentage);

                TariffManager.addTariff(tariff);

                Log.playerMessage(event.getPlayer(), "Set the tariff on " + Objects.requireNonNull(nation).getName() + " (nation) to " + percentage + "%!");
            }else if(args[1].equalsIgnoreCase("town")){
                Town town = TownyAPI.getInstance().getTown(UUID.fromString(args[3]));
                int percentage = Integer.parseInt(args[4]);

                Tariff tariff = new Tariff(player_nation.getUUID(), Objects.requireNonNull(town).getUUID(), percentage);

                TariffManager.addTariff(tariff);

                Log.playerMessage(event.getPlayer(), "Set the tariff on " + Objects.requireNonNull(town).getName() + " (town) to " + percentage + "%!");
            }

            event.setCancelled(true);
        }else if(args[0].equalsIgnoreCase("/wlc-internal-confirm_embargoes")){
            if(args.length != 4){
                Log.consoleError("args.length != 4");
                event.setCancelled(true);
                return;
            }

            Nation player_nation = TownyAPI.getInstance().getNation(UUID.fromString(args[2]));

            if(player_nation == null){
                Log.consoleError("player_nation == null @ wlc-internal-confirm_embargoes");
                event.setCancelled(true);
                return;
            }

            if(args[1].equalsIgnoreCase("nation")){
                Nation nation = TownyAPI.getInstance().getNation(UUID.fromString(args[3]));

                Embargo embargo = new Embargo(player_nation.getUUID(), Objects.requireNonNull(nation).getUUID());

                EmbargoManager.addEmbargo(embargo);

                Log.playerMessage(event.getPlayer(), "Set embargo on " + nation.getName() + " (nation)!");
            }else if(args[1].equalsIgnoreCase("town")){
                Town town = TownyAPI.getInstance().getTown(UUID.fromString(args[3]));

                Embargo embargo = new Embargo(player_nation.getUUID(), Objects.requireNonNull(town).getUUID());

                EmbargoManager.addEmbargo(embargo);

                Log.playerMessage(event.getPlayer(), "Set embargo on " + town.getName() + " (town)!");
            }

            event.setCancelled(true);
        }




    }
}
