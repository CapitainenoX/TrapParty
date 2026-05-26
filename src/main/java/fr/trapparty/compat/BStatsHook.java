package fr.trapparty.compat;

import fr.trapparty.TrapPartyPlugin;

/**
 * Hook bStats reflectif (la classe est shadée + relocatée dans fr.trapparty.libs.bstats).
 * Désactivable via `settings.metrics: false`.
 *
 * Charts exposés :
 *  - parties actives
 *  - kits sélectionnés (top kit par tick)
 *  - économie effective (vault vs coins)
 */
public final class BStatsHook {

    public static final int PLUGIN_ID = 27001; // À enregistrer sur bstats.org si publication

    private BStatsHook() {}

    public static void init(TrapPartyPlugin plugin) {
        if (!plugin.configs().root().getBoolean("settings.metrics", true)) {
            plugin.getLogger().info("bStats metrics disabled via settings.metrics=false");
            return;
        }
        try {
            Class<?> metricsClass = Class.forName("fr.trapparty.libs.bstats.bukkit.Metrics");
            var ctor = metricsClass.getConstructor(
                    org.bukkit.plugin.java.JavaPlugin.class, int.class);
            Object metrics = ctor.newInstance(plugin, PLUGIN_ID);

            // Custom chart : parties actives
            Class<?> spChart = Class.forName("fr.trapparty.libs.bstats.charts.SingleLineChart");
            var spCtor = spChart.getConstructor(String.class, java.util.concurrent.Callable.class);
            metrics.getClass().getMethod("addCustomChart",
                            Class.forName("fr.trapparty.libs.bstats.charts.CustomChart"))
                    .invoke(metrics,
                            spCtor.newInstance("active_games",
                                    (java.util.concurrent.Callable<Integer>) () -> plugin.games().all().size()));

            // Custom chart : économie effective
            Class<?> simpleChart = Class.forName("fr.trapparty.libs.bstats.charts.SimplePie");
            var sCtor = simpleChart.getConstructor(String.class, java.util.concurrent.Callable.class);
            metrics.getClass().getMethod("addCustomChart",
                            Class.forName("fr.trapparty.libs.bstats.charts.CustomChart"))
                    .invoke(metrics,
                            sCtor.newInstance("economy_mode",
                                    (java.util.concurrent.Callable<String>) () ->
                                            plugin.economy().effectiveMode().name().toLowerCase()));

            plugin.getLogger().info("bStats metrics enabled (plugin id " + PLUGIN_ID + ")");
        } catch (ClassNotFoundException cnf) {
            plugin.getLogger().fine("bStats classes not on classpath, skipping metrics.");
        } catch (Throwable t) {
            plugin.getLogger().warning("bStats init failed: " + t.getMessage());
        }
    }
}
