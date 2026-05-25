package fr.trapparty.listeners;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class GameListener implements Listener {

    private final TrapPartyPlugin plugin;

    public GameListener(TrapPartyPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        Game g = plugin.games().forPlayer(victim);
        if (g == null) return;
        if (g.getState() == GameState.PREPARATION && !g.getArena().isPvpInPrep()) {
            e.setCancelled(true);
            return;
        }
        if (g.getState() != GameState.COMBAT && g.getState() != GameState.SUDDEN_DEATH) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageGeneric(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        Game g = plugin.games().forPlayer(victim);
        if (g == null) return;
        if (g.getState() == GameState.WAITING || g.getState() == GameState.STARTING
                || g.getState() == GameState.ENDING || g.getState() == GameState.RESETTING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Game g = plugin.games().forPlayer(victim);
        if (g == null) return;
        Player killer = victim.getKiller();
        boolean trapKill = plugin.traps().recentTriggerFor(victim.getUniqueId()) != null;
        if (trapKill && killer == null) {
            var t = plugin.traps().recentTriggerFor(victim.getUniqueId());
            if (t != null) {
                killer = plugin.getServer().getPlayer(t.getOwner());
            }
        }
        e.setKeepInventory(true);
        e.setKeepLevel(true);
        e.getDrops().clear();
        e.setDeathMessage(null);
        g.onPlayerDeath(victim, killer, trapKill);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Game g = plugin.games().forPlayer(e.getPlayer());
        if (g == null) return;
        if (g.world() != null) {
            e.setRespawnLocation(g.world().getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Game g = plugin.games().forPlayer(e.getPlayer());
        if (g == null) return;
        if (!g.getArena().isBlockPlaceAllowedInPrep() && g.getState() == GameState.PREPARATION) {
            e.setCancelled(true);
            return;
        }
        // register trap si applicable
        var type = fr.trapparty.trap.TrapManager.detectType(e.getBlock().getType());
        if (type != null) {
            plugin.traps().register(g.getId(),
                    new fr.trapparty.trap.Trap(e.getPlayer().getUniqueId(), type, e.getBlock().getLocation()));
            plugin.messages().send(e.getPlayer(), "trap.placed",
                    java.util.Map.of("trap", type.name().toLowerCase()));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Game g = plugin.games().forPlayer(e.getPlayer());
        if (g == null) return;
        if (!g.getArena().isBlockBreakAllowedInPrep() && g.getState() == GameState.PREPARATION) {
            e.setCancelled(true);
            return;
        }
        plugin.traps().unregister(e.getBlock().getLocation());
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        Game g = plugin.games().forPlayer(p);
        if (g == null) return;
        if (!g.getArena().isHunger()) e.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        plugin.shop().handleClick(e);
        if (plugin.kits().gui() != null) plugin.kits().gui().handleClick(e);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        plugin.shop().handleClose(e.getPlayer().getUniqueId());
        if (plugin.kits().gui() != null) plugin.kits().gui().handleClose(e.getPlayer().getUniqueId());
    }
}
