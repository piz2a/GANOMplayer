package kr.ziho.ganomplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AIPlayer extends EntityPlayer {
    private final Location loc;

    public AIPlayer(WorldServer ws, GameProfile gp, Location loc) {
        super(MinecraftServer.getServer(), ws, gp, new PlayerInteractManager(ws));
        this.loc = loc;
        setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()); // set location
    }

    public void spawn() {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            spawnFor(pl); // send all spawn packets
        }
    }

    public void spawnFor(Player p) {
        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;

        // add player in player list for player
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this));
        // make player spawn in world
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(this));
        // change head rotation
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(this, (byte) ((loc.getYaw() * 256f) / 360f)));
        // now remove player from tab list
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, this));
        // here the entity is showed, you can show item in hand like that :
        // connection.sendPacket(new PacketPlayOutEntityEquipment(getId(), 0, CraftItemStack.asNMSCopy(itemInHand)));
    }

    public void remove() {
        this.die();
    }

    public boolean isEntity(Entity et) {
        return this.getId() == et.getId(); // check if it's this entity
    }

    public String getData() {
        return "";
    }

    public static AIPlayer create(Location loc, String name) {
        // get NMS world
        WorldServer nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name); // create game profile
        // use class given just before
        AIPlayer ep = new AIPlayer(nmsWorld, profile, loc);
        // now quickly made player connection
        ep.playerConnection = new PlayerConnection(ep.server, new NetworkManager(EnumProtocolDirection.CLIENTBOUND), ep);

        nmsWorld.addEntity(ep); // add entity to world
        ep.spawn(); // spawn for actual online players

        return ep;
    }
}

