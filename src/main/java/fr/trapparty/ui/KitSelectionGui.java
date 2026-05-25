package fr.trapparty.ui;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.kit.Kit;
import fr.trapparty.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KitSelectionGui {

    private final TrapPartyPlugin plugin;
    private final Set<UUID> openInventories = ConcurrentHashMap.newKeySet();
    private final Map<UUID, List<Kit>> slotIndex = new ConcurrentHashMap<>();

    public KitSelectionGui(TrapPartyPlugin plugin) { this.plugin = plugin; }

    public void open(Player p) {
        List<Kit> kits = new ArrayList<>(plugin.kits().all());
        int rows = Math.max(1, (kits.size() + 8) / 9);
        Inventory inv = Bukkit.createInventory(null, rows * 9, "§eChoisis ton kit");
        for (int i = 0; i < kits.size(); i++) {
            Kit k = kits.get(i);
            List<String> lore = new ArrayList<>();
            lore.add(" ");
            lore.addAll(k.getDescription());
            lore.add(" ");
            lore.add(k.canUse(p) ? "§aClique pour sélectionner" : "§cKit verrouillé");
            inv.setItem(i, new ItemBuilder(k.getIcon())
                    .name(k.getDisplayName())
                    .lore(lore)
                    .build());
        }
        openInventories.add(p.getUniqueId());
        slotIndex.put(p.getUniqueId(), kits);
        p.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!openInventories.contains(p.getUniqueId())) return;
        e.setCancelled(true);
        ItemStack it = e.getCurrentItem();
        if (it == null) return;
        int slot = e.getRawSlot();
        List<Kit> list = slotIndex.get(p.getUniqueId());
        if (list == null || slot < 0 || slot >= list.size()) return;
        Kit k = list.get(slot);
        if (!k.canUse(p)) {
            plugin.messages().send(p, "kit.locked");
            return;
        }
        var game = plugin.games().forPlayer(p);
        if (game == null) {
            plugin.messages().send(p, "generic.not-in-game");
            return;
        }
        game.selectKit(p, k);
        p.closeInventory();
    }

    public void handleClose(UUID uuid) {
        openInventories.remove(uuid);
        slotIndex.remove(uuid);
    }
}
