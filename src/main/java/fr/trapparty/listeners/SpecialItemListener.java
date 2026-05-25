package fr.trapparty.listeners;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpecialItemListener implements Listener {

    private final TrapPartyPlugin plugin;

    public SpecialItemListener(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        plugin.specialItems().handleInteract(e);
    }
}
