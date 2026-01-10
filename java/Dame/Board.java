import java.util.*;

/**
 * Représente le plateau de jeu de dames 10x10 avec toute la logique du jeu.
 * Gère les déplacements, les captures, les promotions et les règles de nullité internationales.
 * 
 * Règles de nullité implémentées (FMJD) :
 * - Répétition de position 3 fois : match nul
 * - 25 coups consécutifs de dames sans capture ni mouvement de pion : match nul
 * - Limite de 400 coups en tournoi : match nul
 * 
 * @author Ulysse Ansoux
 * @author Noé Vander Schueren
 * @author Léo Maquin-Testud
 * @version 1.0
 */
public class Board {
    /** Taille du plateau (10x10) */
    private static final int SIZE = 10;
    /** Grille du plateau contenant les pièces */
    private Piece[][] grid;
    /** Joueur actuel ('w' pour blancs, 'b' pour noirs) */
    private char currentPlayer;
    
    /** Historique des hash de positions pour détecter les répétitions */
    private List<Integer> positionHistory;
    /** Nombre de coups consécutifs de dames sans capture ni mouvement de pion */
    private int movesWithoutCaptureOrPawn;
    
    /**
     * Constructeur du plateau de jeu.
     * Initialise une grille vide, positionne les piéces, et prépare les structures pour les règles de nullité.
     */
    public Board() {
        // Créer une grille 10x10 (initialement vide)
        grid = new Piece[SIZE][SIZE];
        // Le joueur blanc commence toujours
        currentPlayer = 'w';
        // Historique des positions pour détecter les répétitions
        positionHistory = new ArrayList<>();
        // Compteur de coups sans capture ni mouvement de pion (règle des 25 coups)
        movesWithoutCaptureOrPawn = 0;
        // Placer les piéces dans leur position initiale
        initBoard();
    }
    
    /**
     * Initialise le plateau avec les piéces dans leur configuration de début de partie.
     * - Blancs : 20 pions dans les 4 premières rangées (cases noires seulement)
     * - Noirs : 20 pions dans les 4 dernières rangées (cases noires seulement)
     * - Centre vide : 4 rangées centrales sans piéces
     */
    private void initBoard() {
        // Initialiser les piéces blanches en bas (rangées 0-3)
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < SIZE; c++) {
                // Placer les piéces uniquement sur les cases noires (r + c impair)
                if ((r + c) % 2 == 1) {
                    grid[r][c] = new Piece('w');
                }
            }
        }
        
        // Initialiser les piéces noires en haut (rangées 6-9)
        for (int r = 6; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                // Placer les piéces uniquement sur les cases noires
                if ((r + c) % 2 == 1) {
                    grid[r][c] = new Piece('b');
                }
            }
        }
        // Les rangées 4-5 restent vides (centre du plateau)
    }
    
    /**
     * Retourne la grille du plateau.
     * 
     * @return La matrice 10x10 contenant les piéces
     */
    public Piece[][] getGrid() {
        return grid;
    }
    
    /**
     * Retourne la couleur du joueur actuel.
     * 
     * @return 'w' pour blancs, 'b' pour noirs
     */
    public char getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * Définit le joueur actuel (utilisé principalement en tournoi pour réinitialiser le tour).
     * 
     * @param player La couleur du joueur ('w' ou 'b')
     */
    public void setCurrentPlayer(char player) {
        this.currentPlayer = player;
    }
    
    /**
     * Récupére une pièce à une position donnée.
     * 
     * @param row Ligne (0-9)
     * @param col Colonne (0-9)
     * @return La pièce à cette position, ou null si la position est invalide ou vide
     */
    public Piece getPiece(int row, int col) {
        // Vérifier que les coordonnées sont valides
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return null;
        }
        return grid[row][col];
    }
    
    /**
     * Place une pièce à une position donnée.
     * 
     * @param row Ligne (0-9)
     * @param col Colonne (0-9)
     * @param piece La pièce à placer (peut être null pour vider une case)
     */
    public void setPiece(int row, int col, Piece piece) {
        // Vérifier que les coordonnées sont valides avant de placer la pièce
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            grid[row][col] = piece;
        }
    }
    
    /**
     * Vérifie si une position donnée est sur le plateau.
     * 
     * @param row Ligne à vérifier
     * @param col Colonne à vérifier
     * @return true si la position est valide (0-9, 0-9), false sinon
     */
    public boolean isValidPos(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }
    
    /**
     * Retourne tous les déplacements légaux pour un joueur donné.
     * Respecte la règle : les captures sont obligatoires et prioritaires sur les mouvements normaux.
     * 
     * @param player La couleur du joueur ('w' ou 'b')
     * @return Liste de tous les coups légaux pour ce joueur
     */
    public List<Move> legalMoves(char player) {
        // Séparer les captures et les mouvements normaux
        List<Move> captureMoves = new ArrayList<>();
        List<Move> normalMoves = new ArrayList<>();
        
        // Parcourir tout le plateau pour trouver les piéces du joueur
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = grid[r][c];
                // Vérifier que c'est une piéce du joueur actuel
                if (p != null && Character.toLowerCase(p.getCode()) == player) {
                    // Trouver les captures possibles pour cette piéce
                    List<Move> caps = findCaptures(r, c, p);
                    captureMoves.addAll(caps);
                    
                    // Ajouter les mouvements normaux uniquement s'il n'y a pas de captures
                    // (car les captures sont obligatoires)
                    if (captureMoves.isEmpty()) {
                        List<Move> norms = findNormalMoves(r, c, p);
                        normalMoves.addAll(norms);
                    }
                }
            }
        }
        
        // Appliquer la règle de capture obligatoire
        if (!captureMoves.isEmpty()) {
            // Trouver la longueur maximale de capture (nombre de piéces capturées)
            int maxLen = 0;
            for (Move m : captureMoves) {
                if (m.capturedPositions.length > maxLen) {
                    maxLen = m.capturedPositions.length;
                }
            }
            
            // Ne garder que les captures de longueur maximale (captures obligatoires)
            List<Move> result = new ArrayList<>();
            for (Move m : captureMoves) {
                if (m.capturedPositions.length == maxLen) {
                    result.add(m);
                }
            }
            return result;
        }
        
        // Pas de capture disponible : retourner les mouvements normaux
        return normalMoves;
    }
    
    private List<Move> findNormalMoves(int r, int c, Piece piece) {
        List<Move> moves = new ArrayList<>();
        int[][] directions;
        
        if (piece.isDame()) {
            directions = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        } else if (piece.isWhite()) {
            directions = new int[][]{{1, -1}, {1, 1}};
        } else {
            directions = new int[][]{{-1, -1}, {-1, 1}};
        }
        
        for (int[] dir : directions) {
            int dr = dir[0], dc = dir[1];
            
            if (piece.isDame()) {
                // Une dame peut se déplacer de plusieurs cases
                for (int dist = 1; dist < SIZE; dist++) {
                    int nr = r + dr * dist;
                    int nc = c + dc * dist;
                    
                    if (!isValidPos(nr, nc)) break;
                    if (grid[nr][nc] != null) break;
                    
                    moves.add(new Move(r, c, nr, nc, new int[0][0]));
                }
            } else {
                // Un pion se déplace d'une case
                int nr = r + dr;
                int nc = c + dc;
                
                if (isValidPos(nr, nc) && grid[nr][nc] == null) {
                    moves.add(new Move(r, c, nr, nc, new int[0][0]));
                }
            }
        }
        
        return moves;
    }
    
    private List<Move> findCaptures(int r, int c, Piece piece) {
        List<Move> allCaptures = new ArrayList<>();
        boolean[][] visited = new boolean[SIZE][SIZE];
        visited[r][c] = true;
        
        dfsCapture(r, c, r, c, piece, visited, new ArrayList<>(), allCaptures);
        
        return allCaptures;
    }
    
    private void dfsCapture(int startR, int startC, int r, int c, Piece piece, boolean[][] visited, 
                           List<int[]> capturedSoFar, List<Move> allCaptures) {
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        boolean foundCapture = false;
        
        for (int[] dir : directions) {
            int dr = dir[0], dc = dir[1];
            
            if (piece.isDame()) {
                // Une dame peut sauter un adversaire à n'importe quelle distance
                for (int dist = 1; dist < SIZE - 1; dist++) {
                    int enemyR = r + dr * dist;
                    int enemyC = c + dc * dist;
                    
                    if (!isValidPos(enemyR, enemyC)) break;
                    Piece enemy = grid[enemyR][enemyC];
                    
                    if (enemy == null) continue;
                    if (!piece.areOpponents(enemy)) break;
                    
                    boolean alreadyCaptured = false;
                    for (int[] cap : capturedSoFar) {
                        if (cap[0] == enemyR && cap[1] == enemyC) {
                            alreadyCaptured = true;
                            break;
                        }
                    }
                    if (alreadyCaptured) break;
                    
                    // Rechercher les cases d'atterrissage après cet adversaire
                    for (int landDist = dist + 1; landDist < SIZE; landDist++) {
                        int landR = r + dr * landDist;
                        int landC = c + dc * landDist;
                        
                        if (!isValidPos(landR, landC)) break;
                        if (grid[landR][landC] != null) break;
                        if (visited[landR][landC]) break;
                        
                        foundCapture = true;
                        visited[landR][landC] = true;
                        capturedSoFar.add(new int[]{enemyR, enemyC});
                        
                        dfsCapture(startR, startC, landR, landC, piece, visited, capturedSoFar, allCaptures);
                        
                        capturedSoFar.remove(capturedSoFar.size() - 1);
                        visited[landR][landC] = false;
                    }
                    break;
                }
            } else {
                // Un pion saute exactement une case au-dessus d'un adversaire
                int enemyR = r + dr;
                int enemyC = c + dc;
                int landR = r + 2 * dr;
                int landC = c + 2 * dc;
                
                if (!isValidPos(enemyR, enemyC) || !isValidPos(landR, landC)) continue;
                
                Piece enemy = grid[enemyR][enemyC];
                if (enemy == null || !piece.areOpponents(enemy)) continue;
                
                boolean alreadyCaptured = false;
                for (int[] cap : capturedSoFar) {
                    if (cap[0] == enemyR && cap[1] == enemyC) {
                        alreadyCaptured = true;
                        break;
                    }
                }
                if (alreadyCaptured) continue;
                
                if (grid[landR][landC] != null || visited[landR][landC]) continue;
                
                foundCapture = true;
                visited[landR][landC] = true;
                capturedSoFar.add(new int[]{enemyR, enemyC});
                
                dfsCapture(startR, startC, landR, landC, piece, visited, capturedSoFar, allCaptures);
                
                capturedSoFar.remove(capturedSoFar.size() - 1);
                visited[landR][landC] = false;
            }
        }
        
        if (!foundCapture && !capturedSoFar.isEmpty()) {
            int[][] captured = new int[capturedSoFar.size()][2];
            for (int i = 0; i < capturedSoFar.size(); i++) {
                captured[i] = capturedSoFar.get(i);
            }
            // startRow et startCol sont corrigés dans findCaptures
            allCaptures.add(new Move(startR, startC, r, c, captured));
        }
    }
    
    public void applyMove(Move move) {
        Piece piece = grid[move.startRow][move.startCol];
        boolean isPawn = (piece != null && !piece.isDame());
        boolean hasCapture = (move.capturedPositions != null && move.capturedPositions.length > 0);
        
        grid[move.startRow][move.startCol] = null;
        grid[move.endRow][move.endCol] = piece;
        
        // Retirer les pièces capturées
        if (move.capturedPositions != null) {
            for (int[] pos : move.capturedPositions) {
                grid[pos[0]][pos[1]] = null;
            }
        }
        
        // Promotion en dame
        if (piece != null) {
            if (piece.isWhite() && move.endRow == SIZE - 1) {
                piece.promote();
            } else if (piece.isBlack() && move.endRow == 0) {
                piece.promote();
            }
        }
        
        // Mise à jour du compteur pour règle des 25 coups
        if (hasCapture || isPawn) {
            movesWithoutCaptureOrPawn = 0;
        } else {
            movesWithoutCaptureOrPawn++;
        }
        
        // Ajouter la position à l'historique
        positionHistory.add(hashCode());
    }
    
    public MoveUndo makeMove(Move move) {
        Piece piece = grid[move.startRow][move.startCol];
        boolean wasDame = piece != null && piece.isDame();
        int oldMovesWithoutCaptureOrPawn = movesWithoutCaptureOrPawn;
        
        List<Piece> capturedPieces = new ArrayList<>();
        if (move.capturedPositions != null) {
            for (int[] pos : move.capturedPositions) {
                capturedPieces.add(grid[pos[0]][pos[1]]);
            }
        }
        
        applyMove(move);
        
        return new MoveUndo(move, wasDame, capturedPieces, oldMovesWithoutCaptureOrPawn);
    }
    
    public void undoMove(MoveUndo undo) {
        Move move = undo.move;
        Piece piece = grid[move.endRow][move.endCol];
        
        // Restaurer la position de la pièce
        grid[move.startRow][move.startCol] = piece;
        grid[move.endRow][move.endCol] = null;
        
        // Restaurer le statut de dame
        if (piece != null && !undo.wasDame && piece.isDame()) {
            if (piece.isWhite()) {
                piece.setCode('w');
            } else {
                piece.setCode('b');
            }
        }
        
        // Restaurer les pièces capturées
        if (move.capturedPositions != null) {
            for (int i = 0; i < move.capturedPositions.length; i++) {
                int[] pos = move.capturedPositions[i];
                grid[pos[0]][pos[1]] = undo.capturedPieces.get(i);
            }
        }
        
        // Retirer la dernière position de l'historique
        if (!positionHistory.isEmpty()) {
            positionHistory.remove(positionHistory.size() - 1);
        }
        
        // Restaurer le compteur de coups sans progression
        movesWithoutCaptureOrPawn = undo.movesWithoutCaptureOrPawn;
    }
    
    public boolean isTerminal() {
        return legalMoves(currentPlayer).isEmpty();
    }
    
    /**
     * Vérifie si le jeu est terminé en incluant les règles d'égalité.
     * À utiliser uniquement dans le jeu réel, PAS dans minimax.
     */
    public boolean isTerminalWithDraw() {
        return legalMoves(currentPlayer).isEmpty() || isDraw();
    }
    
    /**
     * Vérifie les cas d'égalité selon les règles internationales :
     * 1. Position répétée 3 fois
     * 2. 25 coups de dames consécutifs sans prise ni mouvement de pion
     */
    public boolean isDraw() {
        // Règle 1 : Position répétée 3 fois
        if (positionHistory.size() >= 3) {
            int currentHash = hashCode();
            int repetitions = 0;
            for (int hash : positionHistory) {
                if (hash == currentHash) {
                    repetitions++;
                    if (repetitions >= 3) {
                        return true;
                    }
                }
            }
        }
        
        // Règle 2 : 25 coups de dames sans prise ni mouvement de pion
        if (movesWithoutCaptureOrPawn >= 25) {
            return true;
        }
        
        return false;
    }
    
    public char winner() {
        // Vérifier d'abord les règles d'égalité
        if (isDraw()) {
            return 'd'; // 'd' pour nul
        }
        // Ensuite vérifier si un joueur ne peut plus bouger
        if (!isTerminal()) {
            return ' '; // Partie en cours
        }
        // Le joueur qui ne peut plus bouger perd
        return currentPlayer == 'w' ? 'b' : 'w';
    }
    
    public int[] countPieces() {
        int white = 0;
        int black = 0;
        
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = grid[r][c];
                if (p != null) {
                    if (p.isWhite()) {
                        white++;
                    } else {
                        black++;
                    }
                }
            }
        }
        
        return new int[]{white, black};
    }
    
    public Board copy() {
        Board newBoard = new Board();
        newBoard.currentPlayer = this.currentPlayer;
        newBoard.movesWithoutCaptureOrPawn = this.movesWithoutCaptureOrPawn;
        newBoard.positionHistory = new ArrayList<>(this.positionHistory);
        
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (this.grid[r][c] != null) {
                    newBoard.grid[r][c] = this.grid[r][c].copy();
                } else {
                    newBoard.grid[r][c] = null;
                }
            }
        }
        
        return newBoard;
    }
    
    @Override
    public int hashCode() {
        int hash = currentPlayer;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] != null) {
                    hash = hash * 31 + grid[r][c].getCode() + r * SIZE + c;
                }
            }
        }
        return hash;
    }
    
    public static class MoveUndo {
        public Move move;
        public boolean wasDame;
        public List<Piece> capturedPieces;
        public int movesWithoutCaptureOrPawn;
        
        public MoveUndo(Move move, boolean wasDame, List<Piece> capturedPieces, int movesWithoutCaptureOrPawn) {
            this.move = move;
            this.wasDame = wasDame;
            this.capturedPieces = capturedPieces;
            this.movesWithoutCaptureOrPawn = movesWithoutCaptureOrPawn;
        }
    }
}
