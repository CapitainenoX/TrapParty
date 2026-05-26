package fr.trapparty.api.events;

import fr.trapparty.game.Game;
import org.bukkit.event.Event;

/**
 * Base de tous les events publics TrapParty (pour les plugins tiers : DiscordSRV,
 * BattlePass, Quest plugins, etc.).
 */
public abstract class TrapPartyEvent extends Event {
    protected final Game game;
    protected TrapPartyEvent(Game game) { this.game = game; }
    public Game getGame() { return game; }
}
