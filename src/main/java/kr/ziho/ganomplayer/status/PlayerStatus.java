package kr.ziho.ganomplayer.status;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.HashMap;

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
*/

public class PlayerStatus<T> {

    BehaviorGetter<T> getter;

    public interface BehaviorGetter<T> {
        T get(Player p);
    }

    public PlayerStatus(BehaviorGetter<T> getter) {
        this.getter = getter;
    }

    public T get(Player player) {
        return getter.get(player);
    }

    public static HashMap<String, PlayerStatus<Boolean>> booleanStatus = new HashMap<String, PlayerStatus<Boolean>>() {{
        put("isOnGround", new PlayerStatus<>(player -> ((LivingEntity) player).isOnGround()));
        put("isSneaking", new PlayerStatus<>(Player::isSneaking));
        put("isSprinting", new PlayerStatus<>(Player::isSprinting));
        put("isBlocking", new PlayerStatus<>(Player::isBlocking));
        put("isSleeping", new PlayerStatus<>(Player::isSleeping));
        put("isInsideVehicle", new PlayerStatus<>(Player::isInsideVehicle));
    }};

    public static HashMap<String, PlayerStatus<Float>> floatStatus = new HashMap<String, PlayerStatus<Float>>() {{
        put("getWalkSpeed", new PlayerStatus<>(Player::getWalkSpeed));
    }};

    public static HashMap<String, PlayerStatus<Double>> doubleStatus = new HashMap<String, PlayerStatus<Double>>() {{
        put("getLastDamage", new PlayerStatus<>(Player::getLastDamage));
        put("getHealthRate", new PlayerStatus<>(player -> player.getHealth() / player.getMaxHealth()));
    }};

    public static HashMap<String, PlayerStatus<Integer>> integerStatus = new HashMap<String, PlayerStatus<Integer>>() {{
        put("getFoodLevel", new PlayerStatus<>(Player::getFoodLevel));
    }};

    public static HashMap<String, PlayerStatus<ItemStack>> itemStackStatus = new HashMap<String, PlayerStatus<ItemStack>>() {{
        put("getItemInHand", new PlayerStatus<>(Player::getItemInHand));
    }};

    public interface MiscellaneousStatus {
        PlayerStatus<Vector> getVelocity = new PlayerStatus<>(Player::getVelocity);
        PlayerStatus<EntityEquipment> getEquipment = new PlayerStatus<>(Player::getEquipment);

        PlayerStatus<PlayerInventory> getInventory = new PlayerStatus<>(Player::getInventory);
        PlayerStatus<Block> getTargetBlock = new PlayerStatus<>(player -> {
            BlockIterator iter = new BlockIterator(player, 100);
            Block lastBlock = iter.next();
            while (iter.hasNext()) {
                lastBlock = iter.next();
                if (lastBlock.getType() == Material.AIR) continue;
                break;
            }
            return lastBlock;
        });
    }

}
