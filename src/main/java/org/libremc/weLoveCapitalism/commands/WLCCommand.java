package org.libremc.weLoveCapitalism.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.libremc.weLoveCapitalism.WLCPlayer;
import org.libremc.weLoveCapitalism.WeLoveCapitalism;

public class WLCCommand implements CommandExecutor {



    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player player)){
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if(args[0].equalsIgnoreCase("set")){

            if(args.length < 2 || args.length >= 3){
                player.sendMessage("wlc: proper usage /wlc set <amount>");
            }

            WLCPlayer wlcplayer = WeLoveCapitalism.createWLCPlayer(player);
            if(!wlcplayer.isCreatingShop()){
                player.sendMessage("You are not creating a shop!");
                return true;
            }

            ItemStack stack = player.getInventory().getItemInMainHand().clone();

            stack.setAmount(Integer.valueOf(args[1]));

            wlcplayer.setSetItem(stack);

        }


        return true;
    }

}
