package kr.ziho.ganomplayer.bahavior;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;


public class PlayerBehavior<T> {

    BehaviorGetter<T> getter;
    BehaviorSetter<T> setter;

    public interface BehaviorGetter<T> {
        T get(Player p);
    }

    public interface BehaviorSetter<T> {
        void set(Player p, T value);
    }

    public PlayerBehavior(BehaviorGetter<T> getter, BehaviorSetter<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public T get(Player player) {
        return getter.get(player);
    }

    public void set(Player player, T value) {
        setter.set(player, value);
    }

    public static HashMap<String, PlayerBehavior<Boolean>> booleanBehaviors = new HashMap<String, PlayerBehavior<Boolean>>() {{
        put("jump", new PlayerBehavior<Boolean>((player) -> ((LivingEntity) player).isOnGround(), (player) -> {
            Vector velocity = player.getVelocity();
            player.setVelocity(new Vector(velocity.getX(), 0.4, velocity.getZ()));
        }));
    }};

}
