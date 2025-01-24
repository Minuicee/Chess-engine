
# AI specs

    -uses multithreading
    -uses a database (pgn format)

# Important notes

    -you can only promote to a queen
    -the code for en passant is commented because it has some bugs (which means you can't en passant)
    -move repitition draw is not fully supported (only compares the last 6 half moves)

# Chess AI Bitboard Types

Bitboard types:

    -pawn: 0
    -rook: 1
    -knight: 2
    -bishop: 3
    -queen: 4
    -king: 5

# Game types

(Game.gameMode):

    -player vs. player: 0
    -vs. random bot: 1
    -against ai (depth 1): 101
    -against ai (depth 2): 102
    ...
        
# Different loops

(AI.startLoop()):

    -random vs. random: 1
    -random vs ai (depth 1): 11
    -random vs ai (depth 2): 12
    ...
    -ai vs ai (both depth 1): 101
    -ai vs ai (both depth 2): 102
    ...
