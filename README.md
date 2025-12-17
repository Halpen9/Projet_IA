# Jeu de Dames - Version Java Complète

Ce projet est une implémentation Java complète du jeu de dames (10x10) avec une IA utilisant l'algorithme Minimax avec élagage Alpha-Beta et une alternative Monte Carlo Tree Search (MCTS).

## Structure du projet

```
java/Dame/
├── Piece.java          # Représentation d'une pièce (pion ou dame)
├── Move.java           # Représentation d'un mouvement
├── Board.java          # Plateau de jeu et logique des règles (avec règles d'égalité)
├── IA.java             # Intelligence artificielle (Minimax + Alpha-Beta + 9 heuristiques pondérées)
├── IA_MC.java          # IA Monte Carlo  (simulations aléatoires)
├── GameUI.java         # Interface graphique complète (Swing) - modes HvH, HvIA, IAvsH, IAvsIA
└── TournoiUI.java   # Système de tournoi automatisé avec statistiques et export CSV
```

## Fonctionnalités

- **Règles du jeu de dames internationales (10x10)**
  - Déplacement des pions en diagonale
  - Prise obligatoire avec prise maximale
  - Promotion en dame sur la dernière rangée
  - Déplacement des dames sur plusieurs cases
  - **Règles d'égalité** :
    * Position répétée 3 fois
    * 25 coups de dames consécutifs sans prise ni mouvement de pion
    * Limite de 400 coups dans les tournois

- **Modes de jeu**
  - Humain vs Humain
  - Humain (Blancs) vs IA (Noirs)
  - IA (Blancs) vs Humain (Noirs)
  - IA vs IA (autoplay configurable)

- **Profils d'IA Minimax (8 profils + Monte-Carlo)**
  - **Perdant** : Style d'entraînement/"faible" : priorités mal calibrées.
    Survalorise des aspects secondaires, sous-valorise matériel/centre,initiative
  - **Intermédiaire** : Équilibré, comprend les bases tactiques
  - **Expert** : Très fort, balance parfaite entre tactique et stratégie
  - **Agressif** : Sacrifie la sécurité pour l'attaque et l'initiative
  - **Défensif** : Sécurité maximale, structure solide, contre-attaque
  - **Poids Random** : Poids aléatoires [0-50] pour chaque heuristique
  - **Joue Random** : Joue complètement au hasard (tous poids = 0)
  - **Équilibre** : Joue selon les heuristiques brutes
  - **Monte-Carlo** : MC avec 300 simulations par défaut (50-2000 configurable)

- **Configuration avancée**
  - Profondeur de recherche ajustable (1-8) pour chaque couleur
  - Choix du profil indépendant pour les Blancs et les Noirs
  - Affichage des statistiques de jeu (nombre de pièces)

- **Intelligence Artificielle Minimax**
  - Algorithme Minimax avec élagage Alpha-Beta
  - Table de transposition pour mémoriser les positions évaluées
  - Recherche itérative approfondie (Iterative Deepening)
  - Ordonnancement des coups : priorité aux captures (×50) + scores du cache des positions enfants
  - Compteurs de performance (nœuds visités, cache hits, coupures alpha/beta)

- **Intelligence Artificielle MC**
  - Monte Carlo avec simulations aléatoires
  - 300 parties simulées par coup
  - Aucune heuristique, évaluation par résultats statistiques
  - Meilleur pour les positions tactiques complexes

- **Évaluation heuristique (Minimax) - 9 heuristiques avec poids variables par profil**
  1. **Matériel** : Différence de pièces (pion=1, dame=3)
  2. **Contrôle central** : Bonus pour pièces au centre (4 cases) ou large-centre (16 cases)
  3. **Structure de pions** : Malus pions isolés (-2), bonus pions soutenus (+2)
  4. **Mobilité** : Différence du nombre de coups légaux (noirs - blancs)
  5. **Activité dames** : Distance au bord + nombre de cases libres sur les diagonales
  6. **Potentiel de promotion** : Proximité des pions à la dernière rangée
  7. **Sécurité des pièces** : Pénalité pour pièces pendues/capturable (-4)
  8. **Tempo** : Avance des pions vers la promotion
  9. **Blocages** : Pénalité pour dames coincées en coin (-8)
  - Poids ajustés selon le profil (voir [HEURISTIQUES.md](HEURISTIQUES.md))

- **Système de Tournoi**
  - Interface dédiée pour comparer les profils IA
  - Tournoi round-robin optimisé (chaque paire joue 1 fois avec couleurs équilibrées)
  - Statistiques détaillées : victoires par couleur, nuls par couleur, nœuds moyens
  - Export CSV avec échappement correct des valeurs
  - Configuration : profondeur Minimax (1-8), simulations MC (50-2000)
  - Limite 400 coups par partie

- **Fonctionnalités utilisateur**
  - Annulation de coups (Ctrl+Z)
  - Arrêt des parties IA vs IA en cours
  - Affichage graphique amélioré avec ombres et couronnes dorées
  - Coordonnées affichées sur le plateau
  - Cases surlignées (jaune = sélection, vert = coups possibles)

## Récupération du projet (GitHub)

### Cloner le dépôt
```bash
git clone "Lien du repo"
cd votre-dossier-dames/java/Dame
```

> Remplacez "Lien du repo" par l'URL réelle du dépôt GitHub.

## Compilation et Exécution

### Prérequis
- Java Development Kit (JDK) 8 ou supérieur

### Compilation
```bash
cd "[L'emplacement du dossier]"
javac *.java
```

### Exécution
```bash
java GameUI
```

## Utilisation

### Menus

1. **Menu Jeu**
   - Nouvelle partie : Recommencer
   - Humain vs Humain : Les deux joueurs sont humains
   - Humain (Blancs) vs IA (Noirs) : Vous jouez les Blancs
   - IA (Blancs) vs Humain (Noirs) : Vous jouez les Noirs
   - IA vs IA : Match automatique entre deux IA
   - Arrêter IA vs IA : Stopper le match en cours
   - Annuler (Ctrl+Z) : Annuler le dernier coup
   - Quitter : Fermer l'application

2. **Menu Profil Blancs**
  - Sélectionner le profil de l'IA qui joue les Blancs
  - Choix : Perdant, Intermédiaire, Expert, Agressif, Défensif, Poids Random, Joue Random, Équilibre, Monte-Carlo

3. **Menu Profil Noirs**
  - Sélectionner le profil de l'IA qui joue les Noirs
  - Choix : Perdant, Intermédiaire, Expert, Agressif, Défensif, Poids Random, Joue Random, Équilibre, Monte-Carlo

4. **Menu Profondeur**
   - **Blancs** : Profondeur 1-8 pour l'IA des Blancs
   - **Noirs** : Profondeur 1-8 pour l'IA des Noirs

### Jouer

1. **Cliquer sur une pièce** pour la sélectionner (surbrillance jaune)
2. **Cliquer sur une case verte** pour effectuer le mouvement
3. **Annuler** : Menu Jeu → Annuler ou Ctrl+Z

## Architecture du code

### Classe `Piece`
- Représente une pièce avec son type ('w', 'b', 'W', 'B')
- Méthodes pour vérifier le type (blanc/noir, pion/dame)
- Gestion de la promotion

### Classe `Board`
- Grille 10x10 représentant le plateau
- Génération des mouvements légaux avec captures multiples
- Application et annulation des coups (pour Minimax)
- **Règles d'égalité internationales** :
  * Historique des positions (détection répétition 3×)
  * Compteur coups sans prise/pion (règle 25 coups)
  * `isTerminal()` : vérifie coups légaux uniquement (pour minimax)
  * `isTerminalWithDraw()` : vérifie coups + règles égalité (jeu réel)
- Détection de fin de partie
- Comptage des pièces

### Classe `IA`
- Implémente l'algorithme Minimax avec Alpha-Beta Pruning
- Table de transposition : mémorise les positions (clé = board.hashCode + depth + maximizing + color)
- Iterative Deepening : recherche progressivement à profondeur 1, 2, 3...maxDepth
- Ordonnancement des coups : priorité captures (×50) + scores du cache des enfants
- Profils avec poids différents pour les 9 heuristiques
- Compteurs de performance : nodesVisited, cacheHits, alphaCutoffs, betaCutoffs

### Classe `IA_MC`
- Monte Carlo Tree Search (MCTS) : simulations aléatoires de parties complètes
- Génère le meilleur coup selon le taux de victoire statistique
- Aucune heuristique d'évaluation, purement basée sur résultats concrets
- Configuration : nombre de simulations (défaut 300)

### Classe `GameUI`
- Interface graphique avec Swing
- Gestion des interactions utilisateur
- Modes de jeu multiples avec configuration complète
- Visualisation des coups possibles
- Système d'annulation (undo)
- Affichage des statistiques
- Timer pour les parties IA vs IA

### Classe `TournoiUI`
- Interface Swing dédiée aux tournois entre profils d'IA
- Sélection des profils participants (9 disponibles) via cases à cocher, sélecteurs "Tout sélectionner/Désélectionner"
- Options de tournoi: parties par match (pair, 2-100), profondeur Minimax (1-8), simulations Monte-Carlo (50-2000)
- Contrôles: démarrer/arrêter, barre de progression, journal en temps réel
- Tableau des résultats: victoires/défaites/nuls (totaux et par couleur), % victoire, coups moyens, temps moyen par coup, nœuds moyens, points
- Export CSV manuel (dialogue) et export automatique en fin de tournoi vers `tournoi_resultats_auto.csv`
- Organisation round-robin optimisée: chaque paire joue une seule fois avec couleurs équilibrées
- Points: victoire=3, nul=1, défaite=0

## Optimisations

1. **Alpha-Beta Pruning** : Réduit l'espace de recherche de O(b^d) à O(b^(d/2))
2. **Table de transposition** : Cache les positions évaluées, réutilisable si (board, depth, maximizing, color) identique
3. **Move Ordering** : Teste d'abord captures (×50) et coups en cache → maximise les coupures
4. **Iterative Deepening** : Recherches à prof 1, 2, 3... enrichissent le cache et l'ordre des coups pour les itérations suivantes
5. **MCTS** : Alternative sans heuristique, basée sur simulations statistiques

## Performance

L'IA affiche des statistiques de performance dans la console :
- **Nœuds visités** : nombre total de positions explorées
- **Cache hits** : nombre de positions récupérées du cache (économie de calcul)
- **Coupures Alpha** : nombre de branches élaguées au niveau maximisant
- **Coupures Beta** : nombre de branches élaguées au niveau minimisant
- **Temps** : durée de la recherche en millisecondes

Exemple console :
```
IA Stats - Nodes: 12543, Cache hits: 3421, Alpha cutoffs: 1234, Beta cutoffs: 987
Blancs joue en 234ms
```

Ratio élagage = (nodesVisited) / (b^depth théorique) :
- Sans optimisation : ~1 (explore l'arbre complet)
- Avec Alpha-Beta seul : ~0.5 à 0.8
- Avec Alpha-Beta + Move Ordering + Cache : 0.1 à 0.2 (10-20× plus rapide)

## Comparaison des profils

| Profil | Style | Force | Caractéristiques |
|--------|-------|-------|------------------|
| Perdant | Matériel simple | ⭐ | Sous-estime les heuristiques puissantes |
| Intermédiaire | Équilibré basique | ⭐⭐⭐| Comprend les bases tactiques |
| Expert | Équilibre parfait | ⭐⭐⭐⭐ | Balance tactique/stratégie |
| Agressif | Attaque/initiative | ⭐⭐⭐⭐| Sacrifices, tempo élevé |
| Défensif | Sécurité maximale | ⭐⭐ | Structure solide, contre-attaque |
| Poids Random | Aléatoire pondéré | ⭐⭐⭐ | Style variable chaque partie |
| Joue Random | Hasard pur | ⭐ | Aléatoire complet, pas de stratégie |
| Équilibre | Universel | ⭐⭐⭐ | Joue selon les heuristiques brutes |
| Monte-Carlo | Statistique | ⭐⭐⭐ | Sans heuristique, simulations |


## Améliorations possibles

1. **Zobrist Hashing** : Hash incrémental plus rapide
2. **Bibliothèque d'ouvertures** : Mémoriser les meilleurs premiers coups
3. **Rajout de machine learning ou renforcement** :  Adapter la stratégie

## Auteur

- **Développeur** : Ulysse Ansoux, Noé Vander Schueren et Léo Maquin-Testud
- **Projet** : Jeu de Dames - IA Minimax et MC
- **Version** : 1.0
- **Date** : Décembre 2025
- **Règles** : Jeu de dames international 10×10 (FMJD)

## Licence

Ce projet est fourni à titre éducatif.