package Code;

import Figures.*;
import Tools.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class Player {
    public String color;
    public Map<String, Figure> figure_map = new HashMap<>();
    public Map<String, Figure> removed_figures = new HashMap<>();
    public boolean isTurn;
    public boolean isMainPlayer;
    public boolean hasCastled = false;


    public Player(String color, String piece_positions, boolean isMainPlayer) {
        this.color = color;
        this.isMainPlayer = isMainPlayer;
        isTurn = color.equals("white");
        initializePieces(piece_positions);
    }

    public List<move> getMoves(Game game) {
        List<move> moves = new ArrayList<>();
        Collection<Figure> figures = new ArrayList<>(figure_map.values()); 

        for (Figure figure : figures) {
            if (figure == null) continue;
            long legalMoves = figure.getValidMoves(game, get_squares(game), get_opponent_squares(game));

            while (legalMoves != 0) {
                int targetSquare = Long.numberOfTrailingZeros(legalMoves);
                long moveBitboard = 1L << targetSquare;
                moves.add(new move(figure, moveBitboard));

                legalMoves &= (legalMoves - 1);
            }
        }
        return moves;
    }
    public List<Map.Entry<move, Integer>> getOrderedMoves(Game game) {
        Map<move, Integer> movesWithEvaluation = new HashMap<>();
        
        for (move move : getMoves(game)) {
            move.figure.setPosition(game, move.position, false, false);
            movesWithEvaluation.put(move, game.getGameEvaluation(game));
            game.undoMove();
        }
        
        List<Map.Entry<move, Integer>> orderedMoves = new ArrayList<>(movesWithEvaluation.entrySet());

        // sort by evaluation score
        if(color.equals("white")) orderedMoves.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        else orderedMoves.sort(Map.Entry.comparingByValue());  // for black player, sort in ascending order of evaluation score
        
        return orderedMoves;
    }
    public int validMoveCount(Game game) {
        int count = 0;
        
        Map<String, Figure> copy = new HashMap<>(figure_map);
        for(Figure figure : copy.values()){
            count += figure.count_ValidMoves(game);
        }
        return count;
    }

    public void initializePieces(String piece_positions) {
        long[] positions = convertTool.convert_name_to_bitboard(piece_positions);
    

        String[] pieceTypes = {"pawn", "rook", "knight", "bishop", "queen", "king"};
        

        for (int typeIndex = 0; typeIndex < 6; typeIndex++) {
            long bitboard = positions[typeIndex]; 
    
       
            for (int i = 0; i < 64; i++) {
                if ((bitboard & (1L << i)) != 0) { 
                    int[] pieces_count = count_pieces();
                    String pieceName = pieceTypes[typeIndex] + (pieces_count[typeIndex]+1); 
                    FigureState state = new FigureState(color, 1L << i, false, pieces_count[typeIndex]+1); 
                    switch (typeIndex) {
                        case 0 -> // pawn
                            figure_map.put(pieceName, new pawn(state));
                        case 1 -> // rook
                            figure_map.put(pieceName, new rook(state));
                        case 2 -> // knight
                            figure_map.put(pieceName, new knight(state));
                        case 3 -> // bishop
                            figure_map.put(pieceName, new bishop(state));
                        case 4 -> // queen
                            figure_map.put(pieceName, new queen(state));
                        case 5 -> // king
                            figure_map.put(pieceName, new king(state));
                        
                    } 
                }
            }
        }
    }

    public int[] count_pieces(){
        int returnValues[] = new int[6];
        for(Figure figure : figure_map.values()){
            if(null != figure.getName())switch (figure.getName()) {
                case "p" -> returnValues[0]++;
                case "r" -> returnValues[1]++;
                case "n" -> returnValues[2]++;
                case "b" -> returnValues[3]++;
                case "q" -> returnValues[4]++;
                case "k" -> returnValues[5]++;
                default -> {
                }
            }
        }
        return returnValues;
    }

    public long get_squares(Game game){
        return this.color.equals("white")? combineTool.combine_figure_bitboards(game.white.figure_map) : combineTool.combine_figure_bitboards(game.black.figure_map);
    }
    public long get_opponent_squares(Game game){
        return color.equals("white")? combineTool.combine_figure_bitboards(game.black.figure_map) : combineTool.combine_figure_bitboards(game.white.figure_map);
    }
    public boolean isCheck(Game game) {
        Map<String, Figure> opponentFigures = color.equals("white") ? game.black.figure_map : game.white.figure_map;
        Figure king = figure_map.get("king1");
        for (Figure f : opponentFigures.values()) {
            if(f == null)continue;
            if(king != null && (king.getPosition() & f.generateMoves(game, get_opponent_squares(game), get_squares(game), false, true, false)) != 0) return true;
        }
        
        return false;
    }
    public Figure getFigureAt(long position) {

        for (Figure figure : figure_map.values()) {
            if (figure.getPosition() == position) {
                return figure;
            }
        }
        return null; 
    }
    public int getDefensePieceCombination(String x, String y) {
        int evaluation = 0;

        switch (x) {
            case "p" -> {
                switch (y) { 
                    case "p" -> evaluation += 30;
                    case "r" -> evaluation += 16;
                    case "n" -> evaluation += 12;
                    case "b" -> evaluation += 12;
                    case "q" -> evaluation += 30;
                    case "k" -> evaluation += 0;
                }
            }
            case "r" -> {
                switch (y) {
                    case "p" -> evaluation += 12;
                    case "r" -> evaluation += 24;
                    case "n" -> evaluation += 20;
                    case "b" -> evaluation += 20;
                    case "q" -> evaluation += 40;
                    case "k" -> evaluation += 0;
                }
            }
            case "n" -> {
                switch (y) {
                    case "p" -> evaluation += 8;
                    case "r" -> evaluation += 24;
                    case "n" -> evaluation += 16;
                    case "b" -> evaluation += 16;
                    case "q" -> evaluation += 20;
                    case "k" -> evaluation += 0;
                }
            }
            case "b" -> {
                switch (y) {
                    case "p" -> evaluation += 8;
                    case "r" -> evaluation += 24;
                    case "n" -> evaluation += 16;
                    case "b" -> evaluation += 16;
                    case "q" -> evaluation += 20;
                    case "k" -> evaluation += 0;
                }
            }
            case "q" -> {
                switch (y) {
                    case "p" -> evaluation += 6;
                    case "r" -> evaluation += 20;
                    case "n" -> evaluation += 16;
                    case "b" -> evaluation += 16;
                    case "q" -> evaluation += 20;
                    case "k" -> evaluation += 0;
                }
            }
            case "k" -> {
                switch (y) {
                    case "p" -> evaluation += 4;
                    case "r" -> evaluation += 16;
                    case "n" -> evaluation += 12;
                    case "b" -> evaluation += 12;
                    case "q" -> evaluation += 30;
                    case "k" -> evaluation += 0;
                }
            }
        }

        return evaluation;
    }

    public int getAttackingPieceCombination(String x, String y) {
        int evaluation = 0;

        switch (x) {
            case "p" -> {
                switch (y) {
                    case "p" -> evaluation += 5;
                    case "r" -> evaluation += 30;
                    case "n" -> evaluation += 20;
                    case "b" -> evaluation += 20;
                    case "q" -> evaluation += 90;
                    case "k" -> evaluation += 10;
                }
            }
            case "r" -> {
                switch (y) {
                    case "p" -> evaluation += 20;
                    case "r" -> evaluation += 40;
                    case "n" -> evaluation += 35;
                    case "b" -> evaluation += 35;
                    case "q" -> evaluation += 70;
                    case "k" -> evaluation += 10;
                }
            }
            case "n" -> {
                switch (y) {
                    case "p" -> evaluation += 15;
                    case "r" -> evaluation += 45;
                    case "n" -> evaluation += 30;
                    case "b" -> evaluation += 30;
                    case "q" -> evaluation += 60;
                    case "k" -> evaluation += 10;
                }
            }
            case "b" -> {
                switch (y) {
                    case "p" -> evaluation += 15;
                    case "r" -> evaluation += 50;
                    case "n" -> evaluation += 30;
                    case "b" -> evaluation += 30;
                    case "q" -> evaluation += 60;
                    case "k" -> evaluation += 10;
                }
            }
            case "q" -> {
                switch (y) {
                    case "p" -> evaluation += 10;
                    case "r" -> evaluation += 35;
                    case "n" -> evaluation += 30;
                    case "b" -> evaluation += 30;
                    case "q" -> evaluation += 50;
                    case "k" -> evaluation += 10;
                }
            }
            case "k" -> {
                switch (y) {
                    case "p" -> evaluation += 5;
                    case "r" -> evaluation += 20;
                    case "n" -> evaluation += 15;
                    case "b" -> evaluation += 15;
                    case "q" -> evaluation += 50;
                    case "k" -> evaluation += 0; //king cant attack king
                }
            }
        }

        return evaluation;
    }
    public int getAttackingValue(Game game){
        int evaluation = 0;
        Map<String, Figure> ownMap = figure_map;
        Map<String, Figure> opponentMap = color.equals("white") ? game.black.figure_map : game.white.figure_map;
        long used_squares = get_squares(game);
        long opponent_squares = color.equals("white") ? game.black.get_squares(game) : game.white.get_squares(game);
        for(Figure f : ownMap.values()){
            if(f == null) continue;
            String name = f.getName();
            long generatedMoves = f.generateMoves(game, used_squares, opponent_squares,false, true, false);

            if(f.getName().equals("p") && f.getEnPassantMoves() != 0L){ //en passant
                evaluation += 5;
            }

            for(Figure figure : opponentMap.values()){
                if(figure == null) continue;
                if((generatedMoves & figure.getPosition()) != 0){
                    evaluation += getAttackingPieceCombination(name, figure.getName());
                    
                }
            }
        }
        return evaluation;
    }

    public int getDefendingValue(Game game) {
        int evaluation = 0;
        Map<String, Figure> ownMap = figure_map;
        long used_squares = get_squares(game);
        Player opponent = color.equals("white") ? game.black : game.white;
        long enemyAttackingFields = 0L;

        for(Figure figure : opponent.figure_map.values()) {
            if(figure == null) continue;
            enemyAttackingFields |= figure.generateMoves(game, opponent.get_squares(game), used_squares, false, true, false);
        }
    
        for (Figure f : ownMap.values()) {
            if (f == null) continue;
            String name = f.getName();
            long generatedMoves = f.generateMoves(game, used_squares, opponent.get_squares(game), true, false, false);
            for (Figure figure : ownMap.values()) {
                if (figure == null) continue;
                if ((generatedMoves & figure.getPosition() & enemyAttackingFields) != 0) {
                    evaluation += getDefensePieceCombination(name, figure.getName());
                }
            }
        }
        return evaluation;
    }
    public int getPassedPawnEvaluation(Game game) {
        int evaluation = 0;
        for (Figure figure : figure_map.values()) {
            if (figure == null) continue;
            if (figure.getName().equals("p") && figure.isPassedPawn(game)) {
                evaluation += 15;
            }
        }
        return evaluation;
    }
    public int getDoublePawnEvaluation(Game game) {
        int evaluation = 0;
        int missingPieces = 32 -(game.white.figure_map.size() + game.black.figure_map.size());
        for (Figure figure : figure_map.values()) {
            if (figure == null) continue;
            if (figure.getName().equals("p") && figure.isDoublePawn(game)) {
                evaluation -= missingPieces*1; // the later the game is, the more punishment for double pawns
            }
        }
        return evaluation;
    }
    
    public int getPosEvaluation(Game game) {
        //rather use Game.optimizedPosEvaluation
        //it prevents generating moves from figures of which the moves have already been generated


        int evaluation = 0;

        int missingPieces = 32 - (game.white.figure_map.size() + game.black.figure_map.size());
        //evaluation functions only generate moves that do not take if its check into account 

        //figure worth evaluation
        //piece position evaluation
        //possible move amount evaluation
        for (Figure figure : figure_map.values()) {
            if (figure == null) continue;
            evaluation += figure.getPieceValue() + figure.getPosValue(game) + figure.getPossibleMoveValue(game);
            if(figure.isMoved) evaluation += 10;
        }

        //reward if opponent king is in check
        String lastMove = game.gameHistory.get(game.gameHistory.size()-1);
        if (lastMove.substring(11,12).equals("1") && !isTurn) {
            evaluation += 20 + (missingPieces * 2); //opening checks just gives 20 centipawns. endgame checks are more important and give up to 84 centipawns
        }

        //reward castling
        if (hasCastled) evaluation += 50;

        //reward pieces for defending friendly pieces
        evaluation += getDefendingValue(game); 

        //reward pieces for attacking enemy pieces
        evaluation += getAttackingValue(game);

        //reward passed pawns
        evaluation += getPassedPawnEvaluation(game);

        //punish double pawns
        evaluation += getDoublePawnEvaluation(game);

        return evaluation;
    }
    public Map<String, Figure> createCopyOfMap() {
        Map<String, Figure> copy = new HashMap<>();

        for (Figure figure : figure_map.values()) { //copy white figures
            FigureState state = new FigureState(color, figure.getPosition(), figure.state.isMoved, figure.state.index);
            switch (figure.getName().charAt(0)) {
                case 'p' -> // Pawn
                    copy.put(figure.getFullName(), new pawn(state));
                case 'r' -> // Rook
                    copy.put(figure.getFullName(), new rook(state));
                case 'n' -> // Knight
                    copy.put(figure.getFullName(), new knight(state));
                case 'b' -> // Bishop
                    copy.put(figure.getFullName(), new bishop(state));
                case 'q' -> // Queen
                    copy.put(figure.getFullName(), new queen(state));
                case 'k' -> // King
                    copy.put(figure.getFullName(), new king(state));
            }
        }
        return copy;
    }
    
}
