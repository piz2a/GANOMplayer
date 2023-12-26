package kr.ziho.ganomplayer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public enum ItemLimited {

    OTHER(0), BLOCK(1), SWORD(2), PICKAXE(3), AXE(4), AIR(5);

    private final int value;

    ItemLimited(int value) {
        this.value = value;
    }

    static Map<Integer, ItemLimited> map = new HashMap<>();

    static {
        for (ItemLimited itemLimited : ItemLimited.values()) {
            map.put(itemLimited.value, itemLimited);
        }
    }

    public static ItemLimited from(int code) {
        return map.get(code);
    }

    public static ItemLimited from(ItemStack itemStack) {
        Material material = itemStack.getType();
        switch (material) {
            case AIR:
                return AIR;
            case WOOD_SWORD: case STONE_SWORD: case GOLD_SWORD: case IRON_SWORD: case DIAMOND_SWORD:
                return SWORD;
            case WOOD_PICKAXE: case STONE_PICKAXE: case GOLD_PICKAXE: case IRON_PICKAXE: case DIAMOND_PICKAXE:
                return PICKAXE;
            case WOOD_AXE: case STONE_AXE: case GOLD_AXE: case IRON_AXE: case DIAMOND_AXE:
                return AXE;
        }
        if (material.isBlock())
            return BLOCK;
        return OTHER;
    }

    public ItemStack toItemStack() {
        switch (this) {
            case BLOCK:
                return new ItemStack(Material.COBBLESTONE, 64);
            case SWORD:
                return new ItemStack(Material.STONE_SWORD);
            case PICKAXE:
                return new ItemStack(Material.IRON_PICKAXE);
            case AXE:
                return new ItemStack(Material.IRON_AXE);
            default:
                return null;
        }
    }

    public int getValue() {
        return value;
    }

}
