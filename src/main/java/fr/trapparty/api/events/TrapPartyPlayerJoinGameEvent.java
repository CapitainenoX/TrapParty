package fr.trapparty.api.events;

import fr.trapparty.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class TrapPartyPlayerJoinGameEvent extends TrapPartyEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    public TrapPartyPlayerJoinGameEvent(Game game, Player player) {
        super(game);
        this.player = player;
    }
    public Player getPlayer() { return player; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
