package fr.trapparty.ui;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.config.MessagesManager;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implémentation native d'hologrammes via ArmorStands invisibles.
 * Évite d'avoir une dépendance lourde sur HolographicDisplays / DecentHolograms.
 */
public class HologramManager {

    private final TrapPartyPlugin plugin;
    private final Map<String, List<UUID>> byGame = new ConcurrentHashMap<>();

    public HologramManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawn(String gameId, Location loc, List<String> lines) {
        if (loc == null || loc.getWorld() == null) return;
        List<UUID> ids = byGame.computeIfAbsent(gameId, g -> new ArrayList<>());
        double y = loc.getY() + (lines.size() * 0.27);
        for (String line : lines) {
            Location l = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
            ArmorStand stand = loc.getWorld().spawn(l, ArmorStand.class, s -> {
                s.setVisible(false);
                s.setGravity(false);
                s.setMarker(true);
                s.setSmall(true);
                s.setCustomNameVisible(true);
                s.setCustomName(MessagesManager.color(line));
            });
            ids.add(stand.getUniqueId());
            y -= 0.27;
        }
    }

    public void removeForGame(String gameId) {
        List<UUID> ids = byGame.remove(gameId);
        if (ids == null) return;
        for (UUID u : ids) {
            try {
                org.bukkit.entity.Entity e = plugin.getServer().getEntity(u);
                if (e != null) e.remove();
            } catch (Throwable ignored) {}
        }
    }

    public void removeAll() {
        for (String id : new ArrayList<>(byGame.keySet())) removeForGame(id);
    }
}
