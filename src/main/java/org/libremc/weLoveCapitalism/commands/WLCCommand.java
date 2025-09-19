package org.libremc.weLoveCapitalism.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.libremc.weLoveCapitalism.*;
import org.libremc.weLoveCapitalism.datatypes.*;

import java.sql.SQLException;
import java.util.*;

public class WLCCommand implements CommandExecutor {



    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("WLC - A chestshop and trade interaction plugin");
            player.sendMessage("Developed by LibreMC team");
            player.sendMessage("Source: https://github.com/LibreMC/WeLoveCapitalism");
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {

            if (args.length != 3) {
                Log.playerMessage(player, "Proper usage: /wlc set <amount> <price>");
                return true;
            }

            int amount = 0;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Log.playerMessage(player, "The amount isn't a valid number!");
                return true;
            }

            if (amount <= 0) {
                Log.playerMessage(player, "The amount must be positive!");
                return true;
            } else if (amount > 64) {
                Log.playerMessage(player, "The amount cannot be over 64!");
                return true;
            }

            int price = 0;
            try {
                price = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                Log.playerMessage(player, "The price isn't a valid number!");
                return true;
            }

            if (price <= 0) {
                Log.playerMessage(player, "The price must be positive!");
                return true;
            } else if (price > 20736) {
                Log.playerMessage(player, "The price cannot be over 20736g!");
                return true;
            }

            WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);
            if (!wlcplayer.isCreatingShop()) {
                Log.playerMessage(player, "You are not creating a shop!");
                return true;
            }

            ItemStack stack = player.getInventory().getItemInMainHand().clone();

            if(stack.getType() == Material.AIR){
                Log.playerMessage(player, "Can't sell nothing!");
                return true;
            }

            stack.setAmount(amount);

            wlcplayer.setSetItem(stack);

            wlcplayer.getTask().cancel(); // Cancel timeout

            // Process the shop creation
            assert wlcplayer.getSignBlock() != null;
            Sign sign = (Sign) wlcplayer.getSignBlock().getState();

            ChestShopManager.writeToSign(sign, player);

            Log.playerMessage(player, ChatColor.AQUA + "Created a chestshop");

            assert wlcplayer.getChestBlock() != null;
            ChestShop shop = new ChestShop(sign, (Chest) wlcplayer.getChestBlock().getState(), player.getUniqueId().toString(), stack, price);

            ChestShopManager.addChestShop(shop);

            wlcplayer.setCreatingShop(false);


        } else if (args[0].equalsIgnoreCase("multiplier")) {
            if (args.length != 2) {
                Log.playerMessage(player, "Proper usage: /wlc multiplier <multiplier>");
            }

            WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Log.playerMessage(player, "Multiplier isn't a valid number");
                return true;
            }

            if (amount <= 0) {
                Log.playerMessage(player, "Multiplier must be positive");
                return true;
            } else if (amount > 100) {
                Log.playerMessage(player, "Multiplier can't be over 100x");
                return true;
            }

            wlcplayer.setMultiplier(amount);

            Log.playerMessage(player, ChatColor.AQUA + "Set your buying multiplier to " + amount);
        } else if (args[0].equalsIgnoreCase("get")) {
            if (args.length < 3) {
                Log.playerMessage(player, "Proper usage: /wlc get tariffs/embargoes/upkeep <nation/town>");
                return true;
            }

            String government_name = args[2];

            Nation nation = TownyAPI.getInstance().getNation(government_name);
            Town town = TownyAPI.getInstance().getTown(government_name);

            switch (args[1].toLowerCase()) {
                case "tariffs":

                    if (nation == null && town == null) {
                        Log.playerMessage(player, "Government doesn't exist!");
                        return true;
                    }

                    HashSet<Tariff> tariffs;

                    if (nation != null) {
                        try {
                            tariffs = ChestShopDatabase.getTariffsByGovernment(nation.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        player.sendMessage("\nNations/towns tariffed by " + ChatColor.AQUA + ChatColor.BOLD + government_name + ChatColor.RESET);

                        if (tariffs.isEmpty()) {
                            player.sendMessage("None");
                        } else {
                            for (Tariff tariff : tariffs) {

                                String prefix = ChatColor.DARK_PURPLE + "";

                                if (!tariff.isActive()) {
                                    prefix = ChatColor.GRAY + "(Inactive) ";
                                }

                                Nation _nation = TownyAPI.getInstance().getNation(tariff.getGovernmentTariffed());
                                Town _town = TownyAPI.getInstance().getTown(tariff.getGovernmentTariffed());

                                int upkeep = TradeManager.getTradeLawUpkeep(nation, tariff.getGovernmentTariffed(), Upkeep.TradeLawType.TARIFF);

                                if (_nation == null && _town == null) {
                                    continue;
                                }

                                if (_nation == null) {
                                    String town_tariffed_name = _town.getName();
                                    player.sendMessage(prefix + ChatColor.BOLD + town_tariffed_name + " (town)" + ChatColor.RESET + ": " + ChatColor.GOLD + tariff.getPercentage() + "%" + ChatColor.RESET);
                                    player.sendMessage(ChatColor.AQUA + "To be paid at the end of the day: " + ChatColor.GOLD + ChatColor.BOLD + upkeep + "g");

                                } else {
                                    String nation_tariffed_name = _nation.getName();
                                    player.sendMessage(prefix + ChatColor.BOLD + nation_tariffed_name + " (nation)" + ChatColor.RESET + ": " + ChatColor.GOLD + tariff.getPercentage() + "%" + ChatColor.RESET);
                                    player.sendMessage(ChatColor.AQUA + "To be paid at the end of the day: " + ChatColor.GOLD + ChatColor.BOLD + upkeep + "g");
                                }

                            }
                        }
                    }

                    if (town != null) {
                        try {
                            tariffs = ChestShopDatabase.getTariffsToGovernment(town.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            tariffs = ChestShopDatabase.getTariffsToGovernment(nation.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }


                    player.sendMessage("\nNations tariffing " + ChatColor.AQUA + ChatColor.BOLD + government_name + ChatColor.RESET);

                    if (tariffs.isEmpty()) {
                        player.sendMessage("None");
                    } else {
                        for (Tariff tariff : tariffs) {
                            String prefix = ChatColor.DARK_PURPLE + "";

                            if (!tariff.isActive()) {
                                prefix = ChatColor.GRAY + "(Inactive) ";
                            }

                            Nation _nation = TownyAPI.getInstance().getNation(tariff.getGovernmentTariffing());

                            if (_nation == null) {
                                continue;
                            }

                            String nation_tariffed_name = _nation.getName();

                            player.sendMessage(prefix + ChatColor.BOLD + nation_tariffed_name + ChatColor.RESET + ": " + ChatColor.GOLD + tariff.getPercentage() + "%" + ChatColor.RESET);
                        }
                    }
                    return true;

                case "embargoes":
                    if (nation == null && town == null) {
                        Log.playerMessage(player, "Government doesn't exist!");
                        return true;
                    }

                    if (nation != null) {
                        HashSet<Embargo> embargoed_governments;
                        try {
                            embargoed_governments = ChestShopDatabase.getEmbargoesByGovernment(nation.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        player.sendMessage("\nNations/towns embargoed by " + ChatColor.AQUA + ChatColor.BOLD + government_name + ChatColor.RESET);

                        if (!embargoed_governments.isEmpty()) {
                            for (Embargo embargo : embargoed_governments) {

                                String prefix = ChatColor.DARK_PURPLE + "";

                                if (!embargo.isActive()) {
                                    prefix = ChatColor.GRAY + "(Inactive) ";
                                }

                                Nation _nation = TownyAPI.getInstance().getNation(embargo.getEmbargoedNation());
                                Town _town = TownyAPI.getInstance().getTown(embargo.getEmbargoedNation());

                                int upkeep = TradeManager.getTradeLawUpkeep(nation, embargo.getEmbargoedNation(), Upkeep.TradeLawType.EMBARGO);


                                if (_nation != null) {
                                    player.sendMessage(prefix + ChatColor.BOLD + _nation.getName() + " (nation)" + ChatColor.RESET);
                                    player.sendMessage(ChatColor.AQUA + "To be paid at the end of the day: " + ChatColor.GOLD + ChatColor.BOLD + upkeep + "g");

                                } else if (_town != null) {
                                    player.sendMessage(prefix + ChatColor.BOLD + _town.getName() + " (town)" + ChatColor.RESET);
                                    player.sendMessage(ChatColor.AQUA + "To be paid at the end of the day: " + ChatColor.GOLD + ChatColor.BOLD + upkeep + "g");
                                } else {
                                    /* If government doesn't exist anymore then remove it */
                                    EmbargoManager.removeEmbargo(new Embargo(nation.getUUID(), embargo.getEmbargoedNation()));
                                }
                            }
                        } else {
                            player.sendMessage("None");
                        }
                    }

                    HashSet<Embargo> embargoing_nations;

                    if (nation != null) {
                        try {
                            embargoing_nations = ChestShopDatabase.getEmbargoesToGovernment(nation.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            embargoing_nations = ChestShopDatabase.getEmbargoesToGovernment(town.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }


                    player.sendMessage("\nNations embargoing " + ChatColor.AQUA + ChatColor.BOLD + government_name + ChatColor.RESET);

                    if (!embargoing_nations.isEmpty()) {

                        for (Embargo embargo : embargoing_nations) {

                            String prefix = ChatColor.DARK_PURPLE + "";


                            if (!embargo.isActive()) {
                                prefix = ChatColor.GRAY + "(Inactive) ";
                            }

                            Nation _nation = TownyAPI.getInstance().getNation(embargo.getEmbargoingNation());
                            Town _town = TownyAPI.getInstance().getTown(embargo.getEmbargoingNation());

                            if (_nation != null) {
                                player.sendMessage(prefix + ChatColor.BOLD + _nation.getName() + " (nation)" + ChatColor.RESET);
                            } else if (_town != null) {
                                player.sendMessage(prefix + ChatColor.BOLD + _town.getName() + " (town)" + ChatColor.RESET);
                            } else {
                                /* If government doesn't exist anymore then remove it */
                                EmbargoManager.removeEmbargo(new Embargo(Objects.requireNonNullElse(nation, town).getUUID(), embargo.getEmbargoingNation()));
                            }
                        }
                    } else {
                        player.sendMessage("None");
                    }
                    return true;
                case "upkeep":
                    if (nation == null) {
                        Log.playerMessage(player, "Nation does not exist!");
                        return true;
                    }

                    HashSet<Upkeep> upkeeps = TradeManager.getTradeLawUpkeep(nation);

                    int total = 0;

                    assert upkeeps != null;
                    for (Upkeep upkeep : upkeeps) {
                        total += upkeep.getUpkeep();
                    }

                    player.sendMessage(ChatColor.AQUA + government_name + " has to pay " + ChatColor.GOLD + total + "g" + ChatColor.AQUA + " by the end of the day");

                    return true;
                default:
                    Log.playerMessage(player, "Proper usage: /wlc get tariffs/embargoes/upkeep <nation/town>");
                    return true;

            }

        } else if (args[0].equalsIgnoreCase("introduce")) {
            if (args.length < 3) {
                Log.playerMessage(player, "Proper usage: /wlc introduce tariffs/embargoes <nation/town> <percentage>");
                return true;
            }

            Nation player_nation = TownyAPI.getInstance().getNation(player);
            String government_name = args[2];

            Nation nation = TownyAPI.getInstance().getNation(government_name);
            Town town = TownyAPI.getInstance().getTown(government_name);

            if (player_nation == null) {
                Log.playerMessage(player, "You are not in a nation and therefore cannot introduce new trade laws");
                return true;
            }

            if (!Objects.requireNonNull(TownyAPI.getInstance().getResident(player)).isKing()) {
                Log.playerMessage(player, "Only the leader of a nation can introduce trade laws");
                return true;
            }

            if (player_nation.equals(nation)) {
                Log.playerMessage(player, "You can't add trade laws against your own nation!");
                return true;
            }

            if (player_nation.hasMeta(InsolvencyManager.INSOLVENCY_METADATA_KEY)) {
                Log.playerMessage(player, "You must first pay off your nation's insolvency before you can introduce any new trade laws");
                return true;
            }

            int player_nation_monies = (int) player_nation.getAccount().getHoldingBalance(true);


            switch (args[1].toLowerCase()) {
                case "tariffs":

                    if (args.length < 4) {
                        Log.playerMessage(player, "Proper usage: /wlc introduce tariffs <nation/town> <percentage>");
                        return true;
                    }

                    int percentage;

                    try {
                        percentage = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        Log.playerMessage(player, "Percentage isn't a valid number!");
                        return true;
                    }

                    if (percentage <= 0) {
                        Log.playerMessage(player, "Percentage must be a positive number!");
                        return true;
                    } else if (percentage > 100) {
                        Log.playerMessage(player, "Tariffs cannot be over 100%!");
                        return true;
                    }

                    if (town != null) {
                        Tariff existing_tariff;
                        if ((existing_tariff = TariffManager.getTariff(town, player_nation)) != null) {

                            /* Make people pay for changing their tariff percentage to a lower value */
                            if(existing_tariff.getPercentage() > percentage){
                                int total = TradeManager.getTotalForeignTrade(town.getUUID(), TradeManager.DAY_MILLISECONDS, false);
                                int upkeep = TradeManager.tariffUpkeep(total, existing_tariff.getPercentage()) - TradeManager.tariffUpkeep(total, percentage);

                                if(upkeep > player_nation_monies){
                                    Log.playerMessage(player, "Your nation doesn't have enough gold to pay for the premature " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "upkeep!");
                                    return true;
                                }

                                int balance_after = player_nation_monies - upkeep;

                                if(balance_after <= 0){
                                    balance_after = 0;
                                }

                                player_nation.getAccount().setBalance(balance_after, "WLC - Premature tariff upkeep");

                                Log.playerMessage(player, "Paid " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "for the premature upkeep");

                            }

                            TariffManager.removeTariff(existing_tariff);
                            existing_tariff.setPercentage(percentage);
                            TariffManager.addTariff(existing_tariff);
                            Log.playerMessage(player, "Modified the tariff on " + government_name + " (town) to " + percentage + "%!");
                            return true;
                        }


                        int upkeep = TradeManager.tariffUpkeep(TradeManager.getTotalForeignTrade(town.getUUID(), TradeManager.WEEK_MILLISECONDS, false), percentage);


                        Log.playerMessage(player, "This trade law will by current trade volume cost " + ChatColor.GOLD + upkeep + "g" + ChatColor.AQUA + " at the end of the day!");
                        Log.playerMessage(player, "Are you sure you want to continue?");


                        TextComponent view_text = new TextComponent("[Confirm]");
                        view_text.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                        view_text.setBold(true);
                        view_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-confirm_tariffs town " + player_nation.getUUID() + " " + town.getUUID() + " " + percentage));

                        player.spigot().sendMessage(view_text);

                        return true;
                    } else if (nation != null) {
                        Tariff existing_tariff;
                        if ((existing_tariff = TariffManager.getTariff(nation, player_nation)) != null) {

                            if(existing_tariff.getPercentage() > percentage){
                                int total = TradeManager.getTotalForeignTrade(nation.getUUID(), TradeManager.DAY_MILLISECONDS, false);
                                int upkeep = TradeManager.tariffUpkeep(total, existing_tariff.getPercentage()) - TradeManager.tariffUpkeep(total, percentage);

                                if(upkeep > player_nation_monies){
                                    Log.playerMessage(player, "Your nation doesn't have enough gold to pay for the premature " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "upkeep!");
                                    return true;
                                }

                                int balance_after = player_nation_monies - upkeep;

                                if(balance_after <= 0){
                                    balance_after = 0;
                                }

                                player_nation.getAccount().setBalance(balance_after, "WLC - Premature tariff upkeep");

                                Log.playerMessage(player, "Paid " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "for the premature upkeep");

                            }

                            TariffManager.removeTariff(existing_tariff);
                            existing_tariff.setPercentage(percentage);
                            TariffManager.addTariff(existing_tariff);
                            Log.playerMessage(player, "Modified the tariff on " + government_name + " (nation) to " + percentage + "%!");
                            return true;
                        }

                        int upkeep = TradeManager.tariffUpkeep(TradeManager.getTotalForeignTrade(nation.getUUID(), TradeManager.WEEK_MILLISECONDS, false), percentage);


                        Log.playerMessage(player, "This trade law will by current trade volume cost " + ChatColor.GOLD + upkeep + "g" + ChatColor.AQUA + " at the end of the day!");
                        Log.playerMessage(player, "Are you sure you want to continue?");

                        TextComponent view_text = new TextComponent("[Confirm]");
                        view_text.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                        view_text.setBold(true);
                        view_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-confirm_tariffs nation " + player_nation.getUUID() + " " + nation.getUUID() + " " + percentage));

                        player.spigot().sendMessage(view_text);

                        return true;
                    } else {
                        Log.playerMessage(player, "Government doesn't exist!");
                        return true;
                    }

                case "embargoes":
                    if (args.length != 3) {
                        Log.playerMessage(player, "Proper usage: /wlc introduce embargoes <nation/town>");
                        return true;
                    }

                    if (town != null) {
                        if (EmbargoManager.getEmbargo(town, player_nation) != null) {
                            Log.playerMessage(player, "This government is already embargoed by you!");
                            return true;
                        }

                        int upkeep = TradeManager.embargoUpkeep(TradeManager.getTotalForeignTrade(town.getUUID(), TradeManager.WEEK_MILLISECONDS, false));


                        Log.playerMessage(player, "This trade law will by current trade volume cost " + ChatColor.GOLD + upkeep + "g" + ChatColor.AQUA + " at the end of the day!");
                        Log.playerMessage(player, "Are you sure you want to continue?");

                        TextComponent view_text = new TextComponent("[Confirm]");
                        view_text.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                        view_text.setBold(true);
                        view_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-confirm_embargoes town " + player_nation.getUUID() + " " + town.getUUID()));

                        player.spigot().sendMessage(view_text);

                        return true;
                    } else if (nation != null) {
                        if (EmbargoManager.getEmbargo(nation, player_nation) != null) {
                            Log.playerMessage(player, "This government is already embargoed by you!");
                            return true;
                        }

                        int upkeep = TradeManager.embargoUpkeep(TradeManager.getTotalForeignTrade(nation.getUUID(), TradeManager.WEEK_MILLISECONDS, false));

                        Log.playerMessage(player, "This trade law will by current trade volume cost " + ChatColor.GOLD + upkeep + "g" + ChatColor.AQUA + " at the end of the day!");
                        Log.playerMessage(player, "Are you sure you want to continue?");

                        TextComponent view_text = new TextComponent("[Confirm]");
                        view_text.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                        view_text.setBold(true);
                        view_text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wlc-internal-confirm_embargoes nation " + player_nation.getUUID() + " " + nation.getUUID()));

                        player.spigot().sendMessage(view_text);
                        return true;
                    } else {
                        Log.playerMessage(player, "Government doesn't exist!");
                        return true;
                    }
                default:
                    Log.playerMessage(player, "Proper usage: /wlc introduce embargo <nation/town>");
                    return true;


            }
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length != 3) {
                Log.playerMessage(player, "Proper usage: /wlc remove tariff/embargo <nation/town>");
                return true;
            }

            Nation player_nation = TownyAPI.getInstance().getNation(player);
            String government_name = args[2];

            Nation nation = TownyAPI.getInstance().getNation(government_name);
            Town town = TownyAPI.getInstance().getTown(government_name);

            if (nation == null && town == null) {
                Log.playerMessage(player, "Government " + args[2] + " doesn't exist!");
                return true;
            }

            if (player_nation == null) {
                Log.playerMessage(player, "You are not in a nation and therefore cannot remove trade laws");
                return true;
            }

            if (!Objects.requireNonNull(TownyAPI.getInstance().getResident(player)).isKing()) {
                Log.playerMessage(player, "Only the leader of a nation can remove trade laws");
                return true;
            }

            if (player_nation.equals(nation)) {
                Log.playerMessage(player, "You can't remove trade laws against your own nation!");
                return true;
            }

            int nation_monies = (int) player_nation.getAccount().getHoldingBalance(true);

            switch (args[1].toLowerCase()) {
                case "tariffs":
                    if (town != null) {
                        Tariff tariff;
                        if ((tariff = TariffManager.getTariff(town, player_nation)) != null) {
                            int total = TradeManager.getTotalForeignTrade(town.getUUID(), TradeManager.DAY_MILLISECONDS, false);
                            int upkeep = TradeManager.tariffUpkeep(total, tariff.getPercentage());

                            if(upkeep > nation_monies){
                                Log.playerMessage(player, "Your nation doesn't have enough gold to pay for the " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "upkeep!");
                                return true;
                            }

                            Log.playerMessage(player, "Paid " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "for the upkeep");

                            int after = nation_monies - upkeep;

                            if(after < 0){
                                after = 0;
                            }

                            player_nation.getAccount().setBalance(after, "Trade law upkeep - removal");


                            Log.playerMessage(player, "Removed the tariff on " + town.getName() + " (town)");
                            TariffManager.removeTariff(tariff);
                            return true;
                        }
                    }

                    if (nation != null) {
                        Tariff tariff;
                        if ((tariff = TariffManager.getTariff(nation, player_nation)) != null) {

                            int total = TradeManager.getTotalForeignTrade(nation.getUUID(), TradeManager.DAY_MILLISECONDS, false);
                            int upkeep = TradeManager.tariffUpkeep(total, tariff.getPercentage());

                            if(upkeep > nation_monies){
                                Log.playerMessage(player, "Your nation doesn't have enough gold to pay for the " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "upkeep!");
                                return true;
                            }

                            Log.playerMessage(player, "Paid " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "for the upkeep");

                            int after = nation_monies - upkeep;

                            if(after < 0){
                                after = 0;
                            }

                            player_nation.getAccount().setBalance(after, "Trade law upkeep - removal");

                            Log.playerMessage(player, "Removed the tariff on " + nation.getName() + " (nation)");
                            TariffManager.removeTariff(tariff);
                            return true;
                        }
                    }

                    Log.playerMessage(player, "This government isn't tariffed!");
                    return true;
                case "embargoes":
                    if (town != null) {
                        Embargo embargo;
                        if ((embargo = EmbargoManager.getEmbargo(town, player_nation)) != null) {

                            int total = TradeManager.getTotalForeignTrade(town.getUUID(), TradeManager.DAY_MILLISECONDS, false);
                            int upkeep = TradeManager.embargoUpkeep(total);

                            if(upkeep > nation_monies){
                                Log.playerMessage(player, "Your nation doesn't have enough gold to pay for the " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "upkeep!");
                                return true;
                            }

                            Log.playerMessage(player, "Paid " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "for the upkeep");

                            int after = nation_monies - upkeep;

                            if(after < 0){
                                after = 0;
                            }

                            player_nation.getAccount().setBalance(after, "Trade law upkeep - removal");
                            Log.playerMessage(player, "Removed embargo on " + town.getName() + " (town)!");
                            EmbargoManager.removeEmbargo(embargo);
                            return true;
                        }
                    }

                    if (nation != null) {
                        Embargo embargo;
                        if ((embargo = EmbargoManager.getEmbargo(nation, player_nation)) != null) {

                            int total = TradeManager.getTotalForeignTrade(nation.getUUID(), TradeManager.DAY_MILLISECONDS, false);
                            int upkeep = TradeManager.embargoUpkeep(total);

                            if(upkeep > nation_monies){
                                Log.playerMessage(player, "Your nation doesn't have enough gold to pay for the " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "upkeep!");
                                return true;
                            }

                            Log.playerMessage(player, "Paid " + ChatColor.GOLD + upkeep + "g " + ChatColor.AQUA + "for the upkeep");

                            int after = nation_monies - upkeep;

                            if(after < 0){
                                after = 0;
                            }

                            player_nation.getAccount().setBalance(after, "Trade law upkeep - removal");

                            Log.playerMessage(player, "Removed embargo on " + nation.getName() + " (nation)!");
                            EmbargoManager.removeEmbargo(embargo);
                            return true;
                        }
                    }

                    Log.playerMessage(player, "This government isn't embargoed!");
                    return true;
                default:
                    Log.playerMessage(player, "Proper usage: /wlc remove tariff/embargo <nation/town>");
                    return true;

            }
        } else if (args[0].equalsIgnoreCase("insolvency")) {
            if (args.length > 2) {
                Log.playerMessage(player, "Proper usage: /wlc insolvency (pay)");
                return true;
            }

            Nation nation = TownyAPI.getInstance().getNation(player);
            if (nation == null) {
                Log.playerMessage(player, "You're not in a nation!");
                return true;
            }

            int insolvency = InsolvencyManager.getInsolvency(nation);

            if (insolvency == 0) {
                Log.playerMessage(player, "This nation isn't insolvent");
                return true;
            }

            if (args.length < 2) {
                Log.playerMessage(player, "This nation has to pay " + ChatColor.GOLD + insolvency + "g" + ChatColor.AQUA + " in insolvency!");
                return true;
            }

            if (!args[1].equalsIgnoreCase("pay")) {
                Log.playerMessage(player, "Proper usage: /wlc insolvency pay");
                return true;
            }

            int player_gold = (int) WeLoveCapitalism.getEconomy().getBalance(player);

            if (player_gold < insolvency) {
                Log.playerMessage(player, "You don't have enough money to pay for the insolvency!");
                Log.playerMessage(player, "You need the gold to be in your inventory");
                return true;
            }

            WeLoveCapitalism.getEconomy().withdrawPlayer(player, insolvency);

            Log.playerMessage(player, "Successfully paid " + ChatColor.GOLD + insolvency + "g" + ChatColor.AQUA + " for " + nation.getName() + "'s insolvency!");

            InsolvencyManager.removeInsolvency(nation);

            return true;
        }else if(args[0].equalsIgnoreCase("top")){
            if(args.length != 2){
                Log.playerMessage(player, "Proper usage: /wlc top nation/town");
                return true;
            }

            final long WEEK = 7 * 24 * 60 * 60 * 1000;

            switch (args[1]){
                case "nation":
                    List<Nation> nations = TownyAPI.getInstance().getNations();
                    int top_amount = Math.min(nations.size(), 7);

                    if (top_amount == 0) {
                        player.sendMessage("There are no nations found");
                        return true;
                    }

                    class NationTrade {
                        final Nation nation;
                        final int total;

                        public NationTrade(Nation nation, int total) {
                            this.nation = nation;
                            this.total = total;
                        }
                    }

                    List<NationTrade> nation_trades = new ArrayList<>();
                    for (Nation nation : nations) {
                        nation_trades.add(new NationTrade(nation, TradeManager.getTotalForeignTrade(nation.getUUID(), TradeManager.WEEK_MILLISECONDS, false)));
                    }

                    nation_trades.sort((a, b) -> Integer.compare(b.total, a.total));

                    player.sendMessage(ChatColor.AQUA + "Top " + top_amount + " nations by total foreign trade income the past week:");

                    for (int i = 0; i < top_amount; i++) {
                        NationTrade nt = nation_trades.get(i);
                        player.sendMessage(ChatColor.AQUA + "Num. " + (i + 1) + ": " + nt.nation.getName() + " total: " + ChatColor.GOLD + nt.total + "g");
                    }

                    return true;

                case "town":
                    List<Town> towns = TownyAPI.getInstance().getTowns();
                    int town_top_amount = Math.min(towns.size(), 7);

                    if (town_top_amount == 0) {
                        player.sendMessage("There are no nations found");
                        return true;
                    }

                    class TownTrade {
                        final Town town;
                        final int total;

                        public TownTrade(Town town, int total) {
                            this.town = town;
                            this.total = total;
                        }
                    }

                    List<TownTrade> town_trades = new ArrayList<>();
                    for (Town town : towns) {
                        town_trades.add(new TownTrade(town, TradeManager.getTotalForeignTrade(town.getUUID(), TradeManager.WEEK_MILLISECONDS, false)));
                    }

                    town_trades.sort((a, b) -> Integer.compare(b.total, a.total));

                    player.sendMessage(ChatColor.AQUA + "Top " + town_top_amount + " towns by total foreign trade income the past week:");

                    for (int i = 0; i < town_top_amount; i++) {
                        TownTrade tt = town_trades.get(i);
                        player.sendMessage(ChatColor.AQUA + "Num. " + (i + 1) + ": " + tt.town.getName() + " total: " + ChatColor.GOLD + tt.total + "g");
                    }

                    return true;

                default:
                    Log.playerMessage(player, "Proper usage: /wlc top nation/town");
                    return true;
            }
        }else if(args[0].equalsIgnoreCase("disable")){
            if(args.length != 2){
                Log.playerMessage(player, "Proper usage: /wlc disable on/off");
                return true;
            }

            WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);

            switch (args[1]){
                case "on":
                    if(wlcplayer.isCreationEnabled()){
                        Log.playerMessage(player, "You already have chestshop creation enabled!");
                        return true;
                    }

                    Log.playerMessage(player, "Enabled chestshop creation");

                    wlcplayer.setCreationEnabled(true);
                    return true;
                case "off":
                    if(!wlcplayer.isCreationEnabled()){
                        Log.playerMessage(player, "You already have chestshop creation disabled!");
                        return true;
                    }

                    Log.playerMessage(player, "Disabled chestshop creation");

                    wlcplayer.setCreationEnabled(false);
                    return true;
                default:
                    Log.playerMessage(player, "Proper usage: /wlc disable on/off");
                    return true;

            }

        }else if(args[0].equalsIgnoreCase("help")){
            Log.playerMessage(player, "Chest shop plugin");
            player.sendMessage(ChatColor.AQUA + "/wlc multiplier <multiplier> -  " +ChatColor.DARK_AQUA + "A multiplier to how many items you will buy. It multiplies with the 'amount' of items sold in a chest shop");
            player.sendMessage(ChatColor.AQUA + "/wlc get tariffs/embargoes/upkeep <nation> - " +ChatColor.DARK_AQUA + "Shows you the embargoes and tariffs that 'nation' has set and is receiving");
            player.sendMessage(ChatColor.AQUA + "/wlc introduce tariffs/embargoes <nation/town> - " +ChatColor.DARK_AQUA + "Adds a tariff or embargo on a nation or town");
            player.sendMessage(ChatColor.AQUA + "/wlc remove tariffs/embargoes <nation/town> - " +ChatColor.DARK_AQUA + "Removes a tariff or embargo on a nation or town");
            player.sendMessage(ChatColor.AQUA + "/wlc insolvency (pay) - " +ChatColor.DARK_AQUA + "Get the insolvency of your nation or pay it with /wlc insolvency pay");
            player.sendMessage(ChatColor.AQUA + "/wlc top nation/town - " +ChatColor.DARK_AQUA + "Get the top 7 nations or towns by total foreign trade income the past week");
            player.sendMessage(ChatColor.AQUA + "/wlc disable on/off - " +ChatColor.DARK_AQUA + "Make it so that you won't go into shop creation mode when putting signs on chests");

            return true;
        }else{
            Log.playerMessage(player, "Unknown subcommand `" + args[0] + "'");
            Log.playerMessage(player, "Do `/wlc help' for help!");
            return true;
        }


        return true;
    }

}
