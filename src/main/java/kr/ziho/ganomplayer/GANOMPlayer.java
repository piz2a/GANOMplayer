package kr.ziho.ganomplayer;

import org.bukkit.plugin.java.JavaPlugin;

public class GANOMPlayer extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("onEnable is called!");
    }
    @Override
    public void onDisable() {
        getLogger().info("onDisable is called!");
    }
}
