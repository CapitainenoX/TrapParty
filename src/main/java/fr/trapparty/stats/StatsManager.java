package fr.trapparty.stats;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stockage YAML simple des statistiques joueur. Persistant sur disque ;
 * sauvegarde différée pour éviter d'écrire à chaque kill.
 */
public class StatsManager {

    private final TrapPartyPlugin plugin;
    private final Map<UUID, PlayerStats> cache = new ConcurrentHashMap<>();
    private final File file;
    private FileConfiguration cfg;
    private long lastFlush = 0L;
    private static final long FLUSH_DEBOUNCE_MS = 5_000;
    private final java.util.concurrent.atomic.AtomicBoolean dirty =
            new java.util.concurrent.atomic.AtomicBoolean(false);
    private final Object flushLock = new Object();

    public StatsManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try { file.createNewFile(); } catch (Exception ignored) {}
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
        loadAll();
        // tâche périodique de flush
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::flushIfDue, 200L, 200L);
    }

    private void loadAll() {
        ConfigurationSection root = cfg.getConfigurationSection("players");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                ConfigurationSection s = root.getConfigurationSection(key);
                if (s == null) continue;
                PlayerStats st = new PlayerStats(uuid, s.getString("name", "?"));
                st.addKills(s.getInt("kills"));
                st.addTrapKills(s.getInt("trap_kills"));
                for (int i = 0; i < s.getInt("wins"); i++) st.addWin();
                for (int i = 0; i < s.getInt("games"); i++) st.addGame();
                for (int i = 0; i < s.getInt("deaths"); i++) st.addDeath();
                st.setMmr(s.getInt("mmr", 1000));
                st.addCoins(s.getInt("coins_total"));
                cache.put(uuid, st);
            } catch (Exception ignored) {}
        }
        plugin.getLogger().info("Loaded " + cache.size() + " player stat entries.");
    }

    public PlayerStats get(UUID uuid) {
        return cache.computeIfAbsent(uuid, u -> {
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(u).getName()).orElse("?");
            return new PlayerStats(u, name);
        });
    }

    /** Snapshot trié pour leaderboards. */
    public List<PlayerStats> topBy(String stat, int limit) {
        java.util.Comparator<PlayerStats> cmp = switch (stat.toLowerCase()) {
            case "kills" -> java.util.Comparator.comparingInt(PlayerStats::getKills).reversed();
            case "trap_kills", "trapkills" -> java.util.Comparator.comparingInt(PlayerStats::getTrapKills).reversed();
            case "mmr" -> java.util.Comparator.comparingInt(PlayerStats::getMmr).reversed();
            case "kd" -> java.util.Comparator.comparingDouble(PlayerStats::getKd).reversed();
            case "games" -> java.util.Comparator.comparingInt(PlayerStats::getGamesPlayed).reversed();
            default -> java.util.Comparator.comparingInt(PlayerStats::getWins).reversed();
        };
        return cache.values().stream()
                .sorted(cmp)
                .limit(Math.max(1, limit))
                .toList();
    }

    public void recordWin(UUID uuid, GamePlayer gp) {
        PlayerStats st = get(uuid);
        st.addWin();
        st.addGame();
        st.addKills(gp.getKills());
        st.addTrapKills(gp.getTrapKills());
        st.addCoins(gp.getCoins() + plugin.configs().winReward());
        // MMR Elo simplifié : +25 sur win
        st.setMmr(st.getMmr() + 25);
        // Crédit Vault si dispo
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) plugin.economy().deposit(p, gp, plugin.configs().winReward());
        markDirty();
    }

    public void recordParticipation(UUID uuid, GamePlayer gp) {
        PlayerStats st = get(uuid);
        st.addGame();
        st.addKills(gp.getKills());
        st.addTrapKills(gp.getTrapKills());
        if (!gp.isAlive()) st.addDeath();
        st.addCoins(gp.getCoins());
        // léger -5 mmr si non-vainqueur
        st.setMmr(Math.max(0, st.getMmr() - 5));
        // Crédit Vault de la prime de participation
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) plugin.economy().deposit(p, gp, plugin.configs().participation());
        markDirty();
    }

    public void markDirty() {
        dirty.set(true);
    }

    /**
     * Sérialise puis écrit le YAML de manière atomique (tmp + rename).
     * Synchronisé pour ne jamais croiser deux flush concurrents.
     */
    public void flush() {
        synchronized (flushLock) {
            try {
                cfg.set("players", null);
                for (PlayerStats st : cache.values()) {
                    String k = st.getUuid().toString();
                    cfg.set("players." + k + ".name", st.getName());
                    cfg.set("players." + k + ".kills", st.getKills());
                    cfg.set("players." + k + ".trap_kills", st.getTrapKills());
                    cfg.set("players." + k + ".wins", st.getWins());
                    cfg.set("players." + k + ".games", st.getGamesPlayed());
                    cfg.set("players." + k + ".deaths", st.getDeaths());
                    cfg.set("players." + k + ".mmr", st.getMmr());
                    cfg.set("players." + k + ".coins_total", st.getCoinsTotal());
                }
                File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
                cfg.save(tmp);
                try {
                    java.nio.file.Files.move(tmp.toPath(), file.toPath(),
                            java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException amns) {
                    java.nio.file.Files.move(tmp.toPath(), file.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                lastFlush = System.currentTimeMillis();
                dirty.set(false);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to save stats: " + ex.getMessage());
            }
        }
    }

    private void flushIfDue() {
        if (!dirty.get()) return;
        if (System.currentTimeMillis() - lastFlush < FLUSH_DEBOUNCE_MS) return;
        flush();
    }
}
