package fr.trapparty.events;

import fr.trapparty.game.Game;

/**
 * Modificateurs / événements aléatoires applicables à une partie.
 */
public interface GameEvent {
    String id();
    String displayName();
    void apply(Game game);
    default void cleanup(Game game) {}
}
