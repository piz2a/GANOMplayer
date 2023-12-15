package kr.ziho.ganomplayer;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Vector;

public class GANOMPlayer extends JavaPlugin {

    Vector<AIPlayer> aiPlayers = new Vector<>();

    @Override
    public void onEnable() {
        getLogger().info("Enabling \"/ganom\" command");
        getCommand("ganom").setExecutor(new AIManageCommand(this));
    }
    @Override
    public void onDisable() {
        // getLogger().info("onDisable is called!");
    }

}
