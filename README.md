# TrapParty

Plugin Minecraft **Trap Survival** : mini-jeu compétitif où chaque joueur prépare
ses pièges puis tente de survivre au PvP dans une mini-arène à bordure rétrécissante.

> Construis. Piège. Survis. Domine.

## Highlights

- Plusieurs parties **simultanées**, chacune sur son monde clone.
- Cycle de partie complet : `WAITING → STARTING → PREPARATION → COMBAT → SUDDEN_DEATH → ENDING → RESETTING`.
- **6 kits** équilibrés (Tank, Archer, Assassin, TrapMaster, Engineer, Berserker), entièrement configurables.
- Shop en jeu, catégories (blocs, pièges, utilitaires, redstone, mobilité, consommables), anti-spam et items gratuits.
- Pièges : TNT, toiles, lave, magma, tripwire, cactus… kills attribués à l'owner.
- Événements aléatoires : tempête, météorites, mutations, coffres mystères, speed boost, faible gravité, nuit.
- UI moderne : scoreboard dynamique, boss bar, titres, sons, hologrammes natifs.
- Compatible Paper / Spigot / Purpur, **api-version 1.13** → fonctionne jusqu'à 1.21+.
- Intégration **soft** avec MultiVerse-Core (auto-détectée).
- Stats persistantes (YAML), MMR simple, hooks pour SQLite/MySQL futurs.
- 0 dépendance lourde — hologrammes via ArmorStand natifs.

## Build

```bash
mvn -B package
```

Le plugin compilé sera dans `target/TrapParty-1.0.0.jar`.

### Dépendances de build

- Java 17+
- Paper API 1.20.4 (sur le repo `repo.papermc.io`)
- MultiVerse-Core 4.3.1 *(scope provided, optionnel à l'exécution)*

> Ces dépôts ne sont pas sur Maven Central : un accès réseau aux dépôts
> `repo.papermc.io` et `repo.onarandombox.com` est requis au build.

## Installation

1. Place `TrapParty-1.0.0.jar` dans `plugins/`.
2. (Optionnel) Installe **Multiverse-Core** pour la gestion des mondes.
3. Démarre le serveur ; édite `plugins/TrapParty/config.yml`, `arenas.yml`, `kits.yml`, `shop.yml`, `messages.yml`.
4. Crée une arène avec `/tpadmin create <id> <templateWorld>` puis `/tpadmin setcenter <id>` et `/tpadmin setspawn <id>` (à répéter pour chaque spawn).
5. `/tparty join` pour rejoindre.

## Commandes

| Commande | Description |
|----------|-------------|
| `/tparty join [arène]` | Rejoindre une partie (auto si arène omise) |
| `/tparty leave` | Quitter la partie |
| `/tparty stats` | Voir ses stats |
| `/tparty list` | Lister les parties en cours |
| `/tparty arenas` | Lister les arènes |
| `/kit [id]` | Ouvrir la GUI ou sélectionner un kit |
| `/shop` | Ouvrir la boutique |
| `/spectate <gameId\|arène>` | Spectate une partie |
| `/tpadmin reload` | Recharger la config |
| `/tpadmin create <arena> <templateWorld>` | Créer une arène |
| `/tpadmin delete <arena>` | Supprimer une arène |
| `/tpadmin setspawn <arena>` | Ajouter un spawn à la position courante |
| `/tpadmin setcenter <arena>` | Définir le centre de l'arène |
| `/tpadmin forcestart <gameId>` | Forcer le démarrage d'une partie |
| `/tpadmin forcestop <gameId>` | Forcer la fin d'une partie |
| `/tpadmin debug` | Toggle mode debug |
| `/tpadmin save` | Flush des stats sur disque |

## Permissions

- `trapparty.play` *(default)* — rejoindre
- `trapparty.spectate` *(default)* — spectate
- `trapparty.shop` *(default)* — shop
- `trapparty.kit.<id>` *(default)* — kit donné
- `trapparty.admin` *(op)* — admin
- `trapparty.*` *(op)* — tout

## Architecture

```
fr.trapparty/
├── TrapPartyPlugin            (bootstrap, hooks, services)
├── compat/VersionAdapter      (détection version + helpers cross-version)
├── config/{ConfigManager,MessagesManager}
├── arena/{Arena,ArenaManager}
├── world/{WorldManager,MultiverseHook,BorderManager}
├── game/{Game,GameManager,GameState,GamePlayer}
├── kit/{Kit,KitManager}
├── shop/{Shop,ShopManager,ShopItem,ShopCategory}
├── trap/{Trap,TrapManager}
├── events/                    (modificateurs random)
│   ├── EventManager
│   └── impl/{StormEvent,MeteorEvent,MutationEvent,LootChestEvent,
│             SpeedBoostEvent,LowGravityEvent,NightFallEvent}
├── ui/{ScoreboardManager,BossBarManager,HologramManager,KitSelectionGui}
├── stats/{PlayerStats,StatsManager}
├── commands/{TrapPartyCommand,AdminCommand,KitCommand,ShopCommand,SpectateCommand}
├── listeners/{PlayerListener,GameListener,TrapListener}
└── util/{ItemBuilder,LocationUtil,RandomUtil}
```

## Compatibilité multi-version

Le plugin cible **api-version 1.13** dans `plugin.yml` mais reste fonctionnel
jusqu'aux dernières versions (1.21+) grâce à :

- `VersionAdapter` qui détecte la version serveur et expose des helpers
  (titres, action bars, sons, particules, matériaux, couleurs cuir) avec
  fallbacks réfléchis.
- `ItemBuilder.lookupEnchant(...)` qui résout les enchantements par alias
  ancien/nouveau (`SHARPNESS` ⇆ `DAMAGE_ALL`, `POWER` ⇆ `ARROW_DAMAGE`, …).
- Aucune dépendance NMS directe.
- Les enchantements et matériaux sont résolus par nom (string) avec
  multiples candidats.
- Pas de dépendance dure sur Paper / Adventure — détection via classpath.

## Performances

- Clonage de mondes asynchrone.
- Suppression des mondes temporaires en async + délai configurable.
- Stats : cache mémoire + flush debounced (5 s) en async.
- Tâches Bukkit limitées (BukkitRunnable annulables, pas de leak).
- Hologrammes via ArmorStands marker (zéro tick, pas d'overhead réseau).
- BossBar partagée par partie, pas une par joueur.
- Limite configurable du nombre de parties simultanées (`performance.max-arenas`).

## Configuration

Tout est dans `plugins/TrapParty/` :

- `config.yml` — game settings, timers, économie, performance.
- `messages.yml` — toutes les chaînes (codes couleurs `&`, hex `&#RRGGBB`).
- `arenas.yml` — arènes (templates de mondes).
- `kits.yml` — kits PvP.
- `shop.yml` — boutique avec catégories.

`/tpadmin reload` recharge tout à chaud.

## Roadmap (idées)

- [ ] Mode équipes (toggle déjà dans `config.yml`).
- [ ] Mode ranked complet (MMR Elo K-factor déjà câblé).
- [ ] Quêtes journalières / succès.
- [ ] Replay highlights.
- [ ] Hook PlaceholderAPI.

## Licence

MIT.
