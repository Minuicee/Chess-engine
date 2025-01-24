package Figures;

import Code.Game;

public class knight extends Figure {
    public knight(FigureState state) {
        super(state);
    }

    @Override
    public String getName() {
        return "n";
    }
    @Override
    public String getFullName() {
        return (String) "knight"+state.index;
    }
    @Override
    public int getPieceValue(){
        return 325;
    }
    @Override
    public int getPossibleMoveValue(Game game){
        return count_GenerateMoves(game)  * possibleMoveMultiplier();
    }
    @Override
    public int possibleMoveMultiplier(){
        return 4;
    }
    @Override
    public int getPosValue(Game game){
        if(state.color.equals("white")){
            int[] whiteTable = {
                -50,-40,-30,-30,-30,-30,-40,-50,
                -40,-20,  0,  0,  0,  0,-20,-40,
                -30,  0, 10, 15, 15, 10,  0,-30,
                -30,  5, 15, 20, 20, 15,  5,-30,
                -30,  0, 15, 20, 20, 15,  0,-30,
                -30,  5, 10, 15, 15, 10,  5,-30,
                -40,-20,  0,  5,  5,  0,-20,-40,
                -50,-40,-30,-30,-30,-30,-40,-50,
            };
            return whiteTable[Long.numberOfTrailingZeros(state.position)];
        }
        else{
            int[] blackTable = {
                -50,-40,-30,-30,-30,-30,-40,-50,
                -40,-20,  0,  5,  5,  0,-20,-40,
                -30,  5, 10, 15, 15, 10,  5,-30,
                -30,  0, 15, 20, 20, 15,  0,-30,
                -30,  5, 15, 20, 20, 15,  5,-30,
                -30,  0, 10, 15, 15, 10,  0,-30,
                -40,-20,  0,  0,  0,  0,-20,-40,
                -50,-40,-30,-30,-30,-30,-40,-50,
            };
            return blackTable[Long.numberOfTrailingZeros(state.position)];
        }
    }

    @Override
public long generateMoves(Game game, long used_squares, long opponent_squares, boolean onlyReturnDefendingMoves, boolean onlyReturnAttackingMoves, boolean returnAllMoves) {
    long legalMoves = 0L;

    int[] knightMoves = {17, 15, 10, 6, -17, -15, -10, -6};
    int index = getIndex(state.position);

    for (int move : knightMoves) {
        long tempMove = shift(state.position, move);


        if (tempMove == 0) continue;


        if ((index % 8 == 0 && (move == -10 || move == 15 || move == -17 || move == 6)) || 
            (index % 8 == 1 && (move == -10 || move == 6)) || 
            (index % 8 == 6 && (move == -6 || move == 10)) || 
            (index % 8 == 7 && (move == -15 || move == -6 || move == 10 || move == 17))) {
            continue;
        }


        if (!onlyReturnDefendingMoves && !onlyReturnAttackingMoves && !returnAllMoves) {
            if ((tempMove & used_squares) == 0) {
                legalMoves |= tempMove;
            } else if ((tempMove & opponent_squares) != 0) {
                legalMoves |= tempMove;
            }
        }
        else if( returnAllMoves){
            legalMoves |= tempMove;
        }

        else if (onlyReturnDefendingMoves) {
            if ((tempMove & used_squares) != 0) {
                legalMoves |= tempMove;
            }
        }

        else if (onlyReturnAttackingMoves) {
            if ((tempMove & opponent_squares) != 0) {
                legalMoves |= tempMove;
            }
        }
    }
    return legalMoves;
}



    public int getIndex(long position) {
        int index = Long.numberOfTrailingZeros(position);
        return index;
    }



}
