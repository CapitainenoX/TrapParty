package fr.trapparty.api.events;

import fr.trapparty.game.Game;
import org.bukkit.event.HandlerList;

/** Tiré au démarrage du COMBAT (fin de la prep). */
public class TrapPartyGameStartEvent extends TrapPartyEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    public TrapPartyGameStartEvent(Game game) { super(game); }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
