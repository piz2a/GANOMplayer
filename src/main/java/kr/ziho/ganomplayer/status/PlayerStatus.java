package kr.ziho.ganomplayer.status;

import kr.ziho.ganomplayer.ItemLimited;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
* - Equipment
* - Blocking
* - Sleeping
* - Farming
* - Walk speed change
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

    public static final HashMap<String, PlayerStatus<Boolean>> booleanStatus = new HashMap<String, PlayerStatus<Boolean>>() {{
        put("isOnGround", new PlayerStatus<>(player -> ((LivingEntity) player).isOnGround()));
        put("isSneaking", new PlayerStatus<>(Player::isSneaking));
        put("isSprinting", new PlayerStatus<>(Player::isSprinting));
    }};

    public static final HashMap<String, PlayerStatus<Double>> doubleStatus = new HashMap<String, PlayerStatus<Double>>() {{
        put("getLastDamage", new PlayerStatus<>(Player::getLastDamage));
        put("getHealth", new PlayerStatus<>(Player::getHealth));
    }};

    public static final HashMap<String, PlayerStatus<ItemStack>> itemStackStatus = new HashMap<String, PlayerStatus<ItemStack>>() {{
        put("getItemInHand", new PlayerStatus<>(Player::getItemInHand));
    }};

    public interface MiscellaneousStatus {
        PlayerStatus<ItemLimited> getItemLimitedInHand = new PlayerStatus<>(player -> ItemLimited.from(player.getItemInHand()));
        PlayerStatus<Vector> getVelocity = new PlayerStatus<>(Player::getVelocity);
        /*
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
         */
    }

}
