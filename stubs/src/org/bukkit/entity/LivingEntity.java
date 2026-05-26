package org.bukkit.entity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Collection;
public interface LivingEntity extends Entity, Damageable {
    AttributeInstance getAttribute(Attribute attr);
    void addPotionEffect(PotionEffect effect);
    Collection<PotionEffect> getActivePotionEffects();
    void removePotionEffect(PotionEffectType type);
}
