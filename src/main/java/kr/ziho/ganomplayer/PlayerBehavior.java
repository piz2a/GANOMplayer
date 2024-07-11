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
            double sin = Math.sin(yaw), cos = Math.cos(yaw);
            add(relative ? x * cos - z * sin : x);  // right-side
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

        // Velocity
        JSONArray velocityArray = (JSONArray) jsonObject.get("velocity");
        Location prevLocation = player.getLocation();
        Location newLocation = new Location(
                player.getWorld(),
                prevLocation.getX() + (double) velocityArray.get(0),
                prevLocation.getY(),  // + (double) velocityArray.get(1),
                prevLocation.getZ() + (double) velocityArray.get(2)
        );
        // increase y if teleport destination is not air
        while (newLocation.getBlock().getType() != Material.AIR && newLocation.getY() <= player.getWorld().getMaxHeight()) {
            newLocation.add(new Vector(0, 1, 0));
            System.out.println("Increasing y");
        }
        player.teleport(newLocation);

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

        int attackIndex = (int) jsonObject.get("attackIndex");
        if (attackIndex != -1) {
            List<Player> playerList = player.getWorld().getPlayers();
            for (int i = 0; i < playerList.size(); i++) {
                // Attacks the target player
                Player targetPlayer = playerList.get(i);
                targetPlayer.damage(0.5);
                // Knockback: pass
            }
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
