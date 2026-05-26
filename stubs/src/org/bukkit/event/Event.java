package org.bukkit.event;
public abstract class Event {
    public HandlerList getHandlers() { return null; }
    public String getEventName() { return getClass().getSimpleName(); }
}
