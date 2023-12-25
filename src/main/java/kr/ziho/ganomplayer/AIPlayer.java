package kr.ziho.ganomplayer;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AIPlayer {

    public static void initNPC(NPC npc, Location spawnLocation) {
        npc.setFlyable(false);
        npc.setProtected(false);
        npc.setUseMinecraftAI(true);
        npc.spawn(spawnLocation);
        Player player = (Player) npc.getEntity();
        player.setCanPickupItems(true);
    }

    public static PlayerData getData(NPC npc) {
        Player player = (Player) npc.getEntity();
        return new PlayerData();
    }

    public static void behave(NPC npc, PlayerData playerData) {
        Player player = (Player) npc.getEntity();
    }

}

