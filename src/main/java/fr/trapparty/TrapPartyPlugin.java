package fr.trapparty;

import fr.trapparty.arena.ArenaManager;
import fr.trapparty.commands.AdminCommand;
import fr.trapparty.commands.KitCommand;
import fr.trapparty.commands.ShopCommand;
import fr.trapparty.commands.SpectateCommand;
import fr.trapparty.commands.TrapPartyCommand;
import fr.trapparty.compat.VersionAdapter;
import fr.trapparty.config.ConfigManager;
import fr.trapparty.config.MessagesManager;
import fr.trapparty.economy.EconomyService;
import fr.trapparty.events.EventManager;
import fr.trapparty.game.GameManager;
import fr.trapparty.items.SpecialItemManager;
import fr.trapparty.kit.KitManager;
import fr.trapparty.listeners.GameListener;
import fr.trapparty.listeners.PlayerListener;
import fr.trapparty.listeners.SpecialItemListener;
import fr.trapparty.listeners.TrapListener;
import fr.trapparty.shop.ShopManager;
import fr.trapparty.stats.StatsManager;
import fr.trapparty.trap.TrapManager;
import fr.trapparty.ui.BossBarManager;
import fr.trapparty.ui.HologramManager;
import fr.trapparty.ui.ScoreboardManager;
import fr.trapparty.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class TrapPartyPlugin extends JavaPlugin {

    private static TrapPartyPlugin instance;

    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private VersionAdapter versionAdapter;
    private WorldManager worldManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private ShopManager shopManager;
    private TrapManager trapManager;
    private EventManager eventManager;
    private SpecialItemManager specialItemManager;
    private EconomyService economyService;
    private StatsManager statsManager;
    private GameManager gameManager;
    private ScoreboardManager scoreboardManager;
    private BossBarManager bossBarManager;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();

        saveDefaultResources();

        this.versionAdapter = VersionAdapter.detect(this);
        this.configManager = new ConfigManager(this);
        this.messagesManager = new MessagesManager(this);

        this.economyService = new EconomyService(this);
        this.statsManager = new StatsManager(this);
        this.kitManager = new KitManager(this);
        this.shopManager = new ShopManager(this);
        this.trapManager = new TrapManager(this);
        this.specialItemManager = new SpecialItemManager(this);
        this.worldManager = new WorldManager(this);
        this.arenaManager = new ArenaManager(this);
        this.eventManager = new EventManager(this);

        this.scoreboardManager = new ScoreboardManager(this);
        this.bossBarManager = new BossBarManager(this);
        this.hologramManager = new HologramManager(this);

        this.gameManager = new GameManager(this);

        registerCommands();
        registerListeners();

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("TrapParty enabled in " + elapsed + "ms — server " + versionAdapter.describe());
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.shutdownAll();
        if (worldManager != null) worldManager.cleanupAll();
        if (hologramManager != null) hologramManager.removeAll();
        if (bossBarManager != null) bossBarManager.removeAll();
        if (statsManager != null) statsManager.flush();
        getLogger().info("TrapParty disabled.");
    }

    private void saveDefaultResources() {
        saveDefaultConfig();
        for (String f : new String[]{"messages.yml", "kits.yml", "shop.yml", "arenas.yml"}) {
            if (!new java.io.File(getDataFolder(), f).exists()) {
                saveResource(f, false);
            }
        }
    }

    private void registerCommands() {
        register("trapparty", new TrapPartyCommand(this));
        register("tpadmin", new AdminCommand(this));
        register("kit", new KitCommand(this));
        register("shop", new ShopCommand(this));
        register("spectate", new SpectateCommand(this));
    }

    private void register(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().warning("Command not registered in plugin.yml: " + name);
            return;
        }
        if (executor instanceof org.bukkit.command.CommandExecutor ce) cmd.setExecutor(ce);
        if (executor instanceof org.bukkit.command.TabCompleter tc) cmd.setTabCompleter(tc);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TrapListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SpecialItemListener(this), this);
    }

    public void reloadAll() {
        configManager.reload();
        messagesManager.reload();
        kitManager.reload();
        shopManager.reload();
        arenaManager.reload();
    }

    public static TrapPartyPlugin get() { return instance; }

    public ConfigManager configs() { return configManager; }
    public MessagesManager messages() { return messagesManager; }
    public VersionAdapter version() { return versionAdapter; }
    public WorldManager worlds() { return worldManager; }
    public ArenaManager arenas() { return arenaManager; }
    public KitManager kits() { return kitManager; }
    public ShopManager shop() { return shopManager; }
    public TrapManager traps() { return trapManager; }
    public EventManager events() { return eventManager; }
    public SpecialItemManager specialItems() { return specialItemManager; }
    public EconomyService economy() { return economyService; }
    public StatsManager stats() { return statsManager; }
    public GameManager games() { return gameManager; }
    public ScoreboardManager scoreboards() { return scoreboardManager; }
    public BossBarManager bossbars() { return bossBarManager; }
    public HologramManager holograms() { return hologramManager; }
}
