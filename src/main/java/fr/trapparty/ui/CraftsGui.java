package fr.trapparty.ui;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.items.SpecialItem;
import fr.trapparty.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI d'information sur les items spéciaux (lecture seule).
 * Le fond est dessiné via custom font (resource pack), char .
 * Affiche les 4 items spéciaux et explique comment les obtenir.
 */
public class CraftsGui {

    public static final String GUI_TITLE_CHAR = "";

    /** Slots pour les 4 items spéciaux dans une inv 27-slot (3 lignes x 9). */
    private static final int[] ITEM_SLOTS = { 10, 12, 14, 16 };

    private final TrapPartyPlugin plugin;
    private final Set<UUID> openInventories = ConcurrentHashMap.newKeySet();

    public CraftsGui(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE_CHAR);
        List<SpecialItem> items = new ArrayList<>(plugin.specialItems().all());
        for (int i = 0; i < items.size() && i < ITEM_SLOTS.length; i++) {
            inv.setItem(ITEM_SLOTS[i], buildInfoIcon(items.get(i)));
        }
        openInventories.add(p.getUniqueId());
        p.openInventory(inv);
    }

    private ItemStack buildInfoIcon(SpecialItem def) {
        ItemStack base = plugin.specialItems().build(def);
        List<String> lore = new ArrayList<>();
        if (base.getItemMeta() != null && base.getItemMeta().getLore() != null) {
            lore.addAll(base.getItemMeta().getLore());
        }
        lore.add(" ");
        lore.add("§6&lComment obtenir :");
        if (def.cost() > 0) {
            lore.add("§7• Shop : §e" + plugin.economy().format(def.cost()));
        }
        // Liste les kits qui le contiennent (lecture brute de kits.yml)
        var kitsCfg = plugin.configs().kits().getConfigurationSection("kits");
        if (kitsCfg != null) {
            for (String kitId : kitsCfg.getKeys(false)) {
                var sec = kitsCfg.getConfigurationSection(kitId);
                if (sec == null) continue;
                List<String> specials = sec.getStringList("special-items");
                if (specials.contains(def.id())) {
                    String display = sec.getString("display-name", kitId);
                    lore.add("§7• Kit : §e" + display);
                }
            }
        }
        lore.add("§7• Admin : §e/tpa give §7<player> §e" + def.id());
        return new ItemBuilder(base).lore(lore).build();
    }

    public void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!openInventories.contains(p.getUniqueId())) return;
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getView().getTopInventory().getSize()) return;
        e.setCancelled(true);
    }

    public void handleClose(UUID uuid) {
        openInventories.remove(uuid);
    }
}
