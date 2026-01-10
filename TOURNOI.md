# Système de Tournoi d'IA - Jeu de Dames

## Vue d'ensemble

Le système de tournoi permet de comparer objectivement les performances des différents profils d'IA à travers des matchs automatisés avec collecte de statistiques détaillées.

## Fonctionnalités

### 1. Interface de Tournoi
- **Lancement** : Menu "Jeu" → "Tournoi d'IA" ou `java TournoiUI`
- **Interface claire** : Tableau de résultats en temps réel + journal détaillé
- **Barre de progression** : Suivi du tournoi en cours

### 2. Configuration

#### Sélection des Profils
- ✅ Perdant
- ✅ Intermédiaire
- ✅ Expert
- ✅ Agressif
- ✅ Défensif
- ✅ Poids Random
- ✅ Joue Random
- ✅ Équilibre
- ✅ Monte-Carlo

**Boutons** :
- "Tout sélectionner" - Sélectionne tous les profils
- "Tout désélectionner" - Désélectionne tous les profils

#### Options
- **Parties par match** : 1-100 parties (recommandé : 3-5 pour statistiques fiables)
- **Profondeur Minimax** : configurable dans l'interface (iterative deepening actif)
- **Simulations MCTS** : 50-2000 (défaut : 300) pour IA_MC

### 3. Format du Tournoi

**Round-robin optimisé** :
- Chaque profil joue contre tous les autres exactement 2 fois
- Une fois avec les blancs, une fois avec les noirs
- Formule : n × (n-1) / 2 matchs bidirectionnels
- Exemple : 9 profils = 9×8/2 = 36 matchs (72 parties si 2 parties/match)

**Optimisation** : Les matchs A vs B et B vs A ne sont PAS dupliqués. Chaque paire d'adversaires joue un match avec inversion des couleurs entre les parties, évitant toute redondance.

### 4. Statistiques Collectées

Le tableau affiche pour chaque profil :

| Colonne | Description |
|---------|-------------|
| **Profil** | Nom du profil |
| **Victoires** | Total de parties gagnées |
| **V. Blancs** | Victoires avec les pièces blanches |
| **V. Noirs** | Victoires avec les pièces noires |
| **Défaites** | Total de parties perdues |
| **D. Blancs** | Défaites avec les pièces blanches |
| **D. Noirs** | Défaites avec les pièces noires |
| **Nuls** | Total de parties nulles |
| **N. Blancs** | Nuls avec les pièces blanches |
| **N. Noirs** | Nuls avec les pièces noires |
| **% Victoire** | Pourcentage de victoires (Victoires / Total parties) |
| **Coups moy.** | Nombre moyen de coups par partie |
| **Nœuds moy.** | Nombre moyen de nœuds visités par coup |
| **Temps/coup (ms)** | Temps moyen par coup en millisecondes |
| **Points** | Total points (Victoire=3pts, Nul=1pt, Défaite=0pt) |

**Analyse par couleur** : Les statistiques détaillées par couleur permettent d'identifier les biais de profils (certains jouent mieux avec les blancs ou les noirs).

### 5. Système de Points
- **Victoire** : 3 points
- **Match nul** : 1 point
- **Défaite** : 0 point

Le classement final est trié par points décroissants.

### 6. Export des Résultats

#### Export Automatique
À la fin du tournoi, les résultats sont automatiquement sauvegardés dans :
```
tournoi_resultats_auto.csv
```

#### Export Manuel
Bouton "Exporter CSV" → Choisir l'emplacement et le nom du fichier

#### Format CSV
```csv
Profil,Victoires,V. Blancs,V. Noirs,Défaites,D. Blancs,D. Noirs,Nuls,N. Blancs,N. Noirs,% Victoire,Coups moy.,Nœuds moy.,Temps/coup (ms),Points
Expert,24,13,11,4,2,2,8,4,4,66.7%,58,12543,145.2,80
Agressif,20,12,8,10,4,6,6,3,3,55.6%,46,8932,98.7,66
...
```

**Gestion des caractères spéciaux** : Les valeurs contenant des virgules ou des guillemets sont automatiquement échappées selon le standard CSV (entourées de guillemets doubles).

### 7. Journal du Tournoi

Le journal affiche en temps réel :
- Configuration initiale (profils, nombre de parties)
- Chaque match en cours
- Résultat de chaque partie (vainqueur, nombre de coups)
- Messages d'état

Exemple :
```
=== DÉBUT DU TOURNOI ===
6 profils participants
3 partie(s) par match
Total: 90 parties

Match: Expert (Blancs) vs Agressif (Noirs)
  Partie 1: Victoire Expert en 52 coups
  Partie 2: Victoire Agressif en 48 coups
  Partie 3: Match nul après 87 coups
...
```

### 8. Contrôles

- **Démarrer le tournoi** : Lance le tournoi avec la configuration actuelle
- **Arrêter** : Arrête le tournoi en cours (résultats partiels conservés)
- **Exporter CSV** : Sauvegarde les résultats dans un fichier

### 9. Métriques Analysées

#### Qui gagne le plus ?
→ Colonne **Victoires** et **Points**

#### Qui gagne le plus vite ?
→ Colonne **Coups moy.** (plus bas = plus rapide)

#### Qui joue le plus vite ?
→ Colonne **Temps/coup (ms)** (plus bas = plus rapide)

#### Profil le plus équilibré ?
→ Ratio Victoires/Défaites élevé avec peu de nuls

## Utilisation Recommandée

### Comparaison Rapide (3-5 minutes)
```
- 2-3 profils sélectionnés
- 2 partie par match
```

### Évaluation Complète (15-30 minutes)
```
- Tous les profils sélectionnés
- 4 parties par match
```

### Analyse Approfondie (1-2 heures)
```
- Tous les profils sélectionnés
- 5-10 parties par match
```

## Analyse des Résultats

### Profil Optimal
Le profil avec le meilleur score global (points) est considéré comme le plus performant.

### Profil Rapide
Regarder **Temps/coup (ms)** :

### Profil Efficace
Regarder **Coups moy.** :
- Moins de coups = style plus agressif/efficace
- Plus de coups = style plus positionnel

### Profil Équilibré
Regarder le ratio **Victoires/Défaites** :
- Peu de nuls = style décisif
- Beaucoup de nuls = style prudent

### Règles de Nullité (Règles Internationales)

Le tournoi applique les règles officielles de la FMJD :

1. **Répétition de position (3 fois)** : si la même position se répète 3 fois, la partie est nulle
2. **Règle des 25 coups** : 25 coups consécutifs avec uniquement des dames et sans capture ni mouvement de pion → nulle
3. **Limite du tournoi (400 coups)** : si rien de tout cela n'arrive après 400 coups, la partie est déclarée nulle

Ces règles évitent les parties infinies et reflètent les standards du jeu de dames international.

## Limitations Techniques

- **Profondeur IA** : configurable dans l'UI, iterative deepening active (cache vidé entre les coups)
- **MCTS simulations** : paramétrable, valeur par défaut 300
- **Pas d'ouvertures** : toutes les parties démarrent de la position initiale
- **Pas de livre d'ouvertures** : chaque profil calcule depuis la position de départ

## Fichiers Générés

```
tournoi_resultats_auto.csv  - Export automatique à la fin
tournoi_resultats.csv       - Export manuel (nom par défaut)
```

## Conseils d'Analyse

1. **Plusieurs tournois** : Lancez plusieurs tournois pour confirmer les tendances
2. **Variations** : Modifiez le nombre de parties pour voir la stabilité
3. **CSV dans Excel** : Ouvrez les CSV dans Excel/LibreOffice pour graphiques
4. **Comparaison temporelle** : Gardez les CSV pour comparer avant/après modifications

## Exemple de Résultats Attendus

**Classement type (9 profils, 2 parties/match)** :

1. **Expert** - Force maximale, équilibré
2. **Poids Random** - Comportement variable selon tirage
3. **Défensif** - Beaucoup de nuls, très solide
4. **Équilibre** - Bon ratio victoires/défaites
5. **Joue Random** - Aléatoire, très variable
6. **Agressif** - Décisif mais risqué
7. **Intermédiaire** - Performance correcte
8. **Monte-Carlo** - Variable selon simulations
9. **Perdant** - Faible mais très rapide

## Intégration

Le tournoi est accessible depuis :
- **GameUI** : Menu "Jeu" → "Tournoi d'IA"
- **Standalone** : `java TournoiUI`

Les deux fenêtres peuvent être ouvertes simultanément.
