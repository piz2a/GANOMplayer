package kr.ziho.ganomplayer;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;

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

public class PlayerBehavior extends JSONObject {

    public PlayerBehavior(Player player) {
        super();
        put("isOnGround", ((LivingEntity) player).isOnGround());
        put("sneaking", player.isSneaking());
        put("sprinting", player.isSprinting());

        put("lastDamage", player.getLastDamage());
        put("health", player.getHealth());

        put("itemInHand", ItemLimited.from(player.getItemInHand()).getValue());

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
            add(location.getYaw());
            add(location.getPitch());
        }});
    }

    // Make player follow the instructions included in jsonObject
    public static void behave(Player player, JSONObject jsonObject) {
        // Sneaking & Sprinting
        player.setSneaking((boolean) jsonObject.get("sneaking"));
        player.setSprinting((boolean) jsonObject.get("sprinting"));

        // Item in hand
        player.setItemInHand(ItemLimited.from(((Long) jsonObject.get("itemInHand")).intValue()).toItemStack());

        // Velocity
        JSONArray velocityArray = (JSONArray) jsonObject.get("velocity");
        float walkSpeed = 1;  // player.getWalkSpeed();
        player.setVelocity(new Vector(
                ((double) velocityArray.get(0)) * walkSpeed,
                (double) velocityArray.get(1),
                ((double) velocityArray.get(2)) * walkSpeed
        ));

        // Rotation + Head Rotation
        JSONArray locationArray = (JSONArray) jsonObject.get("location");
        float yaw = (float) locationArray.get(3);
        float pitch = (float) locationArray.get(4);
        int entityID = player.getEntityId();
        PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(entityID, getFixRotation(yaw), getFixRotation(pitch), true);
        PacketPlayOutEntityHeadRotation packetHead = new PacketPlayOutEntityHeadRotation();
        setValue(packetHead, "a", entityID);
        setValue(packetHead, "b", getFixRotation(yaw));
        sendPacket(packet);
        sendPacket(packetHead);
    }

    public static void setValue(Object obj, String name, Object value){
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void sendPacket(Packet<?> packet,Player player){
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendPacket(Packet<?> packet){
        for (Player player : Bukkit.getOnlinePlayers()){
            sendPacket(packet,player);
        }
    }

    public static byte getFixRotation(float yawPitch){
        return (byte) ((int) (yawPitch * 256.0F / 360.0F));
    }

}
