package org.bukkit.attribute;
import org.bukkit.NamespacedKey;
import org.bukkit.Keyed;
public enum Attribute implements Keyed {
    MAX_HEALTH, GENERIC_MAX_HEALTH, GENERIC_MOVEMENT_SPEED;
    public NamespacedKey getKey(){return null;}
}
