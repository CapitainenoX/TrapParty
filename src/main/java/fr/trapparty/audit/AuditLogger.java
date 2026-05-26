package fr.trapparty.audit;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Audit log JSON-line par jour pour les events sensibles (kills, achats,
 * joins, créations de games). Écriture async, fichier journalier rotatif.
 *
 * Désactivable via `settings.audit-log: false`.
 *
 * Format d'une ligne : {"ts":"2026-05-26T15:30:00", "type":"KILL", ...}
 */
public class AuditLogger {

    private final TrapPartyPlugin plugin;
    private final File dir;
    private final boolean enabled;
    private static final DateTimeFormatter TS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AuditLogger(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "logs");
        if (!dir.exists()) dir.mkdirs();
        this.enabled = plugin.configs().root().getBoolean("settings.audit-log", true);
    }

    public void log(String type, Map<String, Object> fields) {
        if (!enabled) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> writeLine(type, fields));
    }

    private void writeLine(String type, Map<String, Object> fields) {
        File f = new File(dir, "events-" + LocalDate.now() + ".log");
        Map<String, Object> all = new LinkedHashMap<>();
        all.put("ts", LocalDateTime.now().format(TS));
        all.put("type", type);
        if (fields != null) all.putAll(fields);
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : all.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(v);
            else sb.append('"').append(escape(v.toString())).append('"');
        }
        sb.append('}');
        try (PrintWriter pw = new PrintWriter(new FileWriter(f, true))) {
            pw.println(sb);
        } catch (IOException ex) {
            plugin.getLogger().warning("AuditLogger write failed: " + ex.getMessage());
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
