package fr.trapparty.events;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.events.impl.*;
import fr.trapparty.game.Game;
import fr.trapparty.util.RandomUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {

    private final TrapPartyPlugin plugin;
    private final Map<String, GameEvent> registered = new LinkedHashMap<>();
    private final Map<String, Long> nextEventAt = new ConcurrentHashMap<>();

    public EventManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        register(new StormEvent());
        register(new MeteorEvent());
        register(new MutationEvent());
        register(new LootChestEvent(plugin));
        register(new SpeedBoostEvent());
        register(new LowGravityEvent());
        register(new NightFallEvent());
    }

    public void register(GameEvent e) { registered.put(e.id(), e); }
    public Collection<GameEvent> all() { return registered.values(); }
    public GameEvent get(String id) { return registered.get(id); }

    public void tickGame(Game game, int combatTimer) {
        if (!plugin.configs().root().getBoolean("events.enabled", true)) return;
        long now = System.currentTimeMillis();
        long next = nextEventAt.computeIfAbsent(game.getId(), id -> scheduleNext(now));
        if (now < next) return;

        nextEventAt.put(game.getId(), scheduleNext(now));
        if (!RandomUtil.chance(plugin.configs().root().getDouble("events.chance", 0.75))) return;

        List<String> pool = plugin.configs().root().getStringList("events.pool");
        if (pool.isEmpty()) return;
        String id = RandomUtil.pick(pool);
        GameEvent e = get(id);
        if (e == null) return;
        e.apply(game);
    }

    private long scheduleNext(long now) {
        int min = plugin.configs().root().getInt("events.min-interval", 60);
        int max = plugin.configs().root().getInt("events.max-interval", 150);
        return now + RandomUtil.between(min, max) * 1000L;
    }

    public void clearGame(String gameId) {
        nextEventAt.remove(gameId);
    }
}
