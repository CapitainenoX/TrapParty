package fr.trapparty.kit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Kit {

    private final String id;
    private final String displayName;
    private final Material icon;
    private final List<String> description;
    private final String permission;
    private final Map<String, ItemStack> items;     // slot key -> item
    private final List<EffectDef> effects;
    private final int extraCoins;
    private final List<String> specialItemIds;       // IDs d'items spéciaux à donner

    public Kit(String id, String displayName, Material icon, List<String> description,
               String permission, Map<String, ItemStack> items, List<EffectDef> effects, int extraCoins,
               List<String> specialItemIds) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description == null ? Collections.emptyList() : description;
        this.permission = permission;
        this.items = items;
        this.effects = effects;
        this.extraCoins = extraCoins;
        this.specialItemIds = specialItemIds == null ? Collections.emptyList() : specialItemIds;
    }

    public void apply(Player p) {
        ItemStack helmet = items.get("helmet");
        ItemStack chest = items.get("chestplate");
        ItemStack legs = items.get("leggings");
        ItemStack boots = items.get("boots");
        if (helmet != null) p.getInventory().setHelmet(helmet);
        if (chest != null) p.getInventory().setChestplate(chest);
        if (legs != null) p.getInventory().setLeggings(legs);
        if (boots != null) p.getInventory().setBoots(boots);

        ItemStack main = items.get("mainhand");
        if (main != null) p.getInventory().setItemInMainHand(main);
        ItemStack off = items.get("offhand");
        if (off != null) {
            try { p.getInventory().setItemInOffHand(off); } catch (Throwable ignored) {}
        }
        for (Map.Entry<String, ItemStack> e : items.entrySet()) {
            if (!e.getKey().startsWith("slot")) continue;
            int idx;
            try { idx = Integer.parseInt(e.getKey().substring(4)); } catch (NumberFormatException ex) { continue; }
            p.getInventory().setItem(idx, e.getValue());
        }
        for (EffectDef ed : effects) ed.apply(p);
        // items spéciaux
        var mgr = fr.trapparty.TrapPartyPlugin.get().specialItems();
        for (String sid : specialItemIds) {
            var def = mgr.get(sid);
            if (def == null) continue;
            p.getInventory().addItem(mgr.build(def));
        }
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public List<String> getDescription() { return description; }
    public String getPermission() { return permission; }
    public int getExtraCoins() { return extraCoins; }

    public boolean canUse(Player p) {
        return permission == null || permission.isEmpty() || p.hasPermission(permission);
    }

    public static class EffectDef {
        private final PotionEffectType type;
        private final int durationTicks;
        private final int amplifier;

        public EffectDef(PotionEffectType type, int durationTicks, int amplifier) {
            this.type = type;
            this.durationTicks = durationTicks;
            this.amplifier = amplifier;
        }

        public void apply(Player p) {
            int dur = durationTicks < 0 ? Integer.MAX_VALUE : durationTicks;
            p.addPotionEffect(new PotionEffect(type, dur, amplifier, true, false));
        }

        public static EffectDef parse(String def) {
            // NAME:duration:amplifier
            String[] parts = def.split(":");
            if (parts.length < 1) return null;
            PotionEffectType type = lookupEffect(parts[0]);
            if (type == null) {
                // alias historiques (anciens noms 1.13-1.20 → noms modernes 1.20.5+)
                String alias = switch (parts[0].toUpperCase()) {
                    case "DAMAGE_RESISTANCE" -> "RESISTANCE";
                    case "INCREASE_DAMAGE" -> "STRENGTH";
                    case "FAST_DIGGING" -> "HASTE";
                    case "SLOW" -> "SLOWNESS";
                    case "SLOW_DIGGING" -> "MINING_FATIGUE";
                    case "JUMP" -> "JUMP_BOOST";
                    case "HARM" -> "INSTANT_DAMAGE";
                    case "HEAL" -> "INSTANT_HEALTH";
                    case "CONFUSION" -> "NAUSEA";
                    default -> null;
                };
                if (alias != null) type = lookupEffect(alias);
            }
            if (type == null) return null;
            int dur = parts.length >= 2 ? safeInt(parts[1], -1) : -1;
            int amp = parts.length >= 3 ? safeInt(parts[2], 0) : 0;
            return new EffectDef(type, dur, amp);
        }

        private static PotionEffectType lookupEffect(String name) {
            if (name == null) return null;
            String upper = name.toUpperCase();
            // Registry moderne
            try {
                org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(upper.toLowerCase());
                PotionEffectType t = org.bukkit.Registry.EFFECT.get(key);
                if (t != null) return t;
            } catch (Throwable ignored) {}
            // Legacy
            try {
                @SuppressWarnings("deprecation")
                PotionEffectType t = PotionEffectType.getByName(upper);
                if (t != null) return t;
            } catch (Throwable ignored) {}
            return null;
        }

        private static int safeInt(String s, int fb) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ex) { return fb; }
        }
    }
}
