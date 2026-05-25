package fr.trapparty.util;

import fr.trapparty.config.MessagesManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material mat) { this(mat, 1); }
    public ItemBuilder(Material mat, int amount) { this.item = new ItemStack(mat, Math.max(1, amount)); }
    public ItemBuilder(ItemStack base) { this.item = base.clone(); }

    public ItemBuilder amount(int n) { item.setAmount(Math.max(1, n)); return this; }

    public ItemBuilder name(String name) {
        return meta(m -> m.setDisplayName(MessagesManager.color(name)));
    }

    public ItemBuilder lore(String... lines) {
        List<String> colored = new ArrayList<>(lines.length);
        for (String l : lines) colored.add(MessagesManager.color(l));
        return meta(m -> m.setLore(colored));
    }

    public ItemBuilder lore(List<String> lines) {
        List<String> colored = new ArrayList<>(lines.size());
        for (String l : lines) colored.add(MessagesManager.color(l));
        return meta(m -> m.setLore(colored));
    }

    public ItemBuilder enchant(Enchantment e, int level) {
        if (e == null) return this;
        return meta(m -> m.addEnchant(e, level, true));
    }

    public ItemBuilder unbreakable(boolean b) {
        return meta(m -> m.setUnbreakable(b));
    }

    public ItemBuilder glow() {
        return meta(m -> {
            Enchantment glow = lookupEnchant("LURE", "UNBREAKING", "DURABILITY");
            if (glow != null) m.addEnchant(glow, 1, true);
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
    }

    /** Cross-version enchantment lookup: try modern names, then legacy. */
    public static Enchantment lookupEnchant(String... candidates) {
        for (String name : candidates) {
            try {
                @SuppressWarnings("deprecation")
                Enchantment e = Enchantment.getByName(name);
                if (e != null) return e;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static final java.util.Map<String, String[]> ENCHANT_ALIASES = java.util.Map.ofEntries(
            java.util.Map.entry("SHARPNESS", new String[]{"SHARPNESS", "DAMAGE_ALL"}),
            java.util.Map.entry("POWER", new String[]{"POWER", "ARROW_DAMAGE"}),
            java.util.Map.entry("INFINITY", new String[]{"INFINITY", "ARROW_INFINITE"}),
            java.util.Map.entry("PUNCH", new String[]{"PUNCH", "ARROW_KNOCKBACK"}),
            java.util.Map.entry("FLAME", new String[]{"FLAME", "ARROW_FIRE"}),
            java.util.Map.entry("PROTECTION", new String[]{"PROTECTION", "PROTECTION_ENVIRONMENTAL"}),
            java.util.Map.entry("EFFICIENCY", new String[]{"EFFICIENCY", "DIG_SPEED"}),
            java.util.Map.entry("UNBREAKING", new String[]{"UNBREAKING", "DURABILITY"}),
            java.util.Map.entry("FORTUNE", new String[]{"FORTUNE", "LOOT_BONUS_BLOCKS"}),
            java.util.Map.entry("LOOTING", new String[]{"LOOTING", "LOOT_BONUS_MOBS"}),
            java.util.Map.entry("FEATHER_FALLING", new String[]{"FEATHER_FALLING", "PROTECTION_FALL"}),
            java.util.Map.entry("FIRE_PROTECTION", new String[]{"FIRE_PROTECTION", "PROTECTION_FIRE"}),
            java.util.Map.entry("BLAST_PROTECTION", new String[]{"BLAST_PROTECTION", "PROTECTION_EXPLOSIONS"}),
            java.util.Map.entry("PROJECTILE_PROTECTION", new String[]{"PROJECTILE_PROTECTION", "PROTECTION_PROJECTILE"})
    );

    public ItemBuilder hideAll() {
        return meta(m -> m.addItemFlags(ItemFlag.values()));
    }

    public ItemBuilder meta(Consumer<ItemMeta> consumer) {
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            consumer.accept(m);
            item.setItemMeta(m);
        }
        return this;
    }

    public ItemStack build() { return item; }

    /**
     * Parse une chaîne "MATERIAL[:amount][:ENCHANT=lvl,...]"
     * Exemples : "DIAMOND_SWORD", "DIAMOND_SWORD:1:SHARPNESS=2,UNBREAKING=3"
     */
    public static ItemStack fromString(String def) {
        if (def == null || def.isEmpty()) return null;
        String[] parts = def.split(":");
        Material mat = Material.matchMaterial(parts[0]);
        if (mat == null) return null;
        int amount = 1;
        if (parts.length >= 2) {
            try { amount = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
        }
        ItemBuilder b = new ItemBuilder(mat, amount);
        if (parts.length >= 3) {
            String[] enchants = parts[2].split(",");
            for (String e : enchants) {
                String[] kv = e.split("=");
                if (kv.length != 2) continue;
                String key = kv[0].toUpperCase();
                String[] aliases = ENCHANT_ALIASES.getOrDefault(key, new String[]{key});
                Enchantment ench = lookupEnchant(aliases);
                if (ench == null) continue;
                int lvl;
                try { lvl = Integer.parseInt(kv[1]); } catch (NumberFormatException ex) { continue; }
                b.enchant(ench, lvl);
            }
        }
        return b.build();
    }
}
