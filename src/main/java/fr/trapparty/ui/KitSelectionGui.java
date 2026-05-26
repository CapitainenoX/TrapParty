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

/**
 * GUI de sélection des kits. Fond dessiné via custom font (resource pack),
 * char . Les kits sont placés à des slots fixes, alignés sur la
 * texture pour que les "boutons" dessinés dans l'image correspondent.
 */
public class KitSelectionGui {

    public static final String GUI_TITLE_CHAR = "";

    /** Slots fixes pour 8 kits (2 rangées x 4) dans une inv 54 slots. */
    private static final int[] KIT_SLOTS = {
            19, 21, 23, 25,    // row 2
            37, 39, 41, 43     // row 4
    };

    private final TrapPartyPlugin plugin;
    private final Set<UUID> openInventories = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Map<Integer, Kit>> slotIndex = new ConcurrentHashMap<>();

    public KitSelectionGui(TrapPartyPlugin plugin) { this.plugin = plugin; }

    public void open(Player p) {
        List<Kit> kits = new ArrayList<>(plugin.kits().all());
        if (kits.size() > KIT_SLOTS.length) {
            plugin.getLogger().warning("Kits.yml a " + kits.size() + " kits mais le GUI ne peut en afficher que "
                    + KIT_SLOTS.length + " — les " + (kits.size() - KIT_SLOTS.length) + " derniers sont cachés.");
        }
        // Récupère le kit actuel (s'il y en a un) pour le mettre en surbrillance
        var game = plugin.games().forPlayer(p);
        var gp = game != null ? game.getPlayer(p.getUniqueId()) : null;
        Kit current = gp != null ? gp.getKit() : null;

        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_CHAR);
        Map<Integer, Kit> mapping = new HashMap<>();
        for (int i = 0; i < kits.size() && i < KIT_SLOTS.length; i++) {
            Kit k = kits.get(i);
            int slot = KIT_SLOTS[i];
            boolean isCurrent = current != null && current.getId().equalsIgnoreCase(k.getId());
            List<String> lore = new ArrayList<>();
            lore.add(" ");
            lore.addAll(k.getDescription());
            lore.add(" ");
            if (isCurrent) lore.add("§b★ Kit actuel");
            else lore.add(k.canUse(p) ? "§a✓ Clique pour sélectionner" : "§c✗ Kit verrouillé");
            ItemBuilder ib = new ItemBuilder(k.getIcon())
                    .name((isCurrent ? "§b★ " : "") + k.getDisplayName())
                    .lore(lore);
            if (isCurrent) ib.glow();
            inv.setItem(slot, ib.build());
            mapping.put(slot, k);
        }
        openInventories.add(p.getUniqueId());
        slotIndex.put(p.getUniqueId(), mapping);
        p.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!openInventories.contains(p.getUniqueId())) return;
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getView().getTopInventory().getSize()) return;
        e.setCancelled(true);
        Map<Integer, Kit> mapping = slotIndex.get(p.getUniqueId());
        if (mapping == null) return;
        Kit k = mapping.get(slot);
        if (k == null) return;
        if (!k.canUse(p)) {
            plugin.messages().send(p, "kit.locked");
            return;
        }
        var game = plugin.games().forPlayer(p);
        if (game == null) {
            plugin.messages().send(p, "generic.not-in-game");
            return;
        }
        // Empêche le kit-switch silencieux en combat
        var st = game.getState();
        if (st != fr.trapparty.game.GameState.WAITING
                && st != fr.trapparty.game.GameState.STARTING
                && st != fr.trapparty.game.GameState.PREPARATION) {
            plugin.messages().send(p, "kit.too-late");
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
