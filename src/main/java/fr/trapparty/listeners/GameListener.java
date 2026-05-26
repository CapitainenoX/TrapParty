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
        switch (g.getState()) {
            case PREPARATION -> { if (!g.getArena().isPvpInPrep()) e.setCancelled(true); }
            case COMBAT, SUDDEN_DEATH -> { /* allowed */ }
            default -> e.setCancelled(true);
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
        // Consomme le trigger pour éviter une 2e attribution sur un autre death
        var trapTrigger = plugin.traps().consumeTrigger(victim.getUniqueId());
        boolean trapKill = trapTrigger != null;
        if (trapKill && killer == null) {
            killer = plugin.getServer().getPlayer(trapTrigger.getOwner());
        }
        // Vérifie que le killer est bien dans la même partie (anti-attribution croisée)
        if (killer != null) {
            Game killerGame = plugin.games().forPlayer(killer);
            if (killerGame != g) killer = null;
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
        if (plugin.crafts() != null) plugin.crafts().handleClick(e);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        plugin.shop().handleClose(e.getPlayer().getUniqueId());
        if (plugin.kits().gui() != null) plugin.kits().gui().handleClose(e.getPlayer().getUniqueId());
        if (plugin.crafts() != null) plugin.crafts().handleClose(e.getPlayer().getUniqueId());
    }
}
