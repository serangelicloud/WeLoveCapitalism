package org.libremc.weLoveCapitalism.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.libremc.weLoveCapitalism.Database;
import org.libremc.weLoveCapitalism.datatypes.Tariff;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if(!(sender instanceof Player)){
            return Collections.emptyList();
        }
        Player player = (Player)sender;

        List<String> completions = new ArrayList<>();

        if(args.length == 1){
            completions.addAll(List.of("introduce", "remove", "get", "multiplier", "set"));
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("introduce") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("get")){
            completions.addAll(List.of("tariffs", "embargoes"));
        }

        if(args.length >= 3){
            if(args[0].equalsIgnoreCase("introduce") || args[0].equalsIgnoreCase("get")){
                List<String> names = new ArrayList<>();
                for(Nation nation : TownyAPI.getInstance().getNations()){
                    names.add(nation.getName());
                }

                for(Town town : TownyAPI.getInstance().getTowns()){
                    names.add(town.getName());
                }

                completions.addAll(names);
            }else if(args[0].equalsIgnoreCase("remove")){
                Nation nation;
                List<String> names = new ArrayList<>();
                if((nation = TownyAPI.getInstance().getNation(player)) != null){
                    try {
                        for(Tariff tariff : Database.getTariffsByGovernment(nation.getUUID())){
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
                completions.addAll(names);
            }
        }

        return completions.stream()
                .filter(s -> s.startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
