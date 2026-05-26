package org.bukkit.scoreboard;
public interface Objective {
    void setDisplaySlot(DisplaySlot slot);
    void setDisplayName(String name);
    Score getScore(String entry);
}
