package fr.trapparty.shop;

import org.bukkit.Material;

import java.util.List;

public class ShopCategory {
    private final String id;
    private final String display;
    private final Material icon;
    private final int slot;
    private final List<ShopItem> items;

    public ShopCategory(String id, String display, Material icon, int slot, List<ShopItem> items) {
        this.id = id;
        this.display = display;
        this.icon = icon;
        this.slot = slot;
        this.items = items;
    }

    public String getId() { return id; }
    public String getDisplay() { return display; }
    public Material getIcon() { return icon; }
    public int getSlot() { return slot; }
    public List<ShopItem> getItems() { return items; }
}
