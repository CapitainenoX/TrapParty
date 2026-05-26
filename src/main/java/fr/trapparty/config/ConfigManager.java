package fr.trapparty.config;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final TrapPartyPlugin plugin;

    private FileConfiguration config;
    private FileConfiguration kits;
    private FileConfiguration shop;
    private FileConfiguration arenas;

    public ConfigManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        kits = load("kits.yml");
        shop = load("shop.yml");
        arenas = load("arenas.yml");
    }

    private FileConfiguration load(String name) {
        File f = new File(plugin.getDataFolder(), name);
        if (!f.exists()) plugin.saveResource(name, false);
        return YamlConfiguration.loadConfiguration(f);
    }

    public void save(String name, FileConfiguration cfg) {
        try {
            cfg.save(new File(plugin.getDataFolder(), name));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + name + ": " + e.getMessage());
        }
    }

    public FileConfiguration root() { return config; }
    public FileConfiguration kits() { return kits; }
    public FileConfiguration shop() { return shop; }
    public FileConfiguration arenas() { return arenas; }

    // Helpers raccourcis
    public int gameMinPlayers() { return config.getInt("game.min-players", 2); }
    public int gameMaxPlayers() { return config.getInt("game.max-players", 12); }
    public int countdown() { return config.getInt("game.countdown", 20); }
    public int preparationTime() { return config.getInt("game.preparation-time", 90); }
    public int combatTime() { return config.getInt("game.combat-time", 240); }
    public int suddenDeathTime() { return config.getInt("game.sudden-death-time", 60); }
    public int endTime() { return config.getInt("game.end-time", 8); }
    public boolean autoRestart() { return config.getBoolean("settings.auto-restart", true); }
    public boolean debug() { return config.getBoolean("settings.debug", false); }
    public String prefix() { return config.getString("settings.prefix", "[TrapParty]"); }
    public boolean useMultiverse() { return config.getBoolean("settings.use-multiverse", true); }

    public int startingCoins() { return config.getInt("economy.starting-coins", 50); }
    public int killReward() { return config.getInt("economy.kill-reward", 25); }
    public int trapKillBonus() { return config.getInt("economy.trap-kill-bonus", 15); }
    public int winReward() { return config.getInt("economy.win-reward", 150); }
    public int participation() { return config.getInt("economy.participation", 25); }

    public int initialBorder() { return config.getInt("arena.border.initial-size", 150); }
    public int combatBorder() { return config.getInt("arena.border.combat-size", 80); }
    public int suddenBorder() { return config.getInt("arena.border.sudden-death-size", 25); }

    public int maxArenas() { return config.getInt("performance.max-arenas", 16); }

    public java.util.Set<String> allowedCommandsInGame() {
        java.util.List<String> raw = config.getStringList("game.allowed-commands");
        if (raw.isEmpty()) {
            return new java.util.HashSet<>(java.util.List.of(
                    "tparty", "trapparty", "kit", "shop", "crafts", "special",
                    "items", "spectate", "spec", "msg", "r", "tell", "w"));
        }
        java.util.Set<String> out = new java.util.HashSet<>();
        for (String s : raw) out.add(s.toLowerCase(java.util.Locale.ROOT));
        return out;
    }

    public boolean scopedChat() { return config.getBoolean("game.scoped-chat", true); }

    // ---- internals tunables ----
    public long joinRateLimitMs()  { return config.getLong("internals.join-rate-limit-ms", 3000); }
    public long createRateLimitMs(){ return config.getLong("internals.create-rate-limit-ms", 10000); }
    public int  joinRetryMax()     { return config.getInt("internals.join-retry-max", 100); }
    public long trapTriggerTtlMs() { return config.getLong("internals.trap-trigger-ttl-ms", 5000); }
    public long trapTriggerPurgeMs(){return config.getLong("internals.trap-trigger-purge-age-ms", 60000); }
    public long statsFlushDebounceMs() { return config.getLong("internals.stats-flush-debounce-ms", 5000); }
    public long drillCooldownMs()  { return config.getLong("internals.drill-cooldown-ms", 1500); }
}
