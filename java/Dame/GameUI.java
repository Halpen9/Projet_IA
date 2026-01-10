import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Interface graphique principale du jeu de dames.
 * 
 * Fonctionnalités :
 * - Modes de jeu : Humain vs Humain, Humain vs IA, IA vs Humain, IA vs IA
 * - Sélection de 9 profils d'IA différents (8 Minimax + Monte-Carlo)
 * - Configuration de la profondeur de recherche (1-10) pour Minimax
 * - Configuration du nombre de simulations (50-2000) pour Monte-Carlo
 * - Système d'annulation de coups (Undo)
 * - Affichage des statistiques en temps réel (nœuds visités, temps par coup)
 * - Application des règles de nullité internationales (FMJD)
 * - Lancement du système de tournoi depuis le menu
 * 
 * @author Ulysse Ansoux
 * @author Noé Vander Schueren
 * @author Léo Maquin-Testud
 * @version 1.0
 */
public class GameUI extends JFrame {
    /** Taille d'une case du plateau en pixels */
    private static final int CELL_SIZE = 50;
    /** Taille du plateau (10x10) */
    private static final int BOARD_SIZE = 10;
    
    private Board board;
    private BoardPanel boardPanel;
    private JLabel statusLabel;
    private JLabel turnLabel;
    private JLabel statsLabel;
    
    private int selectedRow = -1;
    private int selectedCol = -1;
    private List<Move> availableMoves;
    
    private String gameMode = "HvsH"; 
    private IA whiteIA;
    private IA blackIA;
    private IA_MC whiteMC;
    private IA_MC blackMC;
    
    private String whiteProfile = "Expert";
    private String blackProfile = "Expert";
    private int whiteDepth = 6;
    private int blackDepth = 6;
    private int whiteSimulations = 300;
    private int blackSimulations = 300;
    
    private Timer iaTimer;
    private boolean stopIAGame = false;
    
    // Annulation de l'historique
    private Stack<BoardSnapshot> undoHistory;
    
    /**
     * Constructeur de l'interface graphique principale du jeu de dames.
     * Initialise :
     * - Le plateau de jeu (10x10)
     * - La barre de menus (modes de jeu, profils, profondeur, MC)
     * - Le panneau d'affichage du plateau
     * - Les labels de statut (tour actuel, mode de jeu, statistiques)
     * - L'historique d'annulation (undo)
     */
    public GameUI() {
        setTitle("Jeu de Dames - Version Complète");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Initialiser le plateau vierge
        board = new Board();
        // Initialiser la pile d'historique pour l'annulation de coups
        undoHistory = new Stack<>();
        
        // Créer la barre de menus (Jeu, Profils, Profondeur, MC)
        createMenuBar();
        
        // Créer le panneau principal avec bordure
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Créer le panneau du plateau (affichage graphique)
        boardPanel = new BoardPanel();
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        
        // Créer le panneau de statut en bas (tour, mode, statistiques)
        JPanel statusPanel = new JPanel(new GridLayout(3, 1));
        // Label du tour actuel (Blancs/Noirs)
        turnLabel = new JLabel("Tour: Blancs", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        // Label du mode de jeu (HvsH, HvsIA, etc.)
        statusLabel = new JLabel("Mode: Humain vs Humain", SwingConstants.CENTER);
        // Label des statistiques (comptage des pièces)
        statsLabel = new JLabel("", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        
        // Ajouter les labels au panneau de statut
        statusPanel.add(turnLabel);
        statusPanel.add(statusLabel);
        statusPanel.add(statsLabel);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Finir la configuration de la fenêtre
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Rafraîchir l'affichage initial
        updateDisplay();
    }
    
    /**
     * Crée la barre de menus avec tous les éléments de configuration.
     * 
     * Menus disponibles :
     * - Jeu : Nouvelle partie, modes (HvsH, HvsIA, IAvsH, IAvsIA), Arrêter IA, Annuler, Tournoi
     * - Profil Blancs : Sélectionner le profil d'IA pour les blancs (8 Minimax + MC)
     * - Profil Noirs : Sélectionner le profil d'IA pour les noirs (8 Minimax + MC)
     * - Profondeur : Sélectionner la profondeur de recherche (1-8) pour chaque couleur
     * - MC Simulations : Sélectionner le nombre de simulations (50-2000) pour chaque couleur
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Jeu
        JMenu gameMenu = new JMenu("Jeu");
        
        JMenuItem newGameItem = new JMenuItem("Nouvelle partie");
        newGameItem.addActionListener(e -> newGame());
        gameMenu.add(newGameItem);
        
        gameMenu.addSeparator();
        
        JMenuItem hvhItem = new JMenuItem("Humain vs Humain");
        hvhItem.addActionListener(e -> setGameMode("HvsH"));
        gameMenu.add(hvhItem);
        
        JMenuItem hviaItem = new JMenuItem("Humain (Blancs) vs IA (Noirs)");
        hviaItem.addActionListener(e -> setGameMode("HvsIA"));
        gameMenu.add(hviaItem);
        
        JMenuItem iavhItem = new JMenuItem("IA (Blancs) vs Humain (Noirs)");
        iavhItem.addActionListener(e -> setGameMode("IAvsH"));
        gameMenu.add(iavhItem);
        
        JMenuItem iaviaItem = new JMenuItem("IA vs IA");
        iaviaItem.addActionListener(e -> setGameMode("IAvsIA"));
        gameMenu.add(iaviaItem);
        
        gameMenu.addSeparator();
        
        JMenuItem stopItem = new JMenuItem("Arrêter IA vs IA");
        stopItem.addActionListener(e -> stopIAGame());
        gameMenu.add(stopItem);
        
        JMenuItem undoItem = new JMenuItem("Annuler (Ctrl+Z)");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> undoMove());
        gameMenu.add(undoItem);
        
        gameMenu.addSeparator();
        
        JMenuItem TournoiItem = new JMenuItem("Tournoi d'IA");
        TournoiItem.addActionListener(e -> openTournoi());
        gameMenu.add(TournoiItem);
        
        gameMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Quitter");
        exitItem.addActionListener(e -> System.exit(0));
        gameMenu.add(exitItem);
        
        menuBar.add(gameMenu);
        
        // Menu Profil Blancs
        JMenu whiteProfileMenu = new JMenu("Profil Blancs");
        String[] profiles = {"Perdant", "Intermédiaire", "Expert", "Agressif", "Défensif", "Poids Random", "Joue Random", "Équilibre", "Monte-Carlo"};
        ButtonGroup whiteGroup = new ButtonGroup();
        
        for (String profile : profiles) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(profile);
            item.setSelected(profile.equals(whiteProfile));
            item.addActionListener(e -> {
                whiteProfile = profile;
                updateStatusLabel();
                if (gameMode.equals("IAvsIA") || gameMode.equals("IAvsH")) {
                    createIAs();
                }
            });
            whiteGroup.add(item);
            whiteProfileMenu.add(item);
        }
        
        menuBar.add(whiteProfileMenu);
        
        // Menu Profil Noirs
        JMenu blackProfileMenu = new JMenu("Profil Noirs");
        ButtonGroup blackGroup = new ButtonGroup();
        
        for (String profile : profiles) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(profile);
            item.setSelected(profile.equals(blackProfile));
            item.addActionListener(e -> {
                blackProfile = profile;
                updateStatusLabel();
                if (gameMode.equals("IAvsIA") || gameMode.equals("HvsIA")) {
                    createIAs();
                }
            });
            blackGroup.add(item);
            blackProfileMenu.add(item);
        }
        
        menuBar.add(blackProfileMenu);
        
        // Menu Profondeur IA
        JMenu depthMenu = new JMenu("Profondeur");
        
        JMenu whiteDepthMenu = new JMenu("Blancs");
        ButtonGroup whiteDepthGroup = new ButtonGroup();
        for (int d = 1; d <= 8; d++) {
            final int depth = d;
            JRadioButtonMenuItem item = new JRadioButtonMenuItem("Profondeur " + d);
            item.setSelected(d == whiteDepth);
            item.addActionListener(e -> {
                whiteDepth = depth;
                if (gameMode.equals("IAvsIA") || gameMode.equals("IAvsH")) {
                    createIAs();
                }
            });
            whiteDepthGroup.add(item);
            whiteDepthMenu.add(item);
        }
        depthMenu.add(whiteDepthMenu);
        
        JMenu blackDepthMenu = new JMenu("Noirs");
        ButtonGroup blackDepthGroup = new ButtonGroup();
        for (int d = 1; d <= 8; d++) {
            final int depth = d;
            JRadioButtonMenuItem item = new JRadioButtonMenuItem("Profondeur " + d);
            item.setSelected(d == blackDepth);
            item.addActionListener(e -> {
                blackDepth = depth;
                if (gameMode.equals("IAvsIA") || gameMode.equals("HvsIA")) {
                    createIAs();
                }
            });
            blackDepthGroup.add(item);
            blackDepthMenu.add(item);
        }
        depthMenu.add(blackDepthMenu);
        
        menuBar.add(depthMenu);
        
        // Menu Simulations Monte-Carlo
        JMenu MCMenu = new JMenu("MC Simulations");
        
        JMenu whiteSimsMenu = new JMenu("Blancs");
        ButtonGroup whiteSimsGroup = new ButtonGroup();
        int[] simOptions = {50, 100, 200, 300, 500, 1000, 2000};
        for (int sims : simOptions) {
            final int simCount = sims;
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(simCount + " simulations");
            item.setSelected(sims == whiteSimulations);
            item.addActionListener(e -> {
                whiteSimulations = simCount;
                if ((gameMode.equals("IAvsIA") || gameMode.equals("IAvsH")) && whiteProfile.equals("Monte-Carlo")) {
                    createIAs();
                }
            });
            whiteSimsGroup.add(item);
            whiteSimsMenu.add(item);
        }
        MCMenu.add(whiteSimsMenu);
        
        JMenu blackSimsMenu = new JMenu("Noirs");
        ButtonGroup blackSimsGroup = new ButtonGroup();
        for (int sims : simOptions) {
            final int simCount = sims;
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(simCount + " simulations");
            item.setSelected(sims == blackSimulations);
            item.addActionListener(e -> {
                blackSimulations = simCount;
                if ((gameMode.equals("IAvsIA") || gameMode.equals("HvsIA")) && blackProfile.equals("Monte-Carlo")) {
                    createIAs();
                }
            });
            blackSimsGroup.add(item);
            blackSimsMenu.add(item);
        }
        MCMenu.add(blackSimsMenu);
        
        menuBar.add(MCMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Crée les instances d'IA selon le mode de jeu et les profils sélectionnés.
     * 
     * Logique :
     * - Mode HvsH : aucune IA
     * - Mode HvsIA : crée l'IA pour les noirs
     * - Mode IAvsH : crée l'IA pour les blancs
     * - Mode IAvsIA : crée les deux IA
     * 
     * Pour chaque couleur :
     * - Si profil = "Monte-Carlo" : crée une IA_MC avec nombre de simulations configuré
     * - Sinon : crée une IA Minimax avec profil et profondeur configurés
     */
    private void createIAs() {
        // Créer l'IA blanche
        if (gameMode.equals("HvsH") || gameMode.equals("HvsIA")) {
            whiteIA = null;
            whiteMC = null;
        } else {
            if (whiteProfile.equals("Monte-Carlo")) {
                whiteMC = new IA_MC('w', whiteSimulations);
                whiteIA = null;
            } else {
                whiteIA = createIAWithProfil('w', whiteProfile, whiteDepth);
                whiteMC = null;
            }
        }
        
        // Créer l'IA noire
        if (gameMode.equals("HvsH") || gameMode.equals("IAvsH")) {
            blackIA = null;
            blackMC = null;
        } else {
            if (blackProfile.equals("Monte-Carlo")) {
                blackMC = new IA_MC('b', blackSimulations);
                blackIA = null;
            } else {
                blackIA = createIAWithProfil('b', blackProfile, blackDepth);
                blackMC = null;
            }
        }
    }
    
    /**
     * Crée une instance d'IA Minimax avec un profil et une profondeur donnés.
     * Méthode utilitaire utilisée par createIAs().
     * 
     * @param color Couleur de l'IA ('w' ou 'b')
     * @param profile Nom du profil parmi les 8 disponibles
     * @param depth Profondeur de recherche (1-8)
     * @return Une nouvelle instance d'IA configurée
     */
    private IA createIAWithProfil(char color, String profile, int depth) {
        return new IA(color, depth, profile);
    }
    
    /**
     * Change le mode de jeu et réinitialise la partie.
     * 
     * Modes disponibles :
     * - "HvsH" : Humain vs Humain (deux joueurs au clavier)
     * - "HvsIA" : Humain (Blancs) vs IA (Noirs)
     * - "IAvsH" : IA (Blancs) vs Humain (Noirs)
     * - "IAvsIA" : IA vs IA (spectateur)
     * 
     * Actions effectuées :
     * 1. Arrêter le jeu IA en cours
     * 2. Définir le nouveau mode
     * 3. Créer ou détruire les IA selon le nouveau mode
     * 4. Démarrer une nouvelle partie
     * 5. Lancer IAvsIA ou programmer le premier coup IA si applicable
     * 
     * @param mode Le nouveau mode de jeu ("HvsH", "HvsIA", "IAvsH", "IAvsIA")
     */
    private void setGameMode(String mode) {
        stopIAGame();
        gameMode = mode;
        updateStatusLabel();
        createIAs();
        newGame();
        if (gameMode.equals("IAvsIA")) {
            playIAvsIA();
        } else if (gameMode.equals("IAvsH") && board.getCurrentPlayer() == 'w') {
            scheduleIAMove();
        }
    }

    /**
     * Met à jour le label de statut avec la configuration actuelle du jeu.
     * Affiche :
     * - Le mode de jeu (HvsH, HvsIA, IAvsH, IAvsIA)
     * - Les profils d'IA sélectionnés
     * - Les paramètres (profondeur Minimax ou simulations MC)
     * 
     * Exemples de textes affichés :
     * - "Mode: Humain vs Humain"
     * - "Mode: Humain (Blancs) vs IA Noirs (Expert, Prof.6)"
     * - "Mode: IA vs IA - Blancs(Expert-P6) vs Noirs(Agressif-P8)"
     */
    // Met à jour le texte du label de statut en fonction du mode et des profils
    private void updateStatusLabel() {
        String modeText = "";
        switch (gameMode) {
            case "HvsH":
                modeText = "Mode: Humain vs Humain";
                break;
            case "HvsIA":
                if (blackProfile.equals("Monte-Carlo")) {
                    modeText = String.format("Mode: Humain (Blancs) vs IA Noirs (MC, %d sims)", blackSimulations);
                } else {
                    modeText = String.format("Mode: Humain (Blancs) vs IA Noirs (%s, Prof.%d)", blackProfile, blackDepth);
                }
                break;
            case "IAvsH":
                if (whiteProfile.equals("Monte-Carlo")) {
                    modeText = String.format("Mode: IA Blancs (MC, %d sims) vs Humain (Noirs)", whiteSimulations);
                } else {
                    modeText = String.format("Mode: IA Blancs (%s, Prof.%d) vs Humain (Noirs)", whiteProfile, whiteDepth);
                }
                break;
            case "IAvsIA":
                String whiteDesc = whiteProfile.equals("Monte-Carlo") ? "MC-" + whiteSimulations : whiteProfile + "-P" + whiteDepth;
                String blackDesc = blackProfile.equals("Monte-Carlo") ? "MC-" + blackSimulations : blackProfile + "-P" + blackDepth;
                modeText = String.format("Mode: IA vs IA - Blancs(%s) vs Noirs(%s)", whiteDesc, blackDesc);
                break;
        }
        statusLabel.setText(modeText);
    }
    
    /**
     * Arrête un jeu IA en cours (IAvsIA ou IA vs Humain).
     * Arrête le timer qui ordonne les mouvements de l'IA.
     * Permet d'interrompre une partie longue à tout moment.
     */
    private void stopIAGame() {
        stopIAGame = true;
        if (iaTimer != null && iaTimer.isRunning()) {
            iaTimer.stop();
        }
    }
    
    /**
     * Ouvre la fenêtre de tournoi d'IA.
     * Permet de comparer les performances de tous les profils via des matchs automatisés.
     * La fenêtre de tournoi s'ouvre dans une nouvelle fenêtre indépendante.
     */
    private void openTournoi() {
        new TournoiUI();
    }
    
    /**
     * Réinitialise le jeu et lance une nouvelle partie.
     * 
     * Actions :
     * 1. Arrêter tout jeu IA en cours
     * 2. Créer un nouveau plateau vierge
     * 3. Réinitialiser les variables (sélection, historique, etc.)
     * 4. Rafraîchir l'affichage
     * 5. Lancer IAvsIA ou programmer le premier coup IA si applicable
     */
    private void newGame() {
        stopIAGame();
        board = new Board();
        selectedRow = -1;
        selectedCol = -1;
        availableMoves = null;
        undoHistory.clear();
        stopIAGame = false;
        updateDisplay();
        
        if (gameMode.equals("IAvsIA")) {
            Timer startTimer = new Timer(500, e -> playIAvsIA());
            startTimer.setRepeats(false);
            startTimer.start();
        } else if (gameMode.equals("IAvsH") && board.getCurrentPlayer() == 'w') {
            scheduleIAMove();
        }
    }
    
    /**
     * Annule le dernier coup joué et restaure l'état du plateau.
     * 
     * Fonctionnement :
     * 1. Vérifier que l'historique n'est pas vide
     * 2. Récupérer le dernier snapshot du plateau
     * 3. Restaurer le plateau à cet état
     * 4. Rafraîchir l'affichage
     * 
     * Limitations :
     * - Impossible d'annuler pendant un jeu IAvsIA (timer trop rapide)
     * - Chaque coup annulé est supprimé de l'historique
     */
    private void undoMove() {
        if (undoHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun coup à annuler", 
                                         "Annuler", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        BoardSnapshot snapshot = undoHistory.pop();
        board = snapshot.board.copy();
        updateDisplay();
    }
    
    /**
     * Sauvegarde l'état actuel du plateau dans l'historique.
     * Appelée avant chaque mouvement humain pour permettre l'annulation.
     * Le plateau est copié (pas de référence) pour éviter les modifications accidentelles.
     */
    private void saveSnapshot() {
        undoHistory.push(new BoardSnapshot(board.copy()));
    }
    
    /**
     * Rafraîchit l'affichage graphique et les labels d'état.
     * 
     * Mises à jour :
     * 1. Affiche le tour actuel (Blancs/Noirs)
     * 2. Affiche le comptage des pièces
     * 3. Détecte les positions terminales (victoire/nul) et affiche le message final
     * 4. Redessine le plateau graphique
     */
    private void updateDisplay() {
        if (board.getCurrentPlayer() == 'w') {
            turnLabel.setText("Tour: Blancs");
        } else {
            turnLabel.setText("Tour: Noirs");
        }
        
        // Count pieces
        int[] counts = board.countPieces();
        statsLabel.setText(String.format("Pièces - Blancs: %d | Noirs: %d", counts[0], counts[1]));
        
        if (board.isTerminalWithDraw()) {
            char winner = board.winner();
            if (winner == 'w') {
                turnLabel.setText("★ VICTOIRE DES BLANCS! ★");
                turnLabel.setForeground(Color.GREEN);
            } else if (winner == 'b') {
                turnLabel.setText("★ VICTOIRE DES NOIRS! ★");
                turnLabel.setForeground(Color.GREEN);
            } else {
                turnLabel.setText("=== MATCH NUL ===");
                turnLabel.setForeground(Color.ORANGE);
            }
        } else {
            turnLabel.setForeground(Color.BLACK);
        }
        
        boardPanel.repaint();
    }
    
    /**
     * Lance et gère un jeu complet IA vs IA.
     * Utilise un Timer qui appelle les mouvements d'IA alternativement.
     * 
     * Processus :
     * 1. Arrêter tout timer existant
     * 2. Créer un nouveau timer qui se déclenche toutes les 500ms
     * 3. À chaque déclenchement :
     *    - Vérifier si le jeu n'est pas terminé
     *    - Obtenir les IA pour le joueur actuel (Minimax ou MC)
     *    - Exécuter bestMove()
     *    - Appliquer le coup au plateau
     *    - Alterner les joueurs
     *    - Rafraîchir l'affichage
     * 4. Arrêter le timer à la fin du jeu
     */
    private void playIAvsIA() {
        if (iaTimer != null && iaTimer.isRunning()) {
            iaTimer.stop();
        }
        
        stopIAGame = false;
        
        iaTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (stopIAGame || board.isTerminalWithDraw()) {
                    iaTimer.stop();
                    updateDisplay();
                    return;
                }
                
                List<Move> legalMoves = board.legalMoves(board.getCurrentPlayer());
                if (legalMoves.isEmpty()) {
                    iaTimer.stop();
                    updateDisplay();
                    return;
                }
                
                Move move = null;
                long startTime = System.currentTimeMillis();
                
                if (board.getCurrentPlayer() == 'w') {
                    if (whiteMC != null) {
                        move = whiteMC.bestMove(board);
                    } else if (whiteIA != null) {
                        move = whiteIA.bestMove(board);
                    }
                } else {
                    if (blackMC != null) {
                        move = blackMC.bestMove(board);
                    } else if (blackIA != null) {
                        move = blackIA.bestMove(board);
                    }
                }
                
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println(String.format("%s joue en %dms", 
                    board.getCurrentPlayer() == 'w' ? "Blancs" : "Noirs", elapsed));
                
                if (move != null) {
                    board.applyMove(move);
                    board.setCurrentPlayer(board.getCurrentPlayer() == 'w' ? 'b' : 'w');
                    updateDisplay();
                } else {
                    iaTimer.stop();
                }
            }
        });
        iaTimer.start();
    }
    
    /**
     * Programme un coup d'IA pour le prochain tour (mode HvsIA ou IAvsH).
     * Utilise un Timer unique pour éviter les appels simultanés.
     * Le délai de 500ms donne du temps à l'humain de voir les mouvements.
     */
    private void scheduleIAMove() {
        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playIATurn();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Exécute un seul coup d'IA (mode HvAI ou IAvsH).
     * 
     * Processus :
     * 1. Vérifier que le jeu n'est pas terminé
     * 2. Obtenir l'IA pour le joueur actuel (Minimax ou MC)
     * 3. Exécuter bestMove() pour obtenir le meilleur coup
     * 4. Appliquer le coup au plateau
     * 5. Alterner au joueur suivant
     * 6. Rafraîchir l'affichage
     */
    private void playIATurn() {
        if (board.isTerminalWithDraw()) {
            return;
        }
        
        Move move = null;
        
        if (board.getCurrentPlayer() == 'w') {
            if (whiteMC != null) {
                move = whiteMC.bestMove(board);
            } else if (whiteIA != null) {
                move = whiteIA.bestMove(board);
            }
        } else {
            if (blackMC != null) {
                move = blackMC.bestMove(board);
            } else if (blackIA != null) {
                move = blackIA.bestMove(board);
            }
        }
        
        if (move != null) {
            board.applyMove(move);
            board.setCurrentPlayer(board.getCurrentPlayer() == 'w' ? 'b' : 'w');
            updateDisplay();
        }
    }
    
    /**
     * Classe interne représentant le panneau graphique du plateau.
     * Gère :
     * - L'affichage du plateau 10x10 en couleurs
     * - Les pièces (pions et dames) avec leur représentation visuelle
     * - Les cases surlignées (pièce sélectionnée et coups possibles)
     * - Les coordonnées des lignes/colonnes
     * - Les clics souris pour la sélection et le déplacement des pièces
     * - Le rendu avec antialiasing pour une meilleure qualité
     */
    private class BoardPanel extends JPanel {
        /**
         * Constructeur du panneau du plateau.
         * Configure :
         * - La taille préférée du panneau (500x500 pixels)
         * - L'écouteur de souris pour la sélection et le déplacement des pièces
         * - La vérification du mode de jeu (humain peut cliquer seulement s'il a le droit de jouer)
         */
        public BoardPanel() {
            setPreferredSize(new Dimension(CELL_SIZE * BOARD_SIZE, CELL_SIZE * BOARD_SIZE));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (board.isTerminalWithDraw()) return;
                    
                    // Vérifier si c'est le tour d'un humain
                    boolean humanTurn = false;
                    if (gameMode.equals("HvsH")) {
                        humanTurn = true;
                    } else if (gameMode.equals("HvsIA") && board.getCurrentPlayer() == 'w') {
                        humanTurn = true;
                    } else if (gameMode.equals("IAvsH") && board.getCurrentPlayer() == 'b') {
                        humanTurn = true;
                    }
                    
                    if (!humanTurn) {
                        return;
                    }
                    
                    int col = e.getX() / CELL_SIZE;
                    int row = e.getY() / CELL_SIZE;
                    
                    handleClick(row, col);
                }
            });
        }
        
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
         * 3. Si non : désélectionner et recommencer (permettre de changer de pièce)
         * 4. Rafraîchir l'affichage
         * 5. Programmer un coup d'IA si applicable (mode HvsIA ou IAvsH)
         * 
         * @param row La ligne cliquée (0-9)
         * @param col La colonne cliquée (0-9)
         */
        private void handleClick(int row, int col) {
            Piece clicked = board.getPiece(row, col);
            
            if (selectedRow == -1) {
                // Premier clic - sélectionner une pièce
                if (clicked != null && Character.toLowerCase(clicked.getCode()) == board.getCurrentPlayer()) {
                    selectedRow = row;
                    selectedCol = col;
                    availableMoves = board.legalMoves(board.getCurrentPlayer());
                    
                    // Filtrer les coups depuis cette pièce
                    availableMoves.removeIf(m -> m.startRow != row || m.startCol != col);
                    
                    repaint();
                }
            } else {
                // Deuxième clic - tenter le coup
                Move selectedMove = null;
                
                for (Move m : availableMoves) {
                    if (m.endRow == row && m.endCol == col) {
                        selectedMove = m;
                        break;
                    }
                }
                
                if (selectedMove != null) {
                    saveSnapshot();
                    board.applyMove(selectedMove);
                    board.setCurrentPlayer(board.getCurrentPlayer() == 'w' ? 'b' : 'w');
                    
                    selectedRow = -1;
                    selectedCol = -1;
                    availableMoves = null;
                    
                    updateDisplay();
                    
                    // Tour de l'IA si applicable
                    if (!board.isTerminalWithDraw()) {
                        if ((gameMode.equals("HvsIA") && board.getCurrentPlayer() == 'b') ||
                            (gameMode.equals("IAvsH") && board.getCurrentPlayer() == 'w')) {
                            scheduleIAMove();
                        }
                    }
                } else {
                    // Désélectionner ou choisir une autre pièce
                    selectedRow = -1;
                    selectedCol = -1;
                    availableMoves = null;
                    handleClick(row, col);
                }
            }
        }
        
        /**
         * Redessine le panneau du plateau avec tous les éléments visuels.
         * Processus de rendu :
         * 
         * 1. GRILLE DU PLATEAU
         *    - Cases blanches et noires alternées (damier classique)
         *    - Couleurs personnalisées (crème et brun)
         * 
         * 2. COORDONNÉES
         *    - Chiffres 0-9 sur les bords (lignes et colonnes)
         *    - Aide pour lire les positions
         * 
         * 3. SURBRILLANCE
         *    - Pièce sélectionnée en jaune
         *    - Coups possibles en vert clair
         * 
         * 4. PIÈCES
         *    - Pions : cercles blancs (blancs) ou gris (noirs)
         *    - Dames : mêmes que pions mais avec une couronne dorée (♔ symbole)
         *    - Ombres sous les pièces pour l'effet 3D
         *    - Bordure sombre autour de chaque pièce
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Dessiner le plateau
            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    if ((r + c) % 2 == 0) {
                        g2d.setColor(new Color(240, 217, 181));
                    } else {
                        g2d.setColor(new Color(181, 136, 99));
                    }
                    g2d.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
            
            // Dessiner les coordonnées
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i < BOARD_SIZE; i++) {
                g2d.drawString(String.valueOf(i), i * CELL_SIZE + 3, 12);
                g2d.drawString(String.valueOf(i), 3, i * CELL_SIZE + 12);
            }
            
            // Surligner la pièce sélectionnée
            if (selectedRow != -1) {
                g2d.setColor(new Color(255, 255, 0, 100));
                g2d.fillRect(selectedCol * CELL_SIZE, selectedRow * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                
                // Surligner les coups possibles
                if (availableMoves != null) {
                    g2d.setColor(new Color(0, 255, 0, 100));
                    for (Move m : availableMoves) {
                        g2d.fillRect(m.endCol * CELL_SIZE, m.endRow * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
            
            // Dessiner les pièces
            Piece[][] grid = board.getGrid();
            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    Piece p = grid[r][c];
                    if (p != null) {
                        int x = c * CELL_SIZE + CELL_SIZE / 2;
                        int y = r * CELL_SIZE + CELL_SIZE / 2;
                        int radius = CELL_SIZE / 3;
                        
                        // Dessiner l'ombre de la pièce
                        g2d.setColor(new Color(0, 0, 0, 50));
                        g2d.fillOval(x - radius + 2, y - radius + 2, radius * 2, radius * 2);
                        
                        // Dessiner la pièce
                        if (p.isWhite()) {
                            g2d.setColor(Color.WHITE);
                        } else {
                            g2d.setColor(new Color(50, 50, 50));
                        }
                        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                        
                        // Dessiner la bordure
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);
                        
                        // Dessiner la couronne pour les dames
                        if (p.isDame()) {
                            g2d.setColor(new Color(255, 215, 0));
                            g2d.setFont(new Font("Arial", Font.BOLD, 24));
                            String crown = "♛";
                            FontMetrics fm = g2d.getFontMetrics();
                            int textX = x - fm.stringWidth(crown) / 2;
                            int textY = y + fm.getAscent() / 2 - 2;
                            g2d.drawString(crown, textX, textY);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Classe interne pour sauvegarde d'état du plateau.
     * Stocke une copie du plateau pour permettre l'annulation de coups.
     * 
     * Utilisage :
     * 1. Avant chaque coup humain, créer et empiler un snapshot
     * 2. Si l'humain clique sur "Annuler", dépiler et restaurer le dernier snapshot
     * 
     * Important : Le plateau est copié (pas de référence) pour éviter les pb de modification.
     */
    private static class BoardSnapshot {
        Board board;
        
        BoardSnapshot(Board board) {
            this.board = board;
        }
    }
    
    /**
     * Point d'entrée du programme.
     * 
     * Lance la création de la fenêtre principale sur le thread EDT (Event Dispatch Thread) de Swing.
     * SwingUtilities.invokeLater() garantit que :
     * 1. La création de la fenêtre se fait sur le bon thread
     * 2. L'affichage de l'interface graphique est thread-safe
     * 3. Les événements souris et clavier sont bien gérés
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameUI());
    }
}
