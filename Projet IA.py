
import tkinter as tk
from tkinter import messagebox
import copy
import math
import random

# -------------------- Profils IA prédéfinis --------------------
IA_PROFILES = {
    
}

# -------------------- Représentation des pièces --------------------
class Piece:
    def __init__(self, code):
        # code: 'w'/'b' pour pions, 'W'/'B' pour dames
        self.code = code

    def is_white(self):
        return self.code.lower() == 'w'

    def is_black(self):
        return self.code.lower() == 'b'

    def is_queen(self):
        return self.code in ('W', 'B')

    def promote(self):
        if self.code == 'w':
            self.code = 'W'
        elif self.code == 'b':
            self.code = 'B'

    def are_opponents(self, other):
        if other is None:
            return False
        return self.code.lower() != other.code.lower()

    def __repr__(self):
        return self.code


# -------------------- Plateau et règles --------------------
class Board:
    def __init__(self, size=10):
        assert size % 2 == 0 and size >= 8
        self.size = size
        self.grid = [[None for _ in range(size)] for _ in range(size)]
        self.init_start_position()
        self.no_capture_moves = 0  # compteur de coups sans capture
        self.history = []          # pour détecter répétitions de position
        self.history.append(self.hash())


    def init_start_position(self):
        s = self.size
        for r in range(s):
            for c in range(s):
                self.grid[r][c] = None
        rows_each = (self.size // 2) - 1
        for r in range(rows_each):
            for c in range(self.size):
                if (r + c) % 2 == 1:
                    self.grid[r][c] = Piece('b')
        for r in range(self.size - rows_each, self.size):
            for c in range(self.size):
                if (r + c) % 2 == 1:
                    self.grid[r][c] = Piece('w')

    def inside(self, r, c):
        return 0 <= r < self.size and 0 <= c < self.size

    def get(self, r, c):
        if not self.inside(r, c):
            return None
        return self.grid[r][c]

    def set(self, r, c, piece):
        if not self.inside(r, c):
            return
        self.grid[r][c] = piece

    def copy(self):
        return copy.deepcopy(self)

    def hash(self):
        return ''.join(''.join(p.code if p else '.' for p in row) for row in self.grid)
    
    # -------------------- Appliquer mouvement (officiel, final uniquement) --------------------
    def apply_move(self, move):
        # on fait le vrai coup
        self.make_move(move)

        # gestion du compteur sans capture
        captures = move.get('captures', [])
        if captures:
            self.no_capture_moves = 0
        else:
            self.no_capture_moves += 1

        # enregistrer l'état pour répétition (après coup complet, pas pendant animation)
        self.history.append(self.hash())

    
    def is_terminal(self, max_no_capture=50, max_repetition=3):
       
        w, b = self.count_pieces()

        # Un camp n'a plus de pièces
        if w == 0 or b == 0:
            return True

        # Égalité par trop de coups sans capture
        if getattr(self, 'no_capture_moves', 0) >= max_no_capture:
            return True

        # Égalité par répétition (même position vue plusieurs fois)
        if self.history.count(self.hash()) >= max_repetition:
            return True

        return False
    
    def result(self):
        """
        Retourne :
          - 'w' si les blancs ont gagné
          - 'b' si les noirs ont gagné
          - 'draw' si match nul (règles d'égalité)
          - None si la partie n'est pas clairement terminée
        """
        # Répétition de position → nul
        if self.history.count(self.hash()) >= 3:
            return 'draw'

        # Trop de coups sans capture → nul
        if getattr(self, 'no_capture_moves', 0) >= 40:
            return 'draw'

        # Matériel
        w, b = self.count_pieces()
        if w == 0 and b > 0:
            return 'b'
        if b == 0 and w > 0:
            return 'w'

        # On ne traite PLUS ici les "plus de coups légaux".
        # C'est géré dans GameUI.end_turn() en fonction du joueur au trait.
        return None


    # -------------------- Mouvements simples --------------------
    def simple_moves_from(self, r, c):
        piece = self.get(r, c)
        if not piece:
            return []
        moves = []
        if piece.is_queen():
            for dr, dc in [(-1, -1), (-1, 1), (1, -1), (1, 1)]:
                nr, nc = r + dr, c + dc
                while self.inside(nr, nc) and self.get(nr, nc) is None:
                    moves.append((nr, nc))
                    nr += dr
                    nc += dc
        else:
            dr = -1 if piece.is_white() else 1
            for dc in (-1, 1):
                nr, nc = r + dr, c + dc
                if self.inside(nr, nc) and self.get(nr, nc) is None:
                    moves.append((nr, nc))
        return moves

    # -------------------- Captures: séquences complètes --------------------
    def capture_sequences_from(self, r, c):
        sequences = []
        start_piece = self.get(r, c)
        if not start_piece:
            return []

        def dfs(cr, cc, path, captures, visited_captures):
            piece = self.get(cr, cc)
            if not piece:
                return
            found = False
            if piece.is_queen():
                for dr, dc in [(-1, -1), (-1, 1), (1, -1), (1, 1)]:
                    nr, nc = cr + dr, cc + dc
                    while self.inside(nr, nc) and self.get(nr, nc) is None:
                        nr += dr
                        nc += dc
                    if not self.inside(nr, nc):
                        continue
                    mid = self.get(nr, nc)
                    if not mid or not piece.are_opponents(mid):
                        continue
                    if (nr, nc) in visited_captures:
                        continue
                    after_r, after_c = nr + dr, nc + dc
                    while self.inside(after_r, after_c):
                        if self.get(after_r, after_c) is None:
                            m = {'start': (cr, cc), 'sequence': [(after_r, after_c)], 'captures': [(nr, nc)]}
                            undo = self.make_move(m)
                            dfs(after_r, after_c, path + [(after_r, after_c)], captures + [(nr, nc)], visited_captures | {(nr, nc)})
                            self.undo_move(undo)
                            found = True
                            after_r += dr
                            after_c += dc
                        else:
                            break
            else:
                for dr, dc in [(-1, -1), (-1, 1), (1, -1), (1, 1)]:
                    mr, mc = cr + dr, cc + dc
                    ar, ac = cr + 2*dr, cc + 2*dc
                    if not (self.inside(mr, mc) and self.inside(ar, ac)):
                        continue
                    mid = self.get(mr, mc)
                    dest = self.get(ar, ac)
                    if mid and piece.are_opponents(mid) and dest is None and (mr, mc) not in visited_captures:
                        m = {'start': (cr, cc), 'sequence': [(ar, ac)], 'captures': [(mr, mc)]}
                        undo = self.make_move(m)
                        dfs(ar, ac, path + [(ar, ac)], captures + [(mr, mc)], visited_captures | {(mr, mc)})
                        self.undo_move(undo)
                        found = True
            if not found:
                if captures:
                    sequences.append({"path": path.copy(), "captures": captures.copy()})

        dfs(r, c, [], [], set())
        return sequences

    # -------------------- Appliquer mouvement / make / undo --------------------

    def make_move(self, move):
        start = move['start']
        seq = move.get('sequence', [])
        captures = move.get('captures', [])
        r0, c0 = start
        piece = self.get(r0, c0)
        if not piece:
            raise ValueError('No piece at start in make_move')

        undo = {
            'start': (r0, c0),
            'end': seq[-1] if seq else (r0, c0),
            'piece_obj': piece,
            'orig_code': piece.code,
            'captured': [],
            'promoted': False
        }

        self.set(r0, c0, None)

        for cr, cc in captures:
            captured_piece = self.get(cr, cc)
            undo['captured'].append(((cr, cc), captured_piece))
            self.set(cr, cc, None)

        if seq:
            lr, lc = seq[-1]
            self.set(lr, lc, piece)
            if not piece.is_queen():
                if piece.is_white() and lr == 0:
                    undo['promoted'] = True
                    piece.promote()
                elif piece.is_black() and lr == self.size - 1:
                    undo['promoted'] = True
                    piece.promote()
        else:
            self.set(r0, c0, piece)

        return undo

    def undo_move(self, undo):
        start = undo['start']
        end = undo['end']
        piece = undo['piece_obj']
        er, ec = end
        cur = self.get(er, ec)
        if cur is piece:
            self.set(er, ec, None)
        else:
            self.set(er, ec, None)

        sr, sc = start
        if undo.get('promoted', False):
            piece.code = undo['orig_code']
        self.set(sr, sc, piece)

        for (cr, cc), cap_piece in undo['captured']:
            self.set(cr, cc, cap_piece)


    # -------------------- Mouvements légaux --------------------
    def legal_moves(self, player_color):
        all_captures = []
        all_simple = []
        for r in range(self.size):
            for c in range(self.size):
                p = self.get(r, c)
                if p and (p.is_white() if player_color == 'w' else p.is_black()):
                    captures = self.capture_sequences_from(r, c)
                    if captures:
                        for cap in captures:
                            all_captures.append({'start': (r, c), 'sequence': cap['path'], 'captures': cap['captures']})
                    else:
                        simples = self.simple_moves_from(r, c)
                        for s in simples:
                            all_simple.append({'start': (r, c), 'sequence': [s], 'captures': []})
        if all_captures:
            maxlen = max(len(m['captures']) for m in all_captures)
            best = [m for m in all_captures if len(m['captures']) == maxlen]
            return best
        else:
            return all_simple

    def count_pieces(self):
        w = 0
        b = 0
        for r in range(self.size):
            for c in range(self.size):
                p = self.get(r, c)
                if p:
                    if p.is_white():
                        w += 1
                    else:
                        b += 1
        return w, b

    def __str__(self):
        lines = []
        for r in range(self.size):
            row = ''
            for c in range(self.size):
                p = self.get(r, c)
                row += (p.code if p else '.') + ' '
            lines.append(row)
        return "\n".join(lines)


# -------------------- IA améliorée (heuristiques commentées) --------------------
class AI:
    def __init__(self, depth=3, weights=None):
        self.depth = depth
        default = {
            
        }
        self.weights = default if weights is None else weights.copy()
        self.transpo = {}



    #def minimax()
         


# -------------------- Self-play simple (profils VS profils) --------------------
class SelfPlayTrainer:
   
    def __init__(self, depth=2):
        self.depth = depth

    def play_game(self, profile_black, profile_white, max_moves=400):
        # profile_* must be weight dicts
        board = Board(size=10)
        ia_black = AI(depth=self.depth, weights=profile_black)
        ia_white = AI(depth=self.depth, weights=profile_white)
        current = 'w'
        move_count = 0
        while True:
            move_count += 1
            if move_count > max_moves:
                return 0
            if current == 'b':
                score, move = ia_black.minimax(board, ia_black.depth, -math.inf, math.inf, True)
                if move is None:
                    return -1
                board.apply_move(move)
                current = 'w'
            else:
                score, move = ia_white.minimax(board, ia_white.depth, -math.inf, math.inf, False)
                if move is None:
                    return 1
                board.apply_move(move)
                current = 'b'


# -------------------- Jeu + UI Tkinter --------------------
class GameUI:
    def __init__(self, root):
        self.root = root
        self.root.title('Jeu de Dames - Profils et Self-play')
        self.cell = 50
        self.board = Board(size=10)
        self.mode_ai = False
        # default AI uses Professional profile
        self.selected_profile_name = tk.StringVar(value='Professionnel')
        self.ai = AI(depth=3, weights=IA_PROFILES[self.selected_profile_name.get()])
        self.current_player = 'w'
        self.human_color = 'w'   # sera choisi aléatoirement
        self.ai_color = 'b'
        self.flip_board = False  # orientation visuelle du plateau
        self.self_play_mode = False
        self.human_history = []  # snapshots before each human move
        self.phase = 'idle'
        self.selected = None
        self.legal_moves_current = []
        self.ai_step_delay = 300

        self.create_widgets()
        self.draw()

    def create_widgets(self):
        frame = tk.Frame(self.root, bg='#2c3e50')
        frame.pack(padx=10, pady=10)
        self.label = tk.Label(frame, text='Joueur: Blancs', font=('Arial', 16, 'bold'), bg='#2c3e50', fg='white')
        self.label.pack(pady=5)

        # Canvas
        self.canvas = tk.Canvas(frame, width=self.board.size*self.cell, height=self.board.size*self.cell, bg='white')
        self.canvas.pack()
        self.canvas.bind('<Button-1>', self.on_click)

        # Controls row
        bframe = tk.Frame(frame, bg='#2c3e50')
        bframe.pack(pady=6)

        btn_reset = tk.Button(bframe, text='Nouvelle Partie', command=self.new_game, bg='#3498db', fg='white')
        btn_reset.grid(row=0, column=0, padx=4)

        self.btn_ai = tk.Button(bframe, text='Jouer vs IA', command=self.toggle_ai, bg='#e74c3c', fg='white')
        self.btn_ai.grid(row=0, column=1, padx=4)

        # Depth scale
        self.depth_scale = tk.Scale(bframe, from_=1, to=6, orient='horizontal', label='Profondeur IA')
        self.depth_scale.set(self.ai.depth)
        self.depth_scale.grid(row=0, column=2, padx=6)

        # Profile selector
        prof_label = tk.Label(bframe, text='Profil IA:', bg='#2c3e50', fg='white')
        prof_label.grid(row=0, column=3, padx=(10,2))
        prof_menu = tk.OptionMenu(bframe, self.selected_profile_name, *IA_PROFILES.keys())
        prof_menu.grid(row=0, column=4, padx=2)

        # Undo human
        self.btn_undo = tk.Button(bframe, text='Annuler dernier coup (vous)', command=self.undo_human_move, bg='#f1c40f')
        self.btn_undo.grid(row=0, column=5, padx=4)

        # Self-play buttons
        sp_frame = tk.Frame(frame, bg='#2c3e50')
        sp_frame.pack(pady=6)
        self.btn_selfplay_run = tk.Button(sp_frame, text='Self-play profils (A)', command=self.selfplay_profiles_dialog, bg='#8e44ad', fg='white')
        self.btn_selfplay_run.grid(row=0, column=0, padx=4)
        self.btn_selfplay_one = tk.Button(sp_frame, text='Self-play rapide (1 partie)', command=self.selfplay_generate_one, bg='#9b59b6', fg='white')
        self.btn_selfplay_one.grid(row=0, column=1, padx=4)

    def draw(self):
        self.canvas.delete('all')
        s = self.board.size

        # ---------- DESSIN DU PLATEAU ----------
        for vr in range(s):
            for vc in range(s):

                # Inversion visuelle si joueur = noir
                if self.flip_board:
                    r = s - 1 - vr
                    c = s - 1 - vc
                else:
                    r = vr
                    c = vc

                x1 = vc * self.cell
                y1 = vr * self.cell
                x2 = x1 + self.cell
                y2 = y1 + self.cell

                color = '#f0d9b5' if (r + c) % 2 == 0 else '#b58863'
                self.canvas.create_rectangle(x1, y1, x2, y2, fill=color, outline='')

        # ---------- DESSIN DES CASES POSSIBLES ----------
        for m in self.legal_moves_current:
            if self.selected and m['start'] == self.selected:
                for (lr, lc) in m['sequence']:

                    if self.flip_board:
                        vr = (s - 1) - lr
                        vc = (s - 1) - lc
                    else:
                        vr = lr
                        vc = lc

                    cx = vc * self.cell + self.cell // 2
                    cy = vr * self.cell + self.cell // 2

                    self.canvas.create_oval(
                        cx - 10, cy - 10,
                        cx + 10, cy + 10,
                        fill='green',
                        outline='darkgreen',
                        width=2
                    )

        # ---------- DESSIN DES PIONS ----------
        for r in range(s):
            for c in range(s):
                p = self.board.get(r, c)
                if p:

                    if self.flip_board:
                        vr = (s - 1) - r
                        vc = (s - 1) - c
                    else:
                        vr = r
                        vc = c

                    cx = vc * self.cell + self.cell // 2
                    cy = vr * self.cell + self.cell // 2
                    rad = self.cell // 3

                    if p.is_white():
                        fill = 'white'
                        outline = 'gray'
                    else:
                        fill = 'black'
                        outline = 'darkgray'

                    self.canvas.create_oval(
                        cx - rad, cy - rad,
                        cx + rad, cy + rad,
                        fill=fill,
                        outline=outline,
                        width=3
                    )

                    # Dames
                    if p.is_queen():
                        self.canvas.create_text(
                            cx, cy,
                            text='♛',
                            font=('Arial', 22),
                            fill='gold'
                        )

                    # Sélection
                    if self.selected == (r, c):
                        self.canvas.create_oval(
                            cx - rad - 3, cy - rad - 3,
                            cx + rad + 3, cy + rad + 3,
                            outline='yellow',
                            width=4
                        )

    def on_click(self, event):
        vr = event.y // self.cell
        vc = event.x // self.cell

        # inversion plateau si humain = noirs
        if self.flip_board:
            r = (self.board.size - 1) - vr
            c = (self.board.size - 1) - vc
        else:
            r = vr
            c = vc

        if not self.board.inside(r, c):
            return

        # Bloquer clic si IA doit jouer
        if self.mode_ai and self.current_player != self.human_color:
            return

        # --- Sélection d'une pièce ---
        if self.phase == 'idle':
            piece = self.board.get(r, c)
            if piece and ((piece.is_white() and self.current_player == 'w') or
                        (piece.is_black() and self.current_player == 'b')):

                self.selected = (r, c)

                self.legal_moves_current = [
                    m for m in self.board.legal_moves(self.current_player)
                    if m['start'] == self.selected
                ]

                if not self.legal_moves_current:
                    self.selected = None
                    return

                self.phase = 'selected'
                self.draw()
                return

        # --- Déplacement vers case cible ---
        if self.phase == 'selected':
            target = (r, c)
            chosen = None

            for m in self.legal_moves_current:
                if m['sequence'] and m['sequence'][-1] == target:
                    chosen = m
                    break

            if chosen:
                # --- SAUVEGARDE POUR UNDO ---
                if self.mode_ai and self.current_player == self.human_color:
                    self.human_history.append(self.board.copy())

                self.board.apply_move(chosen)
                self.end_turn()
            else:
                self.selected = None
                self.phase = 'idle'
                self.legal_moves_current = []
                self.draw()



    def end_turn(self):
        # 1. Changer de joueur (toujours après le coup joué)
        self.current_player = 'b' if self.current_player == 'w' else 'w'

        self.selected = None
        self.phase = 'idle'
        self.legal_moves_current = []

        # 2. Vérifier si le joueur AU TRAIT peut jouer
        moves = self.board.legal_moves(self.current_player)

        if not moves:
            # Le joueur qui DEVAIT jouer ne peut plus bouger → il perd
            winner = "Blancs" if self.current_player == 'b' else "Noirs"
            messagebox.showinfo("Fin de partie", f"{winner} gagnent !")
            return

        # 3. Vérifier les conditions d’égalité APRES changement de joueur
        if self.board.is_terminal():
            res = self.board.result()
            if res == 'w':
                messagebox.showinfo("Fin de partie", "Les Blancs gagnent !")
            elif res == 'b':
                messagebox.showinfo("Fin de partie", "Les Noirs gagnent !")
            else:
                messagebox.showinfo("Fin de partie", "Match nul.")
            return

        # 4. Redessiner
        self.draw()

        # 5. IA doit-elle jouer maintenant ?
        if self.mode_ai and self.current_player == self.ai_color:
            self.root.after(200, self.play_ai_turn)

    def play_ai_turn(self):
        # Joue uniquement si c'est vraiment au tour de l'IA
        if not self.mode_ai or self.current_player != self.ai_color:
            return

        self.ai.depth = self.depth_scale.get()

        # True si l'IA joue les noirs (maximisation), False si elle joue les blancs
        maximizing = (self.ai_color == 'b')

        score, move = self.ai.minimax(
            self.board,
            self.ai.depth,
            -math.inf,
            math.inf,
            maximizing
        )

        if not move:
            messagebox.showinfo('Fin de partie', "L'IA ne peut plus jouer. Vous avez gagné!")
            self.new_game()
            return

        captures = move.get('captures', []) or []
        sequence = move.get('sequence', []) or []

        # ---- CAS PRISE MULTIPLE (animation propre) ----
        if captures and sequence and len(sequence) > 1:
            steps = len(sequence)
            current_pos = move['start']

            def apply_step(i, current_pos):
                if i >= steps:
                    self.end_turn()
                    return

                next_pos = sequence[i]
                step_move = {
                    'start': current_pos,
                    'sequence': [next_pos],
                    'captures': [captures[i]] if i < len(captures) else []
                }

                self.board.make_move(step_move)
                self.draw()

                self.root.after(self.ai_step_delay, lambda: apply_step(i + 1, next_pos))

            apply_step(0, current_pos)

        # ---- COUP SIMPLE ----
        else:
            self.board.apply_move(move)
            self.end_turn()


    def new_game(self):
        import random

        # Quand on clique "Nouvelle partie" ou qu'on bascule le mode IA,
        # on revient en mode NORMAL (pas self-play visuel).
        self.self_play_mode = False

        # Profil IA
        prof = IA_PROFILES.get(
            self.selected_profile_name.get(),
            IA_PROFILES['Professionnel']
        )
        self.ai = AI(depth=self.depth_scale.get(), weights=prof)

        self.board = Board(size=10)
        self.phase = 'idle'
        self.selected = None
        self.legal_moves_current = []
        self.human_history = []

        # --- MODE SELF-PLAY ---
        if self.self_play_mode:
            self.flip_board = False
            self.current_player = 'w'
            self.label.config(text=f"Blancs : {self.profile_white}   Noirs : {self.profile_black}")
            self.draw()
            return

        # --- MODE HUMAIN vs IA ---
        if self.mode_ai:
            self.human_color = random.choice(['w', 'b'])
            self.ai_color = 'b' if self.human_color == 'w' else 'w'
            self.flip_board = (self.human_color == 'b')
            self.current_player = 'w'

            self.label.config(
                text=f"Vous êtes {'Blancs' if self.human_color == 'w' else 'Noirs'}"
            )

            # si IA est blanche → elle commence
            if self.ai_color == 'w':
                self.root.after(300, self.play_ai_turn)

        # --- MODE HUMAIN vs HUMAIN ---
        else:
            self.human_color = 'w'
            self.ai_color = None
            self.flip_board = False
            self.current_player = 'w'
            self.label.config(text='Joueur : Blancs')

        self.draw()



    def toggle_ai(self):
        # when toggling AI mode on, apply the selected profile
        self.mode_ai = not self.mode_ai
        if self.mode_ai:
            self.btn_ai.config(text='Mode: vs IA', bg='#27ae60')
            prof = IA_PROFILES.get(self.selected_profile_name.get(), IA_PROFILES['Professionnel'])
            self.ai = AI(depth=self.depth_scale.get(), weights=prof)
            self.new_game()
        else:
            self.btn_ai.config(text='Jouer vs IA', bg='#e74c3c')
            self.new_game()

    

    def undo_human_move(self):
        # Pas d'annulation en self-play (il n'y a pas de joueur humain)
        if self.self_play_mode:
            return

        if not self.human_history:
            messagebox.showinfo("Annulation", "Aucun coup humain à annuler.")
            return

        # Restaure le dernier état enregistré
        self.board = self.human_history.pop()

        # Le tour revient au joueur humain (en mode IA) ou au joueur Blanc en mode humain vs humain
        if self.mode_ai:
            self.current_player = self.human_color
        else:
            self.current_player = 'w'

        # Reset sélection
        self.phase = 'idle'
        self.selected = None
        self.legal_moves_current = []

        self.draw()




    def selfplay_generate_one(self):
        """
        Fenêtre self-play rapide : choix des deux profils, couleurs,
        profondeur et vitesse, puis un bouton pour lancer.
        """
        win = tk.Toplevel(self.root)
        win.title("Self-play rapide - 1 partie")
        win.geometry("320x260")  # <-- garanti que le bouton est visible

        # Profil A
        tk.Label(win, text="Profil A :").grid(row=0, column=0, padx=6, pady=6, sticky="w")
        varA = tk.StringVar(value=list(IA_PROFILES.keys())[0])
        tk.OptionMenu(win, varA, *IA_PROFILES.keys()).grid(row=0, column=1, padx=6, pady=6)

        # Profil B
        tk.Label(win, text="Profil B :").grid(row=1, column=0, padx=6, pady=6, sticky="w")
        varB = tk.StringVar(value=list(IA_PROFILES.keys())[1])
        tk.OptionMenu(win, varB, *IA_PROFILES.keys()).grid(row=1, column=1, padx=6, pady=6)

        # Couleurs
        tk.Label(win, text="Attribution des couleurs :").grid(row=2, column=0, padx=6, pady=6, sticky="w")
        color_choice = tk.StringVar(value="Random")
        tk.OptionMenu(win, color_choice, "Random", "A=White", "B=White").grid(row=2, column=1, padx=6, pady=6)

        # Profondeur IA
        tk.Label(win, text="Profondeur :").grid(row=3, column=0, padx=6, pady=6, sticky="w")
        depth_var = tk.IntVar(value=2)
        tk.Entry(win, textvariable=depth_var, width=6).grid(row=3, column=1, padx=6, pady=6)

        # Vitesse
        tk.Label(win, text="Vitesse animation (ms) :").grid(row=4, column=0, padx=6, pady=6, sticky="w")
        delay_var = tk.IntVar(value=self.ai_step_delay)
        tk.Entry(win, textvariable=delay_var, width=6).grid(row=4, column=1, padx=6, pady=6)

        # ---- BOUTON LANCER ----
        def launch():
            profA = varA.get()
            profB = varB.get()
            depth = depth_var.get()
            delay = delay_var.get()
            choice = color_choice.get()
            win.destroy()

            # Self-play → AUCUN joueur humain
            self.self_play_mode = True
            self.mode_ai = False  # important !

            self.visual_selfplay_one(profA, profB, choice, depth, delay)



        btn = tk.Button(
            win,
            text="Lancer et afficher",
            command=launch,
            bg="#2ecc71",
            fg="white",
            font=("Arial", 11, "bold"),
            height=1
        )
        btn.grid(row=5, column=0, columnspan=2, pady=12)

    def visual_selfplay_one(self, profileA, profileB, color_choice, depth, delay):
        # (début inchangé — initialisation, choix des IA, etc.)
        self.board = Board(size=10)
        self.draw()
        self.root.update()

        profA = IA_PROFILES.get(profileA, IA_PROFILES['Professionnel']).copy()
        profB = IA_PROFILES.get(profileB, IA_PROFILES['Professionnel']).copy()

        if color_choice == 'Random':
            if random.random() < 0.5:
                ai_white = AI(depth=depth, weights=profA)
                ai_black = AI(depth=depth, weights=profB)
            else:
                ai_white = AI(depth=depth, weights=profB)
                ai_black = AI(depth=depth, weights=profA)
        elif color_choice == 'A=White':
            ai_white = AI(depth=depth, weights=profA)
            ai_black = AI(depth=depth, weights=profB)
        else:
            ai_white = AI(depth=depth, weights=profB)
            ai_black = AI(depth=depth, weights=profA)

        current = 'w'
        move_count = 0
        max_moves = 800

        def step_game():
            nonlocal current, move_count
            move_count += 1
            if move_count > max_moves:
                messagebox.showinfo('Self-play', 'Trop de coups — match nul.')
                return

            ai = ai_white if current == 'w' else ai_black
            maximizing = (current == 'b')

            score, move = ai.minimax(self.board, depth, -math.inf, math.inf, maximizing)
            if not move:
                winner = 'Noirs' if current == 'w' else 'Blancs'
                messagebox.showinfo('Self-play', f'Partie terminée : {winner}')
                return

            captures = move.get('captures', []) or []
            sequence = move.get('sequence', []) or []
            start_r, start_c = move['start']

            # --- MULTI-CAPTURE ROBUSTE (avec undos) ---
            if captures:
                undos = []  # stocker les undo retournés par make_move pour chaque étape

                def replay_capture_step(i, r, c):
                    nonlocal current, undos
                    # si on a fini toutes les étapes intermédiaires -> restaurer et appliquer le move complet
                    if i >= len(captures):
                        # undo toutes les modifications visuelles intermédiaires (retour à l'état initial)
                        for u in reversed(undos):
                            self.board.undo_move(u)
                        undos.clear()

                        # appliquer la séquence complète de façon atomique
                        self.board.apply_move(move)
                        self.draw()

                        # vérifier fin de partie seulement après application complète
                        if self.board.is_terminal():
                            res = self.board.result()
                            if res == 'w':
                                messagebox.showinfo('Self-play', 'Partie terminée: Blancs gagnent.')
                            elif res == 'b':
                                messagebox.showinfo('Self-play', 'Partie terminée: Noirs gagnent.')
                            else:
                                messagebox.showinfo('Self-play', 'Partie terminée: Match nul.')
                            return

                        # changer de joueur et continuer
                        current = 'b' if current == 'w' else 'w'
                        self.root.after(delay, step_game)
                        return

                    # calculer la case d'arrivée pour le i-ème capture, en partant de (r,c)
                    cap_r, cap_c = captures[i]
                    dr = cap_r - r
                    dc = cap_c - c
                    end_r = cap_r + dr
                    end_c = cap_c + dc

                    step_move = {
                        'start': (r, c),
                        'sequence': [(end_r, end_c)],
                        'captures': [(cap_r, cap_c)]
                    }

                    # appliquer visuellement et garder l'undo
                    undo = self.board.make_move(step_move)
                    undos.append(undo)
                    self.draw()

                    # planifier la prochaine étape depuis la nouvelle position
                    self.root.after(delay, lambda: replay_capture_step(i+1, end_r, end_c))

                # lancer l'animation des étapes (commence à la position de départ)
                replay_capture_step(0, start_r, start_c)

            else:
                # mouvement simple (pas de captures) : appliquer et continuer
                self.board.apply_move(move)
                self.draw()

                if self.board.is_terminal():
                    res = self.board.result()
                    if res == 'w':
                        messagebox.showinfo('Self-play', 'Partie terminée: Blancs gagnent.')
                    elif res == 'b':
                        messagebox.showinfo('Self-play', 'Partie terminée: Noirs gagnent.')
                    else:
                        messagebox.showinfo('Self-play', 'Partie terminée: Match nul.')
                    return

                current = 'b' if current == 'w' else 'w'
                self.root.after(delay, step_game)

        # démarrer le jeu visuel
        self.root.after(200, step_game)


   

    



    def selfplay_profiles_dialog(self):
        # Dialog to pick two profiles and number of games
        win = tk.Toplevel(self.root)
        win.title('Self-play profils')

        tk.Label(win, text='Profil A :').grid(row=0, column=0, sticky='w', padx=6, pady=4)
        varA = tk.StringVar(value='Professionnel')
        tk.OptionMenu(win, varA, *IA_PROFILES.keys()).grid(row=0, column=1, padx=6, pady=4)

        tk.Label(win, text='Profil B :').grid(row=1, column=0, sticky='w', padx=6, pady=4)
        varB = tk.StringVar(value='Agressif')
        tk.OptionMenu(win, varB, *IA_PROFILES.keys()).grid(row=1, column=1, padx=6, pady=4)

        tk.Label(win, text='Attribution des couleurs :').grid(row=2, column=0, sticky='w', padx=6, pady=4)
        varColor = tk.StringVar(value='Random')
        tk.OptionMenu(win, varColor, "Random", "A=White", "B=White").grid(row=2, column=1, padx=6, pady=4)

        tk.Label(win, text='Nombre de parties :').grid(row=3, column=0, sticky='w', padx=6, pady=4)
        nb_var = tk.IntVar(value=20)
        tk.Entry(win, textvariable=nb_var).grid(row=3, column=1, padx=6, pady=4)

        tk.Label(win, text='Profondeur IA :').grid(row=4, column=0, sticky='w', padx=6, pady=4)
        depth_var = tk.IntVar(value=2)
        tk.Entry(win, textvariable=depth_var).grid(row=4, column=1, padx=6, pady=4)

        def launch():
            profileA = varA.get()
            profileB = varB.get()
            n = nb_var.get()
            depth = depth_var.get()
            color_choice = varColor.get()
            win.destroy()
            self.selfplay_profiles(profileA, profileB, n, depth, color_choice)

        tk.Button(win, text='Lancer', command=launch, bg='#2ecc71').grid(
            row=5, column=0, columnspan=2, pady=8
        )


    def selfplay_profiles(self, profileA, profileB, games, depth=3, color_choice="Random"):
        """
        Self-play A: A vs B — avec choix des couleurs :
        - Random
        - A = White
        - B = White
        """

        profA = IA_PROFILES.get(profileA, IA_PROFILES['Professionnel']).copy()
        profB = IA_PROFILES.get(profileB, IA_PROFILES['Professionnel']).copy()

        scoreA = 0
        scoreB = 0
        draws = 0

        count_colors = {
            profileA: {'w': 0, 'b': 0},
            profileB: {'w': 0, 'b': 0}
        }

        for g in range(games):
            board = Board()

            # --- attribution des couleurs ---
            if color_choice == "Random":
                swap = (random.random() < 0.5)
            elif color_choice == "A=White":
                swap = False
            elif color_choice == "B=White":
                swap = True
            else:
                swap = False

            if not swap:
                ai_white = AI(depth=depth, weights=profA)
                ai_black = AI(depth=depth, weights=profB)
                count_colors[profileA]['w'] += 1
                count_colors[profileB]['b'] += 1
            else:
                ai_white = AI(depth=depth, weights=profB)
                ai_black = AI(depth=depth, weights=profA)
                count_colors[profileB]['w'] += 1
                count_colors[profileA]['b'] += 1

            current = 'w'
            move_count = 0
            max_moves = 800

            while True:
                move_count += 1
                if move_count > max_moves:
                    draws += 1
                    break

                ai = ai_white if current == 'w' else ai_black
                maximizing = (current == 'b')

                score, move = ai.minimax(board, depth, -math.inf, math.inf, maximizing)

                if not move:
                    if current == 'w':
                        scoreB += 1
                    else:
                        scoreA += 1
                    break

                board.apply_move(move)

                if board.is_terminal():
                    res = board.result()

                    if res == 'w':
                        if not swap:
                            scoreA += 1
                        else:
                            scoreB += 1

                    elif res == 'b':
                        if not swap:
                            scoreB += 1
                        else:
                            scoreA += 1

                    else:
                        draws += 1
                    break


                current = 'b' if current == 'w' else 'w'

        # --- Résultat final ---
        messagebox.showinfo(
            "Résultat self-play",
            (
                f"Profil A : {profileA}\n"
                f"Profil B : {profileB}\n\n"
                f"Victoires A : {scoreA}\n"
                f"Victoires B : {scoreB}\n"
                f"Matchs nuls : {draws}\n\n"
                f"{profileA} : {count_colors[profileA]['w']} fois Blancs, "
                f"{count_colors[profileA]['b']} fois Noirs\n"
                f"{profileB} : {count_colors[profileB]['w']} fois Blancs, "
                f"{count_colors[profileB]['b']} fois Noirs"
            )
        )


# -------------------- Lancement --------------------
if __name__ == '__main__':
    root = tk.Tk()
    gui = GameUI(root)
    root.mainloop()
