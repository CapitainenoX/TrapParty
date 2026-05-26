package fr.trapparty.trap;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Indexe les pièges par localisation et attribue les kills à leur owner.
 */
public class TrapManager {

    // Overridable via config.yml > internals.*
    private long triggerTtlMs() { return plugin.configs().trapTriggerTtlMs(); }
    private long purgeMaxAgeMs() { return plugin.configs().trapTriggerPurgeMs(); }

    private final TrapPartyPlugin plugin;
    private final Map<String, Trap> trapsByBlock = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> trapsByGame = new ConcurrentHashMap<>();
    private final Map<UUID, TrapTrigger> lastTrigger = new ConcurrentHashMap<>();

    public TrapManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        // Purge périodique des triggers expirés pour éviter le leak mémoire.
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::purgeStaleTriggers, 600L, 600L);
    }

    public void register(String gameId, Trap trap) {
        String key = blockKey(trap.getLocation());
        trapsByBlock.put(key, trap);
        trapsByGame.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(key);
    }

    public Trap atBlock(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        return trapsByBlock.get(blockKey(loc));
    }

    public void unregister(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        String key = blockKey(loc);
        trapsByBlock.remove(key);
        for (Set<String> keys : trapsByGame.values()) keys.remove(key);
    }

    public void clearGame(String gameId) {
        Set<String> keys = trapsByGame.remove(gameId);
        if (keys == null) return;
        for (String k : keys) trapsByBlock.remove(k);
    }

    public void noteTrigger(UUID victim, Trap trap) {
        lastTrigger.put(victim, new TrapTrigger(trap, System.currentTimeMillis()));
    }

    public Trap recentTriggerFor(UUID victim) {
        TrapTrigger t = lastTrigger.get(victim);
        if (t == null) return null;
        if (System.currentTimeMillis() - t.when > triggerTtlMs()) {
            lastTrigger.remove(victim);
            return null;
        }
        return t.trap;
    }

    /** Consomme et retourne le trigger (utilisé lors d'une mort attribuée). */
    public Trap consumeTrigger(UUID victim) {
        TrapTrigger t = lastTrigger.remove(victim);
        if (t == null) return null;
        if (System.currentTimeMillis() - t.when > triggerTtlMs()) return null;
        return t.trap;
    }

    /** Appelé lors d'un quit pour nettoyer les caches. */
    public void forgetTrigger(UUID victim) {
        lastTrigger.remove(victim);
    }

    private void purgeStaleTriggers() {
        long cutoff = System.currentTimeMillis() - purgeMaxAgeMs();
        lastTrigger.entrySet().removeIf(e -> e.getValue().when < cutoff);
    }

    public static Trap.Type detectType(Material material) {
        if (material == null) return null;
        return switch (material.name()) {
            case "TNT" -> Trap.Type.TNT;
            case "COBWEB", "WEB" -> Trap.Type.COBWEB;
            case "TRIPWIRE_HOOK", "STRING" -> Trap.Type.TRIPWIRE;
            case "LAVA_BUCKET" -> Trap.Type.LAVA;
            case "MAGMA_BLOCK" -> Trap.Type.MAGMA;
            case "CACTUS" -> Trap.Type.CACTUS;
            default -> null;
        };
    }

    private static String blockKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private record TrapTrigger(Trap trap, long when) {}
}
