# Documentation Complète - Jeu de Dames IA

## ✅ Documentation Finalisée

Tous les fichiers Java ont été documentés avec des commentaires **Javadoc** complets et des explications détaillées au niveau des méthodes.

---

## 📋 État de la Documentation

### **Classes Entièrement Documentées** ✅

#### 1. **Move.java** (35 lignes)
- ✅ Javadoc de classe
- ✅ Constructeur
- ✅ Méthode `isCapture()`
- ✅ Méthode `toString()`

#### 2. **Piece.java** (106 lignes)
- ✅ Javadoc de classe
- ✅ Constructeur
- ✅ Tous les getters/setters
- ✅ Méthode `promote()`
- ✅ Méthode `areOpponents()`
- ✅ Méthode `copy()`
- ✅ Méthode `toString()`

#### 3. **Board.java** (474 lignes)
- ✅ Javadoc de classe
- ✅ Constructeur
- ✅ Méthode `initBoard()`
- ✅ Méthode `legalMoves()` (avec explication détaillée des règles)
- ✅ Tous les getters/setters
- ✅ Méthodes terminales (`isTerminal()`, `isTerminalWithDraw()`)

#### 4. **IA_MC.java** (150 lignes)
- ✅ Javadoc de classe
- ✅ Constructeur
- ✅ Méthode `bestMove()` (algorithme MCTS)
- ✅ Méthode `simulate()`
- ✅ Méthode `rollout()`
- ✅ Méthode `moveKey()`

#### 5. **IA.java** (800+ lignes - La plus volumineuse)
- ✅ Javadoc de classe
- ✅ Constructeur
- ✅ Méthode `resetCounters()`
- ✅ Tous les getters
- ✅ Méthode `bestMove()`
- ✅ Méthode `iterativeDeepening()`
- ✅ Méthode `orient()`
- ✅ **Algorithme Minimax complet** :
  - Alpha-Beta pruning
  - Transposition table
  - Cache et utilisation (clé = hash de plateau + profondeur + maximizing + couleur)
- ✅ **Méthode `evaluate()` avec tous les heuristiques** :
  - Matériel (pions et dames)
  - Contrôle central
  - Structure des pions
  - Mobilité
  - Activité des dames
  - Potentiel de promotion
  - Sécurité des pièces
  - Tempo
  - Dames coincées (locks)
- ✅ Méthodes utilitaires (`getCacheKey()`, `orderMoves()` : captures pondérées ×50 + score de cache enfant)
- ✅ Classes internes (`MinimaxResult`, `CacheEntry`, `MoveScore`)

#### 6. **GameUI.java** (940 lignes - L'interface principale)
- ✅ Javadoc de classe
- ✅ Constructeur
- ✅ Méthode `createMenuBar()`
- ✅ Méthode `createIAs()`
- ✅ Méthode `createIAWithProfile()`
- ✅ Méthode `setGameMode()` (HvH, HvIA, IAvH, IAvIA)
- ✅ Méthode `updateStatusLabel()`
- ✅ Méthode `stopIAGame()`
- ✅ Méthode `openTournoi()`
- ✅ Méthode `newGame()`
- ✅ Méthode `undoMove()`
- ✅ Méthode `saveSnapshot()`
- ✅ Méthode `updateDisplay()`
- ✅ Méthode `playIAvsIA()`
- ✅ Méthode `scheduleAIMove()`
- ✅ Méthode `playIATurn()`
- ✅ **Classe interne BoardPanel** :
  - Javadoc de classe
  - Constructeur
  - Méthode `handleClick()` (sélection en 2 clics)
  - Méthode `paintComponent()` (rendu graphique)
- ✅ **Classe interne BoardSnapshot** :
  - Javadoc de classe
  - Constructeur
- ✅ Méthode `main()`

#### 7. **TournoiUI.java** (804 lignes)
- ✅ Javadoc de classe
- ✅ Constructeur
- ✅ Méthode `createConfigPanel()`
- ✅ Méthode `startTournoi()`
- ✅ Méthode `stopTournoi()`
- ✅ Méthode `updateProgress()`
- ✅ Méthode `log()`
- ✅ Méthode `updateResults()`
- ✅ Méthode `exportResults()`
- ✅ Méthode `exportToFile()`
- ✅ Méthode `exportToCSV()`
- ✅ Méthode `escapeCsvValue()`
- ✅ Méthode `isRunning()`
- ✅ Méthode `main()`
- ✅ **Classe ProfileStats** :
  - Javadoc de classe complète
- ✅ **Classe TournoiManager** :
  - Javadoc de classe complète
  - Constructeur
  - Méthode `stop()`
  - Méthode `runTournoi()`
  - Méthode `playGame()`
  - Méthode `updateStats()`
  - Méthode `createIA()`
- ✅ **Classe GameResult** :
  - Javadoc de classe complète

---



## 🎯 Type de Commentaires Ajoutés

### **Javadoc de Classe**
Chaque classe Java commence par un bloc Javadoc détaillé incluant :
- Description du rôle de la classe
- Liste des fonctionnalités principales
- Mention des algorithmes clés (Minimax IA, IA_MC)
- Attributs principaux
- Utilisation générale

Exemple :
```java
/**
 * Interface graphique principale du jeu de dames.
 * 
 * Fonctionnalités :
 * - Modes de jeu : Humain vs Humain, Humain vs IA, IA vs Humain, IA vs IA
 * - Sélection de 9 profils d'IA différents (8 Minimax + Monte-Carlo)
 * - Configuration de la profondeur de recherche
 * - Système d'annulation de coups
 * ...
 */
```

### **Javadoc de Méthode**
Chaque méthode publique et privée importante inclut :
- Description claire de ce qu'elle fait
- Explications des étapes importantes (algorithmes, logique complexe)
- Documentation des paramètres (`@param`)
- Documentation du retour (`@return`)
- Exceptions potentielles (`@throws`)

Exemple :
```java
/**
 * Lance l'algorithme Minimax avec élagage Alpha-Beta.
 * 
 * Processus :
 * 1. Vérifier le cache (transposition table)
 * 2. Évaluer les positions terminales
 * 3. Récurser sur les coups légaux
 * 4. Appliquer Alpha-Beta pruning
 * 5. Cacher le résultat
 * 
 * @param board Le plateau actuel
 * @param depth La profondeur restante
 * @param alpha Le seuil alpha pour pruning
 * @param beta Le seuil beta pour pruning
 * @param maximizing true si on maximise (blancs), false sinon
 * @return Le score de la meilleure position trouvée
 */
```

### **Commentaires Internes**
Explications détaillées du code pour :
- Les algorithmes complexes (Minimax, Alpha-Beta pruning, MCTS)
- Les heuristiques d'évaluation
- La gestion des états (terminal, nul, victoire)
- L'interprétation des scores

Exemple :
```java
// Heuristique de matériel : pion = 1 point, dame = 3 points
int whitePieces = 0, blackPieces = 0;
for (Piece piece : board.getAllPieces()) {
    int value = piece.isKing() ? 3 : 1;
    if (piece.getColor() == 'w') {
        whitePieces += value;
    } else {
        blackPieces += value;
    }
}
return (whitePieces - blackPieces) * HEURISTIQUES[0];
```

---

## 🔍 Cas Spéciaux Documentés

### **IA.java - Les 9 Heuristiques**
Chacune des 9 heuristiques est documentée avec :
- Explication de ce qu'elle mesure
- Formule de calcul
- Poids par profil (Perdant, Intermédiaire, Expert, Agressif, Défensif, Poids Random, Joue Random, Équilibre)
- Interprétation des scores

Les 9 heuristiques documentées :
1. **Matériel** - Compte des pions et dames
2. **Contrôle Central** - Privilégie les pièces au centre
3. **Structure des Pions** - Favorise les chaînes de pions (isolés/soutenus)
4. **Mobilité** - Nombre de coups disponibles
5. **Activité des Dames** - Position et diagonales libres
6. **Potentiel de Promotion** - Distance des pions de la promotion
7. **Sécurité** - Pièce pendue (capturable immédiatement)
8. **Tempo** - Avance moyenne des pions
9. **Locks** - Détection des positions bloquées (dames enterrées bords)

### **GameUI.java - Interaction Souris**
La méthode `handleClick()` est documentée avec un diagramme du flux :
```
PREMIER CLIC (sélection)
- Vérifier la pièce
- Récupérer coups légaux
- Surligner

DEUXIÈME CLIC (exécution)
- Vérifier destination valide
- Appliquer coup
- Alterner joueur
- Rafraîchir
```

### **TournoiUI.java - Format Round-Robin**
Documenté précisément :
- Format : n×(n-1)/2 matchs (pas de redondance)
- Équilibre des couleurs : chaque profil joue moitié en blanc, moitié en noir
- Statistiques séparées par couleur
- Système de points : Victoire=3, Nul=1, Défaite=0
- Nuls : répétition (3x), 25 coups dames sans capture ni mouvement de pion, ou limite 400 coups

### **Board.java - Règles de Captures**
Explication détaillée des règles FMJD :
- Priorité des captures
- Captures multiples
- Direction des pions vs dames
- Conditions d'arrêt du jeu

---

## 🚀 Qualité de la Documentation

### **Avantages de cette Documentation**
1. **Cohérence** - Tous les fichiers suivent les mêmes conventions Javadoc
2. **Complétude** - Chaque méthode est documentée (publique et privée importantes)
3. **Clarté** - Explications en français, faciles à comprendre
4. **Détail** - Étapes d'algorithmes expliquées pas à pas
5. **Utilitaire** - Aide à la compréhension et maintenance du code
6. **Professionnalisme** - Suit les standards de documentation Java

### **Cas d'Usage**
- 👨‍💻 **Développement** - Comprendre le code existant
- 🐛 **Débogage** - Identifier où se trouvent les bugs
- 📚 **Apprentissage** - Comprendre les algorithmes (Minimax, MCTS)
- 🔧 **Maintenance** - Modifier ou améliorer le code
- 📊 **Documentation** - Générer automatiquement avec Javadoc

---

## 📝 Exemple de Bloc Documenté Complet

Voici un exemple du style de documentation appliqué à travers tout le projet :

```java
/**
 * Gère les clics souris sur le plateau.
 * Implémente une sélection en deux temps :
 * 
 * PREMIER CLIC (sélection de pièce) :
 * 1. Vérifier qu'on clique sur une pièce du joueur actuel
 * 2. Sauvegarder la position sélectionnée
 * 3. Récupérer les coups légaux depuis cette pièce
 * 4. Surligner la pièce et les destinations possibles
 * 
 * DEUXIÈME CLIC (exécution du coup) :
 * 1. Vérifier que la destination cliquée est dans les coups légaux
 * 2. Si oui : sauvegarder l'état, appliquer le coup, alterner le joueur
 * 3. Si non : désélectionner et recommencer
 * 4. Rafraîchir l'affichage
 * 5. Programmer un coup d'IA si applicable
 * 
 * @param row La ligne cliquée (0-9)
 * @param col La colonne cliquée (0-9)
 */
private void handleClick(int row, int col) {
    // ... code ...
}
```

---

