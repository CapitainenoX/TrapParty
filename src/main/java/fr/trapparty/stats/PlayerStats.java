package fr.trapparty.stats;

import java.util.UUID;

public class PlayerStats {
    private final UUID uuid;
    private String name;
    private int wins;
    private int kills;
    private int trapKills;
    private int gamesPlayed;
    private int deaths;
    private int mmr = 1000;
    private int coinsTotal;

    public PlayerStats(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public int getWins() { return wins; }
    public void addWin() { wins++; }
    public int getKills() { return kills; }
    public void addKills(int v) { kills += v; }
    public int getTrapKills() { return trapKills; }
    public void addTrapKills(int v) { trapKills += v; }
    public int getGamesPlayed() { return gamesPlayed; }
    public void addGame() { gamesPlayed++; }
    public int getDeaths() { return deaths; }
    public void addDeath() { deaths++; }
    public int getMmr() { return mmr; }
    public void setMmr(int m) { this.mmr = m; }
    public int getCoinsTotal() { return coinsTotal; }
    public void addCoins(int v) { coinsTotal += v; }

    public double getKd() {
        if (deaths == 0) return kills;
        return Math.round((kills * 100.0) / deaths) / 100.0;
    }
}
