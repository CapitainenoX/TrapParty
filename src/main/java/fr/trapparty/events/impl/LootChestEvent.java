package fr.trapparty.events.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.events.GameEvent;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import fr.trapparty.util.ItemBuilder;
import fr.trapparty.util.LocationUtil;
import fr.trapparty.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootChestEvent implements GameEvent {
    private final TrapPartyPlugin plugin;
    public LootChestEvent(TrapPartyPlugin plugin) { this.plugin = plugin; }

    @Override public String id() { return "loot_chest"; }
    @Override public String displayName() { return "Coffre mystère"; }

    @Override public void apply(Game game) {
        if (game.world() == null) return;
        List<String> locs = game.getArena().getChestLocations();
        Location target;
        if (!locs.isEmpty()) {
            target = LocationUtil.parse(game.world(), RandomUtil.pick(locs));
        } else {
            Location c = LocationUtil.parse(game.world(), game.getArena().getCenterDef());
            if (c == null) return;
            target = c.clone().add((Math.random() - 0.5) * 30, 0, (Math.random() - 0.5) * 30);
            target.setY(game.world().getHighestBlockYAt(target) + 1);
        }
        Block b = target.getBlock();
        b.setType(Material.CHEST);
        if (b.getState() instanceof Chest chest) {
            Inventory inv = chest.getInventory();
            inv.addItem(
                    new ItemStack(Material.GOLDEN_APPLE, 2),
                    new ItemStack(Material.TNT, 4),
                    new ItemStack(Material.COBWEB, 6),
                    new ItemStack(Material.ENDER_PEARL, 2),
                    new ItemStack(Material.ARROW, 16),
                    new ItemStack(Material.OBSIDIAN, 4),
                    new ItemBuilder(Material.IRON_SWORD)
                            .enchant(ItemBuilder.lookupEnchant("SHARPNESS", "DAMAGE_ALL"), 1).build()
            );
        }
        plugin.messages().broadcastPlayers(onlinePlayersIn(game), "events.loot-chest", null);
        // particules d'indication
        game.world().strikeLightningEffect(target.clone().add(0, 2, 0));
    }

    private static java.util.List<Player> onlinePlayersIn(Game g) {
        java.util.List<Player> list = new java.util.ArrayList<>();
        for (GamePlayer gp : g.getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null) list.add(p);
        }
        return list;
    }
}
