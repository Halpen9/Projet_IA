# Heuristiques Complètes - Jeu de Dames Java

## Vue d'ensemble

Le fichier IA.java implémente les 9 heuristiques.

## Les 9 Heuristiques

### 1. Material - Matériel
- Pion simple : 1 point
- Dame : 3 points
- Score brut : noir positif, blanc négatif
- Pondération : 100 pour tous les profils

### 2. Central - Contrôle du centre
- 4 cases centrales (4,4), (4,5), (5,4), (5,5) : +3 points
- Large centre (3-6, 3-6) : +1 point
- Encourage le contrôle du centre du plateau

### 3. Structure - Structure de pions
- Pion isolé : -2 points (aucun voisin diagonal ami)
- Pion soutenu : +2 points (pion ami en arrière en diagonale, boucle corrigée pour rester dans les bornes du plateau)
- Favorise les formations solides

### 4. Mobility - Mobilité
- Différence entre le nombre de coups légaux des noirs et des blancs
- Plus de mobilité = meilleure position

### 5. King Activity - Activité des dames
- Distance du bord : bonus pour les dames centrales
- Diagonales libres : +0.2 par case libre sur les 4 diagonales
- Encourage les dames actives et mobiles

### 6. Promotion - Potentiel de promotion
- Score basé sur la proximité à la dernière rangée
- Plus un pion est proche de devenir dame, plus il vaut de points
- Formule : (10 - distance_à_dernière_rangée)

### 7. Safety - Sécurité des pièces
- Pièce "pendue" : -4 points (capturable immédiatement)
- Vérifie si un adversaire peut capturer sur les 4 diagonales

### 8. Tempo - Avance des pions
- Mesure la progression générale des pions vers l'avant
- Noirs : +r (plus le pion est bas, plus le score est élevé)
- Blancs : -(9-r) (plus le pion est haut, plus le score est bas)

### 9. Locks - Positions de blocage
- Pénalise les dames dans les coins/bords : -8 points
- Positions vérifiées : (0,1), (1,0), (0,3), (3,0), (9,8), (8,9), (9,6), (6,9)
- Évite d'enterrer les dames dans des positions peu mobiles

## Profils IA

Tous les profils Minimax utilisent les 9 heuristiques avec des poids spécifiques :

### Perdant
```
material: 1, central: 1, structure: 5, mobility: 7
king_activity: 1, promotion: 1, safety: 10, tempo: 1, locks: 2
```

### Intermédiaire
```
material: 15, central: 15, structure: 15, mobility: 15
king_activity: 20, promotion: 20, safety: 10, tempo: 20, locks: 15
```

### Expert
```
material: 60, central: 25, structure: 30, mobility: 20
king_activity: 45, promotion: 45, safety: 10, tempo: 40, locks: 35
```

### Agressif
```
material: 100, central: 25, structure: 8, mobility: 35
king_activity: 95, promotion: 50, safety: 12, tempo: 20, locks: 3
```

### Défensif
```
material: 50, central: 12, structure: 45, mobility: 20
king_activity: 30, promotion: 10, safety: 50, tempo: 1, locks: 25
```

### Poids Random
```
material: [0-50], central: [0-50], structure: [0-50], mobility: [0-50]
king_activity: [0-50], promotion: [0-50], safety: [0-50], tempo: [0-50], locks: [0-50]
```
- Poids aléatoires entiers entre 0 et 50 pour chaque heuristique
- Crée une IA imprévisible avec un style différent à chaque partie
- Utile pour l'entraînement et tester la robustesse de stratégies

### Joue Random
```
material: 0, central: 0, structure: 0, mobility: 0
king_activity: 0, promotion: 0, safety: 0, tempo: 0, locks: 0
```
- Joue complètement au hasard
- Tous les poids sont 0, donc tous les coups ont le même score
- Utile pour tester la robustesse face au hasard pur

### Équilibre
```
material: 10, central: 10, structure: 10, mobility: 10
king_activity: 10, promotion: 10, safety: 10, tempo: 10, locks: 10
```

### Monte-Carlo (IA_MC)
- N'utilise aucune heuristique
- Joue par simulations aléatoires (MCTS)
- Nombre de simulations configurable : 50-2000 (défaut : 300)

## Système d'Orientation des Scores

```
private double orient(double rawValue) {
    return (myColor == 'b') ? rawValue : -rawValue;
}
```

- Score brut : toujours calculé avec noir positif, blanc négatif
- Score orienté : transformé pour que positif = bon pour l'IA
- Cela permet à l'IA de jouer correctement quelle que soit sa couleur

## Minimax

Le minimax utilise :
- Alpha-Beta pruning pour l'élagage
- Iterative deepening (profondeur 1 → maxDepth)
- Transposition table (cache vidé entre les coups)
- Move ordering : captures pondérées (×50) puis score de cache enfant pour l'itération en cours
- Choix aléatoire parmi les coups ex-æquo (évite la répétitivité)

## Statistiques de Performance

Le système affiche :
- Nodes visited : nombre total de nœuds explorés
- Cache hits : nombre de positions trouvées dans le cache
- Alpha cutoffs : élagages alpha (maximizing)
- Beta cutoffs : élagages beta (minimizing)

