package fr.trapparty.api.events;

import fr.trapparty.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/** Tiré à chaque kill (victim + killer). killer peut être null (mort accidentelle). */
public class TrapPartyPlayerKillEvent extends TrapPartyEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player victim;
    private final Player killer;
    private final boolean trapKill;

    public TrapPartyPlayerKillEvent(Game game, Player victim, Player killer, boolean trapKill) {
        super(game);
        this.victim = victim;
        this.killer = killer;
        this.trapKill = trapKill;
    }
    public Player getVictim() { return victim; }
    public Player getKiller() { return killer; }
    public boolean isTrapKill() { return trapKill; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
