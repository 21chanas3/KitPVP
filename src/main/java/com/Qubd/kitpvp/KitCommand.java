package com.Qubd.kitpvp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
        if (sender instanceof Player) {
            Main.applyKitUI((Player) sender);
        }
        return false;
    }
}
