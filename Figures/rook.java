package Figures;

import Code.*;

public class rook extends Figure {
    public rook(FigureState state) {
        super(state);
    }

    @Override
    public String getName() {
        return "r";
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
    public int getPieceValue(){
        return 500;
    }
    @Override
    public String getFullName() {
        return (String) "rook"+state.index;
    }

    @Override
    public int getPosValue(Game game){
        if(state.color.equals("white")){
            int[] whiteTable = {
                0,  0,  0,  0,  0,  0,  0,  0,
                5, 10, 10, 10, 10, 10, 10,  5,
               -5,  0,  0,  0,  0,  0,  0, -5,
               -5,  0,  0,  0,  0,  0,  0, -5,
               -5,  0,  0,  0,  0,  0,  0, -5,
               -5,  0,  0,  0,  0,  0,  0, -5,
               -5,  0,  0,  0,  0,  0,  0, -5,
                0,  0,  0,  5,  5,  0,  0,  0
            };
            return whiteTable[Long.numberOfTrailingZeros(state.position)];
        }
        else{
            int[] blackTable = {
                0,  0,  0,  5,  5,  0,  0,  0,
               -5,  0,  0,  0,  0,  0,  0, -5,
               -5,  0,  0,  0,  0,  0,  0, -5,
               -5,  0,  0,  0,  0,  0,  0, -5,
               -5,  0,  0,  0,  0,  0,  0, -5,
               -5,  0,  0,  0,  0,  0,  0, -5,
                5, 10, 10, 10, 10, 10, 10,  5,
                0,  0,  0,  0,  0,  0,  0,  0
            };
            return blackTable[Long.numberOfTrailingZeros(state.position)];
        }
    }

    @Override
    public long generateMoves(Game game, long used_squares, long opponent_squares, boolean onlyReturnDefendingMoves, boolean onlyReturnAttackingMoves, boolean returnAllMoves) {
        long legalMoves = 0L;
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, 1, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves);  // right
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, -1, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves); // left
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, 8, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves);  // up
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, -8, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves); // down
        return legalMoves;
    }
    public boolean isMoved = false;
}
