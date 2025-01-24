package Figures;

import Code.Game;

public class queen extends Figure {
    public queen(FigureState state) {
        super(state);
    }

    @Override
    public String getName() {
        return "q";
    }
    @Override
    public int getPieceValue(){
        return 1050;
    }
    @Override
    public String getFullName() {
        return (String) "queen"+state.index;
    }
    @Override
    public int getPossibleMoveValue(Game game){
        return count_GenerateMoves(game)  * possibleMoveMultiplier();
    }
    @Override
    public int possibleMoveMultiplier(){
        return 5;
    }
    @Override
    public int getPosValue(Game game){
        if(state.color.equals("white")){
            int[] whiteTable = {
                -20,-10,-10, -5, -5,-10,-10,-20,
                -10,  0,  0,  0,  0,  0,  0,-10,
                -10,  0,  5,  5,  5,  5,  0,-10,
                -5,  0,  5,  5,  5,  5,  0, -5,
                 0,  0,  5,  5,  5,  5,  0, -5,
                -10,  5,  5,  5,  5,  5,  0,-10,
                -10,  0,  5,  0,  0,  0,  0,-10,
                -20,-10,-10, -5, -5,-10,-10,-20
            };
            return whiteTable[Long.numberOfTrailingZeros(state.position)];
        }
        else{
            int[] blackTable = {
                -20,-10,-10, -5, -5,-10,-10,-20,
                -10,  0,  5,  0,  0,  0,  0,-10,
                -10,  5,  5,  5,  5,  5,  0,-10,
                 0,  0,  5,  5,  5,  5,  0, -5,
                -5,  0,  5,  5,  5,  5,  0, -5,
                -10,  0,  5,  5,  5,  5,  0,-10,
                -10,  0,  0,  0,  0,  0,  0,-10,
                -20,-10,-10, -5, -5,-10,-10,-20
            };
            return blackTable[Long.numberOfTrailingZeros(state.position)];
        }
    }

    @Override
    public long generateMoves(Game game, long used_squares, long opponent_squares, boolean onlyReturnDefendingMoves , boolean onlyReturnAttackingMoves, boolean returnAllMoves) {
        long legalMoves = 0L;


        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, 9, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves);  // upper right
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, 7, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves);  // upper left
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, -9, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves); // lower right
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, -7, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves); // lower left


        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, 1, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves);  // right
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, -1, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves); // left
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, 8, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves);  // up
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, -8, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves); // down
        return legalMoves;
    }

}
