package fr.trapparty.game;

public enum GameState {
    WAITING,        // attente de joueurs
    STARTING,       // countdown
    PREPARATION,    // construction & pièges
    COMBAT,         // PvP actif
    SUDDEN_DEATH,   // bordure ferme
    ENDING,         // affichage du gagnant
    RESETTING       // nettoyage et suppression du monde
}
