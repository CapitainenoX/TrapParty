package fr.trapparty.game;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère plusieurs parties simultanées indexées par id, par arène et par joueur.
 *
 * Hardening :
 *  - id de game = arena + UUID complet → pas de collision
 *  - rate limit join : 3s entre tentatives par joueur
 *  - rate limit création : 10s entre créations d'arène
 *  - retry cap sur join (évite la boucle infinie si le monde ne charge pas)
 */
public class GameManager {

    private static final long JOIN_RATE_LIMIT_MS = 3_000;
    private static final long CREATE_RATE_LIMIT_MS = 10_000;
    private static final int JOIN_RETRY_MAX = 100; // ~5s max d'attente du monde

    private final TrapPartyPlugin plugin;
    private final Map<String, Game> gamesById = new ConcurrentHashMap<>();
    private final Map<UUID, Game> playerToGame = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastJoinAttempt = new ConcurrentHashMap<>();
    private final Map<String, Long> lastArenaCreate = new ConcurrentHashMap<>();

    public GameManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public Collection<Game> all() { return gamesById.values(); }

    public Game forPlayer(UUID uuid) { return playerToGame.get(uuid); }

    public Game forPlayer(Player p) { return playerToGame.get(p.getUniqueId()); }

    public Game get(String id) { return gamesById.get(id); }

    public Game findOrCreate(Arena arena) {
        for (Game g : gamesById.values()) {
            if (g.getArena().getId().equalsIgnoreCase(arena.getId()) && g.canJoin()) return g;
        }
        if (gamesById.size() >= plugin.configs().maxArenas()) return null;
        long now = System.currentTimeMillis();
        Long last = lastArenaCreate.get(arena.getId());
        if (last != null && now - last < CREATE_RATE_LIMIT_MS) return null;
        lastArenaCreate.put(arena.getId(), now);
        return create(arena);
    }

    private Game create(Arena arena) {
        // Id unique : arena + UUID complet → 128 bits d'entropie, pas de collision
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String id = arena.getId() + "_" + suffix.substring(0, 12);
        String worldName = "tp_" + arena.getId() + "_" + suffix.substring(0, 16);
        Game game = new Game(plugin, id, arena, worldName);
        gamesById.put(id, game);
        plugin.worlds().cloneAndLoad(arena.getTemplateWorld(), worldName).whenComplete((world, err) -> {
            if (err != null) {
                plugin.getLogger().severe("World clone failed for " + id + ": " + err.getMessage());
                gamesById.remove(id);
                // Purge tous les joueurs mappés à cette game pour ne pas boucler
                for (Map.Entry<UUID, Game> e : new HashMap<>(playerToGame).entrySet()) {
                    if (e.getValue() == game) {
                        playerToGame.remove(e.getKey());
                        Player p = Bukkit.getPlayer(e.getKey());
                        if (p != null) p.sendMessage("§cCréation de partie échouée. Réessaie dans un instant.");
                    }
                }
                return;
            }
            world.setSpawnFlags(false, false);
            applyRule(world, "doDaylightCycle", false);
            applyRule(world, "doMobSpawning", false);
            applyRule(world, "doWeatherCycle", false);
            applyRule(world, "announceAdvancements", false);
            world.setTime(6000);
            game.setWorld(world);
        });
        return game;
    }

    public void joinAuto(Player p) {
        if (forPlayer(p) != null) {
            plugin.messages().send(p, "generic.in-game");
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastJoinAttempt.put(p.getUniqueId(), now);
        if (last != null && now - last < JOIN_RATE_LIMIT_MS) {
            p.sendMessage("§7Patiente avant de retenter.");
            return;
        }
        Arena best;
        Game match = null;
        for (Game g : gamesById.values()) {
            if (g.canJoin()) { match = g; break; }
        }
        if (match == null) {
            Collection<Arena> arenas = plugin.arenas().all();
            if (arenas.isEmpty()) {
                p.sendMessage(plugin.messages().prefix() + "§cAucune arène configurée.");
                return;
            }
            best = arenas.iterator().next();
            match = findOrCreate(best);
            if (match == null) {
                p.sendMessage(plugin.messages().prefix() + "§cToutes les arènes sont occupées.");
                return;
            }
        }
        join(p, match);
    }

    public void join(Player p, Game game) {
        join(p, game, 0);
    }

    private void join(Player p, Game game, int attempt) {
        if (!p.isOnline()) return;
        if (!gamesById.containsValue(game)) {
            p.sendMessage(plugin.messages().prefix() + "§cCette partie n'existe plus.");
            return;
        }
        if (attempt > JOIN_RETRY_MAX) {
            p.sendMessage(plugin.messages().prefix() + "§cLa partie n'a pas pu se charger à temps.");
            return;
        }
        if (game.world() == null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> join(p, game, attempt + 1), 5L);
            return;
        }
        if (!game.canJoin()) {
            plugin.messages().send(p, "join.already-started");
            return;
        }
        playerToGame.put(p.getUniqueId(), game);
        game.join(p);
    }

    public void leave(Player p) {
        Game g = playerToGame.remove(p.getUniqueId());
        if (g != null) g.leave(p);
    }

    public void destroy(Game game) {
        gamesById.remove(game.getId());
        for (Map.Entry<UUID, Game> e : new HashMap<>(playerToGame).entrySet()) {
            if (e.getValue() == game) playerToGame.remove(e.getKey());
        }
    }

    public void shutdownAll() {
        for (Game g : new ArrayList<>(gamesById.values())) {
            g.forceEnd();
        }
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private static void applyRule(org.bukkit.World world, String name, boolean value) {
        try {
            org.bukkit.GameRule<Boolean> rule = (org.bukkit.GameRule<Boolean>) org.bukkit.GameRule.getByName(name);
            if (rule != null) { world.setGameRule(rule, value); return; }
        } catch (Throwable ignored) {}
        try { world.setGameRuleValue(name, String.valueOf(value)); } catch (Throwable ignored) {}
    }
}
