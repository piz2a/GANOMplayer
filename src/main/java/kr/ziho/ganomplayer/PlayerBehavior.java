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
input
- isOnDamage [bool]
- isOnGround [bool]
- isSneaking [bool]
- isSprinting [bool]
- pitch [float]
- velocity (yaw-relative) [float, float, float]

output
- rotation (yaw, pitch) [float, float]
- velocity (x, y, z) [float, float, float]
- isSneaking [bool]
- isSprinting [bool]
- attackIndex [int]
*/

public class PlayerBehavior extends JSONObject {

    public static final boolean mirrorTest = true;

    public PlayerBehavior(Player player, GANOMPlayer plugin) {
        super();
        put("isOnDamage", plugin.damageMap.get(player.getUniqueId()));
        put("isOnGround", ((LivingEntity) player).isOnGround());
        put("isSneaking", player.isSneaking());
        put("isSprinting", player.isSprinting());

        put("lastDamage", player.getLastDamage());
        put("health", player.getHealth());

        put("itemInHand", ItemLimited.from(player.getItemInHand()).getValue());

        Location location = player.getLocation();
        put("pitch", location.getPitch());

        Vector velocity = player.getVelocity();
        put("velocity", new JSONArray() {{  // yaw-relative velocity
            double x = velocity.getX(), z = velocity.getZ();
            double yaw = location.getYaw();
            double sin = Math.sin(yaw), cos = Math.cos(yaw);
            add(x * cos - z * sin);  // right-side
            add(velocity.getY());  // y
            add(z * cos - x * sin);  // front-side
        }});
    }

    // Make player follow the instructions included in jsonObject
    public static void behave(Player player, JSONObject jsonObject) {
        // Sneaking & Sprinting
        player.setSneaking((boolean) jsonObject.get("isSneaking"));
        player.setSprinting((boolean) jsonObject.get("isSprinting"));

        // Item in hand
        player.setItemInHand(ItemLimited.from(((Long) jsonObject.get("itemInHand")).intValue()).toItemStack());

        // Velocity
        JSONArray velocityArray = (JSONArray) jsonObject.get("velocity");
        float walkSpeed = 1;  // player.getWalkSpeed();
        Vector newVelocity = new Vector(
                ((double) velocityArray.get(0)) * walkSpeed,
                (double) velocityArray.get(1),
                ((double) velocityArray.get(2)) * walkSpeed
        );
        player.setVelocity(newVelocity);

        // Rotation + Head Rotation
        float yaw, pitch;
        if (mirrorTest) {
            yaw = player.getLocation().getYaw();
            pitch = (float) jsonObject.get("pitch");
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

    public static void sendPacket(Packet<?> packet, Player player){
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendPacket(Packet<?> packet){
        for (Player player : Bukkit.getOnlinePlayers()){
            sendPacket(packet, player);
        }
    }

    public static byte getFixRotation(float yawPitch){
        return (byte) ((int) (yawPitch * 256.0F / 360.0F));
    }

}
