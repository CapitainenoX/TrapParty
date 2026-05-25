package fr.trapparty.arena;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ArenaManager {

    private final TrapPartyPlugin plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    public ArenaManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        arenas.clear();
        FileConfiguration cfg = plugin.configs().arenas();
        ConfigurationSection root = cfg.getConfigurationSection("arenas");
        if (root == null) {
            plugin.getLogger().info("No arenas defined in arenas.yml");
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            try {
                Arena arena = parse(id, s);
                arenas.put(id.toLowerCase(Locale.ROOT), arena);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load arena " + id + ": " + ex.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + arenas.size() + " arena(s).");
    }

    private Arena parse(String id, ConfigurationSection s) {
        String name = s.getString("display-name", id);
        String template = s.getString("template-world", id);
        int minP = s.getInt("min-players", plugin.configs().gameMinPlayers());
        int maxP = s.getInt("max-players", plugin.configs().gameMaxPlayers());
        String center = s.getString("center", "0,80,0");
        List<String> spawns = s.getStringList("spawns");
        String specSpawn = s.getString("spectator-spawn", center);
        int bInit = s.getInt("border.initial", plugin.configs().initialBorder());
        int bComb = s.getInt("border.combat", plugin.configs().combatBorder());
        int bSudd = s.getInt("border.sudden-death", plugin.configs().suddenBorder());
        List<String> chests = s.getStringList("chest-locations");
        boolean breakOK = s.getBoolean("rules.block-break", true);
        boolean placeOK = s.getBoolean("rules.block-place", true);
        boolean pvpPrep = s.getBoolean("rules.pvp-prep", false);
        boolean hunger = s.getBoolean("rules.hunger", true);
        return new Arena(id, name, template, minP, maxP, center, spawns, specSpawn,
                bInit, bComb, bSudd, chests, breakOK, placeOK, pvpPrep, hunger);
    }

    public Arena get(String id) {
        return arenas.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<Arena> all() { return Collections.unmodifiableCollection(arenas.values()); }

    /** Crée un template vide en config (sans monde — le builder utilise /world du serveur). */
    public boolean createTemplate(String id, String templateWorld) {
        FileConfiguration cfg = plugin.configs().arenas();
        String path = "arenas." + id;
        if (cfg.contains(path)) return false;
        cfg.set(path + ".display-name", id);
        cfg.set(path + ".template-world", templateWorld);
        cfg.set(path + ".min-players", 2);
        cfg.set(path + ".max-players", 8);
        cfg.set(path + ".center", "0,80,0");
        cfg.set(path + ".spawns", new ArrayList<String>());
        cfg.set(path + ".spectator-spawn", "0,120,0");
        cfg.set(path + ".border.initial", plugin.configs().initialBorder());
        cfg.set(path + ".border.combat", plugin.configs().combatBorder());
        cfg.set(path + ".border.sudden-death", plugin.configs().suddenBorder());
        cfg.set(path + ".chest-locations", new ArrayList<String>());
        cfg.set(path + ".rules.block-break", true);
        cfg.set(path + ".rules.block-place", true);
        cfg.set(path + ".rules.pvp-prep", false);
        cfg.set(path + ".rules.hunger", true);
        plugin.configs().save("arenas.yml", cfg);
        reload();
        return true;
    }

    public boolean delete(String id) {
        FileConfiguration cfg = plugin.configs().arenas();
        if (!cfg.contains("arenas." + id)) return false;
        cfg.set("arenas." + id, null);
        plugin.configs().save("arenas.yml", cfg);
        reload();
        return true;
    }
}
