package org.libremc.weLoveCapitalism.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.libremc.weLoveCapitalism.WLCPlayer;
import org.libremc.weLoveCapitalism.WLCPlayerManager;

public class WLCCommand implements CommandExecutor {



    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player player)){
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if(args[0].equalsIgnoreCase("set")){

            if(args.length != 2){
                player.sendMessage("wlc: proper usage /wlc set <amount>");
            }

            WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);
            if(!wlcplayer.isCreatingShop()){
                player.sendMessage("You are not creating a shop!");
                return true;
            }

            ItemStack stack = player.getInventory().getItemInMainHand().clone();

            stack.setAmount(Integer.parseInt(args[1]));

            wlcplayer.setSetItem(stack);

        }else if(args[0].equalsIgnoreCase("amount")){
            if(args.length != 2){
                player.sendMessage("wlc: proper usage /wlc amount <multiplier>");
            }

            WLCPlayer wlcplayer = WLCPlayerManager.createWLCPlayer(player);

            int amount = Integer.parseInt(args[1]);

            wlcplayer.setAmount(amount);

            player.sendMessage(ChatColor.AQUA + "Set your buying amount to " + amount);

        }


        return true;
    }

}
