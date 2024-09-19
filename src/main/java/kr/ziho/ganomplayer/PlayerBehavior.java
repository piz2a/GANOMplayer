package kr.ziho.ganomplayer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;


public class PlayerBehavior extends JSONObject {

    public PlayerBehavior(Player player, GANOMPlayer plugin, int keyLog) {
        super();
        Boolean isOnDamage = plugin.damageMap.get(player.getUniqueId());
        put("Attack", (isOnDamage != null && isOnDamage) ? 0 : -1);  // This will be swapped
        put("Shift", player.isSneaking() ? 1 : 0);
        put("Ctrl", player.isSprinting() ? 1 : 0);

        Location eyeLocation = player.getEyeLocation();
        double yaw = eyeLocation.getYaw();
        double prevYaw = plugin.yawMap.getOrDefault(player.getUniqueId(), yaw);
        if (plugin.yawMap.containsKey(player.getUniqueId()))
            plugin.yawMap.put(player.getUniqueId(), yaw);
        else plugin.yawMap.replace(player.getUniqueId(), yaw);

        double pitch = eyeLocation.getPitch();
        double prevPitch = plugin.pitchMap.getOrDefault(player.getUniqueId(), pitch);
        if (plugin.pitchMap.containsKey(player.getUniqueId()))
            plugin.pitchMap.put(player.getUniqueId(), pitch);
        else plugin.pitchMap.replace(player.getUniqueId(), pitch);
        put("DelYaw", yaw - prevYaw);
        put("DelPitch", pitch - prevPitch);
        put("Pitch", pitch);

        // should be 1 or 0
        put("Space", keyLog / 4);
        put("WSmove", (keyLog % 4) / 2);
        put("ADmove", keyLog % 2);
    }

    // Make player follow the instructions included in jsonObject
    public static void behave(Player player, JSONObject jsonObject, OutputStream modOut, boolean mirrorTest) {
        // Sneaking & Sprinting
        player.setSneaking((boolean) jsonObject.get("Shift"));
        player.setSprinting((boolean) jsonObject.get("Ctrl"));

        // WASD Moves and Rotation
        PrintWriter modWriter = new PrintWriter(modOut, true);
        modWriter.println(jsonObject);

        // Attack other player
        Long attackIndex = (Long) jsonObject.get("Attack");
        if (attackIndex != -1L) {  // -1(none) or 0(player index)
            List<Player> playerList = player.getWorld().getPlayers();
            playerList.removeIf(worldPlayer -> worldPlayer.getUniqueId() == player.getUniqueId());
            // Attacks the target player
            Player targetPlayer = playerList.get(Math.toIntExact(attackIndex));
            // Vector locationDiff = targetPlayer.getLocation().subtract(player.getLocation()).toVector();
            // locationDiff.multiply(0.5);
            targetPlayer.damage(0.5);
            // Knockback: pass
        }
    }

}
