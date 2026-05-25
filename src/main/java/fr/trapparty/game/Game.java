package fr.trapparty.game;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.arena.Arena;
import fr.trapparty.events.GameEvent;
import fr.trapparty.util.LocationUtil;
import fr.trapparty.world.BorderManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Une partie en cours sur une arène.
 * Cycle : WAITING → STARTING → PREPARATION → COMBAT → SUDDEN_DEATH → ENDING → RESETTING
 */
public class Game {

    private final TrapPartyPlugin plugin;
    private final String id;
    private final Arena arena;
    private final String worldName;
    private final BorderManager borderManager;
    private final Map<UUID, GamePlayer> players = new ConcurrentHashMap<>();
    private final List<UUID> joinedOrder = Collections.synchronizedList(new ArrayList<>());
    private final Set<UUID> spectators = ConcurrentHashMap.newKeySet();

    private GameState state = GameState.WAITING;
    private World world;
    private int timer = 0;
    private BukkitTask tickTask;
    private GameEvent currentModifier;
    private UUID winner;

    private final long createdAt = System.currentTimeMillis();

    public Game(TrapPartyPlugin plugin, String id, Arena arena, String worldName) {
        this.plugin = plugin;
        this.id = id;
        this.arena = arena;
        this.worldName = worldName;
        this.borderManager = new BorderManager(plugin);
    }

    public void setWorld(World w) { this.world = w; }
    public World world() { return world; }
    public String getId() { return id; }
    public Arena getArena() { return arena; }
    public String getWorldName() { return worldName; }
    public GameState getState() { return state; }
    public int getTimer() { return timer; }
    public GamePlayer getPlayer(UUID uuid) { return players.get(uuid); }
    public Collection<GamePlayer> getPlayers() { return players.values(); }
    public List<UUID> joinedOrder() { return joinedOrder; }
    public Set<UUID> getSpectators() { return spectators; }
    public long aliveCount() { return players.values().stream().filter(GamePlayer::isAlive).count(); }
    public UUID getWinner() { return winner; }
    public GameEvent getCurrentModifier() { return currentModifier; }
    public void setCurrentModifier(GameEvent m) { this.currentModifier = m; }

    public boolean canJoin() {
        if (state != GameState.WAITING && state != GameState.STARTING) return false;
        return players.size() < arena.getMaxPlayers();
    }

    public boolean isFull() { return players.size() >= arena.getMaxPlayers(); }

    public void join(Player player) {
        GamePlayer gp = new GamePlayer(player);
        gp.snapshot(player);
        gp.setCoins(plugin.configs().startingCoins());
        players.put(player.getUniqueId(), gp);
        joinedOrder.add(player.getUniqueId());

        // tp au lobby de l'arène (premier spawn)
        Location lobbyLoc = pickSpawn();
        if (lobbyLoc != null) player.teleport(lobbyLoc);

        player.setGameMode(GameMode.ADVENTURE);
        clearPlayer(player);

        broadcast("join.broadcast", Map.of(
                "player", player.getName(),
                "count", String.valueOf(players.size()),
                "max", String.valueOf(arena.getMaxPlayers())));

        // assignation automatique du scoreboard
        plugin.scoreboards().attach(this, player);
        plugin.bossbars().attach(this, player);

        // démarrage auto si quorum
        if (state == GameState.WAITING && players.size() >= arena.getMinPlayers()) {
            startCountdown();
        }
    }

    public void leave(Player player) {
        UUID uuid = player.getUniqueId();
        GamePlayer gp = players.remove(uuid);
        spectators.remove(uuid);
        joinedOrder.remove(uuid);
        if (gp == null) return;

        plugin.scoreboards().detach(player);
        plugin.bossbars().detach(player);

        gp.restore(player);
        broadcast("join.leave", Map.of("player", player.getName()));

        // vérifier conditions de fin
        if (state == GameState.COMBAT || state == GameState.SUDDEN_DEATH) {
            checkEndConditions();
        } else if (state == GameState.STARTING && players.size() < arena.getMinPlayers()) {
            cancelCountdown();
        }
    }

    /** Tente une reconnexion ; retourne true si autorisé. */
    public boolean reconnect(Player player) {
        if (!plugin.configs().root().getBoolean("game.allow-rejoin", true)) return false;
        GamePlayer gp = players.get(player.getUniqueId());
        if (gp == null) return false;
        if (!gp.isAlive()) {
            setAsSpectator(player, gp);
            return true;
        }
        Location loc = pickSpawn();
        if (loc != null) player.teleport(loc);
        if (gp.getKit() != null) gp.getKit().apply(player);
        plugin.scoreboards().attach(this, player);
        plugin.bossbars().attach(this, player);
        return true;
    }

    public void selectKit(Player player, fr.trapparty.kit.Kit kit) {
        GamePlayer gp = players.get(player.getUniqueId());
        if (gp == null) return;
        if (state != GameState.WAITING && state != GameState.STARTING && state != GameState.PREPARATION) return;
        gp.setKit(kit);
        plugin.messages().send(player, "kit.selected", Map.of("kit", kit.getDisplayName()));
    }

    // --------- Lifecycle ----------

    private void startCountdown() {
        if (state != GameState.WAITING) return;
        state = GameState.STARTING;
        timer = plugin.configs().countdown();
        startTick();
    }

    private void cancelCountdown() {
        state = GameState.WAITING;
        if (tickTask != null) { tickTask.cancel(); tickTask = null; }
    }

    private void startPreparation() {
        state = GameState.PREPARATION;
        timer = plugin.configs().preparationTime();
        // tp spawns + apply kit
        List<String> spawns = new ArrayList<>(arena.getSpawnDefs());
        Collections.shuffle(spawns);
        int idx = 0;
        for (GamePlayer gp : players.values()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;
            String def = spawns.isEmpty() ? arena.getCenterDef() : spawns.get(idx++ % spawns.size());
            Location loc = LocationUtil.parse(world, def);
            if (loc != null) p.teleport(loc);
            p.setGameMode(GameMode.SURVIVAL);
            clearPlayer(p);
            if (gp.getKit() == null) {
                // kit par défaut
                fr.trapparty.kit.Kit fallback = plugin.kits().get("trapmaster");
                if (fallback != null) gp.setKit(fallback);
            }
            if (gp.getKit() != null) gp.getKit().apply(p);
            plugin.version().sendTitle(p, "&6&l▶ PRÉPARATION", "&7Construis tes pièges !", 10, 50, 10);
        }
        // bordure initiale
        Location center = LocationUtil.parse(world, arena.getCenterDef());
        if (center != null) borderManager.setBorder(world, center, arena.getBorderInitial());
        world.setPVP(arena.isPvpInPrep());
        broadcastPrefixed("game.prep-start", Map.of("time", String.valueOf(timer)));
    }

    private void startCombat() {
        state = GameState.COMBAT;
        timer = plugin.configs().combatTime();
        world.setPVP(true);
        Location center = LocationUtil.parse(world, arena.getCenterDef());
        if (center != null) borderManager.shrink(world, arena.getBorderCombat(), Math.max(20, timer / 2));
        broadcastPrefixed("game.combat-start", null);
        forEach(p -> plugin.version().sendTitle(p, "&c&l⚔ COMBAT", "&7Survis au PvP !", 10, 60, 10));
    }

    private void startSuddenDeath() {
        state = GameState.SUDDEN_DEATH;
        timer = plugin.configs().suddenDeathTime();
        Location center = LocationUtil.parse(world, arena.getCenterDef());
        if (center != null) borderManager.shrink(world, arena.getBorderSuddenDeath(), Math.max(10, timer));
        broadcastPrefixed("game.sudden-death", null);
        forEach(p -> plugin.version().sendTitle(p, "&4&l☠ MORT SUBITE", "&cFini de jouer !", 10, 70, 10));
    }

    private void endGame(UUID winnerUuid) {
        this.winner = winnerUuid;
        state = GameState.ENDING;
        timer = plugin.configs().endTime();

        if (winnerUuid != null) {
            GamePlayer gp = players.get(winnerUuid);
            String name = gp != null ? gp.getName() : "?";
            broadcastPrefixed("game.win", Map.of("player", name));
            forEach(p -> plugin.version().sendTitle(p, "&6&l✦ VICTOIRE", "&e" + name, 10, 70, 10));
            // récompenses
            if (gp != null) {
                plugin.stats().recordWin(winnerUuid, gp);
            }
        } else {
            broadcastPrefixed("game.no-winner", null);
        }
        for (GamePlayer gp : players.values()) {
            plugin.stats().recordParticipation(gp.getUuid(), gp);
        }
    }

    private void reset() {
        state = GameState.RESETTING;
        if (tickTask != null) { tickTask.cancel(); tickTask = null; }
        // tp tout le monde au lobby principal
        World fallback = Bukkit.getWorlds().get(0);
        for (GamePlayer gp : players.values()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null) continue;
            plugin.scoreboards().detach(p);
            plugin.bossbars().detach(p);
            gp.restore(p);
        }
        for (UUID s : spectators) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) p.teleport(fallback.getSpawnLocation());
        }
        players.clear();
        spectators.clear();
        joinedOrder.clear();
        plugin.holograms().removeForGame(id);
        plugin.traps().clearGame(id);

        int delay = plugin.configs().root().getInt("arena.cleanup.delay-after-end", 10);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.worlds().unloadAndDelete(worldName);
            plugin.games().destroy(this);
        }, delay * 20L);
    }

    public void forceStart() {
        if (state == GameState.WAITING || state == GameState.STARTING) {
            startPreparation();
        }
    }

    public void forceEnd() {
        endGame(null);
    }

    public void onPlayerDeath(Player victim, Player killer, boolean trapKill) {
        GamePlayer gv = players.get(victim.getUniqueId());
        if (gv == null) return;
        gv.setAlive(false);
        setAsSpectator(victim, gv);

        if (killer != null) {
            GamePlayer gk = players.get(killer.getUniqueId());
            if (gk != null) {
                gk.addKill();
                gk.addCoins(plugin.configs().killReward() + (trapKill ? plugin.configs().trapKillBonus() : 0));
                if (trapKill) gk.addTrapKill();
                broadcastPrefixed(trapKill ? "game.player-trap-killed" : "game.player-killed",
                        Map.of("player", victim.getName(), "killer", killer.getName()));
            }
        } else {
            broadcastPrefixed("game.player-died", Map.of("player", victim.getName()));
        }
        checkEndConditions();
    }

    private void checkEndConditions() {
        long alive = aliveCount();
        if (alive <= 1 && (state == GameState.COMBAT || state == GameState.SUDDEN_DEATH || state == GameState.PREPARATION)) {
            UUID w = null;
            for (GamePlayer gp : players.values()) {
                if (gp.isAlive()) { w = gp.getUuid(); break; }
            }
            endGame(w);
        }
    }

    public void setAsSpectator(Player p, GamePlayer gp) {
        gp.setSpectator(true);
        spectators.add(p.getUniqueId());
        p.setGameMode(GameMode.SPECTATOR);
        Location specLoc = LocationUtil.parse(world, arena.getSpectatorSpawnDef());
        if (specLoc != null) p.teleport(specLoc);
        clearPlayer(p);
    }

    // --------- Tick loop ----------

    private void startTick() {
        if (tickTask != null) tickTask.cancel();
        tickTask = new BukkitRunnable() {
            @Override public void run() { tick(); }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void tick() {
        switch (state) {
            case STARTING -> {
                if (players.size() < arena.getMinPlayers()) {
                    cancelCountdown();
                    return;
                }
                if (timer <= 0) {
                    startPreparation();
                    return;
                }
                if (timer <= 5 || timer == 10 || timer == 20 || timer == 30) {
                    broadcastPrefixed("game.countdown", Map.of("time", String.valueOf(timer)));
                    forEach(p -> plugin.version().sound(p, "BLOCK_NOTE_BLOCK_PLING", 1f, 1.5f));
                }
                timer--;
            }
            case PREPARATION -> {
                if (timer <= 0) { startCombat(); return; }
                if (timer == 30 || timer == 10 || timer <= 5)
                    forEach(p -> plugin.version().sendActionBar(p, "&ePréparation : &c" + timer + "s"));
                timer--;
            }
            case COMBAT -> {
                plugin.events().tickGame(this, timer);
                if (timer <= 0) { startSuddenDeath(); return; }
                timer--;
            }
            case SUDDEN_DEATH -> {
                if (timer <= 0 || aliveCount() <= 1) {
                    UUID w = null;
                    long min = Long.MAX_VALUE;
                    for (GamePlayer gp : players.values()) if (gp.isAlive()) { w = gp.getUuid(); break; }
                    endGame(w);
                    return;
                }
                timer--;
            }
            case ENDING -> {
                if (timer <= 0) { reset(); return; }
                timer--;
            }
            case WAITING, RESETTING -> { /* noop */ }
            default -> { /* noop */ }
        }
        plugin.scoreboards().refresh(this);
        plugin.bossbars().refresh(this);
    }

    // --------- Helpers ----------

    private Location pickSpawn() {
        if (arena.getSpawnDefs().isEmpty()) {
            return world == null ? null : world.getSpawnLocation();
        }
        String def = arena.getSpawnDefs().get((int) (Math.random() * arena.getSpawnDefs().size()));
        return LocationUtil.parse(world, def);
    }

    private void clearPlayer(Player p) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setLevel(0);
        p.setExp(0);
        p.setHealth(maxHealthOf(p));
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setFireTicks(0);
        for (var e : new ArrayList<>(p.getActivePotionEffects())) p.removePotionEffect(e.getType());
    }

    private void forEach(java.util.function.Consumer<Player> c) {
        for (GamePlayer gp : players.values()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null) c.accept(p);
        }
        for (UUID s : spectators) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) c.accept(p);
        }
    }

    private void broadcast(String key, Map<String, String> ph) {
        List<Player> targets = new ArrayList<>();
        for (GamePlayer gp : players.values()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null) targets.add(p);
        }
        for (UUID s : spectators) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) targets.add(p);
        }
        plugin.messages().broadcastPlayers(targets, key, ph);
    }

    private void broadcastPrefixed(String key, Map<String, String> ph) { broadcast(key, ph); }

    public long uptimeMillis() { return System.currentTimeMillis() - createdAt; }

    /** Max-health cross-version : Attribute moderne, fallback Damageable#getMaxHealth (déprécié). */
    @SuppressWarnings("deprecation")
    private static double maxHealthOf(Player p) {
        try {
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf("MAX_HEALTH");
            var inst = p.getAttribute(attr);
            if (inst != null) return inst.getValue();
        } catch (Throwable ignored) {}
        try {
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.valueOf("GENERIC_MAX_HEALTH");
            var inst = p.getAttribute(attr);
            if (inst != null) return inst.getValue();
        } catch (Throwable ignored) {}
        try { return p.getMaxHealth(); } catch (Throwable t) { return 20.0; }
    }
}
