package Figures;

import Code.*;


public class king extends Figure {
    public king(FigureState state) {
        super(state);
    }

    @Override
    public String getName() {
        return "k";
    }
    @Override
    public String getFullName() {
        return (String) "king"+state.index;
    }
    @Override
    public int getPieceValue(){
        return 0;
    }
    @Override
    public int getPossibleMoveValue(Game game){
        return 0;
    }
    @Override
    public int possibleMoveMultiplier(){
        return 0;
    }
    @Override
    public int getPosValue(Game game){
        int position = Long.numberOfTrailingZeros(state.position);
        int posValue;
        int missingPieces = 32 - (game.white.figure_map.size() + game.black.figure_map.size());
            if(state.color.equals("white")){
                int[] whiteTable = {
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -20,-30,-30,-40,-40,-30,-30,-20,
                    -10,-20,-20,-20,-20,-20,-20,-10,
                    20, 20,  0,  0,  0,  0, 20, 20,
                    20, 30, 10,  0,  0, 10, 30, 20
                };
                int[] whiteEndgameTable = {
                    -50,-40,-30,-20,-20,-30,-40,-50,
                    -30,-20,-10,  0,  0,-10,-20,-30,
                    -30,-10, 20, 30, 30, 20,-10,-30,
                    -30,-10, 30, 40, 40, 30,-10,-30,
                    -30,-10, 30, 40, 40, 30,-10,-30,
                    -30,-10, 20, 30, 30, 20,-10,-30,
                    -30,-30,  0,  0,  0,  0,-30,-30,
                    -50,-30,-30,-30,-30,-30,-30,-50
                };
                int difference = whiteEndgameTable[position]*2 - whiteTable[position];
                if (difference == 0) {
                    posValue = whiteTable[position];
                    return posValue;
                }
                posValue = whiteTable[position] + (int)((difference / 32.0) * missingPieces);
                return posValue;
            }
            else{
                int[] blackTable = {
                    20, 30, 10,  0,  0, 10, 30, 20,
                    20, 20,  0,  0,  0,  0, 20, 20,
                    -10,-20,-20,-20,-20,-20,-20,-10,
                    -20,-30,-30,-40,-40,-30,-30,-20,
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -30,-40,-40,-50,-50,-40,-40,-30,
                    -30,-40,-40,-50,-50,-40,-40,-30
                };
                int[] blackEndgameTable = {
                    -50,-30,-30,-30,-30,-30,-30,-50,
                    -30,-30,  0,  0,  0,  0,-30,-30,
                    -30,-10, 20, 30, 30, 20,-10,-30,
                    -30,-10, 30, 40, 40, 30,-10,-30,
                    -30,-10, 30, 40, 40, 30,-10,-30,
                    -30,-10, 20, 30, 30, 20,-10,-30,
                    -30,-20,-10,  0,  0,-10,-20,-30,
                    -50,-40,-30,-20,-20,-30,-40,-50
                };
                int difference = blackEndgameTable[position]*2 - blackTable[position];
                if (difference == 0) {
                    posValue = blackTable[position];
                    return posValue;
                }
                posValue = blackTable[position] + (int)((difference / 32.0) * missingPieces);
                return posValue;
            
        }
    }

    @Override
    public long generateMoves(Game game, long used_squares, long opponent_squares, boolean onlyReturnDefendingMoves, boolean onlyReturnAttackingMoves, boolean returnAllMoves) {
        long legalMoves = 0L;
        
        if(!onlyReturnDefendingMoves && !onlyReturnAttackingMoves) legalMoves |= getCastlingMoves(game, used_squares, opponent_squares);

        //normal moves
        int[] directions = {1, -1, 8, -8, 9, -9, 7, -7};
        for (int dir : directions) {
            long tempPos = shift(state.position, dir);

            if ((state.position & 0x8080808080808080L) != 0) {
                if ((tempPos & 0x0101010101010101L) != 0) {
                    continue;
                }
            } else if ((state.position & 0x0101010101010101L) != 0) {
                if ((tempPos & 0x8080808080808080L) != 0) {
                    continue;
                }
            }
            if(!onlyReturnDefendingMoves && !onlyReturnAttackingMoves && !returnAllMoves){
                if (tempPos != 0 && (tempPos & used_squares) == 0) {
                    legalMoves |= tempPos;
                }
            }
            else if (returnAllMoves){
                if(tempPos != 0){
                    legalMoves |= tempPos;
                }
            }
            else if (onlyReturnAttackingMoves) {
                if (tempPos != 0 && (tempPos & opponent_squares) != 0) {
                    legalMoves |= tempPos;
                }
            }
            else if (onlyReturnDefendingMoves) {
                if (tempPos != 0 && (tempPos & opponent_squares) == 0 && (tempPos & used_squares) != 0) {
                    legalMoves |= tempPos;
                }
            }
        }
        return legalMoves;
    }
    @Override
    public long getCastlingMoves(Game game, long used_squares, long opponent_squares){

        long legalMoves = 0L;
        if(!state.isMoved){
            if (state.color.equals("white")) {
                long kingsideSquares = 0x6000000000000000L;
                long kingPath = 0x7000000000000000L;
                long rookPosition = 0x8000000000000000L;

                if ((kingsideSquares & used_squares) == 0 && (kingPath & opponent_squares) == 0) {
                    Figure rook = game.white.getFigureAt(rookPosition);
                    if (rook != null && !rook.state.isMoved) {
                        legalMoves |= shift(state.position, 2); 
                    }
                }

                long queensideSquares = 0x0E00000000000000L;
                long kingPathQueenside = 0x1C00000000000000L;
                long rookPositionQueenside = 0x0100000000000000L;

                if ((queensideSquares & used_squares) == 0 && (kingPathQueenside & opponent_squares) == 0) {
                    Figure rook = game.white.getFigureAt(rookPositionQueenside);
                    if (rook != null && !rook.state.isMoved) {
                        legalMoves |= shift(state.position, -2); 
                    }
                }
            } else {
                long kingsideSquares = 0x0000000000000060L;
                long kingPath = 0x0000000000000070L;
                long rookPosition = 0x0000000000000080L;

                if ((kingsideSquares & used_squares) == 0 && (kingPath & opponent_squares) == 0) {
                    Figure rook = game.black.getFigureAt(rookPosition);
                    if (rook != null && !rook.state.isMoved) {
                        legalMoves |= shift(state.position, 2); 
                    }
                }

                long queensideSquares = 0x000000000000000EL;
                long kingPathQueenside = 0x000000000000001CL;
                long rookPositionQueenside = 0x0000000000000001L;

                if ((queensideSquares & used_squares) == 0 && (kingPathQueenside & opponent_squares) == 0) {
                    Figure rook = game.black.getFigureAt(rookPositionQueenside);
                    if (rook != null && !rook.state.isMoved) {
                        legalMoves |= shift(state.position, -2); 
                    }
                }
            }
        }
        return legalMoves;
    }

    
}
