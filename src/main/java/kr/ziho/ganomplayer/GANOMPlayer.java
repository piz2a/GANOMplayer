package kr.ziho.ganomplayer;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Vector;

public class GANOMPlayer extends JavaPlugin {

    FileConfiguration config = getConfig();
    Vector<NPC> aiPlayers = new Vector<>();
    Vector<Connection> connections = new Vector<>();

    @Override
    public void onEnable() {
        getLogger().info("Enabling \"/ganom\" command");
        getCommand("ganom").setExecutor(new AIManageCommand(this));

        config.addDefault("host", "127.0.0.1");
        config.addDefault("port", 25566);
        config.options().copyDefaults(true);
        saveConfig();
    }
    @Override
    public void onDisable() {
        // getLogger().info("onDisable is called!");
    }

}
