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
 * Shop custom multi-onglets utilisant Vault si dispo, sinon pièces de partie.
 * UI textures custom (custom_model_data 20xx) cf. resourcepack/TEXTURES.md.
 */
public class ShopManager {

    // Custom model data des éléments d'UI (mapping documenté dans TEXTURES.md)
    public static final int CMD_BTN_BLOCKS      = 2001;
    public static final int CMD_BTN_TRAPS       = 2002;
    public static final int CMD_BTN_UTILITY     = 2003;
    public static final int CMD_BTN_CONSUMABLES = 2004;
    public static final int CMD_BTN_REDSTONE    = 2005;
    public static final int CMD_BTN_MOBILITY    = 2006;
    public static final int CMD_BTN_SPECIAL     = 2007;
    public static final int CMD_CURRENCY        = 2010;
    public static final int CMD_BTN_BUY         = 2020;
    public static final int CMD_BTN_LOCKED      = 2021;
    public static final int CMD_BTN_BACK        = 2022;
    public static final int CMD_BTN_CLOSE       = 2023;
    public static final int CMD_BORDER          = 2030;
    public static final int CMD_LOGO            = 2031;

    private static final Map<String, Integer> CATEGORY_BUTTON_CMD = Map.of(
            "blocks",      CMD_BTN_BLOCKS,
            "traps",       CMD_BTN_TRAPS,
            "utility",     CMD_BTN_UTILITY,
            "consumables", CMD_BTN_CONSUMABLES,
            "redstone",    CMD_BTN_REDSTONE,
            "mobility",    CMD_BTN_MOBILITY
    );

    private static final int MAIN_INV_SIZE = 54;
    private static final int CAT_INV_SIZE = 54;

    private final TrapPartyPlugin plugin;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();
    private final Map<UUID, Long> lastFreePickup = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<Long>> clickHistory = new ConcurrentHashMap<>();
    private final Map<UUID, String> openCategory = new ConcurrentHashMap<>();
    private final Map<UUID, List<ShopItem>> categoryItemIndex = new ConcurrentHashMap<>();
    private final Map<UUID, List<fr.trapparty.items.SpecialItem>> specialIndex = new ConcurrentHashMap<>();
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

    // ---------- Menu principal ----------

    public void openMain(Player p) {
        if (!plugin.configs().root().getBoolean("shop.enabled", true)) {
            p.sendMessage("§cShop désactivé.");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, MAIN_INV_SIZE,
                "§c§lTrap§f§lParty §8» §6Boutique");
        decorateBorder(inv, MAIN_INV_SIZE);

        // Logo central
        inv.setItem(4, customStack(Material.NETHER_STAR, CMD_LOGO,
                "§6§lBoutique §8»§r §eTrapParty",
                List.of("§7Bienvenue dans le shop !",
                        "§7Économie : §e" + economyLabel())));

        // Catégories autour
        int[] catSlots = { 19, 21, 23, 25, 28, 30, 32 };
        List<ShopCategory> cats = new ArrayList<>(categories.values());
        for (int i = 0; i < cats.size() && i < catSlots.length - 1; i++) {
            ShopCategory c = cats.get(i);
            int cmd = CATEGORY_BUTTON_CMD.getOrDefault(c.getId().toLowerCase(), CMD_BTN_UTILITY);
            inv.setItem(catSlots[i], customStack(Material.PAPER, cmd,
                    c.getDisplay(),
                    List.of("§7" + c.getItems().size() + " items disponibles",
                            "§eClique pour ouvrir.")));
        }
        // Bouton items spéciaux toujours en dernier slot
        inv.setItem(catSlots[catSlots.length - 1],
                customStack(Material.PAPER, CMD_BTN_SPECIAL,
                        "§d§l✦ Items Spéciaux",
                        List.of("§7Outils premium :",
                                "§7super foreuse, activateur, grappin...",
                                "§eClique pour ouvrir.")));

        // Solde joueur en bas
        inv.setItem(49, balanceDisplay(p));
        // Bouton close
        inv.setItem(45, customStack(Material.BARRIER, CMD_BTN_CLOSE,
                "§c§lFermer", List.of("§7Quitter le shop.")));

        openCategory.remove(p.getUniqueId());
        categoryItemIndex.remove(p.getUniqueId());
        specialIndex.remove(p.getUniqueId());
        openShopInventories.add(p.getUniqueId());
        p.openInventory(inv);
    }

    // ---------- Sous-menu catégorie ----------

    public void openCategory(Player p, ShopCategory cat) {
        Inventory inv = Bukkit.createInventory(null, CAT_INV_SIZE,
                "§c§lShop §8» §6" + stripColors(cat.getDisplay()));
        decorateBorder(inv, CAT_INV_SIZE);

        List<Integer> itemSlots = innerSlots(CAT_INV_SIZE);
        List<ShopItem> items = cat.getItems();
        for (int i = 0; i < items.size() && i < itemSlots.size(); i++) {
            ShopItem it = items.get(i);
            inv.setItem(itemSlots.get(i), itemDisplay(p, it));
        }
        categoryItemIndex.put(p.getUniqueId(), items);
        specialIndex.remove(p.getUniqueId());

        // Boutons bas
        inv.setItem(45, customStack(Material.ARROW, CMD_BTN_BACK,
                "§a§l← Retour", List.of("§7Retour au menu principal.")));
        inv.setItem(49, balanceDisplay(p));
        inv.setItem(53, customStack(Material.BARRIER, CMD_BTN_CLOSE,
                "§c§lFermer", List.of("§7Quitter le shop.")));

        openCategory.put(p.getUniqueId(), cat.getId().toLowerCase(Locale.ROOT));
        openShopInventories.add(p.getUniqueId());
        p.openInventory(inv);
    }

    public void openSpecialItems(Player p) {
        Inventory inv = Bukkit.createInventory(null, CAT_INV_SIZE,
                "§c§lShop §8» §dItems Spéciaux");
        decorateBorder(inv, CAT_INV_SIZE);

        List<Integer> itemSlots = innerSlots(CAT_INV_SIZE);
        List<fr.trapparty.items.SpecialItem> list = new ArrayList<>();
        for (var item : plugin.specialItems().all()) {
            if (item.cost() < 0) continue;
            list.add(item);
        }
        for (int i = 0; i < list.size() && i < itemSlots.size(); i++) {
            inv.setItem(itemSlots.get(i), specialDisplay(p, list.get(i)));
        }
        specialIndex.put(p.getUniqueId(), list);
        categoryItemIndex.remove(p.getUniqueId());

        inv.setItem(45, customStack(Material.ARROW, CMD_BTN_BACK,
                "§a§l← Retour", List.of("§7Retour au menu principal.")));
        inv.setItem(49, balanceDisplay(p));
        inv.setItem(53, customStack(Material.BARRIER, CMD_BTN_CLOSE,
                "§c§lFermer", List.of("§7Quitter le shop.")));

        openCategory.put(p.getUniqueId(), "__special__");
        openShopInventories.add(p.getUniqueId());
        p.openInventory(inv);
    }

    // ---------- Click handling ----------

    public void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!openShopInventories.contains(p.getUniqueId())) return;
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (!checkRateLimit(p)) return;

        // Détection bouton par custom_model_data
        Integer cmd = customModelDataOf(clicked);
        if (cmd != null) {
            switch (cmd) {
                case CMD_BTN_BACK -> { openMain(p); return; }
                case CMD_BTN_CLOSE -> { p.closeInventory(); return; }
                case CMD_BTN_SPECIAL -> { openSpecialItems(p); return; }
                case CMD_BORDER, CMD_LOGO, CMD_CURRENCY -> { return; }
                default -> {
                    // Bouton catégorie ?
                    for (var entry : CATEGORY_BUTTON_CMD.entrySet()) {
                        if (entry.getValue().equals(cmd)) {
                            ShopCategory cat = category(entry.getKey());
                            if (cat != null) { openCategory(p, cat); return; }
                        }
                    }
                }
            }
        }

        String cat = openCategory.get(p.getUniqueId());
        if (cat == null) return;

        if (cat.equals("__special__")) {
            List<fr.trapparty.items.SpecialItem> list = specialIndex.get(p.getUniqueId());
            if (list == null) return;
            int idx = innerSlotIndex(CAT_INV_SIZE, e.getRawSlot());
            if (idx < 0 || idx >= list.size()) return;
            buySpecial(p, list.get(idx));
            return;
        }

        List<ShopItem> items = categoryItemIndex.get(p.getUniqueId());
        if (items == null) return;
        int idx = innerSlotIndex(CAT_INV_SIZE, e.getRawSlot());
        if (idx < 0 || idx >= items.size()) return;
        buy(p, items.get(idx));
    }

    public void handleClose(UUID uuid) {
        openShopInventories.remove(uuid);
        openCategory.remove(uuid);
        categoryItemIndex.remove(uuid);
        specialIndex.remove(uuid);
    }

    // ---------- Achat ----------

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
        refreshBalance(p);
    }

    private void buySpecial(Player p, fr.trapparty.items.SpecialItem def) {
        Game g = plugin.games().forPlayer(p);
        GamePlayer gp = g != null ? g.getPlayer(p.getUniqueId()) : null;

        if (!plugin.economy().withdraw(p, gp, def.cost())) {
            plugin.messages().send(p, "shop.not-enough",
                    Map.of("amount", plugin.economy().format(def.cost())));
            plugin.version().sound(p, "ENTITY_VILLAGER_NO", 1f, 1.2f);
            return;
        }
        p.getInventory().addItem(plugin.specialItems().build(def));
        plugin.messages().send(p, "shop.bought", Map.of(
                "item", def.id(),
                "amount", plugin.economy().format(def.cost())));
        plugin.version().sound(p, "BLOCK_ENCHANTMENT_TABLE_USE", 1f, 1.5f);
        refreshBalance(p);
    }

    // ---------- Helpers UI ----------

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

    private ItemStack specialDisplay(Player p, fr.trapparty.items.SpecialItem def) {
        Game g = plugin.games().forPlayer(p);
        GamePlayer gp = g != null ? g.getPlayer(p.getUniqueId()) : null;
        double balance = plugin.economy().balance(p, gp);
        boolean affordable = balance >= def.cost();

        ItemStack stack = plugin.specialItems().build(def);
        List<String> lore = new ArrayList<>();
        if (stack.getItemMeta() != null && stack.getItemMeta().getLore() != null) {
            lore.addAll(stack.getItemMeta().getLore());
        }
        lore.add(" ");
        lore.add("§6Prix : §e" + plugin.economy().format(def.cost()));
        lore.add(affordable ? "§a✓ Clique pour acheter" : "§c✗ Fonds insuffisants");
        return new ItemBuilder(stack).lore(lore).build();
    }

    private ItemStack balanceDisplay(Player p) {
        Game g = plugin.games().forPlayer(p);
        GamePlayer gp = g != null ? g.getPlayer(p.getUniqueId()) : null;
        double balance = plugin.economy().balance(p, gp);
        return customStack(Material.GOLD_NUGGET, CMD_CURRENCY,
                "§6§l💰 Solde",
                List.of("§7Économie : §e" + economyLabel(),
                        "§7Solde : §e" + plugin.economy().format(balance)));
    }

    private void refreshBalance(Player p) {
        // remplace l'item slot 49 dans la vue ouverte
        var view = p.getOpenInventory();
        if (view == null) return;
        Inventory top = view.getTopInventory();
        if (top != null && top.getSize() >= 50) {
            top.setItem(49, balanceDisplay(p));
        }
    }

    private void decorateBorder(Inventory inv, int size) {
        Material pane = ItemBuilder.resolveMaterial(
                "WHITE_STAINED_GLASS_PANE|STAINED_GLASS_PANE|GLASS_PANE");
        if (pane == null) pane = Material.GLASS_PANE;
        ItemStack border = customStack(pane, CMD_BORDER, " ", List.of());
        // top et bottom row
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, border);
            if (inv.getItem(size - 9 + i) == null) inv.setItem(size - 9 + i, border);
        }
        // colonnes gauche/droite
        for (int row = 1; row < size / 9 - 1; row++) {
            if (inv.getItem(row * 9) == null) inv.setItem(row * 9, border);
            if (inv.getItem(row * 9 + 8) == null) inv.setItem(row * 9 + 8, border);
        }
    }

    private List<Integer> innerSlots(int size) {
        List<Integer> slots = new ArrayList<>();
        int rows = size / 9;
        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c <= 7; c++) slots.add(r * 9 + c);
        }
        return slots;
    }

    private int innerSlotIndex(int size, int rawSlot) {
        List<Integer> slots = innerSlots(size);
        return slots.indexOf(rawSlot);
    }

    private ItemStack customStack(Material mat, int cmd, String name, List<String> lore) {
        return new ItemBuilder(mat)
                .name(name)
                .lore(lore)
                .meta(m -> { try { m.setCustomModelData(cmd); } catch (Throwable ignored) {} })
                .hideAll()
                .build();
    }

    private Integer customModelDataOf(ItemStack stack) {
        if (stack == null || stack.getItemMeta() == null) return null;
        try { return stack.getItemMeta().hasCustomModelData() ? stack.getItemMeta().getCustomModelData() : null; }
        catch (Throwable t) { return null; }
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

    private String economyLabel() {
        return plugin.economy().effectiveMode() == fr.trapparty.economy.EconomyService.Mode.VAULT
                ? "Vault (" + plugin.economy().currencySymbol() + ")"
                : "Pièces de partie";
    }

    private static String stripColors(String s) {
        if (s == null) return "";
        return s.replaceAll("&[0-9a-fk-or]", "").replaceAll("§[0-9a-fk-or]", "");
    }
}
