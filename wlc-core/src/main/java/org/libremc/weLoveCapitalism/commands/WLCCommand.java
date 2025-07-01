package org.libremc.weLoveCapitalism.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import org.bukkit.ChatColor;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class WLCCommand implements CommandExecutor {



    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {

        if(!(sender instanceof Player player)){
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if(args.length == 0){
            player.sendMessage("WLC - A chestshop and trade interaction plugin");
            player.sendMessage("Developed by LibreMC team");
            player.sendMessage("Source: https://github.com/LibreMC/WeLoveCapitalism");
            return true;
        }

        if(args[0].equalsIgnoreCase("set")){

            if(args.length != 3){
                Log.playerMessage(player, "Proper usage: /wlc set <amount> <price>");
            }

            int amount = 0;
            try {
                amount = Integer.parseInt(args[1]);
            }catch (NumberFormatException e){
                Log.playerMessage(player, "The amount isn't a valid number!");
            }

            if(amount <= 0){
                Log.playerMessage(player, "The amount must be positive!");
                return true;
            }else if(amount > 64){
                Log.playerMessage(player, "The amount cannot be over 64!");
                return true;
            }

            int price = 0;
            try {
                price = Integer.parseInt(args[2]);
            }catch (NumberFormatException e){
                Log.playerMessage(player, "The price isn't a valid number!");
            }

            if(price < 0){
                Log.playerMessage(player, "The price cannot be negative!");
                return true;
            }else if(price > 20736){
                Log.playerMessage(player, "The price cannot be over 20736g!");
                return true;
            }

            WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);
            if(!wlcplayer.isCreatingShop()){
                Log.playerMessage(player, "You are not creating a shop!");
                return true;
            }

            ItemStack stack = player.getInventory().getItemInMainHand().clone();

            stack.setAmount(amount);

            wlcplayer.setSetItem(stack);

            wlcplayer.getTask().cancel(); // Cancel timeout

            // Process the shop creation
            Sign sign = (Sign) wlcplayer.getSignBlock().getState();
            ChestShopManager.writeToSign(sign, player);

            Log.playerMessage(player, ChatColor.AQUA + "Created a chestshop");

            ChestShop shop = new ChestShop(sign, (Chest)wlcplayer.getChestBlock().getState(), player.getUniqueId().toString(), stack, price);

            ChestShopManager.addChestShop(shop);

            wlcplayer.setCreatingShop(false);


        }else if(args[0].equalsIgnoreCase("multiplier")){
            if(args.length != 2){
                Log.playerMessage(player, "Proper usage: /wlc multiplier <multiplier>");
            }

            WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            }catch (NumberFormatException e){
                Log.playerMessage(player, "Multiplier isn't a valid number");
                return true;
            }

            if(amount <= 0){
                Log.playerMessage(player, "Multiplier must be positive");
                return true;
            }else if(amount > 100){
                Log.playerMessage(player, "Multiplier can't be over 100x");
                return true;
            }

            wlcplayer.setMultiplier(amount);

            Log.playerMessage(player, ChatColor.AQUA + "Set your buying multiplier to " + amount);
        }else if(args[0].equalsIgnoreCase("get")){
            if(args.length < 3){
                Log.playerMessage(player, "Proper usage: /wlc get tariffs/embargoes <nation/town>");
                return true;
            }

            String government_name = args[2];

            switch(args[1].toLowerCase()){
                case "tariffs":
                    Nation nation = TownyAPI.getInstance().getNation(government_name);
                    Town town = TownyAPI.getInstance().getTown(government_name);


                    if(nation == null && town == null){

                        return true;
                    }

                    HashSet<Tariff> tariffs;

                    if(nation != null){
                        try {
                            tariffs = ChestShopDatabase.getTariffsByGovernment(nation.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        player.sendMessage("\nNations/towns tariffed by "+ ChatColor.AQUA + ChatColor.BOLD + government_name + ChatColor.RESET);

                        if(tariffs.isEmpty()){
                            player.sendMessage("None");
                        }else{
                            for(Tariff tariff : tariffs){
                                Nation _nation = TownyAPI.getInstance().getNation(tariff.getGovernmentTariffed());
                                Town _town = TownyAPI.getInstance().getTown(tariff.getGovernmentTariffed());

                                int upkeep = TradeManager.getTradeLawUpkeep(nation, tariff.getGovernmentTariffed(), Upkeep.TradeLawType.TARIFF);

                                if(_nation == null && _town == null){
                                    continue;
                                }

                                if(_nation == null){
                                    String town_tariffed_name = _town.getName();
                                    player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + town_tariffed_name + " (town)" + ChatColor.RESET + ": " + ChatColor.GOLD + tariff.getPercentage() + "%" + ChatColor.RESET);
                                    player.sendMessage(ChatColor.AQUA + "To be paid at the end of the day: " + ChatColor.GOLD + ChatColor.BOLD + upkeep + "g");

                                }else{
                                    String nation_tariffed_name = _nation.getName();
                                    player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + nation_tariffed_name + " (nation)" + ChatColor.RESET + ": " + ChatColor.GOLD + tariff.getPercentage() + "%" + ChatColor.RESET);
                                    player.sendMessage(ChatColor.AQUA + "To be paid at the end of the day: " + ChatColor.GOLD + ChatColor.BOLD + upkeep + "g");
                                }

                            }
                        }
                    }

                    if(town != null){
                        try {
                            tariffs = ChestShopDatabase.getTariffsToGovernment(town.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }else {
                        try {
                            tariffs = ChestShopDatabase.getTariffsToGovernment(nation.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }


                    player.sendMessage("\nNations tariffing "+ ChatColor.AQUA + ChatColor.BOLD + government_name + ChatColor.RESET);

                    if(tariffs.isEmpty()){
                        player.sendMessage("None");
                    }else{
                        for(Tariff tariff : tariffs){
                            Nation _nation = TownyAPI.getInstance().getNation(tariff.getGovernmentTariffing());

                            if(_nation == null){
                                continue;
                            }

                            String nation_tariffed_name = _nation.getName();

                            player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + nation_tariffed_name + ChatColor.RESET + ": " + ChatColor.GOLD + tariff.getPercentage() + "%" + ChatColor.RESET);
                        }
                    }
                    return true;

                case "embargoes":
                    Nation embargo_nation = TownyAPI.getInstance().getNation(government_name);
                    Town embargo_town = TownyAPI.getInstance().getTown(government_name);

                    if(embargo_nation == null && embargo_town == null){
                        Log.playerMessage(player, "Government doesn't exist!");
                        return true;
                    }

                    if(embargo_nation != null){
                        HashSet<Embargo> embargoed_governments;
                        try {
                            embargoed_governments = ChestShopDatabase.getEmbargoesByGovernment(embargo_nation.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        player.sendMessage("\nNations/towns embargoed by "+ ChatColor.AQUA + ChatColor.BOLD + government_name + ChatColor.RESET);

                        if(!embargoed_governments.isEmpty()){
                            for(Embargo embargo : embargoed_governments){
                                Nation _nation = TownyAPI.getInstance().getNation(embargo.getEmbargoedNation());
                                Town _town = TownyAPI.getInstance().getTown(embargo.getEmbargoedNation());

                                int upkeep = TradeManager.getTradeLawUpkeep(embargo_nation, embargo.getEmbargoedNation(), Upkeep.TradeLawType.EMBARGO);



                                if(_nation != null){
                                    player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + _nation.getName() + " (nation)" + ChatColor.RESET);
                                    player.sendMessage(ChatColor.AQUA + "To be paid at the end of the day: " + ChatColor.GOLD + ChatColor.BOLD + upkeep + "g");

                                }else if(_town != null){
                                    player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + _town.getName() + " (town)" + ChatColor.RESET);
                                    player.sendMessage(ChatColor.AQUA + "To be paid at the end of the day: " + ChatColor.GOLD + ChatColor.BOLD + upkeep + "g");
                                }else{
                                    /* If government doesn't exist anymore then remove it */
                                    EmbargoManager.removeEmbargo(new Embargo(embargo_nation.getUUID(), embargo.getEmbargoedNation()));
                                }
                            }
                        }else{
                            player.sendMessage("None");
                        }
                    }

                    HashSet<Embargo> embargoing_nations;

                    if(embargo_nation != null){
                        try {
                            embargoing_nations = ChestShopDatabase.getEmbargoesToGovernment(embargo_nation.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        try {
                            embargoing_nations = ChestShopDatabase.getEmbargoesToGovernment(embargo_town.getUUID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }


                    player.sendMessage("\nNations embargoing "+ ChatColor.AQUA + ChatColor.BOLD + government_name + ChatColor.RESET);

                    if(!embargoing_nations.isEmpty()){
                        for(Embargo embargo : embargoing_nations){
                            Nation _nation = TownyAPI.getInstance().getNation(embargo.getEmbargoingNation());
                            Town _town = TownyAPI.getInstance().getTown(embargo.getEmbargoingNation());

                            if(_nation != null){
                                player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + _nation.getName() + " (nation)" + ChatColor.RESET);
                            }else if(_town != null){
                                player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + _town.getName() + " (town)" + ChatColor.RESET);
                            }else{
                                /* If government doesn't exist anymore then remove it */
                                EmbargoManager.removeEmbargo(new Embargo(Objects.requireNonNullElse(embargo_nation, embargo_town).getUUID(), embargo.getEmbargoingNation()));
                            }
                        }
                    }else{
                        player.sendMessage("None");
                    }
                    return true;

                default:
                    Log.playerMessage(player, "Proper usage: /wlc get tariffs/embargoes <nation/town>");
                    return true;

            }

        }else if(args[0].equalsIgnoreCase("introduce")){
            if(args.length < 3){
                Log.playerMessage(player, "Proper usage: /wlc introduce tariffs/embargoes <nation/town> <percentage>");
                return true;
            }

            Nation player_nation = TownyAPI.getInstance().getNation(player);
            String government_name = args[2];

            Nation nation = TownyAPI.getInstance().getNation(government_name);
            Town town = TownyAPI.getInstance().getTown(government_name);

            if(player_nation == null){
                Log.playerMessage(player, "You are not in a nation and therefore cannot introduce new trade laws");
                return true;
            }

            if(!Objects.requireNonNull(TownyAPI.getInstance().getResident(player)).isKing()){
                Log.playerMessage(player, "Only the leader of a nation can introduce trade laws");
                return true;
            }

            if(player_nation.equals(nation)){
                Log.playerMessage(player, "You can't add trade laws against your own nation!");
                return true;
            }

            switch(args[1].toLowerCase()){
                case "tariffs":

                    if(args.length < 4){
                        Log.playerMessage(player, "Proper usage: /wlc introduce tariffs <nation/town> <percentage>");
                        return true;
                    }

                    int percentage;

                    try {
                        percentage = Integer.parseInt(args[3]);
                    }catch (NumberFormatException e){
                        Log.playerMessage(player,"Percentage isn't a valid number!");
                        return true;
                    }

                    if(percentage <= 0){
                        Log.playerMessage(player,"Percentage must be a positive number!");
                        return true;
                    }else if(percentage > 100){
                        Log.playerMessage(player,"Tariffs cannot be over 100%!");
                        return true;
                    }

                    if(town != null){
                        Tariff existing_tariff;
                        if((existing_tariff = TariffManager.getTariff(town, player_nation)) != null){
                            TariffManager.removeTariff(existing_tariff);
                            existing_tariff.setPercentage(percentage);
                            TariffManager.addTariff(existing_tariff);
                            Log.playerMessage(player, "Modified the tariff on " + government_name + " (town) to " + percentage + "%!");
                            return true;
                        }

                        Tariff tariff = new Tariff(player_nation.getUUID(), town.getUUID(), percentage);

                        TariffManager.addTariff(tariff);

                        Log.playerMessage(player, "Set the tariff on " + government_name + " (town) to " + percentage + "%!");
                        return true;
                    }else if(nation != null){
                        Tariff existing_tariff;
                        if((existing_tariff = TariffManager.getTariff(nation, player_nation)) != null){
                            TariffManager.removeTariff(existing_tariff);
                            existing_tariff.setPercentage(percentage);
                            TariffManager.addTariff(existing_tariff);
                            Log.playerMessage(player, "Modified the tariff on " + government_name + " (nation) to " + percentage + "%!");
                            return true;
                        }

                        Tariff tariff = new Tariff(player_nation.getUUID(), nation.getUUID(), percentage);

                        TariffManager.addTariff(tariff);

                        Log.playerMessage(player, "Set the tariff on " + government_name + " (nation) to " + percentage + "%!");
                        return true;
                    }else{
                        Log.playerMessage(player, "Government doesn't exist!");
                        return true;
                    }

                case "embargoes":
                    if(args.length != 3){
                        Log.playerMessage(player, "Proper usage: /wlc introduce embargoes <nation/town>");
                        return true;
                    }

                    if(town != null){
                        if(EmbargoManager.getEmbargo(town, player_nation) != null){
                            Log.playerMessage(player, "This government is already embargoed by you!");
                            return true;
                        }

                        Embargo embargo = new Embargo(player_nation.getUUID(), town.getUUID());

                        EmbargoManager.addEmbargo(embargo);

                        Log.playerMessage(player, "Set embargo on " + town.getName() +" (town)!");
                        return true;
                    }else if(nation != null){
                        if(EmbargoManager.getEmbargo(nation, player_nation) != null){
                            Log.playerMessage(player, "This government is already embargoed by you!");
                            return true;
                        }

                        Embargo embargo = new Embargo(player_nation.getUUID(), nation.getUUID());

                        EmbargoManager.addEmbargo(embargo);

                        Log.playerMessage(player, "Set embargo on " + nation.getName() +" (nation)!");
                        return true;
                    }else{
                        Log.playerMessage(player, "Government doesn't exist!");
                        return true;
                    }
                default:
                    Log.playerMessage(player, "Proper usage: /wlc introduce embargo <nation/town>");
                    return true;


            }
        }else if(args[0].equalsIgnoreCase("remove")){
            if(args.length != 3){
                Log.playerMessage(player, "Proper usage: /wlc remove tariff/embargo <nation/town>");
                return true;
            }

            Nation player_nation = TownyAPI.getInstance().getNation(player);
            String government_name = args[2];

            Nation nation = TownyAPI.getInstance().getNation(government_name);
            Town town = TownyAPI.getInstance().getTown(government_name);

            if(nation == null && town == null){
                Log.playerMessage(player, "Government " + args[2] + " doesn't exist!");
                return true;
            }

            if(player_nation == null){
                player.sendMessage("You are not in a nation and therefore cannot remove trade laws");
                return true;
            }

            if(!Objects.requireNonNull(TownyAPI.getInstance().getResident(player)).isKing()){
                Log.playerMessage(player, "Only the leader of a nation can remove trade laws");
                return true;
            }

            if(player_nation.equals(nation)){
                Log.playerMessage(player, "You can't remove trade laws against your own nation!");
                return true;
            }

            switch (args[1].toLowerCase()){
                case "tariffs":
                    if(town != null){
                        Tariff tariff;
                        if((tariff = TariffManager.getTariff(town, player_nation)) != null){
                            player.sendMessage("Removed the tariff on " + town.getName() + "(town)");
                            TariffManager.removeTariff(tariff);
                            return true;
                        }
                    }

                    if(nation != null){
                        Tariff tariff;
                        if((tariff = TariffManager.getTariff(nation, player_nation)) != null){
                            player.sendMessage("Removed the tariff on " + nation.getName() + "(nation)");
                            TariffManager.removeTariff(tariff);
                            return true;
                        }
                    }

                    player.sendMessage("This government isn't tariffed!");
                    return true;
                case "embargoes":
                    if(town != null){
                        Embargo embargo;
                        if((embargo = EmbargoManager.getEmbargo(town, player_nation)) != null){
                            Log.playerMessage(player, "Removed tariff on " + town.getName() + " (town)!");
                            EmbargoManager.removeEmbargo(embargo);
                            return true;
                        }
                    }

                    if(nation != null){
                        Embargo embargo;
                        if((embargo = EmbargoManager.getEmbargo(nation, player_nation)) != null){
                            if (town != null) {
                                Log.playerMessage(player, "Removed tariff on " + town.getName() + " (nation)!");
                            }
                            EmbargoManager.removeEmbargo(embargo);
                            return true;
                        }
                    }

                    Log.playerMessage(player,"This government isn't embargoed!");
                    return true;
                default:
                    Log.playerMessage(player, "Proper usage: /wlc remove tariff/embargo <nation/town>");
                    return true;

            }
        }else if(args[0].equalsIgnoreCase("insolvency")){
            if(args.length != 2){
                Log.playerMessage(player, "Proper usage: /wlc insolvency pay");
                return true;
            }

            if(!args[1].equalsIgnoreCase("pay")){
                Log.playerMessage(player, "Proper usage: /wlc insolvency pay");
                return true;
            }

            Nation nation = TownyAPI.getInstance().getNation(player);

            if(nation == null){
                Log.playerMessage(player, "You need to be in a nation to pay for its insolvency!");
                return true;
            }

            int insolvency = InsolvencyManager.getInsolvency(nation);

            if(insolvency == 0){
                Log.playerMessage(player, "This nation does not have any insolvency!");
                return true;
            }

            int player_gold = (int) WeLoveCapitalism.getEconomy().getBalance(player);

            if(player_gold < insolvency){
                Log.playerMessage(player, "You don't have enough money to pay for the insolvency!");
                Log.playerMessage(player, "You need the gold to be in your inventory");
                return true;
            }

            WeLoveCapitalism.getEconomy().withdrawPlayer(player, insolvency);

            Log.playerMessage(player, "Successfully paid for " + nation.getName() + "'s insolvency!");

            InsolvencyManager.removeInsolvency(nation);

            return true;
        }else{
            Log.playerMessage(player, "Unknown subcommand `" + args[0] + "'");
            Log.playerMessage(player, "Do `/wlc help' for help!");
        }


        return true;
    }

}
