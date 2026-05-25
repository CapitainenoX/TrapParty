package fr.trapparty.items;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.items.impl.*;
import fr.trapparty.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Registre des items spéciaux. Construit les ItemStack avec metadata
 * persistent (id + uses restantes) et dispatche les events.
 */
public class SpecialItemManager {

    private final TrapPartyPlugin plugin;
    private final Map<String, SpecialItem> items = new LinkedHashMap<>();

    public final NamespacedKey KEY_ID;
    public final NamespacedKey KEY_USES;

    public SpecialItemManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        this.KEY_ID = new NamespacedKey(plugin, "special_id");
        this.KEY_USES = new NamespacedKey(plugin, "special_uses");
        registerDefaults();
    }

    private void registerDefaults() {
        register(new SuperDrill());
        register(new RemoteActivator());
        register(new TrapCaller());
        register(new GrapplingHook());
    }

    public void register(SpecialItem item) {
        items.put(item.id().toLowerCase(Locale.ROOT), item);
    }

    public Collection<SpecialItem> all() { return Collections.unmodifiableCollection(items.values()); }
    public SpecialItem get(String id) { return id == null ? null : items.get(id.toLowerCase(Locale.ROOT)); }

    /**
     * Construit l'ItemStack final. Pas de texture custom : le rendu visuel
     * vient du base material + glow d'enchantement. Le nom et la lore
     * identifient l'item, et le PDC porte l'id pour le dispatch d'events.
     */
    public ItemStack build(SpecialItem def) {
        Material mat = ItemBuilder.resolveMaterial(def.baseMaterial());
        if (mat == null) mat = Material.STICK;
        List<String> lore = new ArrayList<>(def.lore());
        lore.add(" ");
        if (def.maxUses() > 0) lore.add("§7Utilisations : §a" + def.maxUses());
        lore.add("§eClic droit§7 pour utiliser.");
        return new ItemBuilder(mat)
                .name(def.displayName())
                .lore(lore)
                .meta(m -> {
                    PersistentDataContainer c = m.getPersistentDataContainer();
                    c.set(KEY_ID, PersistentDataType.STRING, def.id());
                    c.set(KEY_USES, PersistentDataType.INTEGER, def.maxUses());
                })
                .glow()
                .build();
    }

    public String idOf(ItemStack stack) {
        if (stack == null) return null;
        ItemMeta m = stack.getItemMeta();
        if (m == null) return null;
        return m.getPersistentDataContainer().get(KEY_ID, PersistentDataType.STRING);
    }

    public int usesOf(ItemStack stack) {
        if (stack == null) return 0;
        ItemMeta m = stack.getItemMeta();
        if (m == null) return 0;
        Integer u = m.getPersistentDataContainer().get(KEY_USES, PersistentDataType.INTEGER);
        return u == null ? -1 : u;
    }

    public void decrementUses(Player p, ItemStack stack) {
        ItemMeta m = stack.getItemMeta();
        if (m == null) return;
        Integer u = m.getPersistentDataContainer().get(KEY_USES, PersistentDataType.INTEGER);
        if (u == null || u < 0) return; // illimité
        int next = u - 1;
        if (next <= 0) {
            stack.setAmount(0);
            plugin.version().sound(p, "ENTITY_ITEM_BREAK", 1f, 1.2f);
            return;
        }
        m.getPersistentDataContainer().set(KEY_USES, PersistentDataType.INTEGER, next);
        // Met à jour la lore
        List<String> lore = m.hasLore() ? new ArrayList<>(m.getLore()) : new ArrayList<>();
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains("Utilisations")) {
                lore.set(i, "§7Utilisations : §a" + next);
                break;
            }
        }
        m.setLore(lore);
        stack.setItemMeta(m);
    }

    public boolean handleInteract(PlayerInteractEvent e) {
        ItemStack in = e.getItem();
        if (in == null) return false;
        String id = idOf(in);
        if (id == null) return false;
        SpecialItem def = get(id);
        if (def == null) return false;
        e.setCancelled(true);
        // PlayerInteractEvent fire 2x (main+off) — on filtre la main droite
        if (e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return true;
        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return false;
        boolean consumed = def.onUse(e.getPlayer(), e);
        if (consumed) decrementUses(e.getPlayer(), in);
        return true;
    }
}
