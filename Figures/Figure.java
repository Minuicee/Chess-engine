package Figures;

import Code.*;
import Tools.*;
import java.util.Iterator;
import java.util.Map;

public abstract class Figure {
    public FigureState state;
    public boolean isMoved;

    public abstract int getPosValue(Game game);

    public abstract String getFullName();

    public abstract int getPieceValue();

    public abstract int getPossibleMoveValue(Game game);

    public int count_GenerateMoves(Game game){
        long used_squares = state.color.equals("white") ? game.white.get_squares(game) : game.black.get_squares(game);
        long opponent_squares = state.color.equals("white") ? game.black.get_squares(game) : game.white.get_squares(game);
        return Long.bitCount(generateMoves(game, used_squares, opponent_squares,false, false, false));
    }
    public abstract int possibleMoveMultiplier();

    public int count_ValidMoves(Game game){
        long used_squares = state.color.equals("white")? game.white.get_squares(game) : game.black.get_squares(game);
        long opponent_squares = state.color.equals("white")? game.black.get_squares(game) : game.white.get_squares(game);
        return Long.bitCount(getValidMoves(game, used_squares, opponent_squares));
    }
    
    public void setMoved(boolean isMoved) {
        state.isMoved = isMoved; 
    }
	
    public Figure(FigureState currentState) {
        this.state = currentState;
    }

    public long getPosition() {
        return state.position;
    }

    public boolean isPassedPawn(Game game) {
        if(getName().equals("p")) {
            int col = Long.numberOfTrailingZeros(state.position) % 8;
            int right = col - 1;
            int left = col + 1;
            Player opponent = state.color.equals("white")? game.black : game.white;
            for(Figure figure : opponent.figure_map.values()){
                int opponentCol = Long.numberOfTrailingZeros(figure.state.position) % 8;
                if(figure.getName().equals("p") && (opponentCol == col || opponentCol == left || opponentCol == right)) return false;
            }
            return true;
        }
        return false;
    }

    public boolean isDoublePawn(Game game){
        if(getName().equals("p")) {
            Player self = state.color.equals("white")? game.white : game.black;
            int col = Long.numberOfTrailingZeros(state.position) % 8;
            for(Figure figure : self.figure_map.values()){
                if(figure.getName().equals("p") && (Long.numberOfTrailingZeros(figure.state.position) % 8) == col && figure.state.index != state.index) return true;
            }
        }
        return false;
    }

    public void addGameHistory(String startingSquare, String targetSquare, String wasEnpassant, boolean isOpponentCheck, Game game){
        String isMovedString = state.isMoved ? "1" : "0";
        String isCheck = isOpponentCheck ? "1" : "0";
        if(wasEnpassant.equals("0")) wasEnpassant = "00";
        game.gameHistory.put(game.gameHistory.size(), startingSquare + targetSquare + isMovedString + wasEnpassant + isCheck);

    }
    public void setPosition_ignore_rest(long position) {
        state.position = position;
    }

    public void setPosition(Game game, long position, boolean shouldUpdate, boolean shouldEndgame) {
        Player opponent = state.color.equals("white") ? game.black : game.white;
        String capture_name = "0";
        int capture_index = 0;
        String wasEnpassant = "00";

        Iterator<Map.Entry<String, Figure>> iterator = opponent.figure_map.entrySet().iterator(); //remove opponent figure
            while (iterator.hasNext()) {
                Map.Entry<String, Figure> entry = iterator.next();
                String key = entry.getKey();
                Figure figure = entry.getValue();
                if (figure != null && (figure.getPosition() & position) != 0) {
                    opponent.removed_figures.put(key, figure);
                    capture_name = figure.getName();
                    capture_index = figure.state.index;
                    iterator.remove();
                    break;
                }
        }
        
        //* en passant is commented since it brings some bugs
        //for en passant
        // if(getName().equals("p") && (position & getEnPassantMoves()) != 0){  
        //     capture_index = convertTool.convert_long_to_int(position) % 8 + 1;
        //     capture_name = "p";
        //     if (state.color.equals("white")) {
        //         Figure capturedPawn = Game.black.figure_map.get("pawn" + capture_index);
        //         if (capturedPawn != null) {
        //             wasEnpassant = convertTool.convert_chess_notation(capturedPawn.getPosition());
        //             Game.black.removed_figures.put("pawn" + capture_index, capturedPawn);
        //             Game.black.figure_map.remove("pawn" + capture_index);
        //         }
        //         else{
        //             capture_name = "0";
        //             capture_index = 0;
        //         }
        //     } else {
        //         Figure capturedPawn = Game.white.figure_map.get("pawn" + capture_index);
        //         if (capturedPawn != null) {
        //             wasEnpassant = convertTool.convert_chess_notation(capturedPawn.getPosition());
        //             Game.white.removed_figures.put("pawn" + capture_index, capturedPawn);
        //             Game.white.figure_map.remove("pawn" + capture_index);
        //         } 
        //         else{
        //             capture_name = "0";
        //             capture_index = 0;
        //         }
        //     }
        // }


        //for game history
        String gameHistoryStartingString = getName()+state.index+convertTool.convert_chess_notation(state.position);
        String gameHistoryTargetString = capture_name+capture_index+convertTool.convert_chess_notation(position);

        //for castling
        if(getName().equals("k")) tryCastle(game, position);

        //for promotion
        if(!(getName().equals("p") && tryPromote( game, position, gameHistoryStartingString, gameHistoryTargetString, opponent))){ //only set position if there was no promotion. because in that case the pawn would be deleted so the position can't be set

            //set position
            state.position = position;

            //for game history
            addGameHistory(gameHistoryStartingString, gameHistoryTargetString, wasEnpassant, opponent.isCheck(game), game);

           //useful for rooks and kings for castling
            setMoved(true); 
        }
        game.change_isTurn();
        if(shouldUpdate){
            Main.game.board.update_board(convertTool.convert_map_to_name_both(game.white.figure_map, game.black.figure_map));
            game.isGameOver(game, shouldEndgame); 
            Main.game.board.testDisplay1.setText(String.format(" White eval: %d", game.getGameEvaluation(game)));
        }
    }
    
    public boolean tryPromote(Game game, long position, String gameHistoryStartingString, String gameHistoryTargetString, Player opponent){
        //will promote if possible (only queen) and return true if promotion happened
        if(state.color.equals("white") && (position & 0x00000000000000FFL) != 0){
            Figure removedPawn = game.white.figure_map.remove("pawn" + state.index);
            game.white.removed_figures.put("pawn" + state.index, removedPawn);
            game.white.figure_map.put("queen"+String.format("%d", state.index+1), new queen(new FigureState("white", position, true, state.index+1)));
            addGameHistory(gameHistoryStartingString, gameHistoryTargetString, "0", opponent.isCheck(game), game);
            return true;
        }
        else if(state.color.equals("black") && (position & 0xFF00000000000000L) != 0){
            Figure removedPawn = game.black.figure_map.remove("pawn" + state.index);
            game.black.removed_figures.put("pawn" + state.index, removedPawn);
            game.black.figure_map.put("queen"+String.format("%d", state.index+1), new queen(new FigureState("black", position, true, state.index+1)));
            addGameHistory(gameHistoryStartingString, gameHistoryTargetString, "0", opponent.isCheck(game), game);
            return true;
        }
        return false;
    }


    public long twoStepMoves(long used_squares, long opponent_squares){
        int direction = state.color.equals("black") ? 8 : -8;
        long twoSteps = shift(state.position, 2 * direction);
        if ((twoSteps & (used_squares | opponent_squares)) == 0) {
            return twoSteps;
        }
        return 0L;
    }

    public void tryCastle(Game game, long position){
        long own_squares = state.color.equals("white") ? game.white.get_squares(game) : game.black.get_squares(game);
        long opponent_squares = state.color.equals("white") ? game.black.get_squares(game) : game.white.get_squares(game);
        long castlingMoves = getCastlingMoves(game ,own_squares, opponent_squares);
        Figure rook1 = (state.color.equals("white")) ? game.white.figure_map.get("rook1") : game.black.figure_map.get("rook1");
        Figure rook2 = (state.color.equals("white")) ? game.white.figure_map.get("rook2") : game.black.figure_map.get("rook2");
        if((castlingMoves & position) != 0){
            if (position == 0x0000000000000040L && rook2!=null) { // White, Kingside
                rook2.setPosition_ignore_rest(0x0000000000000020L);
            } else if (position == 0x0000000000000004L  && rook1!=null) { // White, Queenside
                rook1.setPosition_ignore_rest(0x0000000000000008L);
            } else if (position == 0x4000000000000000L  && rook2!=null) { // Black, Kingside
                rook2.setPosition_ignore_rest(0x2000000000000000L);
            } else if (position == 0x0400000000000000L  && rook1!=null ) { // Black, Queenside
                rook1.setPosition_ignore_rest(0x0800000000000000L);
            }
        }
    }

    public abstract String getName();

    public abstract long  generateMoves(Game game, long used_squares, long opponent_squares, boolean onlyReturnDefendingMoves, boolean onlyReturnAttackingMoves, boolean returnAllMoves); 

    public long getValidMoves(Game game, long used_squares, long opponent_squares) {
        long validMoves = generateMoves(game, used_squares, opponent_squares, false, false, false);
        long safeMoves = 0L;
        
    
        for (long move = 1L; move != 0; move <<= 1) {
            if ((validMoves & move) != 0) {

                setPosition(game, move, false, false);
                if (state.color.equals("white") && !game.white.isCheck(game)) {
                    safeMoves |= move;
                }
                else if (state.color.equals("black") &&!game.black.isCheck(game)) {
                    safeMoves |= move;
                }

                game.undoMove();
            }
        }
        return safeMoves;
    }

    public long moveInDirection(long pos, long occupied, long opponent, int direction, boolean onlyReturnDefendingMoves, boolean onlyReturnAttackingMoves, boolean returnAllMoves) { 
        long moves = 0L;
        long tempPos = pos;
        long lastPos = pos;

        while (true) {
            tempPos = shift(tempPos, direction);
            if(tempPos == 0) break;
            if ((tempPos & 0x8080808080808080L) != 0) {
                if ((lastPos & 0x0101010101010101L) != 0) {
                    break;
                }
            } else if ((tempPos & 0x0101010101010101L) != 0) {
                if ((lastPos & 0x8080808080808080L) != 0) {
                    break;
                }
            }
            lastPos = tempPos;
            if ((tempPos & occupied) != 0){
                if(onlyReturnDefendingMoves || returnAllMoves) {
                    moves |= tempPos;
                }
                break;
            }
            else if ((tempPos & opponent) != 0){
                moves |= tempPos;
                break;
            } 
            if(!onlyReturnAttackingMoves && !onlyReturnDefendingMoves){
                moves |= tempPos;
            }
        }

        return moves;
    }

    public long shift(long bitboard, int shift) {
        return shift > 0 ? (bitboard << shift) : (bitboard >>> -shift);
    }
    public long getEnPassantMoves() {
        long enPassantMoves = 0L;
    
        //* en passant is commented since it brings some bugs
        // if (!Game.gameHistory.isEmpty()) {
        //     String lastMove = Game.gameHistory.get(Game.gameHistory.size() - 1);
        //     boolean whiteEnPassantPossible = (lastMove.charAt(3) == '7' && lastMove.charAt(7) == '5') && (state.position & 0x00000000FF000000L) != 0;
        //     boolean blackEnPassantPossible = (lastMove.charAt(3) == '2' && lastMove.charAt(7) == '4') && (state.position & 0x000000FF00000000L) != 0;
        //     int direction = convertTool.convert_chess_notation_to_int(lastMove.substring(6, 8)) - convertTool.convert_long_to_int(state.position);
    
        //     if (whiteEnPassantPossible && direction == -1) { // if en passant is possible and the direction is left
        //         enPassantMoves |= shift(state.position, -9);
        //     } else if (blackEnPassantPossible && direction == 1) { // if direction is left
        //         enPassantMoves |= shift(state.position, 9);
        //     } else if (whiteEnPassantPossible && direction == 1) { // if direction is right
        //         enPassantMoves |= shift(state.position, -7);
        //     } else if (blackEnPassantPossible && direction == -1) { // if direction is right
        //         enPassantMoves |= shift(state.position, 7);
        //     }
        // }
    
        return enPassantMoves;
    }
    public long getCastlingMoves(Game game , long used_squares, long opponent_squares) {
        long castlingMoves = 0L;
        if (state.color.equals("white")) {
            if (!state.isMoved && !game.white.isCheck(game)) {
                if ((used_squares & 0x0000000000000060L) == 0 && (opponent_squares & 0x0000000000000070L) == 0 && game.white.figure_map.get("rook2").state.isMoved == false) {
                    castlingMoves |= 0x0000000000000040L; // White king-side castling
                }
                if ((used_squares & 0x000000000000000EL) == 0 && (opponent_squares & 0x000000000000001CL) == 0 && game.white.figure_map.get("rook1").state.isMoved == false) {
                    castlingMoves |= 0x0000000000000004L; // White queen-side castling
                }
            }
        } else {
            if (!state.isMoved && !game.black.isCheck(game)) {
                if ((used_squares & 0x6000000000000000L) == 0 && (opponent_squares & 0x7000000000000000L) == 0 && game.black.figure_map.get("rook2").state.isMoved == false) {
                    castlingMoves |= 0x4000000000000000L; // Black king-side castling
                }
                if ((used_squares & 0x0E00000000000000L) == 0 && (opponent_squares & 0x1C00000000000000L) == 0 && game.black.figure_map.get("rook1").state.isMoved == false) {
                    castlingMoves |= 0x0400000000000000L; // Black queen-side castling
                }
            }
        }
        return castlingMoves;
    }


}
