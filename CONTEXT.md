# TrapParty - Contexte du projet

## Objectif
Plugin Minecraft mini-jeu **Trap Survival** compétitif, premium, scalable.
- Multi-parties simultanées
- Multi-versions Minecraft (1.13+ idéalement, optimisé 1.16 → 1.21+)
- Intégration MultiVerse-Core
- Compatible Paper / Spigot / Purpur

## Branche de dev
`claude/plugin-custom-optimize-fvfdS`

## Architecture cible
- **Java 17** (compromis: support large 1.17+ moderne ; on garde une abstraction pour 1.13–1.16)
- **Maven** (POM unique, shade des libs internes)
- **Paper API** comme base (compatible Spigot/Purpur)
- **Abstraction multi-version** via `compat/` (VersionAdapter, reflection NMS minimale)
- **Architecture orientée services** : Managers injectés par le plugin principal
- **Pas de NMS direct** : on passe par l'API Bukkit + Adventure (Paper) avec fallback BungeeChat
- **Sans dépendance obligatoire** : MultiVerse-Core en soft-depend

## Modules principaux
| Module | Rôle |
|--------|------|
| `game` | Cycle de partie (états, manager, joueur) |
| `arena` | Arènes, templates, clonage |
| `world` | WorldManager, MultiverseHook, BorderManager |
| `kit` | Kits PvP (Tank, Archer, Assassin, TrapMaster, Engineer, Berserker) |
| `shop` | Boutique fluide, catégories, cooldowns |
| `trap` | Pièges (TNT, Pit, Lava, Web, FakeBlock, etc.) |
| `events` | Événements random (météo, mutations, perks, mort subite) |
| `ui` | Scoreboard, BossBar, GUI, particules, sons |
| `stats` | Stats joueurs persistantes |
| `commands` | Cmds admin + joueur |
| `config` | Hot-reload config et messages |
| `compat` | VersionAdapter (reflection minimale) |
| `listeners` | Bukkit events |
| `util` | ItemBuilder, ColorUtil, RandomUtil |

## États de partie
WAITING → STARTING → PREPARATION → COMBAT → SUDDEN_DEATH → ENDING → RESETTING

## Fonctionnalités originales ajoutées
- Vote de modifiers en lobby
- Coffres mystères pendant la partie
- Mort subite avec lave montante
- Replays "highlights" via captures de positions
- Système ranked / casual
- Quêtes journalières & succès
- Perks temporaires
- Mutations (force, vitesse, double-jump…)

## Choix techniques notables
- Pas de dépendance Adventure obligatoire : utiliser API Paper si dispo, fallback ChatColor pour compat large
- Hologrammes : implémentation native via ArmorStand invisibles (pas de DecentHolograms requis)
- Stockage : YAML par défaut, hook SQLite/MySQL prévu via abstraction
- Worlds temporaires : clonage par copie de dossier + chargement, suppression complète après partie
- Tâches async : I/O (chargement maps), event scheduling

## Statut
En cours de génération initiale.
