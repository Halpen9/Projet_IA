/**
 * Représente une pièce du jeu de dames.
 * Une pièce peut être un pion blanc ('w'), un pion noir ('b'),
 * une dame blanche ('W') ou une dame noire ('B').
 * 
 * @author Ulysse Ansoux
 * @author Noé Vander Schueren
 * @author Léo Maquin-Testud
 * @version 1.0
 */
public class Piece {
    /** Code de la pièce : 'w' (pion blanc), 'b' (pion noir), 'W' (dame blanche), 'B' (dame noire) */
    private char code;
    
    /**
     * Constructeur d'une pièce.
     * 
     * @param code Le code de la pièce ('w', 'b', 'W', 'B')
     */
    public Piece(char code) {
        this.code = code;
    }
    
    /**
     * Retourne le code de la pièce.
     * 
     * @return Le code de la pièce ('w', 'b', 'W', 'B')
     */
    public char getCode() {
        return code;
    }
    
    /**
     * Définit le code de la pièce.
     * 
     * @param code Le nouveau code de la pièce
     */
    public void setCode(char code) {
        this.code = code;
    }
    
    /**
     * Vérifie si la pièce est blanche.
     * 
     * @return true si la pièce est blanche (pion ou dame), false sinon
     */
    public boolean isWhite() {
        return Character.toLowerCase(code) == 'w';
    }
    
    /**
     * Vérifie si la pièce est noire.
     * 
     * @return true si la pièce est noire (pion ou dame), false sinon
     */
    public boolean isBlack() {
        return Character.toLowerCase(code) == 'b';
    }
    
    /**
     * Vérifie si la pièce est une dame.
     * 
     * @return true si la pièce est une dame (blanche ou noire), false sinon
     */
    public boolean isDame() {
        return code == 'W' || code == 'B';
    }
    
    /**
     * Promeut un pion en dame.
     * Transforme 'w' en 'W' ou 'b' en 'B'.
     * Utilisé quand un pion atteint la dernière rangée de l'adversaire.
     */
    public void promote() {
        // Si c'est un pion blanc, le transformer en dame blanche
        if (code == 'w') {
            code = 'W';
        } 
        // Si c'est un pion noir, le transformer en dame noire
        else if (code == 'b') {
            code = 'B';
        }
        // Les dames et les pièces nulles ne changent pas
    }
    
    /**
     * Vérifie si cette pièce est adversaire d'une autre pièce.
     * 
     * @param other L'autre pièce à comparer
     * @return true si les deux pièces sont de couleurs différentes, false sinon
     */
    public boolean areOpponents(Piece other) {
        if (other == null) {
            return false;
        }
        return Character.toLowerCase(this.code) != Character.toLowerCase(other.code);
    }
    
    /**
     * Crée une copie de cette pièce.
     * 
     * @return Une nouvelle instance de Piece avec le même code
     */
    public Piece copy() {
        return new Piece(this.code);
    }
    
    /**
     * Retourne une représentation textuelle de la pièce.
     * 
     * @return Le code de la pièce sous forme de chaîne
     */
    @Override
    public String toString() {
        return String.valueOf(code);
    }
}
