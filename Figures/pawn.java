package Figures;

import Code.Game;



public class pawn extends Figure {
    public pawn(FigureState state) {
        super(state);
    }

    @Override
    public String getName() {
        return "p";
    }
    @Override
    public int getPieceValue(){
        return 100;
    }
    @Override
    public String getFullName() {
        return (String) "pawn"+state.index;
    }
    @Override
    public int getPossibleMoveValue(Game game){
        return count_GenerateMoves(game)  * possibleMoveMultiplier();
    }
    @Override
    public int possibleMoveMultiplier(){
        return 2;
    }

    @Override
    public int getPosValue(Game game) {
        int position = Long.numberOfTrailingZeros(state.position);
        int missingPieces = 32 - (game.white.figure_map.size() + game.black.figure_map.size());

        boolean isWhite = state.color.equals("white");
        int[] table = isWhite ? whiteTable : blackTable;
        int[] endgameTable = isWhite ? whiteEndgameTable : blackEndgameTable;

        int difference = endgameTable[position] - table[position];
        int multiplier = isPassedPawn(game) ? 2 : 4;
        return table[position] + ((int)((difference / 32.0) * missingPieces) * multiplier);
    }

    // Statische Tabellen als Felder
    private static final int[] whiteTable = {
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5,  5, 10, 25, 25, 10,  5,  5,
        0,  0,  0, 20, 21,  0,  0,  0,
        5, -5,-10,  0,  0,-10, -5,  5,
        5, 10, 10,-20,-20, 10, 10,  5,
        0,  0,  0,  0,  0,  0,  0,  0
    };
    private static final int[] whiteEndgameTable = {
        0,  0,  0,  0,  0,  0,  0,  0,
        60, 60, 60, 60, 60, 60, 60, 60,
        50, 50, 50, 50, 50, 50, 50, 50,
        40, 40, 40, 40, 40, 40, 40, 40,
        30, 30, 30, 30, 30, 30, 30, 30,
        20, 20, 20, 20, 20, 20, 20, 20,
        10, 10, 10, 10, 10, 10, 10, 10,
        0,  0,  0,  0,  0,  0,  0,  0
    };
    private static final int[] blackTable = {
        0,  0,  0,  0,  0,  0,  0,  0,
        5, 10, 10,-20,-20, 10, 10,  5,
        5, -5,-10,  0,  0,-10, -5,  5,
        0,  0,  0, 21, 20,  0,  0,  0,
        5,  5, 10, 25, 25, 10,  5,  5,
        10, 10, 20, 30, 30, 20, 10, 10,
        50, 50, 50, 50, 50, 50, 50, 50,
        0,  0,  0,  0,  0,  0,  0,  0
    };
    private static final int[] blackEndgameTable = {
        0,  0,  0,  0,  0,  0,  0,  0,
        10, 10, 10, 10, 10, 10, 10, 10,
        20, 20, 20, 20, 20, 20, 20, 20,
        30, 30, 30, 30, 30, 30, 30, 30,
        40, 40, 40, 40, 40, 40, 40, 40,
        50, 50, 50, 50, 50, 50, 50, 50,
        60, 60, 60, 60, 60, 60, 60, 60,
        0,  0,  0,  0,  0,  0,  0,  0
    };



    @Override
    public long generateMoves(Game game, long used_squares, long opponent_squares, boolean onlyReturnDefendingMoves , boolean onlyReturnAttackingMoves, boolean returnAllMoves) {
        long legalMoves = 0L;
        int direction = state.color.equals("black") ? 8 : -8;

        if(!onlyReturnDefendingMoves && !onlyReturnAttackingMoves){
            long oneStep = shift(state.position, direction);
            if ((oneStep & (used_squares | opponent_squares)) == 0) {
                legalMoves |= oneStep;
                long startRowMask = state.color.equals("black") ? 0x000000000000FF00L : 0x00FF000000000000L;
                if((state.position & startRowMask) != 0){
                    legalMoves |= twoStepMoves(used_squares, opponent_squares);
                }
            }
        }
    

        int[] captureDirections;
        if ((state.position & 0x0101010101010101L) != 0) {
            captureDirections = new int[]{direction + 1};
        }
        else if ((state.position & 0x8080808080808080L) != 0) {
            captureDirections = new int[]{direction - 1};
        }
        else {
            captureDirections = new int[]{direction + 1, direction - 1}; 
        }
        for (int capDir : captureDirections) {
            
            long captureMove = shift(state.position, capDir);
            if(!onlyReturnDefendingMoves){
                if ((captureMove & opponent_squares) != 0) {
                    legalMoves |= captureMove;
                }
            } else if(onlyReturnDefendingMoves || returnAllMoves){
                if ((captureMove & used_squares) != 0) {
                    legalMoves |= captureMove;
                }
            }
        }
        
        //for en passant
        //*if(!onlyReturnDefendingMoves)legalMoves |= getEnPassantMoves();


        return legalMoves;
    }
    

}
