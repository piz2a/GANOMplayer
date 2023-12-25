package kr.ziho.ganomplayer.status;

import kr.ziho.ganomplayer.ItemLimited;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/*
 * Excluding:
 * - Potion effects
 * - Exhaustion
 * - Saturation
 * - Statistics
 * - Exp
 * - Chat
 * - Chest & Ender chest
 * - Absolute location
 * - Metadata
 * - Worlds (Nether, The End)
 * - Enchanting
 * - Changing max health
 * - Passengers
 * - Projectile
 * - Vehicle
 * - Equipment
 * - Blocking
 * - Sleeping
 * - Farming
 * - Walk speed change
 */

public class PlayerStatusFrame extends JSONObject {

    public PlayerStatusFrame(Player player) {
        super();
        put("isOnGround", ((LivingEntity) player).isOnGround());
        put("isSneaking", player.isSneaking());
        put("isSprinting", player.isSprinting());

        put("lastDamage", player.getLastDamage());
        put("health", player.getHealth());

        put("itemInHand", ItemLimited.from(player.getItemInHand()));

        Vector velocity = player.getVelocity();
        put("velocity", new JSONArray() {{
            add(velocity.getX());
            add(velocity.getY());
            add(velocity.getZ());
        }});

        Location location = player.getLocation();
        put("location", new JSONArray() {{
            add(location.getX());
            add(location.getY());
            add(location.getZ());
        }});
    }

}
