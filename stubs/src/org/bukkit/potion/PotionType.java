package org.bukkit.potion;
import org.bukkit.NamespacedKey;
import org.bukkit.Keyed;
public enum PotionType implements Keyed {
    HEALING, SWIFTNESS, STRENGTH, WATER, AWKWARD, MUNDANE, REGEN, REGENERATION;
    public NamespacedKey getKey(){return null;}
}
