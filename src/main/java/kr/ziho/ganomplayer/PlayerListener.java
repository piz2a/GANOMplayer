package kr.ziho.ganomplayer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    GANOMPlayer plugin;

    public PlayerListener(GANOMPlayer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        if (!plugin.damageMap.containsKey(uuid))
            plugin.damageMap.put(uuid, false);
        plugin.damageMap.replace(uuid, true);
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            plugin.damageMap.replace(uuid, false);
        }).start();
    }
}
