package fr.trapparty.game;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère plusieurs parties simultanées indexées par id, par arène et par joueur.
 */
public class GameManager {

    private final TrapPartyPlugin plugin;
    private final Map<String, Game> gamesById = new ConcurrentHashMap<>();
    private final Map<UUID, Game> playerToGame = new ConcurrentHashMap<>();

    public GameManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public Collection<Game> all() { return gamesById.values(); }

    public Game forPlayer(UUID uuid) { return playerToGame.get(uuid); }

    public Game forPlayer(Player p) { return playerToGame.get(p.getUniqueId()); }

    public Game get(String id) { return gamesById.get(id); }

    /** Trouve une partie joinable pour une arène donnée, ou en crée une. */
    public Game findOrCreate(Arena arena) {
        for (Game g : gamesById.values()) {
            if (g.getArena().getId().equalsIgnoreCase(arena.getId()) && g.canJoin()) return g;
        }
        if (gamesById.size() >= plugin.configs().maxArenas()) return null;
        return create(arena);
    }

    private Game create(Arena arena) {
        String id = arena.getId() + "_" + System.currentTimeMillis() % 100000;
        String worldName = "tp_" + arena.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);
        Game game = new Game(plugin, id, arena, worldName);
        gamesById.put(id, game);
        // clone & load le monde
        plugin.worlds().cloneAndLoad(arena.getTemplateWorld(), worldName).whenComplete((world, err) -> {
            if (err != null) {
                plugin.getLogger().severe("World clone failed: " + err.getMessage());
                gamesById.remove(id);
                return;
            }
            // tweaks de monde
            world.setSpawnFlags(false, false);
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doWeatherCycle", "false");
            world.setGameRuleValue("announceAdvancements", "false");
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
        Arena best = null;
        Game match = null;
        for (Game g : gamesById.values()) {
            if (g.canJoin()) { match = g; best = g.getArena(); break; }
        }
        if (match == null) {
            // crée sur l'arène avec le moins de monde / la première dispo
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
        if (game.world() == null) {
            // monde encore en cours de chargement
            Bukkit.getScheduler().runTaskLater(plugin, () -> join(p, game), 20L);
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
        // purge tout joueur encore mappé
        for (Map.Entry<UUID, Game> e : new HashMap<>(playerToGame).entrySet()) {
            if (e.getValue() == game) playerToGame.remove(e.getKey());
        }
    }

    public void shutdownAll() {
        for (Game g : new ArrayList<>(gamesById.values())) {
            g.forceEnd();
        }
    }
}
