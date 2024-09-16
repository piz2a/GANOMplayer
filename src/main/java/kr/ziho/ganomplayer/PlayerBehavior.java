package kr.ziho.ganomplayer;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;


public class PlayerBehavior extends JSONObject {

    public PlayerBehavior(Player player, GANOMPlayer plugin, int keyLog, Location prevLocation, boolean relative) {
        super();
        // Boolean isOnDamage = plugin.damageMap.get(player.getUniqueId());
        // put("isOnDamage", isOnDamage != null && isOnDamage);
        // put("isOnGround", ((LivingEntity) player).isOnGround());
        put("Shift", player.isSneaking());
        put("Ctrl", player.isSprinting());

        // put("lastDamage", player.getLastDamage());
        // put("health", player.getHealth());

        // put("itemInHand", ItemLimited.from(player.getItemInHand()).getValue());

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
        if (!relative) put("DelYaw", yaw - prevYaw);
        put("DelPitch", pitch - prevPitch);

        // should be 1 or 0
        put("Space", keyLog / 4);
        put("WSmove", (keyLog % 4) / 2);
        put("ADmove", keyLog % 2);
        /*
        Location location = player.getLocation();
        Vector locationDiff = new Vector(
                location.getX() - prevLocation.getX(),
                location.getY() - prevLocation.getY(),
                location.getZ() - prevLocation.getZ()
        );
        put("velocity", new JSONArray() {{  // yaw-relative velocity
            double x = locationDiff.getX(), z = locationDiff.getZ();
            // Converting absolute velocity(v_x, v_z) to yaw-relative velocity(v_right, v_front)
            double sin = Math.sin(Math.toRadians(yaw)), cos = Math.cos(Math.toRadians(yaw));
            add(relative ? - x * cos - z * sin : x);  // right-side
            add(locationDiff.getY() > 0 ? 1 : 0);  // y > 0
            add(relative ? z * cos - x * sin : z);  // front-side
        }});
        */
    }

    // Make player follow the instructions included in jsonObject
    public static void behave(Player player, JSONObject jsonObject, OutputStream modOut, boolean mirrorTest) {
        // Sneaking & Sprinting
        player.setSneaking((boolean) jsonObject.get("Shift"));
        player.setSprinting((boolean) jsonObject.get("Ctrl"));

        // Item in hand
        // player.setItemInHand(ItemLimited.from(((Long) jsonObject.get("itemInHand")).intValue()).toItemStack());

        // Rotation + Head Rotation
        double yaw, pitch;
        if (mirrorTest) {
            yaw = player.getLocation().getYaw();  // Yaw is constant in mirror test
            pitch = (double) jsonObject.get("pitch");
        } else {
            float dyaw = ((Double) jsonObject.get("DelYaw")).floatValue();
            float dpitch = ((Double) jsonObject.get("DelPitch")).floatValue();
            yaw = dyaw + player.getLocation().getYaw();
            pitch = dpitch + player.getLocation().getPitch();
        }

        int entityID = player.getEntityId();
        PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(entityID, getFixRotation(yaw), getFixRotation(pitch), true);
        PacketPlayOutEntityHeadRotation packetHead = new PacketPlayOutEntityHeadRotation();
        setValue(packetHead, "a", entityID);
        setValue(packetHead, "b", getFixRotation(yaw));
        sendPacket(packet);
        sendPacket(packetHead);

        // Velocity (Yaw-relative)
        /*
        JSONArray velocityArray = (JSONArray) jsonObject.get("velocity");
        Location newLocation = getNewLocation(player, velocityArray, yaw);
        // increase y if teleport destination is not air
        while (newLocation.getBlock().getType() != Material.AIR && newLocation.getY() <= player.getWorld().getMaxHeight()) {
            newLocation = newLocation.add(new Vector(0, 0.1, 0));
            System.out.println("Increasing y");
        }
        player.teleport(newLocation);
        */

        // WASD Moves
        JSONObject modJson = new JSONObject();
        modJson.put("strafe", jsonObject.get("ADmove"));
        modJson.put("forward", jsonObject.get("WSmove"));
        PrintWriter modWriter = new PrintWriter(modOut, true);
        modWriter.println(modJson);

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

    private static Location getNewLocation(Player player, JSONArray velocityArray, double yaw) {
        double vr = (double) velocityArray.get(0), vf = (double) velocityArray.get(2);
        double sin = Math.sin(Math.toRadians(yaw)), cos = Math.cos(Math.toRadians(yaw));
        // Converting yaw-relative velocity(v_right, v_front) to absolute velocity(v_x, v_z)
        double vx = - vr * cos - vf * sin, vz = vf * cos - vr * sin;
        Location prevLocation = player.getLocation();
        Location newLocation = new Location(
                player.getWorld(),
                prevLocation.getX() + vx,
                prevLocation.getY() + (((LivingEntity) player).isOnGround() ? ((double) velocityArray.get(1)) : -0.3f),
                prevLocation.getZ() + vz
        );
        return newLocation;
    }

    public static void setValue(Object obj, String name, Object value){
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendPacket(Packet<?> packet, Player player){
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendPacket(Packet<?> packet){
        for (Player player : Bukkit.getOnlinePlayers()){
            sendPacket(packet, player);
        }
    }

    public static byte getFixRotation(double yawPitch){
        return (byte) ((int) (yawPitch * 256.0F / 360.0F));
    }

}
