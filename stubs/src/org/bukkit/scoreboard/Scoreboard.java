package org.bukkit.scoreboard;
import java.util.Set;
public interface Scoreboard {
    Objective registerNewObjective(String name, String criteria, String displayName);
    Objective getObjective(String name);
    Set<String> getEntries();
    void resetScores(String entry);
}
