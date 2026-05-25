package fr.trapparty.arena;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Template d'arène : représente la config persistée (immutable).
 * Le clonage en partie produit un World temporaire.
 */
public class Arena {

    private final String id;
    private final String displayName;
    private final String templateWorld;
    private final int minPlayers;
    private final int maxPlayers;
    private final String centerDef;
    private final List<String> spawnDefs;
    private final String spectatorSpawnDef;
    private final int borderInitial;
    private final int borderCombat;
    private final int borderSuddenDeath;
    private final List<String> chestLocations;
    private final boolean blockBreakAllowedInPrep;
    private final boolean blockPlaceAllowedInPrep;
    private final boolean pvpInPrep;
    private final boolean hunger;

    public Arena(String id, String displayName, String templateWorld,
                 int minPlayers, int maxPlayers,
                 String centerDef, List<String> spawnDefs, String spectatorSpawnDef,
                 int borderInitial, int borderCombat, int borderSuddenDeath,
                 List<String> chestLocations,
                 boolean blockBreakAllowedInPrep, boolean blockPlaceAllowedInPrep,
                 boolean pvpInPrep, boolean hunger) {
        this.id = id;
        this.displayName = displayName;
        this.templateWorld = templateWorld;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.centerDef = centerDef;
        this.spawnDefs = new ArrayList<>(spawnDefs);
        this.spectatorSpawnDef = spectatorSpawnDef;
        this.borderInitial = borderInitial;
        this.borderCombat = borderCombat;
        this.borderSuddenDeath = borderSuddenDeath;
        this.chestLocations = chestLocations == null ? new ArrayList<>() : new ArrayList<>(chestLocations);
        this.blockBreakAllowedInPrep = blockBreakAllowedInPrep;
        this.blockPlaceAllowedInPrep = blockPlaceAllowedInPrep;
        this.pvpInPrep = pvpInPrep;
        this.hunger = hunger;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getTemplateWorld() { return templateWorld; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public String getCenterDef() { return centerDef; }
    public List<String> getSpawnDefs() { return spawnDefs; }
    public String getSpectatorSpawnDef() { return spectatorSpawnDef; }
    public int getBorderInitial() { return borderInitial; }
    public int getBorderCombat() { return borderCombat; }
    public int getBorderSuddenDeath() { return borderSuddenDeath; }
    public List<String> getChestLocations() { return chestLocations; }
    public boolean isBlockBreakAllowedInPrep() { return blockBreakAllowedInPrep; }
    public boolean isBlockPlaceAllowedInPrep() { return blockPlaceAllowedInPrep; }
    public boolean isPvpInPrep() { return pvpInPrep; }
    public boolean isHunger() { return hunger; }
}
