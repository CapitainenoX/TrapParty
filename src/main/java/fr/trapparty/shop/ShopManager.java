package fr.trapparty.shop;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import fr.trapparty.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shop ultra-simplifié : un seul écran qui montre les items piège,
 * avec le fond GUI dessiné via custom font (resource pack).
 *
 * Le rendu visuel des boutons / titre / cadre est entièrement contenu
 * dans la texture `assets/trapparty/textures/gui/shop_traps.png`.
 * Le plugin ne place que les items et capture les clics.
 */
public class ShopManager {

    /** Caractère du titre qui déclenche l'affichage du fond GUI (custom font). */
    public static final String GUI_TITLE_CHAR = "";

    /** Slots Bukkit où placer les items piège. La texture doit aligner les "Buy" sous chaque slot. */
    private static final int[] ITEM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,    // row 1
            19, 20, 21, 22, 23, 24, 25,    // row 2
            28, 29, 30, 31, 32, 33, 34     // row 3
    };

    private static final int CLOSE_SLOT   = 49;

    private final TrapPartyPlugin plugin;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();
    private final Map<UUID, Long> lastFreePickup = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<Long>> clickHistory = new ConcurrentHashMap<>();
    private final Map<UUID, List<ShopItem>> displayedItems = new ConcurrentHashMap<>();
    private final Set<UUID> openShopInventories = ConcurrentHashMap.newKeySet();

    public ShopManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        categories.clear();
        ConfigurationSection root = plugin.configs().shop().getConfigurationSection("categories");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            String display = s.getString("display", id);
            Material icon = ItemBuilder.resolveMaterial(s.getString("icon", "CHEST"));
            if (icon == null) icon = Material.CHEST;
            int slot = s.getInt("slot", 10);
            List<ShopItem> items = new ArrayList<>();
            for (Map<?, ?> raw : s.getMapList("items")) {
                Material mat = ItemBuilder.resolveMaterial(String.valueOf(raw.get("material")));
                if (mat == null) continue;
                int amt = raw.get("amount") instanceof Number n ? n.intValue() : 1;
                int cost = raw.get("cost") instanceof Number n2 ? n2.intValue() : 0;
                boolean free = raw.get("free") instanceof Boolean b && b;
                String potion = raw.get("potion") != null ? raw.get("potion").toString() : null;
                items.add(new ShopItem(mat, amt, cost, free, potion));
            }
            categories.put(id.toLowerCase(Locale.ROOT), new ShopCategory(id, display, icon, slot, items));
        }
        plugin.getLogger().info("Loaded " + categories.size() + " shop categor(y/ies).");
    }

    public Collection<ShopCategory> categories() { return categories.values(); }
    public ShopCategory category(String id) { return categories.get(id.toLowerCase(Locale.ROOT)); }

    public void openMain(Player p) {
        if (!plugin.configs().root().getBoolean("shop.enabled", true)) {
            p.sendMessage("§cShop désactivé.");
            return;
        }
        ShopCategory trapsCat = category("traps");
        if (trapsCat == null) {
            p.sendMessage("§cAucune catégorie 'traps' configurée dans shop.yml.");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_CHAR);

        List<ShopItem> items = new ArrayList<>(trapsCat.getItems());
        // limite au nombre de slots dispo
        if (items.size() > ITEM_SLOTS.length) items = items.subList(0, ITEM_SLOTS.length);
        for (int i = 0; i < items.size(); i++) {
            inv.setItem(ITEM_SLOTS[i], itemDisplay(p, items.get(i)));
        }
        displayedItems.put(p.getUniqueId(), items);
        openShopInventories.add(p.getUniqueId());
        p.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!openShopInventories.contains(p.getUniqueId())) return;
        // Cancel uniquement les clics sur l'inventaire haut, pas l'inv du joueur
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getView().getTopInventory().getSize()) return;
        e.setCancelled(true);

        if (!checkRateLimit(p)) return;

        // Slot de fermeture (le bouton est dessiné dans la texture, le slot est vide)
        if (slot == CLOSE_SLOT) { p.closeInventory(); return; }

        // Item piège
        int idx = -1;
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            if (ITEM_SLOTS[i] == slot) { idx = i; break; }
        }
        if (idx < 0) return;

        List<ShopItem> items = displayedItems.get(p.getUniqueId());
        if (items == null || idx >= items.size()) return;
        buy(p, items.get(idx));
    }

    public void handleClose(UUID uuid) {
        openShopInventories.remove(uuid);
        displayedItems.remove(uuid);
    }

    private void buy(Player p, ShopItem item) {
        Game g = plugin.games().forPlayer(p);
        GamePlayer gp = g != null ? g.getPlayer(p.getUniqueId()) : null;

        if (item.isFree()) {
            int cooldown = plugin.configs().root().getInt("shop.free-item-cooldown", 30);
            long now = System.currentTimeMillis();
            long last = lastFreePickup.getOrDefault(p.getUniqueId(), 0L);
            long remaining = (cooldown * 1000L) - (now - last);
            if (remaining > 0) {
                plugin.messages().send(p, "shop.free-cooldown",
                        Map.of("time", String.valueOf(remaining / 1000 + 1)));
                plugin.version().sound(p, "ENTITY_VILLAGER_NO", 1f, 1.2f);
                return;
            }
            lastFreePickup.put(p.getUniqueId(), now);
        } else {
            if (!plugin.economy().withdraw(p, gp, item.getCost())) {
                plugin.messages().send(p, "shop.not-enough",
                        Map.of("amount", plugin.economy().format(item.getCost())));
                plugin.version().sound(p, "ENTITY_VILLAGER_NO", 1f, 1.2f);
                return;
            }
        }

        p.getInventory().addItem(item.toStack());
        plugin.messages().send(p, "shop.bought", Map.of(
                "item", item.getMaterial().name(),
                "amount", plugin.economy().format(item.getCost())));
        plugin.version().sound(p, "ENTITY_EXPERIENCE_ORB_PICKUP", 1f, 1.6f);
        // Rafraîchit l'affichage pour mettre à jour "Fonds insuffisants"
        refresh(p);
    }

    private void refresh(Player p) {
        List<ShopItem> items = displayedItems.get(p.getUniqueId());
        if (items == null) return;
        var view = p.getOpenInventory();
        if (view == null) return;
        Inventory top = view.getTopInventory();
        if (top == null || top.getSize() < 54) return;
        for (int i = 0; i < items.size() && i < ITEM_SLOTS.length; i++) {
            top.setItem(ITEM_SLOTS[i], itemDisplay(p, items.get(i)));
        }
    }

    private ItemStack itemDisplay(Player p, ShopItem it) {
        Game g = plugin.games().forPlayer(p);
        GamePlayer gp = g != null ? g.getPlayer(p.getUniqueId()) : null;
        double balance = plugin.economy().balance(p, gp);
        boolean affordable = it.isFree() || balance >= it.getCost();

        List<String> lore = new ArrayList<>();
        lore.add("§7Quantité : §f" + it.getAmount());
        if (it.isFree()) {
            lore.add("§a§lGRATUIT §7(cooldown " + plugin.configs().root().getInt("shop.free-item-cooldown", 30) + "s)");
        } else {
            lore.add("§6Prix : §e" + plugin.economy().format(it.getCost()));
        }
        lore.add(" ");
        lore.add(affordable ? "§a✓ Clique pour acheter" : "§c✗ Fonds insuffisants");
        return new ItemBuilder(it.toStack()).lore(lore).build();
    }

    private boolean checkRateLimit(Player p) {
        int max = plugin.configs().root().getInt("shop.anti-spam-clicks-per-second", 4);
        Deque<Long> history = clickHistory.computeIfAbsent(p.getUniqueId(), u -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        while (!history.isEmpty() && now - history.peekFirst() > 1000) history.pollFirst();
        if (history.size() >= max) return false;
        history.addLast(now);
        return true;
    }
}
