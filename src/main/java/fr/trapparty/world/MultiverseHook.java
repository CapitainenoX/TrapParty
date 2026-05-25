package fr.trapparty.world;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Hook MultiVerse-Core par reflection pour éviter une dépendance dure.
 * Si MultiVerse n'est pas présent, WorldManager retombe sur l'API Bukkit native.
 */
public class MultiverseHook {

    private final TrapPartyPlugin plugin;
    private final Plugin mv;
    private final Object worldManager;
    private final Method addWorld;
    private final Method unloadWorld;
    private final Method deleteWorld;

    private MultiverseHook(TrapPartyPlugin plugin, Plugin mv, Object wm,
                           Method addWorld, Method unloadWorld, Method deleteWorld) {
        this.plugin = plugin;
        this.mv = mv;
        this.worldManager = wm;
        this.addWorld = addWorld;
        this.unloadWorld = unloadWorld;
        this.deleteWorld = deleteWorld;
    }

    public static MultiverseHook tryHook(TrapPartyPlugin plugin) {
        if (!plugin.configs().useMultiverse()) return null;
        Plugin mv = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (mv == null || !mv.isEnabled()) return null;
        try {
            Method getMVWorldManager = mv.getClass().getMethod("getMVWorldManager");
            Object wm = getMVWorldManager.invoke(mv);
            // addWorld(String name, World.Environment env, String seed, WorldType type, Boolean generateStructures, String generator)
            Method addWorld = null;
            for (Method m : wm.getClass().getMethods()) {
                if (m.getName().equals("addWorld") && m.getParameterCount() == 6) { addWorld = m; break; }
            }
            Method unload = wm.getClass().getMethod("unloadWorld", String.class);
            Method delete = null;
            try { delete = wm.getClass().getMethod("deleteWorld", String.class); } catch (NoSuchMethodException ignored) {}
            return new MultiverseHook(plugin, mv, wm, addWorld, unload, delete);
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to hook Multiverse: " + t.getMessage());
            return null;
        }
    }

    public String describe() { return mv.getDescription().getVersion(); }

    public World importWorld(String name) {
        try {
            if (addWorld != null) {
                addWorld.invoke(worldManager, name, org.bukkit.World.Environment.NORMAL, null,
                        org.bukkit.WorldType.NORMAL, false, null);
            }
        } catch (Throwable t) {
            plugin.getLogger().fine("Multiverse addWorld failed, falling back: " + t.getMessage());
        }
        return Bukkit.getWorld(name);
    }

    public void unloadWorld(String name) {
        try { if (unloadWorld != null) unloadWorld.invoke(worldManager, name); }
        catch (Throwable t) { Bukkit.unloadWorld(name, false); }
    }

    public void deleteWorld(String name) {
        try { if (deleteWorld != null) deleteWorld.invoke(worldManager, name); }
        catch (Throwable ignored) {}
    }
}
