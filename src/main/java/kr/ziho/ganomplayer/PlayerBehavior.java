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

import java.lang.reflect.Field;
import java.util.List;


public class PlayerBehavior extends JSONObject {

    public PlayerBehavior(Player player, GANOMPlayer plugin, Location prevLocation, boolean relative) {
        super();
        Boolean isOnDamage = plugin.damageMap.get(player.getUniqueId());
        put("isOnDamage", isOnDamage != null && isOnDamage);
        put("isOnGround", ((LivingEntity) player).isOnGround());
        put("isSneaking", player.isSneaking());
        put("isSprinting", player.isSprinting());

        // put("lastDamage", player.getLastDamage());
        // put("health", player.getHealth());

        // put("itemInHand", ItemLimited.from(player.getItemInHand()).getValue());

        Location eyeLocation = player.getEyeLocation();
        double yaw = eyeLocation.getYaw();
        if (!relative) put("yaw", yaw);
        put("pitch", eyeLocation.getPitch());

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
    }

    // Make player follow the instructions included in jsonObject
    public static void behave(Player player, JSONObject jsonObject, boolean mirrorTest) {
        // Sneaking & Sprinting
        player.setSneaking((boolean) jsonObject.get("isSneaking"));
        player.setSprinting((boolean) jsonObject.get("isSprinting"));

        // Item in hand
        // player.setItemInHand(ItemLimited.from(((Long) jsonObject.get("itemInHand")).intValue()).toItemStack());

        // Rotation + Head Rotation
        double yaw, pitch;
        if (mirrorTest) {
            yaw = player.getLocation().getYaw();  // Yaw is constant in mirror test
            pitch = (double) jsonObject.get("pitch");
        } else {
            JSONArray locationArray = (JSONArray) jsonObject.get("rotation");
            yaw = ((Double) locationArray.get(0)).floatValue();
            pitch = ((Double) locationArray.get(1)).floatValue();
        }

        int entityID = player.getEntityId();
        PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(entityID, getFixRotation(yaw), getFixRotation(pitch), true);
        PacketPlayOutEntityHeadRotation packetHead = new PacketPlayOutEntityHeadRotation();
        setValue(packetHead, "a", entityID);
        setValue(packetHead, "b", getFixRotation(yaw));
        sendPacket(packet);
        sendPacket(packetHead);
        /*
        sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                entityID,
                (byte) (128 * 32 * newVelocity.getX()),
                (byte) (128 * 32 * newVelocity.getY()),
                (byte) (128 * 32 * newVelocity.getZ()),
                true
        ));
        */

        // Velocity (Yaw-relative)
        JSONArray velocityArray = (JSONArray) jsonObject.get("velocity");
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
        // increase y if teleport destination is not air
        while (newLocation.getBlock().getType() != Material.AIR && newLocation.getY() <= player.getWorld().getMaxHeight()) {
            newLocation = newLocation.add(new Vector(0, 0.1, 0));
            System.out.println("Increasing y");
        }
        player.teleport(newLocation);

        // Attack other player
        Long attackIndex = (Long) jsonObject.get("attackIndex");
        if (attackIndex != -1L) {
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
