package kr.ziho.ganomplayer.status;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PlayerStatusFrame extends JSONObject {

    public PlayerStatusFrame(Player player) {
        super();
        for (Map.Entry<String, PlayerStatus<Boolean>> entry : PlayerStatus.booleanStatus.entrySet()) {
            put(entry.getKey(), entry.getValue().get(player));
        }
        for (Map.Entry<String, PlayerStatus<Double>> entry : PlayerStatus.doubleStatus.entrySet()) {
            put(entry.getKey(), entry.getValue().get(player));
        }
        for (Map.Entry<String, PlayerStatus<ItemStack>> entry : PlayerStatus.itemStackStatus.entrySet()) {
            put(entry.getKey(), entry.getValue().get(player));
        }
        Vector velocity = PlayerStatus.MiscellaneousStatus.getVelocity.get(player);
        put("itemInHand", PlayerStatus.MiscellaneousStatus.getItemLimitedInHand.get(player).getValue());
        put("velocity", new JSONArray() {{
            add(velocity.getX());
            add(velocity.getY());
            add(velocity.getZ());
        }});
    }

}
