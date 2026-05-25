package fr.trapparty.ui;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager {

    private final TrapPartyPlugin plugin;
    private final Map<String, BossBar> bars = new ConcurrentHashMap<>();

    public BossBarManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public void attach(Game game, Player player) {
        BossBar bar = bars.computeIfAbsent(game.getId(), id -> {
            BossBar b = Bukkit.createBossBar("§eTrap Party", BarColor.PURPLE, BarStyle.SOLID);
            b.setProgress(1.0);
            return b;
        });
        bar.addPlayer(player);
    }

    public void detach(Player player) {
        for (BossBar b : bars.values()) b.removePlayer(player);
    }

    public void refresh(Game game) {
        BossBar bar = bars.get(game.getId());
        if (bar == null) return;
        bar.setTitle(buildTitle(game));
        bar.setColor(colorFor(game));
        bar.setProgress(progress(game));
    }

    private String buildTitle(Game game) {
        String state = switch (game.getState()) {
            case WAITING -> "§7En attente";
            case STARTING -> "§eDémarrage §f" + game.getTimer() + "s";
            case PREPARATION -> "§6Préparation §f" + game.getTimer() + "s";
            case COMBAT -> "§cCombat §f" + game.getTimer() + "s";
            case SUDDEN_DEATH -> "§4☠ Mort subite §f" + game.getTimer() + "s";
            case ENDING -> "§6Fin de partie";
            case RESETTING -> "§7Reset";
        };
        return "§l" + state + "  §7§l|  §a" + game.aliveCount() + "§7/§f" + game.getPlayers().size();
    }

    private BarColor colorFor(Game game) {
        return switch (game.getState()) {
            case WAITING, STARTING -> BarColor.GREEN;
            case PREPARATION -> BarColor.YELLOW;
            case COMBAT -> BarColor.RED;
            case SUDDEN_DEATH -> BarColor.WHITE;
            default -> BarColor.PURPLE;
        };
    }

    private double progress(Game game) {
        int max = switch (game.getState()) {
            case STARTING -> plugin.configs().countdown();
            case PREPARATION -> plugin.configs().preparationTime();
            case COMBAT -> plugin.configs().combatTime();
            case SUDDEN_DEATH -> plugin.configs().suddenDeathTime();
            case ENDING -> plugin.configs().endTime();
            default -> 1;
        };
        return Math.max(0, Math.min(1.0, (double) game.getTimer() / Math.max(1, max)));
    }

    public void removeAll() {
        for (BossBar b : bars.values()) {
            b.removeAll();
        }
        bars.clear();
    }
}
