package kr.ziho.ganomplayer;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class AIManageCommand implements CommandExecutor {

    private final GANOMPlayer plugin;

    public AIManageCommand(GANOMPlayer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof ConsoleCommandSender || commandSender instanceof Player))
            return false;
        if (commandSender instanceof Player) {
            Player playerSender = (Player) commandSender;
            if (!playerSender.isOp())
                return false;
        }
        if (strings.length == 0) return false;
        try {
            // for (int i = 0; i < strings.length; i++) commandSender.sendMessage(i + " " + strings[i]);
            if (strings[0].equals("add"))
                return add(commandSender, strings);
            else if (strings[0].equals("remove"))
                return remove(commandSender, strings);
            else if (strings[0].equals("train"))
                return train(commandSender, strings);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private boolean add(CommandSender commandSender, String[] strings) {
        // Usage: /ganom add Notch 1 2 3 (world)
        World spawnWorld = strings.length == 6 ? plugin.getServer().getWorld(strings[5]) : null;
        if (strings.length != 5 && strings.length != 6) return false;
        if (strings.length == 5) {
            spawnWorld = commandSender instanceof Player ?
                    ((Player) commandSender).getWorld() : plugin.getServer().getWorld("world");
        }
        Location spawnLocation = new Location(
                spawnWorld,
                Double.parseDouble(strings[2]),
                Double.parseDouble(strings[3]),
                Double.parseDouble(strings[4])
        );
        for (AIPlayer aiPlayer : plugin.aiPlayers) {
            if (strings[1].equals(aiPlayer.getName())) {
                commandSender.sendMessage(ChatColor.RED + "The AIPlayer with given name already exists.");
                return true;
            }
        }
        plugin.aiPlayers.add(AIPlayer.create(spawnLocation, strings[1]));
        return true;
    }

    private boolean remove(CommandSender commandSender, String[] strings) {
        // Usage: /ganom remove Notch
        if (strings.length != 2) return false;
        for (AIPlayer aiPlayer : plugin.aiPlayers) {
            if (strings[1].equals(aiPlayer.getName())) {
                aiPlayer.remove();
                plugin.aiPlayers.remove(aiPlayer);
                return true;
            }
        }
        commandSender.sendMessage(ChatColor.RED + "The AIPlayer with given name doesn't exist.");
        return true;
    }

    private boolean train(CommandSender commandSender, String[] strings) {
        // Usage: /ganom train Notch
        if (strings.length != 2) return false;
        AIPlayer aiPlayer = null;
        for (AIPlayer iter : plugin.aiPlayers) {
            if (strings[1].equals(iter.getName())) {
                aiPlayer = iter;
                break;
            }
        }
        if (aiPlayer == null) {
            commandSender.sendMessage(ChatColor.RED + "The AIPlayer with given name doesn't exist.");
            return true;
        }
        Connection connection = new Connection(plugin, aiPlayer);
        connection.start();
        plugin.connections.add(connection);
        return true;
    }

}
