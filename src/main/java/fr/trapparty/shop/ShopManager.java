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

public class ShopManager {

    private final TrapPartyPlugin plugin;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();
    private final Map<UUID, Long> lastFreePickup = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<Long>> clickHistory = new ConcurrentHashMap<>();
    private final Map<UUID, String> openCategory = new ConcurrentHashMap<>();
    private final Set<UUID> openShopInventories = ConcurrentHashMap.newKeySet();

    public ShopManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        categories.clear();
        ConfigurationSection root = plugin.configs().shop().getConfigurationSection("categories");
        if (root == null) {
            plugin.getLogger().warning("No shop categories in shop.yml");
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            String display = s.getString("display", id);
            Material icon = fr.trapparty.util.ItemBuilder.resolveMaterial(s.getString("icon", "CHEST"));
            if (icon == null) icon = Material.CHEST;
            int slot = s.getInt("slot", 10);
            List<ShopItem> items = new ArrayList<>();
            for (Map<?, ?> raw : s.getMapList("items")) {
                Material mat = fr.trapparty.util.ItemBuilder.resolveMaterial(String.valueOf(raw.get("material")));
                if (mat == null) {
                    plugin.getLogger().fine("Shop: skip item, material not found: " + raw.get("material"));
                    continue;
                }
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
        Inventory inv = Bukkit.createInventory(null, 54, plugin.messages().get("shop.opened"));
        // titre & catégories
        for (ShopCategory c : categories.values()) {
            inv.setItem(c.getSlot(), new ItemBuilder(c.getIcon())
                    .name(c.getDisplay())
                    .lore("§7Clique pour ouvrir.")
                    .build());
        }
        // Catégorie "special" pour les items spéciaux (modèle resource pack custom)
        inv.setItem(40, new ItemBuilder(Material.NETHER_STAR)
                .name("&d&l✦ Items Spéciaux")
                .lore("§7Outils &dpremium&7 :",
                        "§7super foreuse, activateur,",
                        "§7grappin, magnet bomb...",
                        " ",
                        "§7Clique pour ouvrir.")
                .glow()
                .build());
        openCategory.remove(p.getUniqueId());
        openShopInventories.add(p.getUniqueId());
        p.openInventory(inv);
    }

    public void openSpecialItems(Player p) {
        Inventory inv = Bukkit.createInventory(null, 36,
                plugin.messages().get("shop.opened") + " §8» §dSpécial");
        int slot = 10;
        for (fr.trapparty.items.SpecialItem item : plugin.specialItems().all()) {
            if (item.cost() < 0) continue;
            org.bukkit.inventory.ItemStack stack = plugin.specialItems().build(item);
            java.util.List<String> lore = new java.util.ArrayList<>();
            if (stack.getItemMeta() != null && stack.getItemMeta().getLore() != null) {
                lore.addAll(stack.getItemMeta().getLore());
            }
            lore.add(" ");
            lore.add("§6Coût : §e" + item.cost() + " pièces");
            lore.add("§7Clique pour acheter.");
            org.bukkit.inventory.ItemStack icon = new ItemBuilder(stack).lore(lore).build();
            inv.setItem(slot++, icon);
            if (slot == 17) slot = 19;
            if (slot >= 35) break;
        }
        openCategory.put(p.getUniqueId(), "__special__");
        openShopInventories.add(p.getUniqueId());
        p.openInventory(inv);
    }

    public void openCategory(Player p, ShopCategory cat) {
        Inventory inv = Bukkit.createInventory(null, 54,
                plugin.messages().get("shop.opened") + " §8» " + cat.getDisplay());
        int slot = 0;
        for (ShopItem it : cat.getItems()) {
            List<String> lore = new ArrayList<>();
            lore.add("§7Quantité: §f" + it.getAmount());
            if (it.isFree()) {
                lore.add("§aGRATUIT §7(cooldown " + plugin.configs().root().getInt("shop.free-item-cooldown", 30) + "s)");
            } else {
                lore.add("§6Coût: §e" + it.getCost() + " pièces");
            }
            lore.add(" ");
            lore.add("§7Clique pour acheter.");
            ItemStack icon = new ItemBuilder(it.toStack()).lore(lore).build();
            inv.setItem(slot++, icon);
            if (slot >= 54) break;
        }
        openCategory.put(p.getUniqueId(), cat.getId().toLowerCase(Locale.ROOT));
        openShopInventories.add(p.getUniqueId());
        p.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!openShopInventories.contains(p.getUniqueId())) return;
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // anti-spam
        if (!checkRateLimit(p)) return;

        String cat = openCategory.get(p.getUniqueId());
        if (cat == null) {
            // niveau catégories : detect special button
            if (clicked.getType() == Material.NETHER_STAR) { openSpecialItems(p); return; }
            for (ShopCategory c : categories.values()) {
                if (c.getIcon() == clicked.getType()) { openCategory(p, c); return; }
            }
            return;
        }
        if (cat.equals("__special__")) {
            buySpecial(p, clicked);
            return;
        }
        ShopCategory category = category(cat);
        if (category == null) return;

        // find by slot
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= category.getItems().size()) return;
        buy(p, category.getItems().get(slot));
    }

    public void handleClose(UUID uuid) {
        openShopInventories.remove(uuid);
        openCategory.remove(uuid);
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

    private void buy(Player p, ShopItem item) {
        Game g = plugin.games().forPlayer(p);
        if (g == null) {
            p.sendMessage("§cTu n'es pas en partie.");
            return;
        }
        GamePlayer gp = g.getPlayer(p.getUniqueId());
        if (gp == null) return;

        if (item.isFree()) {
            int cooldown = plugin.configs().root().getInt("shop.free-item-cooldown", 30);
            long now = System.currentTimeMillis();
            long last = lastFreePickup.getOrDefault(p.getUniqueId(), 0L);
            long remaining = (cooldown * 1000L) - (now - last);
            if (remaining > 0) {
                plugin.messages().send(p, "shop.free-cooldown",
                        Map.of("time", String.valueOf(remaining / 1000 + 1)));
                return;
            }
            lastFreePickup.put(p.getUniqueId(), now);
        } else {
            if (!gp.spend(item.getCost())) {
                plugin.messages().send(p, "shop.not-enough", Map.of("amount", String.valueOf(item.getCost())));
                return;
            }
        }

        p.getInventory().addItem(item.toStack());
        plugin.messages().send(p, "shop.bought", Map.of(
                "item", item.getMaterial().name(),
                "amount", String.valueOf(item.getCost())));
        plugin.version().sound(p, "ENTITY_EXPERIENCE_ORB_PICKUP", 1f, 1.6f);
    }

    /** Achat d'un item spécial via son matériau d'icône (les icônes sont uniques). */
    private void buySpecial(Player p, ItemStack clicked) {
        // L'item cliqué porte déjà la metadata du SpecialItem (id PDC)
        String id = plugin.specialItems().idOf(clicked);
        if (id == null) return;
        fr.trapparty.items.SpecialItem def = plugin.specialItems().get(id);
        if (def == null || def.cost() < 0) return;
        fr.trapparty.game.Game g = plugin.games().forPlayer(p);
        if (g == null) { plugin.messages().send(p, "generic.not-in-game"); return; }
        fr.trapparty.game.GamePlayer gp = g.getPlayer(p.getUniqueId());
        if (gp == null) return;
        if (!gp.spend(def.cost())) {
            plugin.messages().send(p, "shop.not-enough", Map.of("amount", String.valueOf(def.cost())));
            return;
        }
        p.getInventory().addItem(plugin.specialItems().build(def));
        plugin.messages().send(p, "shop.bought", Map.of(
                "item", def.id(),
                "amount", String.valueOf(def.cost())));
        plugin.version().sound(p, "BLOCK_ENCHANTMENT_TABLE_USE", 1f, 1.5f);
    }
}
