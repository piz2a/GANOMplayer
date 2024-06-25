package kr.ziho.ganomplayer;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

public class GANOMPlayer extends JavaPlugin {

    Vector<NPC> aiPlayers = new Vector<>();
    Vector<Connection> connections = new Vector<>();
    HashMap<UUID, Boolean> damageMap = new HashMap<>();
    HashMap<UUID, Location> locationMap = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Enabling \"/ganom\" command");
        getLogger().info("test");
        getCommand("ganom").setExecutor(new AIManageCommand(this));

        FileConfiguration config = getConfig();
        config.addDefault("host", "127.0.0.1");
        config.addDefault("port", 25566);
        config.addDefault("framesInTimeline", 10);
        config.addDefault("frameInterval", 200);  // microseconds
        config.addDefault("debug", false);
        config.options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    @Override
    public void onDisable() {
        getLogger().info("Removing all AI Players");
        for (NPC npc : aiPlayers) {
            npc.destroy();
        }
    }

}
