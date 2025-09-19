package org.libremc.weLoveCapitalism.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.libremc.weLoveCapitalism.ChestShopDatabase;
import org.libremc.weLoveCapitalism.datatypes.Tariff;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args){
        if(!(sender instanceof Player player)){
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if(args.length == 1){
            completions.addAll(List.of("introduce", "remove", "get", "multiplier", "set", "help", "insolvency", "top", "disable"));
        }

        if(args.length == 2 && (args[0].equalsIgnoreCase("introduce") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("get"))){
            completions.addAll(List.of("tariffs", "embargoes"));
            if(args[0].equalsIgnoreCase("get")){
                completions.add("upkeep");
            }
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("top")){
            completions.addAll(List.of("nation", "town"));
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("insolvency")){
            completions.add("pay");
        }


        if(args.length == 2 && args[0].equalsIgnoreCase("disable")){
            completions.addAll(List.of("on", "off"));
        }


        if(args.length == 3){
            if(args[0].equalsIgnoreCase("introduce") || args[0].equalsIgnoreCase("get")){
                List<String> names = new ArrayList<>();
                for(Nation nation : TownyAPI.getInstance().getNations()){
                    names.add(nation.getName());
                }

                for(Town town : TownyAPI.getInstance().getTowns()){
                    names.add(town.getName());
                }

                List<String> strings = new ArrayList<>();
                for(String str : names){
                    if(str.startsWith(args[2])){
                        strings.add(str);
                    }
                }

                return strings;
            }else if(args[0].equalsIgnoreCase("remove")){
                Nation nation;
                List<String> names = new ArrayList<>();
                if((nation = TownyAPI.getInstance().getNation(player)) != null){
                    try {
                        for(Tariff tariff : ChestShopDatabase.getTariffsByGovernment(nation.getUUID())){
                            Nation nat = TownyAPI.getInstance().getNation(tariff.getGovernmentTariffed());
                            Town town = TownyAPI.getInstance().getTown(tariff.getGovernmentTariffed());

                            if(nat != null){
                                names.add(nat.getName());
                            }else if(town != null){
                                names.add(town.getName());
                            }

                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                List<String> strings = new ArrayList<>();
                for(String str : names){
                    if(str.startsWith(args[2])){
                        strings.add(str);
                    }
                }

                return strings;
            }
        }

        return completions.stream()
                .filter(s -> s.startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }



}
