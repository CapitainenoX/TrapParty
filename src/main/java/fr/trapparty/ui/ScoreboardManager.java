package fr.trapparty.ui;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {

    private final TrapPartyPlugin plugin;
    private final Map<UUID, Scoreboard> playerBoards = new ConcurrentHashMap<>();

    public ScoreboardManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public void attach(Game game, Player p) {
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("trapparty", "dummy",
                plugin.messages().get("scoreboard.title"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        playerBoards.put(p.getUniqueId(), sb);
        p.setScoreboard(sb);
        refreshPlayer(game, p);
    }

    public void detach(Player p) {
        playerBoards.remove(p.getUniqueId());
        try { p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()); } catch (Throwable ignored) {}
    }

    public void refresh(Game game) {
        for (GamePlayer gp : game.getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null) refreshPlayer(game, p);
        }
        for (UUID s : game.getSpectators()) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) refreshPlayer(game, p);
        }
    }

    private void refreshPlayer(Game game, Player p) {
        Scoreboard sb = playerBoards.get(p.getUniqueId());
        if (sb == null) { attach(game, p); return; }
        Objective obj = sb.getObjective("trapparty");
        if (obj == null) return;
        // clear
        for (String entry : sb.getEntries()) sb.resetScores(entry);

        GamePlayer gp = game.getPlayer(p.getUniqueId());
        List<String> lines = new ArrayList<>();
        lines.add(plugin.messages().get("scoreboard.state",
                Map.of("state", game.getState().name())));
        lines.add(plugin.messages().get("scoreboard.time",
                Map.of("time", String.valueOf(game.getTimer()))));
        lines.add(plugin.messages().get("scoreboard.players",
                Map.of("alive", String.valueOf(game.aliveCount()),
                        "total", String.valueOf(game.getPlayers().size()))));
        if (gp != null) {
            lines.add(plugin.messages().get("scoreboard.kit",
                    Map.of("kit", gp.getKit() == null ? "?" : gp.getKit().getDisplayName())));
            lines.add(plugin.messages().get("scoreboard.coins",
                    Map.of("coins", String.valueOf(gp.getCoins()))));
            lines.add(plugin.messages().get("scoreboard.kills",
                    Map.of("kills", String.valueOf(gp.getKills()))));
        }
        lines.add(plugin.messages().get("scoreboard.arena",
                Map.of("arena", game.getArena().getDisplayName())));
        lines.add(plugin.messages().get("scoreboard.footer"));

        // unicité des entries via suffixe invisible
        int score = lines.size();
        Set<String> used = new HashSet<>();
        for (String raw : lines) {
            String entry = raw;
            int salt = 0;
            while (used.contains(entry)) {
                entry = raw + new String(new char[salt + 1]).replace("\0", "§r");
                salt++;
            }
            used.add(entry);
            obj.getScore(entry).setScore(score--);
        }
    }
}
