package Figures;

import Code.Game;

public class bishop extends Figure {
    public bishop(FigureState state) {
        super(state);
    }

    @Override
    public String getName() {
        return "b";
    }
    @Override
    public String getFullName() {
        return (String) "bishop"+state.index;
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
            int[] whitePawnTable = {
                -20,-10,-10,-10,-10,-10,-10,-20,
                -10,  0,  0,  0,  0,  0,  0,-10,
                -10,  0,  5, 10, 10,  5,  0,-10,
                -10,  5,  5, 10, 10,  5,  5,-10,
                -10,  0, 10, 10, 10, 10,  0,-10,
                -10, 10, 10, 10, 10, 10, 10,-10,
                -10,  5,  0,  0,  0,  0,  5,-10,
                -20,-10,-10,-10,-10,-10,-10,-20,
            };
            return whitePawnTable[Long.numberOfTrailingZeros(state.position)];
        }
        else{
            int[] blackPawnTable = {
                -20,-10,-10,-10,-10,-10,-10,-20,
                -10,  5,  0,  0,  0,  0,  5,-10,
                -10, 10, 10, 10, 10, 10, 10,-10,
                -10,  0, 10, 10, 10, 10,  0,-10,
                -10,  5,  5, 10, 10,  5,  5,-10,
                -10,  0,  5, 10, 10,  5,  0,-10,
                -10,  0,  0,  0,  0,  0,  0,-10,
                -20,-10,-10,-10,-10,-10,-10,-20,
            };
            return blackPawnTable[Long.numberOfTrailingZeros(state.position)];
        }
    }

    @Override
    public long generateMoves(Game game, long used_squares, long opponent_squares, boolean onlyReturnDefendingMoves, boolean onlyReturnAttackingMoves, boolean returnAllMoves) {
        long legalMoves = 0L;
        

        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, 9, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves);  // upper right
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, 7, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves);  // upper left
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, -9, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves); // lower right
        legalMoves |= moveInDirection(state.position, used_squares, opponent_squares, -7, onlyReturnDefendingMoves, onlyReturnAttackingMoves, returnAllMoves); // lower left

        return legalMoves;
    }


}