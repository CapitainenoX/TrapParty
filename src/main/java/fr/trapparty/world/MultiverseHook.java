package fr.trapparty.world;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Hook MultiVerse-Core soft : détecte la présence du plugin (v4 ou v5)
 * et tente d'enregistrer/désenregistrer les mondes temporaires via
 * reflection. Si quoi que ce soit échoue, on retombe proprement sur
 * l'API Bukkit native (WorldCreator + Bukkit.unloadWorld).
 *
 * v4 : groupId com.onarandombox.multiversecore, MultiverseCore#getMVWorldManager()
 * v5 : groupId org.mvplugins.multiverse.core, API DI via MultiverseCoreApi#get()
 */
public class MultiverseHook {

    private final TrapPartyPlugin plugin;
    private final Plugin mv;
    private final boolean v5;

    // v4 reflection cache
    private Object v4WorldManager;
    private Method v4AddWorld;
    private Method v4UnloadWorld;
    private Method v4DeleteWorld;

    // v5 reflection cache
    private Object v5WorldManager;
    private Method v5LoadWorld;
    private Method v5UnloadWorld;
    private Method v5RemoveWorld;

    private MultiverseHook(TrapPartyPlugin plugin, Plugin mv, boolean v5) {
        this.plugin = plugin;
        this.mv = mv;
        this.v5 = v5;
    }

    public static MultiverseHook tryHook(TrapPartyPlugin plugin) {
        if (!plugin.configs().useMultiverse()) return null;
        Plugin mv = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (mv == null || !mv.isEnabled()) return null;

        String pkg = mv.getClass().getPackageName();
        boolean v5 = pkg.startsWith("org.mvplugins");
        MultiverseHook hook = new MultiverseHook(plugin, mv, v5);
        if (v5) hook.initV5();
        else hook.initV4();
        return hook;
    }

    private void initV4() {
        try {
            Method getWm = mv.getClass().getMethod("getMVWorldManager");
            v4WorldManager = getWm.invoke(mv);
            for (Method m : v4WorldManager.getClass().getMethods()) {
                if (m.getName().equals("addWorld") && m.getParameterCount() == 6) v4AddWorld = m;
            }
            try { v4UnloadWorld = v4WorldManager.getClass().getMethod("unloadWorld", String.class); } catch (NoSuchMethodException ignored) {}
            try { v4DeleteWorld = v4WorldManager.getClass().getMethod("deleteWorld", String.class); } catch (NoSuchMethodException ignored) {}
        } catch (Throwable t) {
            plugin.getLogger().fine("MV v4 init failed: " + t.getMessage());
        }
    }

    private void initV5() {
        try {
            // MultiverseCoreApi.get().getWorldManager()
            Class<?> apiClass = Class.forName("org.mvplugins.multiverse.core.MultiverseCoreApi");
            Object api = apiClass.getMethod("get").invoke(null);
            v5WorldManager = api.getClass().getMethod("getWorldManager").invoke(api);
            for (Method m : v5WorldManager.getClass().getMethods()) {
                switch (m.getName()) {
                    case "loadWorld" -> { if (v5LoadWorld == null) v5LoadWorld = m; }
                    case "unloadWorld" -> { if (v5UnloadWorld == null) v5UnloadWorld = m; }
                    case "removeWorld", "deleteWorld" -> { if (v5RemoveWorld == null) v5RemoveWorld = m; }
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().fine("MV v5 init failed: " + t.getMessage());
        }
    }

    public String describe() {
        return mv.getDescription().getVersion() + (v5 ? " (API v5)" : " (API v4)");
    }

    /**
     * Tente d'enregistrer un monde dans MV. Si rien ne marche, on charge via Bukkit
     * — MV détectera le monde au prochain /mv import.
     */
    public World importWorld(String name) {
        if (v5) {
            try {
                if (v5LoadWorld != null) {
                    // L'API v5 prend un builder ou une string ; on tente avec une string.
                    for (Method m : v5WorldManager.getClass().getMethods()) {
                        if (m.getName().equals("loadWorld") && m.getParameterCount() == 1
                                && m.getParameterTypes()[0] == String.class) {
                            m.invoke(v5WorldManager, name);
                            break;
                        }
                    }
                }
            } catch (Throwable t) {
                plugin.getLogger().fine("MV v5 loadWorld failed: " + t.getMessage());
            }
        } else if (v4AddWorld != null) {
            try {
                v4AddWorld.invoke(v4WorldManager, name,
                        org.bukkit.World.Environment.NORMAL, null,
                        org.bukkit.WorldType.NORMAL, false, null);
            } catch (Throwable t) {
                plugin.getLogger().fine("MV v4 addWorld failed: " + t.getMessage());
            }
        }
        World w = Bukkit.getWorld(name);
        if (w == null) w = new WorldCreator(name).createWorld();
        return w;
    }

    public void unloadWorld(String name) {
        try {
            if (v5 && v5UnloadWorld != null) {
                for (Method m : v5WorldManager.getClass().getMethods()) {
                    if (m.getName().equals("unloadWorld") && m.getParameterCount() == 1
                            && m.getParameterTypes()[0] == String.class) {
                        m.invoke(v5WorldManager, name);
                        return;
                    }
                }
            } else if (v4UnloadWorld != null) {
                v4UnloadWorld.invoke(v4WorldManager, name);
                return;
            }
        } catch (Throwable ignored) {}
        Bukkit.unloadWorld(name, false);
    }

    public void deleteWorld(String name) {
        try {
            if (v5 && v5RemoveWorld != null) {
                for (Method m : v5WorldManager.getClass().getMethods()) {
                    if ((m.getName().equals("removeWorld") || m.getName().equals("deleteWorld"))
                            && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                        m.invoke(v5WorldManager, name);
                        return;
                    }
                }
            } else if (v4DeleteWorld != null) {
                v4DeleteWorld.invoke(v4WorldManager, name);
            }
        } catch (Throwable ignored) {}
    }
}
