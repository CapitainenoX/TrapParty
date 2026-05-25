package fr.trapparty.config;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagesManager {

    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final TrapPartyPlugin plugin;
    private FileConfiguration messages;

    public MessagesManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File f = new File(plugin.getDataFolder(), "messages.yml");
        if (!f.exists()) plugin.saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(f);
    }

    public String raw(String path) {
        String s = messages.getString(path);
        return s == null ? path : s;
    }

    public String get(String path) { return color(raw(path)); }

    public String get(String path, Map<String, String> placeholders) {
        String s = raw(path);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                s = s.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return color(s);
    }

    public String prefix() { return color(messages.getString("prefix", "")); }

    public void send(CommandSender to, String path) {
        if (to == null) return;
        to.sendMessage(prefix() + get(path));
    }

    public void send(CommandSender to, String path, Map<String, String> placeholders) {
        if (to == null) return;
        to.sendMessage(prefix() + get(path, placeholders));
    }

    public void broadcast(String path) {
        plugin.getServer().broadcastMessage(prefix() + get(path));
    }

    public void broadcastPlayers(Iterable<Player> players, String path, Map<String, String> placeholders) {
        String msg = prefix() + get(path, placeholders);
        for (Player p : players) p.sendMessage(msg);
    }

    public static String color(String input) {
        if (input == null) return "";
        Matcher m = HEX.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hex = m.group(1);
            // ne tente pas Adventure ici : on convertit en code legacy si dispo (1.16+)
            try {
                m.appendReplacement(sb, Matcher.quoteReplacement(net.md_5.bungee.api.ChatColor.of("#" + hex).toString()));
            } catch (Throwable t) {
                m.appendReplacement(sb, "");
            }
        }
        m.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}
