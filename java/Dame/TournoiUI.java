import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Interface de tournoi automatisé pour comparer les performances des différents profils d'IA.
 * 
 * Fonctionnalités :
 * - Format round-robin optimisé : chaque profil joue contre tous les autres
 * - Optimisation : n×(n-1)/2 matchs au lieu de n×(n-1) (évite les doublons A vs B / B vs A)
 * - Statistiques détaillées par couleur (victoires/défaites/nuls en tant que blancs et noirs)
 * - Métriques de performance : nœuds moyens visités, temps moyen par coup
 * - Système de points : Victoire = 3pts, Nul = 1pt, Défaite = 0pt
 * - Export CSV automatique et manuel avec échappement correct des caractères spéciaux
 * - Barre de progression et journal détaillé en temps réel
 * - Configuration du nombre de parties par match (2-100, pair)
 * - Sélection flexible des profils participants (9 profils disponibles)
 * 
 * Les 9 profils disponibles :
 * Perdant, Intermédiaire, Expert, Agressif, Défensif, Poids Random, Joue Random, Équilibre, Monte-Carlo
 * 
 * @author Ulysse Ansoux
 * @author Noé Vander Schueren
 * @author Léo Maquin-Testud
 * @version 1.0
 */
public class TournoiUI extends JFrame {
    /** Tableau des résultats du tournoi */
    private JTable resultsTable;
    /** Modèle de données du tableau */
    private DefaultTableModel tableModel;
    /** Zone de texte pour le journal du tournoi */
    private JTextArea logArea;
    /** Barre de progression du tournoi */
    private JProgressBar progressBar;
    /** Bouton de démarrage du tournoi */
    private JButton startButton;
    /** Bouton d'arrêt du tournoi */
    private JButton stopButton;
    /** Bouton d'export CSV */
    private JButton exportButton;
    /** Sélecteur du nombre de parties par match */
    private JSpinner gamesPerMatchSpinner;
    /** Sélecteur de la profondeur de recherche pour Minimax */
    private JSpinner depthSpinner;
    /** Sélecteur du nombre de simulations pour Monte-Carlo */
    private JSpinner MCSimsSpinner;
    
    /** Indique si un tournoi est en cours */
    private volatile boolean running = false;
    /** Gestionnaire du tournoi */
    private tournoiManager manager;
    
    /** Cases à cocher pour sélectionner les profils participants */
    private JCheckBox[] profileCheckboxes;
    /** Liste de tous les profils disponibles */
    private String[] allProfiles = {"Perdant", "Intermédiaire", "Expert", "Agressif", "Défensif", "Poids Random", "Joue Random", "Équilibre", "Monte-Carlo"};
    
    public TournoiUI() {
        setTitle("Tournoi d'IA - Jeu de Dames");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());
        
        // Panel de configuration
        JPanel configPanel = createConfigPanel();
        add(configPanel, BorderLayout.NORTH);
        
        // Panel central avec table de résultats
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        String[] columns = {"Profil", "Victoires", "V.Blancs", "V.Noirs", "Défaites", "D.Blancs", "D.Noirs", 
                           "Nuls", "N.Blancs", "N.Noirs", "% Victoire", "Coups moy.", "Temps/coup (ms)", "Nœuds moy.", "Points"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Ajuster la largeur des colonnes
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Profil
        resultsTable.getColumnModel().getColumn(10).setPreferredWidth(80); // % Victoire
        resultsTable.getColumnModel().getColumn(11).setPreferredWidth(90); // Coups moy.
        resultsTable.getColumnModel().getColumn(12).setPreferredWidth(120); // Temps/coup
        resultsTable.getColumnModel().getColumn(13).setPreferredWidth(100); // Nœuds moy.
        
        JScrollPane tableScroll = new JScrollPane(resultsTable);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel du bas avec log et contrôles
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Journal du tournoi"));
        bottomPanel.add(logScroll, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new FlowLayout());
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(200, 25));
        
        startButton = new JButton("Démarrer le tournoi");
        startButton.addActionListener(e -> starttournoi());
        
        stopButton = new JButton("Arrêter");
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stoptournoi());
        
        exportButton = new JButton("Exporter CSV");
        exportButton.addActionListener(e -> exportResults());
        
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(progressBar);
        controlPanel.add(exportButton);
        
        bottomPanel.add(controlPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Crée le panneau de configuration du tournoi.
     * 
     * Composants :
     * 1. SÉLECTION DES PROFILS (3x3 checkbox grid)
     *    - 9 profils disponibles, tous sélectionnés par défaut
     *    - Boutons "Tout sélectionner" et "Tout désélectionner" pour commodité
     * 
     * 2. OPTIONS DE TOURNOI
     *    - Nombre de parties par match (pair, 2-100) :
     *      La valeur doit être paire pour équilibrer les couleurs (moitié en blanc, moitié en noir)
     *    - Profondeur Minimax (1-8) : contrôle la force de recherche
     *    - Simulations MC (50-2000) : contrôle la force du Monte-Carlo
     * 
     * @return Le panneau de configuration avec tous les contrôles
     */
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Configuration du tournoi"));
        
        // Panel de sélection des profils
        JPanel profilePanel = new JPanel(new GridLayout(3, 3, 10, 5));
        profilePanel.setBorder(BorderFactory.createTitledBorder("Profils participants"));
        
        profileCheckboxes = new JCheckBox[allProfiles.length];
        for (int i = 0; i < allProfiles.length; i++) {
            profileCheckboxes[i] = new JCheckBox(allProfiles[i], true);
            profilePanel.add(profileCheckboxes[i]);
        }
        
        panel.add(profilePanel, BorderLayout.CENTER);
        
        // Panel d'options
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.add(new JLabel("Parties par match (pair):"));
        gamesPerMatchSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 100, 2));
        optionsPanel.add(gamesPerMatchSpinner);
        
        optionsPanel.add(Box.createHorizontalStrut(20));
        optionsPanel.add(new JLabel("Profondeur Minimax:"));
        depthSpinner = new JSpinner(new SpinnerNumberModel(6, 1, 8, 1));
        optionsPanel.add(depthSpinner);
        
        optionsPanel.add(Box.createHorizontalStrut(20));
        optionsPanel.add(new JLabel("Simulations MC:"));
        MCSimsSpinner = new JSpinner(new SpinnerNumberModel(300, 50, 2000, 50));
        optionsPanel.add(MCSimsSpinner);
        
        JButton selectAllBtn = new JButton("Tout sélectionner");
        selectAllBtn.addActionListener(e -> {
            for (JCheckBox cb : profileCheckboxes) cb.setSelected(true);
        });
        
        JButton deselectAllBtn = new JButton("Tout désélectionner");
        deselectAllBtn.addActionListener(e -> {
            for (JCheckBox cb : profileCheckboxes) cb.setSelected(false);
        });
        
        optionsPanel.add(selectAllBtn);
        optionsPanel.add(deselectAllBtn);
        
        panel.add(optionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Lance le tournoi automatisé.
     * 
     * Processus :
     * 1. Récupérer les profils sélectionnés par l'utilisateur
     * 2. Vérifier qu'au moins 2 profils sont sélectionnés
     * 3. Initialiser les éléments d'interface (tableau, journal)
     * 4. Créer un gestionnaire de tournoi avec les paramètres
     * 5. Lancer le tournoi dans un thread séparé pour ne pas bloquer l'interface
     * 6. Faire l'export automatique à la fin
     * 
     * Note : Le tournoi utilise le format round-robin optimisé :
     * - Chaque paire de profils joue exactement une fois
     * - Les parties sont équilibrées en couleurs (moitié en blanc, moitié en noir)
     * - Total de matches = n×(n-1)/2 × gamesPerMatch
     */
    private void starttournoi() {
        // Récupérer les profils sélectionnés
        List<String> selectedProfiles = new ArrayList<>();
        for (int i = 0; i < profileCheckboxes.length; i++) {
            if (profileCheckboxes[i].isSelected()) {
                selectedProfiles.add(allProfiles[i]);
            }
        }
        
        if (selectedProfiles.size() < 2) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner au moins 2 profils pour le tournoi",
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int gamesPerMatch = (Integer) gamesPerMatchSpinner.getValue();
        int depth = (Integer) depthSpinner.getValue();
        int MCSims = (Integer) MCSimsSpinner.getValue();
        
        // Initialiser le tableau
        tableModel.setRowCount(0);
        logArea.setText("");
        
        running = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        
        // Lancer le tournoi dans un thread séparé
        manager = new tournoiManager(selectedProfiles, gamesPerMatch, depth, MCSims);
        new Thread(() -> {
            manager.runtournoi(this);
            SwingUtilities.invokeLater(() -> {
                running = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                progressBar.setValue(100);
                log("=== TOURNOI TERMINÉ ===");
                exportToFile();
            });
        }).start();
    }
    
    /**
     * Arrête le tournoi en cours.
     * 
     * Actions :
     * 1. Mettre à jour le flag running à false
     * 2. Notifier le gestionnaire de tournoi pour arrêter les parties en cours
     * 3. Réactiver les boutons (Start au lieu de Stop)
     * 4. Enregistrer le message d'arrêt dans le journal
     */
    private void stoptournoi() {
        running = false;
        if (manager != null) {
            manager.stop();
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        log("Tournoi arrêté par l'utilisateur");
    }
    
    /**
     * Met à jour la barre de progression du tournoi.
     * Appelé depuis le thread du tournoi pour signaler l'avancement.
     * Utilise SwingUtilities.invokeLater() pour thread-safety.
     * 
     * @param value Pourcentage de progression (0-100)
     */
    public void updateProgress(int value) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(value));
    }
    
    /**
     * Enregistre un message dans le journal du tournoi.
     * Les messages sont affichés en temps réel dans une zone de texte.
     * Le curseur se déplace automatiquement à la fin pour les derniers messages.
     * 
     * @param message Le message à afficher
     */
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * Met à jour le tableau des résultats du tournoi.
     * Cette méthode est appelée après chaque partie pour afficher les statistiques en temps réel.
     * 
     * Colonnes affichées :
     * - Profil : nom du profil d'IA
     * - Victoires/Défaites/Nuls : totaux
     * - V.Blancs/V.Noirs, D.Blancs/D.Noirs, N.Blancs/N.Noirs : par couleur
     * - % Victoire : pourcentage de victoires
     * - Coups moy. : nombre moyen de coups par partie
     * - Temps/coup : temps moyen en millisecondes
     * - Nœuds moy. : nombre moyen de nœuds visités (Minimax)
     * - Points : total (Victoire=3, Nul=1, Défaite=0)
     * 
     * @param stats Map des statistiques pour chaque profil
     */
    public void updateResults(Map<String, ProfileStats> stats) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            
            // Trier par points décroissants
            List<Map.Entry<String, ProfileStats>> sorted = new ArrayList<>(stats.entrySet());
            sorted.sort((a, b) -> Integer.compare(b.getValue().getPoints(), a.getValue().getPoints()));
            
            for (Map.Entry<String, ProfileStats> entry : sorted) {
                String profile = entry.getKey();
                ProfileStats s = entry.getValue();
                
                double winRate = s.totalGames > 0 ? (s.wins * 100.0 / s.totalGames) : 0;
                double avgMoves = s.totalGames > 0 ? (s.totalMoves * 1.0 / s.totalGames) : 0;
                double avgTime = s.totalMoves > 0 ? (s.totalMoveTime * 1.0 / s.totalMoves) : 0;
                double avgNodes = s.totalMoves > 0 ? (s.totalNodesVisited * 1.0 / s.totalMoves) : 0;
                
                tableModel.addRow(new Object[] {
                    profile,
                    s.wins,
                    s.winsAsWhite,
                    s.winsAsBlack,
                    s.losses,
                    s.lossesAsWhite,
                    s.lossesAsBlack,
                    s.draws,
                    s.drawsAsWhite,
                    s.drawsAsBlack,
                    String.format("%.1f%%", winRate),
                    String.format("%.1f", avgMoves),
                    String.format("%.1f", avgTime),
                    String.format("%.0f", avgNodes),
                    s.getPoints()
                });
            }
        });
    }
    
    /**
     * Exporte les résultats du tournoi via une boîte de dialogue fichier.
     * 
     * Processus :
     * 1. Vérifier qu'il y a des résultats (au moins une ligne dans le tableau)
     * 2. Afficher un sélecteur fichier pré-rempli avec "tournoi_resultats.csv"
     * 3. Exporter au format CSV avec échappement correct des caractères spéciaux
     * 4. Afficher un message de confirmation ou d'erreur
     */
    private void exportResults() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Aucun résultat à exporter", 
                                         "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("tournoi_resultats.csv"));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                exportToCSV(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Résultats exportés avec succès!", 
                                             "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'export: " + ex.getMessage(), 
                                             "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Exporte les résultats automatiquement dans un fichier CSV à la fin du tournoi.
     * 
     * Processus :
     * 1. Créer un fichier "tournoi_resultats_auto.csv" dans le répertoire courant
     * 2. Exporter le tableau des résultats au format CSV
     * 3. Afficher le chemin complet du fichier dans le journal
     * 4. Gérer les erreurs de fichier (permissions, disque plein, etc.)
     */
    private void exportToFile() {
        try {
            File file = new File("tournoi_resultats_auto.csv");
            exportToCSV(file);
            log("Résultats sauvegardés dans: " + file.getAbsolutePath());
        } catch (IOException ex) {
            log("Erreur lors de la sauvegarde automatique: " + ex.getMessage());
        }
    }
    
    /**
     * Exporte le tableau des résultats au format CSV.
     * 
     * Format CSV :
     * - En-têtes : noms de colonnes du tableau
     * - Données : une ligne par profil avec toutes les statistiques
     * - Séparateur : virgule
     * - Escape : guillemets doublés (") pour les valeurs contenant des virgules
     * 
     * @param file Le fichier de destination
     * @throws IOException Si une erreur d'écriture se produit
     */
    private void exportToCSV(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // En-têtes
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.print(escapeCsvValue(tableModel.getColumnName(i)));
                if (i < tableModel.getColumnCount() - 1) writer.print(",");
            }
            writer.println();
            
            // Données
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(row, col);
                    writer.print(escapeCsvValue(value != null ? value.toString() : ""));
                    if (col < tableModel.getColumnCount() - 1) writer.print(",");
                }
                writer.println();
            }
        }
    }
    
    /**
     * Échappe une valeur pour l'export CSV selon la norme RFC 4180.
     * 
     * Règles d'échappement :
     * 1. Si la valeur ne contient aucun caractère spécial, la retourner as-is
     * 2. Si elle contient une virgule, un guillemet ou une nouvelle ligne :
     *    - Doubler tous les guillemets (" → "")
     *    - Encadrer avec des guillemets ("valeur")
     * 
     * @param value La valeur à échapper
     * @return La valeur échappée selon les règles CSV
     */
    private String escapeCsvValue(String value) {
        if (value == null) return "";
        
        // Si la valeur contient une virgule, un guillemet ou une nouvelle ligne, l'encadrer avec des guillemets
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // Échapper les guillemets en les doublant
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        
        return value;
    }
    
    /**
     * Vérifie si un tournoi est actuellement en cours d'exécution.
     * @return true si un tournoi s'exécute, false sinon
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Point d'entrée du programme de tournoi.
     * 
     * Lance la fenêtre du tournoi sur le thread EDT (Event Dispatch Thread) de Swing.
     * SwingUtilities.invokeLater() garantit que :
     * 1. La création de la fenêtre se fait sur le bon thread
     * 2. L'affichage de l'interface graphique est thread-safe
     * 3. Les événements de souris et clavier sont bien gérés
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TournoiUI());
    }
}

/**
 * Classe pour gérer les statistiques d'un profil d'IA.
 * 
 * Attributs :
 * - Wins/Losses/Draws : totaux dans le tournoi
 * - WinsAsWhite/WinsAsBlack, etc. : ventilation par couleur
 * - avgMovesPerGame : nombre moyen de coups par partie
 * - totalGameTime : temps total d'exécution (en ms)
 * - totalNodesVisited : nombre total de nœuds explorés (Minimax)
 * 
 * Utilisage :
 * 1. Créer une instance par profil au démarrage du tournoi
 * 2. Mettre à jour les statistiques après chaque partie
 * 3. Calculer les moyennes (getPoints, getAverageNodes, etc.) pour l'affichage
 */
class ProfileStats {
    int wins = 0;
    int winsAsWhite = 0;
    int winsAsBlack = 0;
    int losses = 0;
    int lossesAsWhite = 0;
    int lossesAsBlack = 0;
    int draws = 0;
    int drawsAsWhite = 0;
    int drawsAsBlack = 0;
    int totalGames = 0;
    int totalMoves = 0;
    long totalMoveTime = 0; // en millisecondes
    long totalNodesVisited = 0; // Total des nœuds parcourus
    
    public int getPoints() {
        return wins * 3 + draws * 1;
    }
}

/**
 * Classe pour gérer l'exécution du tournoi.
 * 
 * Responsabilités :
 * 1. Organiser les matches (format round-robin optimisé)
 * 2. Gérer les statistiques de chaque profil
 * 3. Exécuter les parties et mettre à jour les résultats
 * 4. Permettre l'arrêt du tournoi (flag running)
 * 
 * Algorithme du tournoi :
 * - Pour chaque paire (i, j) avec i < j :
 *   * Jouer gamesPerMatch/2 avec i en blanc, j en noir
 *   * Jouer gamesPerMatch/2 avec j en blanc, i en noir
 * - Total : n×(n-1)/2 × gamesPerMatch parties
 * 
 * Thread-safety :
 * - Chaque partie est jouée complètement avant la suivante
 * - Les statistiques sont mises à jour immédiatement
 * - Le flag running peut être modifié par d'autres threads
 */
// Classe pour gérer le tournoi
class tournoiManager {
    private List<String> profiles;
    private int gamesPerMatch;
    private int depth;
    private int MCSims;
    private Map<String, ProfileStats> stats;
    private volatile boolean running = true;
    
    /**
     * Constructeur du gestionnaire de tournoi.
     * 
     * @param profiles Liste des profils d'IA participants
     * @param gamesPerMatch Nombre de parties par match (doit être pair pour équilibrer les couleurs)
     * @param depth Profondeur de recherche pour Minimax (1-8)
     * @param MCSims Nombre de simulations pour Monte-Carlo
     */
    public tournoiManager(List<String> profiles, int gamesPerMatch, int depth, int MCSims) {
        this.profiles = profiles;
        this.gamesPerMatch = gamesPerMatch;
        this.depth = depth;
        this.MCSims = MCSims;
        this.stats = new HashMap<>();
        
        for (String profile : profiles) {
            stats.put(profile, new ProfileStats());
        }
    }
    
    /**
     * Arrête l'exécution du tournoi.
     * Utilisé par TournoiUI.stopButton pour interruption utilisateur.
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Lance le tournoi avec tous les profils sélectionnés.
     * 
     * Format du tournoi :
     * - Round-robin optimisé : chaque paire joue exactement une fois
     * - Équilibre des couleurs : chaque profil joue moitié en blanc, moitié en noir
     * - Nombre total de parties = n×(n-1)/2 × gamesPerMatch
     * 
     * Boucle principale :
     * 1. Pour chaque paire de profils (i < j) :
     *    - Profil i en blanc, profil j en noir pour la première moitié
     *    - Profil j en blanc, profil i en noir pour la deuxième moitié
     * 2. Pour chaque partie :
     *    - Jouer jusqu'à terminal (victoire, nul, ou limite 400 coups)
     *    - Mettre à jour les statistiques
     *    - Rafraîchir l'interface (barre de progression, tableau)
     * 
     * Thread-safety :
     * - Tous les appels à l'interface via SwingUtilities.invokeLater
     * - Le flag running peut arrêter le tournoi à tout moment
     * 
     * @param ui La fenêtre du tournoi pour afficher les mises à jour
     */
    public void runtournoi(TournoiUI ui) {
        // Calculer le nombre de matches sans redondance (combinaison, pas permutation)
        int numMatches = (profiles.size() * (profiles.size() - 1)) / 2;
        int totalMatches = numMatches * gamesPerMatch;
        int completedMatches = 0;
        
        ui.log("=== DÉBUT DU TOURNOI ===");
        ui.log(profiles.size() + " profils participants");
        ui.log(gamesPerMatch + " partie(s) par match");
        ui.log("Profondeur Minimax: " + depth);
        ui.log("Simulations MC: " + MCSims);
        ui.log("Total: " + totalMatches + " parties");
        ui.log("");
        
        // Chaque paire de profils joue UNE SEULE FOIS (avec couleurs équilibrées)
        for (int i = 0; i < profiles.size() && running; i++) {
            for (int j = i + 1; j < profiles.size() && running; j++) {
                String profile1 = profiles.get(i);
                String profile2 = profiles.get(j);
                
                ui.log(String.format("Match: %s vs %s", profile1, profile2));
                
                // Jouer la moitié des parties avec profile1 en blanc
                for (int game = 0; game < gamesPerMatch/2 && running; game++) {
                    GameResult result = playGame(profile1, profile2, ui);
                    updateStats(profile1, profile2, result, true, ui, game+1);
                    
                    completedMatches++;
                    ui.updateProgress((int)(completedMatches * 100.0 / totalMatches));
                    ui.updateResults(stats);
                }
                
                // Jouer l'autre moitié avec couleurs inversées
                for (int game = 0; game < gamesPerMatch/2 && running; game++) {
                    GameResult result = playGame(profile2, profile1, ui);
                    updateStats(profile2, profile1, result, false, ui, game+1+gamesPerMatch/2);
                    
                    completedMatches++;
                    ui.updateProgress((int)(completedMatches * 100.0 / totalMatches));
                    ui.updateResults(stats);
                }
                
                ui.log("");
            }
        }
    }
    
    /**
     * Joue une partie complète entre deux profils.
     * 
     * Processus :
     * 1. Créer un nouveau plateau vierge
     * 2. Instancier les IA selon leurs profils (Minimax ou MC)
     * 3. Boucle de jeu :
     *    - Faire jouer le joueur actuel
     *    - Appliquer le coup
     *    - Vérifier la condition terminale
     *    - Limiter à 400 coups maximum (règle FMJD)
     * 4. Retourner le résultat (gagnant, nombre de coups, temps, nœuds)
     * 
     * @param whiteProfile Le profil pour le joueur blanc
     * @param blackProfile Le profil pour le joueur noir
     * @param ui La fenêtre du tournoi (pour possible interruption)
     * @return Le résultat de la partie
     */
    private GameResult playGame(String whiteProfile, String blackProfile, TournoiUI ui) {
        Board board = new Board();
        IA whiteIA = createIA('w', whiteProfile);
        IA blackIA = createIA('b', blackProfile);
        IA_MC whiteMC = whiteProfile.equals("Monte-Carlo") ? new IA_MC('w', MCSims) : null;
        IA_MC blackMC = blackProfile.equals("Monte-Carlo") ? new IA_MC('b', MCSims) : null;
        
        int moveCount = 0;
        long whiteTotalTime = 0;
        long blackTotalTime = 0;
        long whiteNodesVisited = 0;
        long blackNodesVisited = 0;
        int maxMoves = 400; // Limite pour éviter les parties infinies
        
        while (!board.isTerminalWithDraw() && moveCount < maxMoves && running) {
            char currentPlayer = board.getCurrentPlayer();
            
            long startTime = System.currentTimeMillis();
            Move move = null;
            
            if (currentPlayer == 'w') {
                if (whiteMC != null) {
                    move = whiteMC.bestMove(board);
                } else {
                    move = whiteIA.bestMove(board);
                    whiteNodesVisited += whiteIA.getNodesVisited();
                }
            } else {
                if (blackMC != null) {
                    move = blackMC.bestMove(board);
                } else {
                    move = blackIA.bestMove(board);
                    blackNodesVisited += blackIA.getNodesVisited();
                }
            }
            
            long moveTime = System.currentTimeMillis() - startTime;
            
            if (move == null) break;
            
            board.makeMove(move);
            // Changer le joueur courant après chaque coup
            board.setCurrentPlayer(currentPlayer == 'w' ? 'b' : 'w');
            moveCount++;
            
            if (currentPlayer == 'w') {
                whiteTotalTime += moveTime;
            } else {
                blackTotalTime += moveTime;
            }
        }
        
        GameResult result = new GameResult();
        
        // Si on a atteint la limite de coups (400), c'est un nul par limite
        if (moveCount >= maxMoves) {
            result.winner = 'l'; // 'l' pour limit (limite)
        } else {
            result.winner = board.winner();
        }
        
        result.moves = moveCount;
        result.whiteTotalTime = whiteTotalTime;
        result.blackTotalTime = blackTotalTime;
        result.whiteNodesVisited = whiteNodesVisited;
        result.blackNodesVisited = blackNodesVisited;
        
        return result;
    }
    
    /**
     * Met à jour les statistiques après une partie.
     * 
     * Mises à jour :
     * 1. Incrémenter les victoires/défaites/nuls pour les deux profils
     * 2. Mettre à jour les statistiques par couleur (AsWhite/AsBlack)
     * 3. Enregistrer les métriques (coups moyens, temps, nœuds)
     * 4. Afficher le résultat dans le journal
     * 
     * @param whiteProfile Le profil qui jouait en blanc
     * @param blackProfile Le profil qui jouait en noir
     * @param result Le résultat de la partie (gagnant, stats)
     * @param firstSet true si c'est la première moitié (couleurs non inversées), false sinon
     * @param ui La fenêtre du tournoi
     * @param gameNum Le numéro de la partie dans la séquence (pour affichage)
     */
    private void updateStats(String whiteProfile, String blackProfile, GameResult result, boolean firstSet, TournoiUI ui, int gameNum) {
        ProfileStats whiteStats = stats.get(whiteProfile);
        ProfileStats blackStats = stats.get(blackProfile);
        
        whiteStats.totalGames++;
        blackStats.totalGames++;
        whiteStats.totalMoves += result.moves;
        blackStats.totalMoves += result.moves;
        whiteStats.totalMoveTime += result.whiteTotalTime;
        blackStats.totalMoveTime += result.blackTotalTime;
        whiteStats.totalNodesVisited += result.whiteNodesVisited;
        blackStats.totalNodesVisited += result.blackNodesVisited;
        
        if (result.winner == 'w') {
            whiteStats.wins++;
            whiteStats.winsAsWhite++;
            blackStats.losses++;
            blackStats.lossesAsBlack++;
            ui.log("  Partie " + gameNum + ": Victoire " + whiteProfile + " (Blancs) en " + result.moves + " coups");
        } else if (result.winner == 'b') {
            blackStats.wins++;
            blackStats.winsAsBlack++;
            whiteStats.losses++;
            whiteStats.lossesAsWhite++;
            ui.log("  Partie " + gameNum + ": Victoire " + blackProfile + " (Noirs) en " + result.moves + " coups");
        } else if (result.winner == 'd') {
            whiteStats.draws++;
            whiteStats.drawsAsWhite++;
            blackStats.draws++;
            blackStats.drawsAsBlack++;
            ui.log("  Partie " + gameNum + ": Match nul (égalité règles internationales) après " + result.moves + " coups");
        } else if (result.winner == 'l') {
            whiteStats.draws++;
            whiteStats.drawsAsWhite++;
            blackStats.draws++;
            blackStats.drawsAsBlack++;
            ui.log("  Partie " + gameNum + ": Match nul (limite de " + 400 + " coups atteinte) après " + result.moves + " coups");
        } else {
            whiteStats.draws++;
            whiteStats.drawsAsWhite++;
            blackStats.draws++;
            blackStats.drawsAsBlack++;
            ui.log("  Partie " + gameNum + ": Match nul (raison inconnue) après " + result.moves + " coups");
        }
    }
    
    /**
     * Crée une IA du profil spécifié.
     * 
     * @param color Couleur de l'IA ('w' pour blanc, 'b' pour noir)
    * @param profile Nom du profil ("Perdant", "Expert", etc.)
     * @return Une nouvelle instance d'IA Minimax avec les paramètres configurés
     *         (ou null si le profil est "Monte-Carlo", cas traité à part)
     */
    private IA createIA(char color, String profile) {
        if (profile.equals("Monte-Carlo")) {
            return null; // Utiliser IA_MC à la place
        }
        return new IA(color, depth, profile);
    }
}

/**
 * Classe pour stocker le résultat d'une partie.
 * 
 * Attributs :
 * - winner : 'w' (blanc gagne), 'b' (noir gagne), 'd' (nul par règles), 'l' (limite 400 coups)
 * - moves : nombre total de coups joués
 * - whiteTotalTime, blackTotalTime : temps d'exécution de chaque joueur (ms)
 * - whiteNodesVisited, blackNodesVisited : nombre de nœuds explorés (Minimax uniquement)
 * 
 * Utilisage :
 * 1. Remplie à la fin de chaque partie
 * 2. Passée à updateStats() pour mise à jour des statistiques du tournoi
 */
class GameResult {
    char winner;  // 'w', 'b', 'd' (nul règles), 'l' (limite coups), ou ' ' (en cours)
    int moves;
    long whiteTotalTime;
    long blackTotalTime;
    long whiteNodesVisited;
    long blackNodesVisited;
}
