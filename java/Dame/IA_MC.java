import java.util.*;

/**
 * Intelligence artificielle pour le jeu de dames utilisant la méthode Monte Carlo Tree Search (MCTS).
 * 
 * Cette IA ne repose sur aucune heuristique. Elle évalue les coups en effectuant
 * de nombreuses simulations de parties aléatoires à partir de chaque coup possible.
 * Le coup ayant mené au meilleur taux de victoire est sélectionné.
 * 
 * Avantages :
 * - Aucune connaissance du domaine nécessaire
 * - Découvre des stratégies émergentes
 * - Performances prévisibles selon le nombre de simulations
 * 
 * Configuration typique : 50-2000 simulations (défaut: 300)
 * 
 * @author Ulysse Ansoux
 * @author Noé Vander Schueren
 * @author Léo Maquin-Testud
 * @version 1.0
 */
public class IA_MC {
    /** Nombre de simulations à effectuer par coup */
    private int simulations;
    /** Couleur de l'IA ('w' ou 'b') */
    private char myColor;
    /** Générateur de nombres aléatoires pour les simulations */
    private Random random;
    
    /**
     * Constructeur d'une IA utilisant Monte-Carlo Tree Search.
     * 
     * @param myColor Couleur de l'IA ('w' ou 'b')
     * @param simulations Nombre de simulations à effectuer pour évaluer chaque coup
     */
    public IA_MC(char myColor, int simulations) {
        // Configurer les paramètres de base
        this.myColor = myColor;
        this.simulations = simulations;
        this.random = new Random();
    }
    
    /**
     * Détermine le meilleur coup à jouer en utilisant des simulations Monte-Carlo.
     * Pour chaque coup possible, effectue 'simulations' parties aléatoires compilètes.
     * 
     * @param board L'état actuel du plateau
     * @return Le meilleur coup identifié, ou null si aucun coup legal n'existe
     */
    public Move bestMove(Board board) {
        // Récupérer tous les coups légaux depuis la position actuelle
        List<Move> moves = board.legalMoves(board.getCurrentPlayer());
        if (moves.isEmpty()) {
            return null; // Pas de coup légal disponible
        }
        
        // Initialiser les scores et compteurs pour chaque coup
        Map<String, Double> scores = new HashMap<>(); // Somme des résultats
        Map<String, Integer> counts = new HashMap<>(); // Nombre de simulations par coup
        
        for (Move m : moves) {
            String key = moveKey(m);
            scores.put(key, 0.0);
            counts.put(key, 1); // Commencer à 1 pour éviter division par zéro
        }
        
        // Effectuer les simulations
        for (int i = 0; i < simulations; i++) {
            // Sélectionner un coup aléatoirement
            Move move = moves.get(random.nextInt(moves.size()));
            // Simuler une partie complète à partir de ce coup
            double result = simulate(board, move, board.getCurrentPlayer());
            
            // Mettre à jour le score du coup
            String key = moveKey(move);
            scores.put(key, scores.get(key) + result);
            counts.put(key, counts.get(key) + 1);
        }
        
        // Trouver le ou les coups avec le meilleur score moyen
        List<Move> bestMoves = new ArrayList<>();
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Move m : moves) {
            String key = moveKey(m);
            // Calculer le score moyen pour ce coup
            double avgScore = scores.get(key) / counts.get(key);
            
            if (avgScore > bestScore) {
                // Nouveau meilleur coup
                bestScore = avgScore;
                bestMoves.clear();
                bestMoves.add(m);
            } else if (avgScore == bestScore) {
                // Coup égal au meilleur
                bestMoves.add(m);
            }
        }
        
        // Sélectionner aléatoirement parmi les meilleurs coups 
        Move chosenMove = bestMoves.get(random.nextInt(bestMoves.size()));
        
        // Afficher les statistiques de la recherche
        System.out.println(String.format("MCTS - Simulations: %d, Best score: %.3f", simulations, bestScore));
        
        return chosenMove;
    }
    
    /**
     * Simule une partie complète à partir d'un coup donné avec jeu aléatoire.
     * 
     * @param board L'état du plateau avant le coup
     * @param move Le coup à évaluer
     * @param startingColor La couleur du joueur qui joue le coup
     * @return 1.0 si l'IA gagne, -1.0 si l'IA perd, 0.0 en cas de nul
     */
    private double simulate(Board board, Move move, char startingColor) {
        // Créer une copie du plateau pour la simulation
        Board sim = board.copy();
        // Appliquer le coup à évaluer
        sim.applyMove(move);
        
        // Lancer une simulation complète à partir de cet état
        char winner = rollout(sim, startingColor == 'w' ? 'b' : 'w');
        
        // Retourner le résultat orienté selon la couleur de l'IA
        if (winner == myColor) {
            return 1.0; // L'IA gagne
        } else if (winner == ' ') {
            return 0.0; // Match nul
        } else {
            return -1.0; // L'IA perd
        }
    }
    
    /**
     * Effectue un déroulement (rollout) aléatoire jusqu'à la fin de la partie.
     * Les deux joueurs jouent des coups aléatoires jusqu'au terminal ou limite de 400 coups.
     * 
     * @param board L'état du plateau à partir duquel dérouler
     * @param current La couleur du joueur actuel
     * @return Le gagnant : 'w' (blancs), 'b' (noirs), ou ' ' (nul)
     */
    private char rollout(Board board, char current) {
        // Limite de 400 coups pour éviter les boucles infinies
        for (int i = 0; i < 400; i++) {
            // Récupérer les coups légaux pour le joueur actuel
            List<Move> moves = board.legalMoves(current);
            
            // Vérifier si le joueur actuel n'a plus de coups (défaite)
            if (moves.isEmpty()) {
                return current == 'w' ? 'b' : 'w'; // L'adversaire gagne
            }
            
            // Jouer un coup aléatoire
            board.applyMove(moves.get(random.nextInt(moves.size())));
            
            // Vérifier si le jeu est terminé
            if (board.isTerminal()) {
                return board.winner();
            }
            
            // Passer au joueur suivant
            current = current == 'w' ? 'b' : 'w';
        }
        
        // Limite de coups atteinte = match nul
        return ' ';
    }
    
    /**
     * Crée une clé unique pour identifier un coup.
     * Utilisée pour mapper les scores aux coups dans la méthode bestMove().
     * 
     * @param m Le coup à encoder
     * @return Une chaîne unique représentant le coup
     */
    private String moveKey(Move m) {
        return String.format("%d,%d->%d,%d", m.startRow, m.startCol, m.endRow, m.endCol);
    }
}
