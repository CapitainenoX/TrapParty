package fr.trapparty.api.events;

import fr.trapparty.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/** Tiré avant l'achat — cancellable (un anti-cheat / quest plugin peut bloquer). */
public class TrapPartyShopPurchaseEvent extends TrapPartyEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String itemId;
    private final double cost;
    private boolean cancelled;

    public TrapPartyShopPurchaseEvent(Game game, Player player, String itemId, double cost) {
        super(game);
        this.player = player;
        this.itemId = itemId;
        this.cost = cost;
    }
    public Player getPlayer() { return player; }
    public String getItemId() { return itemId; }
    public double getCost() { return cost; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { this.cancelled = c; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
