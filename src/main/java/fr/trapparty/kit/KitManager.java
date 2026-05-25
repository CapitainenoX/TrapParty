package fr.trapparty.kit;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitManager {

    private final TrapPartyPlugin plugin;
    private final Map<String, Kit> kits = new LinkedHashMap<>();

    private fr.trapparty.ui.KitSelectionGui gui;

    public KitManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        reload();
        this.gui = new fr.trapparty.ui.KitSelectionGui(plugin);
    }

    public fr.trapparty.ui.KitSelectionGui gui() { return gui; }

    public void reload() {
        kits.clear();
        ConfigurationSection root = plugin.configs().kits().getConfigurationSection("kits");
        if (root == null) {
            plugin.getLogger().warning("No kits in kits.yml");
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            try {
                kits.put(id.toLowerCase(Locale.ROOT), parse(id, s));
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load kit " + id + ": " + ex.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + kits.size() + " kit(s).");
    }

    private Kit parse(String id, ConfigurationSection s) {
        String display = s.getString("display-name", id);
        Material icon = Material.matchMaterial(s.getString("icon", "PAPER"));
        if (icon == null) icon = Material.PAPER;
        List<String> desc = s.getStringList("description");
        String perm = s.getString("permission", "");
        Map<String, ItemStack> items = new LinkedHashMap<>();
        ConfigurationSection itemsSec = s.getConfigurationSection("items");
        if (itemsSec != null) {
            for (String slot : itemsSec.getKeys(false)) {
                String def = itemsSec.getString(slot);
                ItemStack it = ItemBuilder.fromString(def);
                if (it != null) items.put(slot, it);
            }
        }
        List<Kit.EffectDef> effects = new ArrayList<>();
        for (String e : s.getStringList("effects")) {
            Kit.EffectDef ed = Kit.EffectDef.parse(e);
            if (ed != null) effects.add(ed);
        }
        int extraCoins = s.getInt("extra-coins", 0);
        return new Kit(id, display, icon, desc, perm, items, effects, extraCoins);
    }

    public Kit get(String id) {
        if (id == null) return null;
        return kits.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<Kit> all() { return Collections.unmodifiableCollection(kits.values()); }
}
