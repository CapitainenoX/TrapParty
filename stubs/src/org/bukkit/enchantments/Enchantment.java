package org.bukkit.enchantments;
import org.bukkit.NamespacedKey;
import org.bukkit.Keyed;
public class Enchantment implements Keyed {
    public static final Enchantment LURE = new Enchantment();
    public static final Enchantment SHARPNESS = new Enchantment();
    public static final Enchantment DAMAGE_ALL = new Enchantment();
    public static Enchantment getByName(String name){return null;}
    public NamespacedKey getKey(){return null;}
}
