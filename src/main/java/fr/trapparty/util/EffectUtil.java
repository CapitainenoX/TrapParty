package fr.trapparty.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

/**
 * Résolution cross-version des PotionEffectType.
 * Sur 1.20.5+ / 26.x les types sont accessibles via Registry.EFFECT ; les anciens
 * champs statiques (SPEED, JUMP, INCREASE_DAMAGE…) sont dépréciés voire supprimés
 * dans les versions très récentes.
 */
public final class EffectUtil {

    private EffectUtil() {}

    public static PotionEffectType byName(String... candidates) {
        for (String name : candidates) {
            if (name == null || name.isEmpty()) continue;
            String upper = name.toUpperCase();
            String lower = upper.toLowerCase();
            // 1) Registry moderne
            try {
                NamespacedKey key = NamespacedKey.minecraft(lower);
                PotionEffectType t = Registry.EFFECT.get(key);
                if (t != null) return t;
            } catch (Throwable ignored) {}
            // 2) getByName legacy
            try {
                @SuppressWarnings("deprecation")
                PotionEffectType t = PotionEffectType.getByName(upper);
                if (t != null) return t;
            } catch (Throwable ignored) {}
        }
        return null;
    }
}
