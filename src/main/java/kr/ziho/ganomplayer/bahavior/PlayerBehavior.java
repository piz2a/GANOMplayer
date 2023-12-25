package kr.ziho.ganomplayer.bahavior;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.HashMap;


public class PlayerBehavior<T> {
    BehaviorSetter<T> setter;

    public interface BehaviorSetter<T> {
        void set(Player p, T value);
    }

    public PlayerBehavior(BehaviorSetter<T> setter) {
        this.setter = setter;
    }

    public void set(Player player, T value) {
        setter.set(player, value);
    }

    public static HashMap<String, PlayerBehavior<Boolean>> booleanBehavior = new HashMap<String, PlayerBehavior<Boolean>>() {{
        // put("isOnGround", new PlayerBehavior<>(player -> ((LivingEntity) player).isOnGround()));
        put("setSneaking", new PlayerBehavior<>(Player::setSneaking));
        put("setSprinting", new PlayerBehavior<>(Player::setSprinting));
        // put("isBlocking", new PlayerBehavior<>(Player::isBlocking));
        // put("isSleeping", new PlayerBehavior<>(Player::isSleeping));
        // put("isInsideVehicle", new PlayerBehavior<>(Player::isInsideVehicle));
    }};

    public static HashMap<String, PlayerBehavior<Float>> floatStatus = new HashMap<String, PlayerBehavior<Float>>() {{
        put("setWalkSpeed", new PlayerBehavior<>(Player::setWalkSpeed));
    }};

    public static HashMap<String, PlayerBehavior<Double>> doubleStatus = new HashMap<String, PlayerBehavior<Double>>() {{
        // put("getLastDamage", new PlayerBehavior<>(Player::getLastDamage));
        // put("getHealthRate", new PlayerBehavior<>(player -> player.getHealth() / player.getMaxHealth()));
    }};

    public static HashMap<String, PlayerBehavior<Integer>> integerStatus = new HashMap<String, PlayerBehavior<Integer>>() {{
        // put("getFoodLevel", new PlayerBehavior<>(Player::getFoodLevel));
    }};

    public static HashMap<String, PlayerBehavior<ItemStack>> itemStackStatus = new HashMap<String, PlayerBehavior<ItemStack>>() {{
        put("setItemInHand", new PlayerBehavior<>(Player::setItemInHand));
    }};

    public interface MiscellaneousStatus {
        PlayerBehavior<Vector> setVelocity = new PlayerBehavior<>(Player::setVelocity);
        /*
        PlayerBehavior<EntityEquipment> getEquipment = new PlayerBehavior<>(Player::getEquipment);

        PlayerBehavior<PlayerInventory> getInventory = new PlayerBehavior<>(Player::getInventory);
        PlayerBehavior<Block> getTargetBlock = new PlayerBehavior<>(player -> {
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