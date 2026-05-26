package fr.trapparty.api.events;

import fr.trapparty.game.Game;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/** Tiré à la fin de partie ; winner peut être null (pas de survivant). */
public class TrapPartyGameEndEvent extends TrapPartyEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID winner;
    public TrapPartyGameEndEvent(Game game, UUID winner) {
        super(game);
        this.winner = winner;
    }
    public UUID getWinner() { return winner; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
