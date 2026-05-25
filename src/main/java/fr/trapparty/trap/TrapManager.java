package fr.trapparty.trap;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Indexe les pièges par localisation (block xyz worldname). Permet
 * d'attribuer un kill quand une entité meurt à cause d'un piège proche.
 */
public class TrapManager {

    private final TrapPartyPlugin plugin;
    private final Map<String, Trap> trapsByBlock = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> trapsByGame = new ConcurrentHashMap<>();
    // dernière interaction d'un joueur avec un piège (pour death attribution)
    private final Map<UUID, TrapTrigger> lastTrigger = new ConcurrentHashMap<>();

    public TrapManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(String gameId, Trap trap) {
        String key = blockKey(trap.getLocation());
        trapsByBlock.put(key, trap);
        trapsByGame.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(key);
    }

    public Trap atBlock(Location loc) {
        return trapsByBlock.get(blockKey(loc));
    }

    public void unregister(Location loc) {
        Trap t = trapsByBlock.remove(blockKey(loc));
        if (t == null) return;
        for (Set<String> keys : trapsByGame.values()) keys.remove(blockKey(loc));
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
        if (System.currentTimeMillis() - t.when > 5000) return null;
        return t.trap;
    }

    /** Détermine si un Material posé est un piège (et son type). */
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
