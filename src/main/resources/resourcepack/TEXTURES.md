# TrapParty Resource Pack — textures du menu

Le resource pack ne contient **que les éléments d'interface** du shop. Les
items spéciaux (Super Foreuse, Activateur à distance, Trap Caller,
Grappling Hook) ne sont **pas** des textures custom : ce sont des items
vanilla (`NETHERITE_PICKAXE`, `COMPASS`, `BLAZE_ROD`, `FISHING_ROD`)
auxquels on ajoute un *glow* d'enchantement, un nom personnalisé et de la
lore.

Les 14 PNG du dossier `assets/trapparty/textures/item/` sont fournis
**vides** (16×16 transparent). Remplace-les par tes propres designs en
gardant exactement les mêmes noms de fichier.

## Comment utiliser les prompts nano-banana

[Nano Banana](https://gemini.google/overview/image-generation/) (Gemini
2.5 Flash Image) génère des images à partir d'instructions textuelles.
Pour chaque texture ci-dessous, copie le prompt dans Nano Banana, génère,
puis enregistre en **PNG 16×16 RGBA** dans le chemin indiqué.

> **Recettes générales à ajouter à tout prompt** si tu veux un rendu
> cohérent avec Minecraft :
> *"16x16 pixel art icon, Minecraft texture style, transparent
> background, clean palette, no anti-aliasing, sharp pixel edges,
> vanilla-friendly art direction."*

> Si Nano Banana sort en plus haute résolution, downscale à 16×16 avec un
> nearest-neighbor (GIMP, Pixilart, ImageMagick `-filter point -resize 16x16`).

---

## Boutons de catégorie (PAPER, custom_model_data 2001-2007)

### `shop_button_blocks.png` — CMD 2001 — Catégorie "Blocs"

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a stack of three cobblestone
> blocks with a small green plus sign in the top-right corner, gray
> rocky texture with darker outlines, vanilla Minecraft palette,
> transparent background, no anti-aliasing, sharp pixel edges."

### `shop_button_traps.png` — CMD 2002 — Catégorie "Pièges"

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a primed red TNT block with a
> burning fuse, sparkles around it, classic red and white TNT texture,
> orange glowing fuse on top, transparent background, no anti-aliasing,
> sharp pixel edges."

### `shop_button_utility.png` — CMD 2003 — Catégorie "Utilitaires"

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a wooden shield with metal
> rim and crossed iron sword behind it, blue and silver tones, hero
> emblem in the shield center, transparent background, no anti-aliasing,
> sharp pixel edges."

### `shop_button_consumables.png` — CMD 2004 — Catégorie "Consommables"

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon showing a golden apple with a
> small red health heart floating above it, warm yellow and red palette,
> transparent background, no anti-aliasing, sharp pixel edges."

### `shop_button_redstone.png` — CMD 2005 — Catégorie "Redstone"

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a redstone dust pile with
> bright red glow and a tiny electric spark, dark red and crimson palette,
> faint red outline glow, transparent background, no anti-aliasing,
> sharp pixel edges."

### `shop_button_mobility.png` — CMD 2006 — Catégorie "Mobilité"

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a white feather with light
> blue motion lines behind it suggesting fast movement, soft cyan glow,
> simple and stylized, transparent background, no anti-aliasing, sharp
> pixel edges."

### `shop_button_special.png` — CMD 2007 — Catégorie "Items Spéciaux"

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a glowing magenta four-pointed
> star with golden sparkles around it, mystical and rare feeling, pink
> and gold palette, slight purple aura, transparent background, no
> anti-aliasing, sharp pixel edges."

---

## Solde et boutons d'action

### `shop_currency.png` — `gold_nugget` CMD 2010 — Affichage du solde

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a single gold coin with a
> dollar-like '$' symbol engraved in the center, shiny yellow and
> orange shading, slight metallic gleam in the top-left, transparent
> background, no anti-aliasing, sharp pixel edges."

### `shop_buy_button.png` — `lime_dye` CMD 2020 — Bouton "Acheter"

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a bold green checkmark in a
> rounded green button, light green highlight on top, dark green
> outline, success/confirmation feel, transparent background, no
> anti-aliasing, sharp pixel edges."

### `shop_locked_button.png` — `red_dye` CMD 2021 — "Fonds insuffisants"

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a closed dark padlock with a
> small red X overlay in the bottom-right corner, gray metallic body,
> red warning accent, transparent background, no anti-aliasing, sharp
> pixel edges."

### `shop_back_button.png` — `arrow` CMD 2022 — Retour menu

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a thick left-pointing arrow
> with a clean triangular head, bright yellow color with a darker yellow
> outline, simple and readable, transparent background, no anti-aliasing,
> sharp pixel edges."

### `shop_close_button.png` — `barrier` CMD 2023 — Fermer le shop

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art icon of a bold red X cross on a
> circular dark red background, slightly thicker strokes, danger/close
> feel, transparent background, no anti-aliasing, sharp pixel edges."

---

## Décoration

### `shop_border.png` — `white_stained_glass_pane` CMD 2030 — Bordure GUI

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art tileable decorative border panel,
> ornate golden filigree on a dark blue background, intricate corner
> details, fantasy castle vibe, the edges must connect when tiled side
> by side, transparent background outside the ornament, no anti-aliasing,
> sharp pixel edges."

### `shop_logo.png` — `nether_star` CMD 2031 — Logo central

> **Prompt nano-banana :**
> "Minecraft-style 16x16 pixel art emblem showing a stylized red letter
> 'T' on a dark gradient circular badge with golden ornament around it,
> the T has a small white skull or trap motif underneath, premium
> minigame logo feel, red gold and black palette, transparent
> background, no anti-aliasing, sharp pixel edges."

---

## Pack icon

`pack.png` à la racine du resource pack — affiché dans le sélecteur de
resource pack du client. **Format : 64×64 ou 128×128 PNG**.

> **Prompt nano-banana :**
> "Square game logo, 128x128 pixels, the words 'TRAP PARTY' on two lines,
> red and white blocky pixel font with golden outline, sharp Minecraft
> pixel art aesthetic, dynamic explosive trap motif in the background
> (TNT sparks, tripwires), dark blue gradient backdrop, vibrant
> high-contrast palette, premium minigame branding."

---

## Workflow

1. Génère les 14 PNG via Nano Banana avec les prompts ci-dessus.
2. Si nécessaire, downscale à 16×16 en nearest-neighbor.
3. Sauvegarde-les dans `src/main/resources/resourcepack/assets/trapparty/textures/item/`
   en gardant les noms exacts ci-dessus.
4. Génère le `pack.png` (64×64 ou 128×128) à la racine du resource pack.
5. Rebuild : `bash scripts/build_pack.sh` → produit `trapparty-pack.zip`
   et affiche le SHA-1.
6. Upload le zip sur ton CDN, mets l'URL + SHA-1 dans
   `plugins/TrapParty/config.yml > resource-pack`.

Les joueurs reçoivent le pack automatiquement à la connexion.
