import java.util.*;

/**
 * Intelligence artificielle pour le jeu de dames utilisant l'algorithme Minimax
 * avec élagage Alpha-Beta, table de transposition et recherche itérative approfondie.
 * 
 * Supporte 8 profils de jeu différents :
 * - Perdant : Joue faiblement, privilégie mal ses priorités
 * - Intermédiaire : Équilibré, comprend les bases tactiques
 * - Expert : Très fort, équilibre parfait entre tactique et stratégie
 * - Agressif : Sacrifie la sécurité pour l'attaque et l'initiative
 * - Défensif : Sécurité maximale, structure solide
 * - Poids Random : Poids aléatoires entiers [0-50] pour chaque heuristique
 * - Joue Random : Joue complètement au hasard (tous poids = 0)
 * - Équilibre : Tout les poids à l'équilibre (10.0)
 * 
 * Utilise 9 heuristiques avec pondérations spécifiques par profil :
 * material, central, structure, mobility, Dame_activity, promotion, safety, tempo, locks
 * 
 * @author Ulysse Ansoux
 * @author Noé Vander Schueren
 * @author Léo Maquin-Testud
 * @version 1.0
 */
public class IA {
    /** Couleur de l'IA ('w' ou 'b') */
    private char myColor;
    /** Profondeur maximale de recherche */
    private int maxDepth;
    /** Table de transposition pour mémoriser les positions évaluées */
    private Map<String, CacheEntry> transpositionTable;
    /** Générateur de nombres aléatoires pour choisir entre coups égaux */
    private Random random;
    
    /** Nombre de nœuds visités pendant la recherche */
    private int nodesVisited;
    /** Nombre de hits dans le cache */
    private int cacheHits;
    /** Nombre de coupures alpha */
    private int alphaCutoffs;
    /** Nombre de coupures beta */
    private int betaCutoffs;
    
    /** Poids des heuristiques selon le profil sélectionné */
    private Map<String, Double> weights;
    
    // Zones centrales
    private static final Set<String> CENTER = new HashSet<>(Arrays.asList("4,4", "4,5", "5,4", "5,5"));
    private static final Set<String> WIDE_CENTER = new HashSet<>();
    static {
        for (int r = 3; r < 7; r++) {
            for (int c = 3; c < 7; c++) {
                WIDE_CENTER.add(r + "," + c);
            }
        }
    }
    
    /**
     * Constructeur simplifié de l'IA avec profil par défaut.
     * 
     * @param myColor Couleur de l'IA ('w' ou 'b')
     * @param maxDepth Profondeur maximale de recherche
     */
    public IA(char myColor, int maxDepth) {
        this(myColor, maxDepth, "Expert"); // Utilise le profil Expert par défaut
    }
    
    /**
     * Constructeur complet de l'IA avec sélection du profil.
     * 
     * @param myColor Couleur de l'IA ('w' ou 'b')
     * @param maxDepth Profondeur maximale de recherche 
     * @param profile Profil de jeu parmi les 8 styles disponibles
     */
    public IA(char myColor, int maxDepth, String profile) {
        // Paramètres de base
        this.myColor = myColor;
        this.maxDepth = maxDepth;
        // Table de transposition pour cacher les résultats d'evaluations
        this.transpositionTable = new HashMap<>();
        // Générateur aléatoire pour résoudre les égalités
        this.random = new Random();
        // Charger les poids heuristiques du profil sélectionné
        this.weights = getProfileWeights(profile);
        // Initialiser les compteurs de performance
        resetCounters();
    }
    
    /**
     * Charge les poids des heuristiques pour un profil donné.
     * Les poids déterminent le style de jeu et la priorité accordée à chaque aspect du jeu.
     * Chaque profil représente un style d'IA différent (inspiré des grands maîtres d'échecs).
     * 
    * @param profile Nom du profil ('Perdant', 'Expert', 'Agressif', etc.)
     * @return Une map contenant les poids des 9 heuristiques
     */
    private Map<String, Double> getProfileWeights(String profile) {
        Map<String, Double> w = new HashMap<>();
        
        // Chaque profil a une stratégie différente avec des poids distinctifs
        if (profile=="Perdant"){
            // Style d'entraînement/"faible" : priorités mal calibrées
            // Survalorise des aspects secondaires, sous-valorise matériel/centre/initiative
            w.put("material", 1.0);          // Sous-évalue le matériel
            w.put("central", 1.0);           // Peu de contrôle du centre
            w.put("structure", 5.0);         // Survalorise la structure de pions passivement
            w.put("mobility", 7.0);          // Valorise la mobilité sans plan clair
            w.put("Dame_activity", 1.0);     // Dames peu actives
            w.put("promotion", 1.0);         // Pousse rarement à la promotion
            w.put("safety", 10.0);           // Privilégie trop la sécurité
            w.put("tempo", 1.0);             // Joue lentement (peu d'initiative)
            w.put("locks", 2.0);             // Peu sensible aux positions bloquées/coins
        }

        else if(profile=="Intermédiaire") {
            // Équilibré, comprend les bases tactiques
            // Bons principes : centre, matériel, activité et initiative modérées
            w.put("material", 15.0);         // Valeur correcte du matériel
            w.put("central", 15.0);          // Contrôle central raisonnable
            w.put("structure", 15.0);        // Structure de pions soignée
            w.put("mobility", 15.0);         // Mobilité équilibrée
            w.put("Dame_activity", 20.0);    // Sait activer les dames
            w.put("promotion", 20.0);        // Cherche la promotion quand possible
            w.put("safety", 10.0);           // Sécurité raisonnable
            w.put("tempo", 20.0);            // Prend l'initiative sans excès
            w.put("locks", 15.0);            // Évite les positions de coin/bloquées
        }

        else if(profile=="Expert") {
            // Très fort : équilibre tactique/stratégie, priorités robustes
            // Forte valeur du matériel, activation des dames et pression constante
            w.put("material", 60.0);         // Priorise fortement le matériel
            w.put("central", 25.0);          // Bon contrôle du centre
            w.put("structure", 30.0);        // Structure solide et flexible
            w.put("mobility", 20.0);         // Mobilité suffisante sans surpondérer
            w.put("Dame_activity", 45.0);    // Dames très actives
            w.put("promotion", 45.0);        // Valorise grandement la promotion
            w.put("safety", 10.0);           // Accepte des risques calculés
            w.put("tempo", 40.0);            // Forte initiative/pression
            w.put("locks", 35.0);            // Évite les positions enterrées/coins
        }
            
        else if (profile=="Agressif") {
            // Sacrifie la sécurité pour l'attaque et l'initiative
            // Pousse les pions, active les dames rapidement
            w.put("material", 100.0);
            w.put("central", 25.0);        // ↑ Contrôle le centre agressivement
            w.put("structure", 8.0);       // ↓ Accepte de casser sa structure
            w.put("mobility", 35.0);       // ↑ Veut beaucoup d'options d'attaque
            w.put("Dame_activity", 95.0);  // ↑ Dames très actives
            w.put("promotion", 50.0);      // ↑ Force la promotion
            w.put("safety", 12.0);         // ↓ Prend des risques
            w.put("tempo", 20.0);          // ↑ Joue vite, gagne du tempo
            w.put("locks", 15.0);          // ↑ Evite de bloquer les Dames
        }
            
        else if (profile=="Défensif") {
            // Sécurité maximale, structure solide, contre-attaque
            w.put("material", 50.0);
            w.put("central", 12.0);        // Contrôle modéré
            w.put("structure", 45.0);      // ↑ Structure impeccable
            w.put("mobility", 20.0);       // ↑ Garde des options de repli
            w.put("Dame_activity", 30.0);  // Dames moins exposées
            w.put("promotion", 10.0);      // ↓ Pas pressé de promouvoir
            w.put("safety", 50.0);         // ↑ Sécurité maximale
            w.put("tempo", 1.0);           // ↓ Joue lentement et sûrement
            w.put("locks", 25.0);          // ↑ Evite les blocages
        }
            
        else if (profile=="Poids Random") {
            // Poids aléatoires entiers entre 0 et 50 pour chaque heuristique
            // Crée une IA imprévisible avec un style différent à chaque partie
            Random rand = new Random();
            w.put("material", (double) rand.nextInt(51));      // [0-50]
            w.put("central", (double) rand.nextInt(51));       // [0-50]
            w.put("structure", (double) rand.nextInt(51));     // [0-50]
            w.put("mobility", (double) rand.nextInt(51));      // [0-50]
            w.put("Dame_activity", (double) rand.nextInt(51)); // [0-50]
            w.put("promotion", (double) rand.nextInt(51));     // [0-50]
            w.put("safety", (double) rand.nextInt(51));        // [0-50]
            w.put("tempo", (double) rand.nextInt(51));         // [0-50]
            w.put("locks", (double) rand.nextInt(51));         // [0-50]
        }
            
        else if (profile=="Joue Random") {
            // Joue complètement au hasard : tous les coups ont le même score
            w.put("material", 0.0);
            w.put("central", 0.0);
            w.put("structure", 0.0);
            w.put("mobility", 0.0);
            w.put("Dame_activity", 0.0);
            w.put("promotion", 0.0);
            w.put("safety", 0.0);
            w.put("tempo", 0.0);
            w.put("locks", 0.0);
        }
            
        else if (profile=="Équilibre") {
            // Même poids pour toutes les heuristiques : jeu équilibré
            w.put("material", 10.0);
            w.put("central", 10.0);
            w.put("structure", 10.0);
            w.put("mobility", 10.0);
            w.put("Dame_activity", 10.0);
            w.put("promotion", 10.0);
            w.put("safety", 10.0);
            w.put("tempo", 10.0);
            w.put("locks", 10.0);
        }
            
        else {
            return getProfileWeights("Expert");
    }
    
    return w;
}
    
    /**
     * Réinitialise tous les compteurs de performance.
     * Appelé au début de chaque recherche pour avoir des évaluations propres.
     */
    public void resetCounters() {
        // Nombre total de nœuds visités dans l'arbre Minimax
        nodesVisited = 0;
        // Nombre de positions récupérées du cache (accélérations)
        cacheHits = 0;
        // Nombre de coupures alpha effectuées
        alphaCutoffs = 0;
        // Nombre de coupures bêta effectuées
        betaCutoffs = 0;
    }
    
    /**
     * Retourne le nombre de nœuds visités.
     * @return Compteur de nœuds visités
     */
    public int getNodesVisited() { 
        return nodesVisited; 
    }
    
    /**
     * Retourne le nombre de hits du cache.
     * @return Compteur de hits du cache
     */
    public int getCacheHits() { 
        return cacheHits; 
    }
    
    /**
     * Retourne le nombre de coupures alpha.
     * @return Compteur de coupures alpha
     */
    public int getAlphaCutoffs() { 
        return alphaCutoffs; 
    }
    
    /**
     * Retourne le nombre de coupures bêta.
     * @return Compteur de coupures bêta
     */
    public int getBetaCutoffs() { 
        return betaCutoffs; 
    }
    
    /**
     * Détermine le meilleur coup à jouer dans la position actuelle.
     * Utilise la recherche itérative approfondie pour explorer progressivement de plus en plus profond.
     * 
     * @param board L'état actuel du plateau
     * @return Le meilleur coup trouvé
     */
    public Move bestMove(Board board) {
        // Réinitialiser les compteurs pour cette recherche
        resetCounters();
        // Vider la table de transposition (ne pas réutiliser les données de la recherche précédente)
        transpositionTable.clear();
        // Lancer la recherche avec recherche itérative approfondie
        return iterativeDeepening(board);
    }
    
    /**
     * Effectue une recherche Minimax itérative approfondie.
     * Recherche d'abord à profondeur 1, puis 2, puis 3, etc. jusqu'à maxDepth.
     * Permet d'avoir un bon coup rapidement et d'améliorer progressivement.
     * 
     * @param board L'état du plateau
     * @return Le meilleur coup trouvé
     */
    private Move iterativeDeepening(Board board) {
        Move bestMove = null;
        
        // Rechercher progressivement de plus en plus profond
        for (int depth = 1; depth <= maxDepth; depth++) {
            // Lancer une recherche Minimax à cette profondeur
            MinimaxResult result = minimax(board, depth, Double.NEGATIVE_INFINITY, 
                                          Double.POSITIVE_INFINITY, true);
            // Mettre à jour le meilleur coup trouvé
            if (result.move != null) {
                bestMove = result.move;
            }
        }
        
        // Afficher les statistiques de la recherche
        System.out.println(String.format("IA Stats - Nodes: %d, Cache hits: %d, Alpha cutoffs: %d, Beta cutoffs: %d",
                          nodesVisited, cacheHits, alphaCutoffs, betaCutoffs));
        
        return bestMove;
    }
    
    /**
     * Oriente un score selon la couleur de l'IA.
     * Les scores bruts ont les noirs positifs et blancs négatifs.
     * Cette méthode transforme le score pour que positif = bon pour l'IA, négatif = mauvais pour l'IA.
     * 
     * @param rawValue Score brut du plateau
     * @return Score orienté selon la couleur de l'IA
     */
    private double orient(double rawValue) {
        // Si l'IA est noire, garder le score tel quel (noirs positifs)
        // Si l'IA est blanche, inverser le score (blancs positifs)
        return (myColor == 'b') ? rawValue : -rawValue;
    }
    
    /**
     * Implémente l'algorithme Minimax avec élagage Alpha-Beta et table de transposition.
     * Cette méthode est le cœur de l'IA : elle explore l'arbre de jeu en profondeur.
     * 
     * Fonctionnement :
     * - maximizing=true : cherche le meilleur coup pour l'IA (valeur maximale)
     * - maximizing=false : cherche le pire coup pour l'adversaire (valeur minimale)
     * - Alpha-Beta pruning : coupe les branches non prometteuses pour gagner du temps
     * - Table de transposition : mémorise les positions déjà évaluées
     * 
     * @param board L'état du plateau
     * @param depth Profondeur restante de recherche
     * @param alpha Meilleure valeur trouvée pour le joueur maximisant
     * @param beta Meilleure valeur trouvée pour le joueur minimisant
     * @param maximizing true si on maximise (tour de l'IA), false si on minimise (tour adverse)
     * @return Un objet contenant le score et le meilleur coup trouvé
     */
    private MinimaxResult minimax(Board board, int depth, double alpha, double beta, boolean maximizing) {
        // Incrémenter le compteur de nœuds visités
        nodesVisited++;
        
        // OPTIMISATION 1 : Vérifier si la position est déjà évaluée (cache/transposition table)
        String key = getCacheKey(board, depth, maximizing);
        if (transpositionTable.containsKey(key)) {
            // Utiliser le résultat en cache au lieu de re-calculer
            cacheHits++;
            CacheEntry entry = transpositionTable.get(key);
            return new MinimaxResult(entry.score, entry.move);
        }
        
        // CONDITION D'ARRÊT 1 : Profondeur limite atteinte
        // CONDITION D'ARRÊT 2 : Position terminale (quelqu'un a gagné ou c'est un nul)
        if (depth == 0 || board.isTerminal()) {
            // Évaluer cette position avec toutes les heuristiques
            double score = evaluate(board);
            return new MinimaxResult(score, null);
        }
        
        // Récupérer tous les coups légaux pour le joueur actuel
        List<Move> moves = board.legalMoves(board.getCurrentPlayer());
        if (moves.isEmpty()) {
            // Aucun coup légal = défaite pour le joueur actuel
            double score = evaluate(board);
            return new MinimaxResult(score, null);
        }
        
        // OPTIMISATION 2 : Classer les coups pour mieux élaguer les branches
        // Les captures sont testées en premier (meilleur élagage)
        moves = orderMoves(board, moves, depth, maximizing);
        
        // Liste des meilleurs coups en cas d'égalité de score
        List<Move> bestMoves = new ArrayList<>();
        // Initialiser avec la pire valeur possible
        double bestScore = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        
        // Boucle sur tous les coups possibles
        for (Move move : moves) {
            // Appliquer le coup au plateau et mémoriser comment l'annuler
            Board.MoveUndo undo = board.makeMove(move);
            char oldPlayer = board.getCurrentPlayer();
            // Passer au joueur suivant
            board.setCurrentPlayer(oldPlayer == 'w' ? 'b' : 'w');
            
            // APPEL RÉCURSIF : Évaluer la position après ce coup
            // Réduire la profondeur, inverser maximizing
            MinimaxResult result = minimax(board, depth - 1, alpha, beta, !maximizing);
            double score = result.score;
            
            // Restaurer l'état du plateau
            board.setCurrentPlayer(oldPlayer);
            board.undoMove(undo);
            
            // LOGIQUE MINIMAX
            if (maximizing) {
                // Chercher le score MAXIMUM
                if (score > bestScore) {
                    // Nouveau meilleur coup trouvé
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                }
                else if (score == bestScore) {
                    // Score égal au meilleur
                    bestMoves.add(move);
                }
                
                // ALPHA-BETA : Mettre à jour alpha
                alpha = Math.max(alpha, score);
                // Si beta <= alpha, on peut couper les autres branches
                if (beta <= alpha) {
                    alphaCutoffs++; // Compter la coupure
                    break; // Sortir de la boucle (élagage)
                }
            } else {
                // Chercher le score MINIMUM
                if (score < bestScore) {
                    // Nouveau meilleur coup trouvé
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(move);
                }
                else if (score == bestScore) {
                    // Score égal au meilleur
                    bestMoves.add(move);
                }
                
                // ALPHA-BETA : Mettre à jour beta
                beta = Math.min(beta, score);
                // Si beta <= alpha, on peut couper les autres branches
                if (beta <= alpha) {
                    betaCutoffs++; // Compter la coupure
                    break; // Sortir de la boucle (élagage)
                }
            }
        }
        
        // Choisir aléatoirement parmi les meilleurs coups en cas d'égalité
        // Cela rend l'IA moins prévisible et plus variée
        Move chosenMove = bestMoves.isEmpty() ? null : bestMoves.get(random.nextInt(bestMoves.size()));
        
        // OPTIMISATION : Stocker ce résultat en cache pour ne pas le recalculer
        transpositionTable.put(key, new CacheEntry(bestScore, chosenMove));
        
        // Retourner le score et le meilleur coup trouvé
        return new MinimaxResult(bestScore, chosenMove);
    }
    
    /**
     * Crée une clé unique pour une position dans la table de transposition.
     * Cette clé identifie de manière unique une position + profondeur + contexte.
     * 
     * Format de la clé : hashCode_depth_maximizing_myColor
     * - hashCode du plateau identifie la position
     * - depth différencie les recherches à différentes profondeurs
     * - maximizing distingue les nœuds maximisants et minimisants
     * - myColor s'assure que l'IA ne réutilise pas les valeurs d'une couleur opposée
     * 
     * @param board L'état du plateau
     * @param depth Profondeur actuelle de recherche
     * @param maximizing true si c'est un nœud maximisant
     * @return Une chaîne unique identifiant cette position
     */
    private String getCacheKey(Board board, int depth, boolean maximizing) {
        // Combiner le hachage du plateau, la profondeur, le contexte et la couleur
        return board.hashCode() + "_" + depth + "_" + maximizing + "_" + myColor;
    }
    
    /**
     * Évalue une position de jeu en utilisant les 9 heuristiques pondérées.
     * C'est la fonction d'évaluation complète qui combine tous les aspects du jeu.
     * 
     * Processus :
     * 1. Si la position est terminale (quelqu'un a gagné), retourner un score extrême
     * 2. Sinon, calculer un score brut en combinant les 9 heuristiques avec leurs poids
     * 3. Orienter le score selon la couleur de l'IA
     * 
     * Les 9 heuristiques sont pondérées différemment selon le profil sélectionné :
     * - matériel, contrôle du centre, structure de pions
     * - mobilité, activité des dames, potentiel de promotion
     * - sécurité des pièces, tempo (avance), positions de blocage
     * 
     * @param board L'état du plateau à évaluer
     * @return Un score numérique (positif = bon pour l'IA, négatif = mauvais)
     */
    private double evaluate(Board board) {
        // ÉVALUATION DES POSITIONS TERMINALES
        if (board.isTerminal()) {
            // Déterminer le gagnant
            char winner = board.winner();
            // Score brut (noir positif, blanc négatif)
            double raw;
            if (winner == 'w') {
                raw = -10000.0; // Blanc gagne = très bon pour blancs, très mauvais pour noirs
            } else if (winner == 'b') {
                raw = 10000.0; // Noir gagne = très bon pour noirs, très mauvais pour blancs
            } else {
                raw = 0.0; // Nul = neutre
            }
            // IMPORTANT : Orienter le score terminal aussi !
            return orient(raw);
        }
        
        // ÉVALUATION DES POSITIONS NON-TERMINALES
        // Score brut (noir positif, blanc négatif)
        double raw = 0.0;
        
        // Ajouter la contribution de chaque heuristique multipliée par son poids
        raw += weights.get("material") * material(board);
        raw += weights.get("central") * centralControl(board);
        raw += weights.get("structure") * pawnStructure(board);
        raw += weights.get("mobility") * mobility(board);
        raw += weights.get("Dame_activity") * DameActivity(board);
        raw += weights.get("promotion") * promotionPotential(board);
        raw += weights.get("safety") * pieceSafety(board);
        raw += weights.get("tempo") * tempo(board);
        raw += weights.get("locks") * lockPositions(board);
        
        // Orientation du score selon la couleur de l'IA
        // Transforme le score pour que positif = bon pour l'IA
        return orient(raw);
    }
    
    /**
     * Heuristique 1 : MATÉRIEL
     * Calcule la différence de pièces entre noirs et blancs.
     * Un pion vaut 1 point, une dame vaut 3 points (meilleure mobilité).
     * 
     * Calcul du score brut (avant orientation) :
     * - Score noir positif : + valeur des pions/dames noirs
     * - Score blanc négatif : - valeur des pions/dames blancs
     * 
     * Exemple : 20 pions noirs vs 18 pions blancs = score brut de +2
     * 
     * @param board L'état du plateau
     * @return Différence matériel (noirs positifs, blancs négatifs)
     */
    private double material(Board board) {
        double val = 0.0;
        Piece[][] grid = board.getGrid();
        
        // Parcourir tout le plateau
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Piece p = grid[r][c];
                if (p == null) continue; // Case vide
                
                // Déterminer la valeur : dame=3, pion=1
                double v = p.isDame() ? 3.0 : 1.0;
                // Ajouter ou soustraire selon la couleur
                val += p.isBlack() ? v : -v;
            }
        }
        
        return val;
    }
    
    /**
     * Heuristique 2 : CONTRÔLE DU CENTRE
     * Bonus pour les pièces proches du centre du plateau.
     * Les pièces centrales sont plus flexibles et puissantes.
     * 
     * Zones d'évaluation :
     * - CENTRE (4 cases) : bonus +3 par pièce
     * - LARGE_CENTRE (16 cases) : bonus +1 par pièce
     * - Autres cases : bonus 0
     * 
     * @param board L'état du plateau
     * @return Score de contrôle du centre (noirs positifs, blancs négatifs)
     */
    private double centralControl(Board board) {
        double score = 0.0;
        Piece[][] grid = board.getGrid();
        
        // Parcourir tout le plateau
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Piece p = grid[r][c];
                if (p == null) continue; // Case vide
                
                String pos = r + "," + c;
                double bonus = 0.0;
                
                // Déterminer le bonus selon la zone
                if (CENTER.contains(pos)) {
                    bonus = 3.0; // Centre pur
                } else if (WIDE_CENTER.contains(pos)) {
                    bonus = 1.0; // Centre large
                }
                
                // Ajouter le bonus au score
                if (bonus > 0) {
                    score += p.isBlack() ? bonus : -bonus;
                }
            }
        }
        
        return score;
    }
    
    /**
     * Heuristique 3 : STRUCTURE DE PIONS
     * Évalue la cohésion et la solidité de la formation de pions.
     * 
     * Pénalités :
     * - Pion isolé (aucun allié adjacent) : -2 pour noir, +2 pour blanc
     * 
     * Bonus :
     * - Pion soutenu (allié derrière lui) : +2 pour noir, -2 pour blanc
     * 
     * Les pions soutenus sont plus forts car ils ne peuvent pas être capturés"à l'envers".
     * 
     * @param board L'état du plateau
     * @return Score de structure de pions (noirs positifs, blancs négatifs)
     */
    private double pawnStructure(Board board) {
        double score = 0.0;
        Piece[][] grid = board.getGrid();
        int size = 10;
        
        // Parcourir tous les pions
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Piece p = grid[r][c];
                // Ignorer les cases vides et les dames (qui ont une mobilité complète)
                if (p == null || p.isDame()) continue;
                
                // ===== ISOLEMENT =====
                // Vérifier si le pion a au moins un allié adjacent (sur les 4 diagonales)
                boolean isolated = true;
                int[][] neighbors = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};
                for (int i=0; i<neighbors.length && isolated; i++) {
                    int[] dir = neighbors[i];
                    int nr = r + dir[0];
                    int nc = c + dir[1];
                    if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                        Piece q = grid[nr][nc];
                        // Vérifier qu'il y a un allié (même couleur)
                        if (q != null && !p.areOpponents(q)) {
                            isolated = false;
                        }
                    }
                }
                // Pénaliser les pions isolés
                if (isolated) {
                    score += p.isBlack() ? -2.0 : 2.0;
                }
                
                // ===== SOUTIEN =====
                // Vérifier si le pion a un allié "derrière" lui (dans sa direction d'avance)
                boolean support = false;
                // Si noir (avance vers bas), regarder les diagonales bas
                // Si blanc (avance vers haut), regarder les diagonales haut
                int[][] checks = p.isBlack() ? new int[][]{{1,1}, {1,-1}} : new int[][]{{-1,1}, {-1,-1}};
                for (int i=0; i<checks.length && !support; i++) {
                    int[] dir = checks[i];
                    int nr = r + dir[0];
                    int nc = c + dir[1];
                    if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                        Piece q = grid[nr][nc];
                        // Vérifier qu'il y a un allié
                        if (q != null && !p.areOpponents(q)) {
                            support = true;
                        }
                    }
                }
                // Bonuser les pions soutenus
                if (support) {
                    score += p.isBlack() ? 2.0 : -2.0;
                }
            }
        }
        
        return score;
    }
    
    /**
     * Heuristique 4 : MOBILITÉ
     * Calcule la différence entre le nombre de coups légaux des noirs et des blancs.
     * Plus de coups = plus d'options = meilleure position.
     * 
     * Calcul :
     * - Compter les coups légaux des noirs
     * - Compter les coups légaux des blancs
     * - Retourner : noirs - blancs
     * 
     * Exemple : 15 coups noirs, 10 coups blancs = score de +5
     * 
     * @param board L'état du plateau
     * @return Score de mobilité (noirs positifs, blancs négatifs)
     */
    private double mobility(Board board) {
        // Sauvegarder le joueur actuel pour le restaurer après
        char currentPlayer = board.getCurrentPlayer();
        
        // Compter les coups légaux des noirs
        board.setCurrentPlayer('b');
        int blackMoves = board.legalMoves('b').size();
        
        // Compter les coups légaux des blancs
        board.setCurrentPlayer('w');
        int whiteMoves = board.legalMoves('w').size();
        
        // Restaurer le joueur actuel
        board.setCurrentPlayer(currentPlayer);
        
        // Retourner la différence
        return blackMoves - whiteMoves;
    }
    
    /**
     * Heuristique 5 : ACTIVITÉ DES DAMES
     * Mesure l'activité et la puissance des dames sur le plateau.
     * 
     * Facteurs considérés :
     * - Distance du bord : Une dame au centre est plus utile qu'une dame en coin
     * - Liberté de mouvement : Nombre de cases libres sur les 4 diagonales
     * 
     * Formule :
     * - Distance du bord : min(distanceHaut, distanceBas, distanceGauche, distanceDroite)
     * - Cases libres multipliées par 0.2 (moins important que la distance du bord)
     * 
     * @param board L'état du plateau
     * @return Score d'activité des dames (noirs positifs, blancs négatifs)
     */
    private double DameActivity(Board board) {
        double score = 0.0;
        Piece[][] grid = board.getGrid();
        int size = 10;
        
        // Parcourir tout le plateau
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Piece p = grid[r][c];
                // Ignorer les cases vides et les pions (ils ne participent pas à cette heuristique)
                if (p == null || !p.isDame()) continue;
                
                // ===== DISTANCE DU BORD =====
                // Une dame au centre est meilleure qu'une dame en coin
                int distEdge = Math.min(
                    Math.min(r, size - 1 - r),           // Min distance haut/bas
                    Math.min(c, size - 1 - c)            // Min distance gauche/droite
                );
                // Ajouter cette distance au score
                score += p.isBlack() ? distEdge : -distEdge;
                
                // ===== CASES LIBRES SUR LES DIAGONALES =====
                // Compter combien de cases libres sont disponibles sur les 4 diagonales
                int freeSteps = 0;
                int[][] dirs = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};
                for (int[] dir : dirs) {
                    int nr = r + dir[0];
                    int nc = c + dir[1];
                    // Avancer tant qu'on reste dans le plateau et que la case est libre
                    while (nr >= 0 && nr < size && nc >= 0 && nc < size && grid[nr][nc] == null) {
                        freeSteps++;
                        nr += dir[0];
                        nc += dir[1];
                    }
                }
                // Ajouter les cases libres (avec coefficient 0.2 car moins important que distance du bord)
                score += (p.isBlack() ? freeSteps * 0.2 : -freeSteps * 0.2);
            }
        }
        
        return score;
    }
    
    /**
     * Heuristique 6 : POTENTIEL DE PROMOTION
     * Mesure à quel point les pions sont proches de la promotion.
     * Un pion proche de la dernière rangée est très dangereux.
     * 
     * Calcul :
     * - Pour chaque pion noir : score = distance restante (9 - ligne)
     * - Pour chaque pion blanc : score = - distance restante (ligne)
     * 
     * Exemple : Un pion noir à la ligne 8 (1 case avant promotion) = +9 points
     * Exemple : Un pion blanc à la ligne 1 (8 cases avant promotion) = -9 points
     * 
     * @param board L'état du plateau
     * @return Score de potentiel de promotion (noirs positifs, blancs négatifs)
     */
    private double promotionPotential(Board board) {
        double score = 0.0;
        Piece[][] grid = board.getGrid();
        int size = 10;
        
        // Parcourir tous les pions
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Piece p = grid[r][c];
                // Ignorer les cases vides et les dames (qui sont déjà promues)
                if (p == null || p.isDame()) continue;
                
                // Les noirs avancent vers le bas (ligne 9 = promotion)
                if (p.isBlack()) {
                    int dist = (size - 1) - r; // Distance jusqu'à la ligne 9
                    score += (size - dist); // Plus proche = plus élevé
                } 
                // Les blancs avancent vers le haut (ligne 0 = promotion)
                else {
                    int dist = r; // Distance jusqu'à la ligne 0
                    score -= (size - dist); // Plus proche = plus bas (négatif)
                }
            }
        }
        
        return score;
    }
    
    /**
     * Heuristique 7 : SÉCURITÉ DES PIÈCES
     * Pénalise les pièces "en l'air" (capturable au coup suivant).
     * Une pièce en l'air est une faiblesse tactique majeure.
     * 
     * @param board L'état du plateau
     * @return Score de sécurité (noirs positifs, blancs négatifs)
     */
    private double pieceSafety(Board board) {
        double score = 0.0;
        Piece[][] grid = board.getGrid();
        
        // Parcourir tout le plateau
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Piece p = grid[r][c];
                if (p == null) continue;
                
                // Vérifier si cette pièce est capturable sans risque
                if (squareIsHanging(board, r, c)) {
                    // Pénaliser fortement les pièces en l'air (-4)
                    score += p.isBlack() ? -4.0 : 4.0;
                }
            }
        }
        
        return score;
    }
    
    /**
     * Vérifie si une pièce à une position donnée est "en l'air" (capturable sans défense).
     * Une pièce en l'air est une pièce adjacente à une pièce ennemie
     * avec une case vide de l'autre côté (permettant une capture).
     * 
     * @param board L'état du plateau
     * @param r Ligne de la pièce à vérifier
     * @param c Colonne de la pièce à vérifier
     * @return true si la pièce peut être capturée, false sinon
     */
    private boolean squareIsHanging(Board board, int r, int c) {
        Piece piece = board.getGrid()[r][c];
        if (piece == null) return false;
        
        int size = 10;
        // Parcourir les 4 diagonales
        int[][] dirs = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};
        for (int[] dir : dirs) {
            // Position de la pièce ennemie potentielle
            int mr = r + dir[0];
            int mc = c + dir[1];
            // Position d'atterrissage après capture
            int ar = r + 2 * dir[0];
            int ac = c + 2 * dir[1];
            
            // Vérifier les limites du plateau
            if (mr < 0 || mr >= size || mc < 0 || mc >= size) continue;
            if (ar < 0 || ar >= size || ac < 0 || ac >= size) continue;
            
            Piece q = board.getGrid()[mr][mc];
            // Vérifier : (1) il y a une pièce ennemie (2) la case d'arrivée est libre
            if (q != null && q.areOpponents(piece) && board.getGrid()[ar][ac] == null) {
                return true; // Cette pièce peut être capturée
            }
        }
        
        return false; // Cette pièce est défendue ou non capturable
    }
    
    /**
     * Heuristique 8 : tempo (AVANCE DES PIONS)
     * Mesure à quel point les pions ont avancé vers la promotion.
     * Les pions avancés menacent la promotion et gênent l'adversaire.
     * 
     * Calcul :
     * - Pour chaque pion noir : score = +ligne (avance vers ligne 9)
     * - Pour chaque pion blanc : score = -(9-ligne) (avance vers ligne 0)
     * 
     * @param board L'état du plateau
     * @return Score de tempo (noirs positifs, blancs négatifs)
     */
    private double tempo(Board board) {
        double score = 0.0;
        Piece[][] grid = board.getGrid();
        int size = 10;
        
        // Parcourir tous les pions
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Piece p = grid[r][c];
                // Ignorer les cases vides et les dames
                if (p == null || p.isDame()) continue;
                
                // Noirs avancent vers le bas : récompenser l'avance
                if (p.isBlack()) {
                    score += r; // Plus r est grand, plus le pion est avancé
                } 
                // Blancs avancent vers le haut : récompenser l'avance
                else {
                    score -= ((size - 1) - r); // Plus r est petit, plus le pion est avancé
                }
            }
        }
        
        return score;
    }
    
    /**
     * Heuristique 9 : POSITIONS DE BLOCAGE / COINS
     * Pénalise les dames "enterrées" dans les coins difficiles du plateau.
     * Les positions de coin limitent la mobilité des dames.
     * 
     * Positions de coins problématiques :
     * - (0,1), (1,0), (0,3), (3,0) : coin haut-gauche
     * - (9,8), (8,9), (9,6), (6,9) : coin bas-droit
     * 
     * Une dame dans une position de coin = -8 pour noirs, +8 pour blancs
     * 
     * @param board L'état du plateau
     * @return Score de positions de blocage (noirs positifs, blancs négatifs)
     */
    private double lockPositions(Board board) {
        double score = 0.0;
        Piece[][] grid = board.getGrid();
        
        // Positions de coin problématiques (difficiles à défendre, mobilité limitée)
        int[][] cornersLike = {
            {0,1}, {1,0}, {0,3}, {3,0}, // Coin haut-gauche
            {9,8}, {8,9}, {9,6}, {6,9}  // Coin bas-droit
        };
        
        // Parcourir les positions de coin
        for (int[] pos : cornersLike) {
            int r = pos[0];
            int c = pos[1];
            if (r < 0 || r >= 10 || c < 0 || c >= 10) continue;
            
            Piece p = grid[r][c];
            // Uniquement les dames sont affectées (les pions peuvent s'y échapper par promotion)
            if (p != null && p.isDame()) {
                // Pénaliser fortement (-8) une dame en position de coin
                score += p.isBlack() ? -8.0 : 8.0;
            }
        }
        
        return score;
    }
    
    /**
     * Ordonne les coups pour optimiser l'élagage Alpha-Beta.
     * Les meilleurs coups sont testés en premier pour maximiser les coupures.
     * 
     * Critères de tri (par ordre d'importance) :
     * 1. Captures en priorité (poids = nombre de pièces capturées × 50)
     * 2. Score réel de la table de transposition pour la position enfant (si disponible)
     * 3. Autres mouvements
     * 
     * Exemple : Un coup qui capture 2 pièces (+100) et mène à une position en cache de score +30
     * obtient un score d'ordre de 130.
     * 
     * @param board L'état du plateau courant
     * @param moves La liste des coups candidats depuis ce plateau
     * @param depth La profondeur restante au nœud courant (l'enfant est évalué à depth-1)
     * @param maximizing true si le nœud courant est maximisant (l'enfant est alors minimisant)
     * @return La liste de coups triée par ordre d'intérêt décroissant
     */
    private List<Move> orderMoves(Board board, List<Move> moves, int depth, boolean maximizing) {
        // Créer une liste de paires (coup, score)
        List<MoveScore> scored = new ArrayList<>();
        
        for (Move move : moves) {
            double score = 0.0;
            
            // CRITÈRE 1 : Priorité aux captures (très importantes)
            if (move.isCapture()) {
                // Plus on capture de pièces, plus le coup est intéressant
                score += move.capturedPositions.length * 50;
            }
            
            // Bonus supplémentaire si le coup mène à une position déjà évaluée (cache)
            Board.MoveUndo undo = board.makeMove(move);
            char oldPlayer = board.getCurrentPlayer();
            board.setCurrentPlayer(oldPlayer == 'w' ? 'b' : 'w');

            int childDepth = Math.max(0, depth - 1);
            String key = getCacheKey(board, childDepth, !maximizing);
            if (transpositionTable.containsKey(key)) {
                CacheEntry entry = transpositionTable.get(key);
                score += entry.score;   // score réel du cache
            }

            board.setCurrentPlayer(oldPlayer);
            board.undoMove(undo);
            
            scored.add(new MoveScore(move, score));
        }
        
        // Trier les coups par score décroissant (meilleurs d'abord)
        scored.sort((a, b) -> {
            if (a.score > b.score) return -1;  // a avant b (score plus élevé)
            if (a.score < b.score) return 1;   // b avant a (score plus bas)
            return 0;  // égalité
        });
        
        // Extraire les coups de la liste triée
        List<Move> ordered = new ArrayList<>();
        for (MoveScore ms : scored) {
            ordered.add(ms.move);
        }
        
        return ordered;
    }
    
    /**
     * Classe interne pour stocker un résultat Minimax.
     * Contient à la fois le score d'une position et le meilleur coup menant à cette position.
     */
    private static class MinimaxResult {
        /** Score d'évaluation de la position */
        double score;
        /** Meilleur coup trouvé pour cette position */
        Move move;
        
        /**
         * Constructeur d'un résultat Minimax.
         * @param score Le score d'évaluation
         * @param move Le meilleur coup (peut être null en feuille)
         */
        MinimaxResult(double score, Move move) {
            this.score = score;
            this.move = move;
        }
    }
    
    /**
     * Classe interne pour stocker une entrée dans la table de transposition.
     * Mémorise le score et le meilleur coup pour une position donnée.
     */
    private static class CacheEntry {
        /** Score mémorisé pour cette position */
        double score;
        /** Meilleur coup trouvé pour cette position */
        Move move;
        
        /**
         * Constructeur d'une entrée de cache.
         * @param score Le score à mémoriser
         * @param move Le coup à mémoriser
         */
        CacheEntry(double score, Move move) {
            this.score = score;
            this.move = move;
        }
    }
    
    /**
     * Classe interne pour associer un coup à son score d'ordre de mouvements.
     * Utilisée lors du tri des coups pour optimiser Alpha-Beta.
     */
    private static class MoveScore {
        /** Le coup à ordonner */
        Move move;
        /** Le score d'ordre (captures > coups vers le centre > autres) */
        double score;
        
        /**
         * Constructeur pour une paire coup-score.
         * @param move Le coup
         * @param score Son score d'ordre
         */
        MoveScore(Move move, double score) {
            this.move = move;
            this.score = score;
        }
    }
}
