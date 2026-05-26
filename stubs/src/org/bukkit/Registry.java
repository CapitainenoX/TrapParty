package org.bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
public interface Registry<T> {
    T get(NamespacedKey key);
    Registry<Enchantment> ENCHANTMENT = null;
    Registry<PotionEffectType> EFFECT = null;
    Registry<Attribute> ATTRIBUTE = null;
    Registry<PotionType> POTION = null;
}
