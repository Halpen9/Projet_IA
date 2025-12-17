/**
 * Représente un déplacement d'une pièce sur le plateau de jeu.
 * Contient les coordonnées de départ, d'arrivée et les positions des pièces capturées.
 * 
 * @author Ulysse Ansoux
 * @author Noé Vander Schueren
 * @author Léo Maquin-Testud
 * @version 1.0
 */
public class Move {
    /** Ligne de départ de la pièce */
    public int startRow;
    /** Colonne de départ de la pièce */
    public int startCol;
    /** Ligne d'arrivée de la pièce */
    public int endRow;
    /** Colonne d'arrivée de la pièce */
    public int endCol;
    /** Tableau des positions [ligne, colonne] des pièces capturées pendant ce déplacement */
    public int[][] capturedPositions;
    
    /**
     * Constructeur d'un déplacement.
     * 
     * @param startRow Ligne de départ
     * @param startCol Colonne de départ
     * @param endRow Ligne d'arrivée
     * @param endCol Colonne d'arrivée
     * @param capturedPositions Positions des pièces capturées (null si aucune capture)
     */
    public Move(int startRow, int startCol, int endRow, int endCol, int[][] capturedPositions) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.capturedPositions = capturedPositions;
    }
    
    /**
     * Vérifie si ce déplacement est une capture.
     * 
     * @return true si au moins une pièce est capturée, false sinon
     */
    public boolean isCapture() {
        return capturedPositions != null && capturedPositions.length > 0;
    }
    
    /**
     * Retourne une représentation textuelle du déplacement.
     * Format : (startRow,startCol)->(endRow,endCol) avec liste optionnelle des captures.
     * 
     * @return Chaîne de caractères représentant le déplacement
     */
    @Override
    public String toString() {
        // Construire la chaîne de base : position de départ vers position d'arrivée
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("(%d,%d)->(%d,%d)", startRow, startCol, endRow, endCol));
        
        // Ajouter les positions capturées si c'est une capture
        if (isCapture()) {
            sb.append(" captures:");
            for (int[] pos : capturedPositions) {
                sb.append(String.format("(%d,%d)", pos[0], pos[1]));
            }
        }
        return sb.toString();
    }
}
