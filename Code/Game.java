package Code;

import Figures.Figure;
import Figures.move;
import Tools.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game{
    public Map<Integer,String> gameHistory;
    public Player black;
	public Player white;
	public Board board;
    public int gameMode;
    public boolean isGameEnded = false;
    public String board_string;
    public boolean isWhiteMainPlayer;
    public Map<String, Integer> transpositionTable = new HashMap<>();
    public boolean historyInDatabase = true;

    public Game(String board_string, String isMainPlayer, int gameMode){
        this.isWhiteMainPlayer = isMainPlayer.equals("white");
        this.board_string = board_string;
        this.gameMode = gameMode;
        gameHistory = new HashMap<>();
        board = new Board();
        if(board_string.length() != 64){
            System.out.println("Invalid board size: "+board_string.length()+" -> (must be 64 characters)");
            System.exit(0);
        }
        String[] converted_piece_positions = convertTool.String_to_two_strings(board_string);
        black = new Player("black", converted_piece_positions[0], isMainPlayer.equals("black"));
        white = new Player("white", converted_piece_positions[1], isMainPlayer.equals("white"));

    }

    
    public void startGame(){
        board.init_board(convertTool.convert_map_to_name_both(white.figure_map, black.figure_map));

    }
    public void change_isTurn(){
        white.isTurn = !white.isTurn;
        black.isTurn = !black.isTurn;
        if( gameMode == 0){
        white.isMainPlayer = !white.isMainPlayer;  
        black.isMainPlayer = !black.isMainPlayer;
        }
    }
    public boolean mainPlayer_isTurn(){
        return white.isMainPlayer ? white.isTurn : black.isTurn;
    }
    public String getIsTurn(){
        return white.isTurn ? "white" : "black";
    }
    public void endGame(){
        isGameEnded = true;
        //more todo
    }
    public int isGameOver(Game game, boolean shouldEndgame) { 
        if (white.figure_map == null || black.figure_map == null) {
            throw new IllegalStateException("Figure maps must be initialized.");
        }

        Player player = white.isTurn ? white : black;
        List<move> possibleMoves = player.getMoves(game);
        if(possibleMoves.isEmpty()){
            if(player.isCheck(game)){
                if(shouldEndgame) endGame();
                return 1;
            }
            else{
                if(shouldEndgame) endGame();
                return 2;
            }
        }
        if (white.figure_map.size() == 1 && black.figure_map.size() == 1) {
            if(shouldEndgame) endGame();
            return 2;
        }
        if(isRepetitionDraw()){
            if(shouldEndgame) endGame();
            return 1;
        }
        return 0;

    }
    public void undoMove() {
        if (gameHistory == null || gameHistory.isEmpty()) {
            return;
        }
    
        boolean isColorWhite = gameHistory.size() % 2 == 1;
        String lastMoveString = gameHistory.remove(gameHistory.size() - 1);
    
        if (lastMoveString.length() < 9) {
            throw new IllegalArgumentException("Invalid move string format.");
        }
    
        String figureName = lastMoveString.substring(0, 1);
        String figureIndex = lastMoveString.substring(1, 2);
        String figurePos = lastMoveString.substring(2, 4);
        String targetName = lastMoveString.substring(4, 5);
        String targetIndex = lastMoveString.substring(5, 6);
        boolean wasMoved = lastMoveString.substring(8, 9).equals("1");
    
        Figure figure;
        boolean wasPromotion = false;
        //boolean wasEnPassant = lastMoveString.length() >= 11 && !lastMoveString.substring(9, 11).equals("00");
    

        //* en passant is commented since it brings some bugs
        // Handle en passant
        // if (wasEnPassant) {
        //     if (isColorWhite) {
        //         Figure removedFigure = black.removed_figures.remove("pawn"+targetIndex);
        //         black.figure_map.put("pawn" + targetIndex, removedFigure);
        //     } else {
        //         Figure removedFigure = white.removed_figures.remove("pawn"+targetIndex);
        //         white.figure_map.put("pawn" + targetIndex, removedFigure);
        //     }
        // }
    
        // Handle promotion
        if (figureName.equals("p")) { 
            if (isColorWhite && lastMoveString.substring(7, 8).equals("8")) {
                Figure removedFigure = white.removed_figures.remove("pawn"+figureIndex);
                white.figure_map.put("pawn" + figureIndex, removedFigure);
                white.figure_map.remove("queen" + (Integer.parseInt(figureIndex) + 1));
                wasPromotion = true;
            } else if (!isColorWhite && lastMoveString.substring(7, 8).equals("1")) {
                Figure removedFigure = black.removed_figures.remove("pawn"+figureIndex);
                black.figure_map.put("pawn" + figureIndex, removedFigure);
                black.figure_map.remove("queen" + (Integer.parseInt(figureIndex) + 1));
                wasPromotion = true;
            }
        }
    
        // Handle usual pieces
        figure = switch (figureName) {
            case "r" -> isColorWhite ? white.figure_map.get("rook" + figureIndex) : black.figure_map.get("rook" + figureIndex);
            case "n" -> isColorWhite ? white.figure_map.get("knight" + figureIndex) : black.figure_map.get("knight" + figureIndex);
            case "b" -> isColorWhite ? white.figure_map.get("bishop" + figureIndex) : black.figure_map.get("bishop" + figureIndex);
            case "q" -> isColorWhite ? white.figure_map.get("queen" + figureIndex) : black.figure_map.get("queen" + figureIndex);
            case "k" -> isColorWhite ? white.figure_map.get("king1") : black.figure_map.get("king1");
            default -> isColorWhite ? white.figure_map.get("pawn" + figureIndex) : black.figure_map.get("pawn" + figureIndex);
        };
    
        // Handle castling
        if (figureName.equals("k")) {
            switch (lastMoveString.substring(0, 8)) {
                case "k1e100g1" -> {
                    Figure rook = isColorWhite ? white.figure_map.get("rook2") : black.figure_map.get("rook2");
                    if (rook != null) {
                        if(isColorWhite) white.hasCastled = true; else {black.hasCastled = true;}
                        rook.setPosition_ignore_rest(1L << 63);
                        rook.setMoved(false);
                        figure.setMoved(false);
                    }
                }
                case "k1e800g8" -> {
                    Figure rook = isColorWhite ? white.figure_map.get("rook2") : black.figure_map.get("rook2");
                    if (rook != null) {
                        if(isColorWhite) white.hasCastled = true; else {black.hasCastled = true;}
                        rook.setPosition_ignore_rest(1L << 7);
                        rook.setMoved(false);
                        figure.setMoved(false);
                    }
                }
                case "k1e100c1" -> {
                    Figure rook = isColorWhite ? white.figure_map.get("rook1") : black.figure_map.get("rook1");
                    if (rook != null) {
                        if(isColorWhite) white.hasCastled = true; else {black.hasCastled = true;}
                        rook.setPosition_ignore_rest(1L << 56);
                        rook.setMoved(false);
                        figure.setMoved(false);
                    }
                }
                case "k1e800c8" -> {
                    Figure rook = isColorWhite ? white.figure_map.get("rook1") : black.figure_map.get("rook1");
                    if (rook != null) {
                        if(isColorWhite) white.hasCastled = true; else {black.hasCastled = true;}
                        rook.setPosition_ignore_rest(1L);
                        rook.setMoved(false);
                        figure.setMoved(false);
                    }
                }
                default -> {
                }
            }
        }
    
        // Handle captured pieces
        if (!targetName.equals("0")){ //* && !wasEnPassant) {
    
            switch (targetName) {
                case "r" -> {
                    if (!isColorWhite) {
                        Figure removedFigure = white.removed_figures.get("rook" + targetIndex);
                        white.figure_map.put("rook" + targetIndex, removedFigure);
                        white.removed_figures.remove("rook" + targetIndex);
                    } else {
                        Figure removedFigure = black.removed_figures.get("rook" + targetIndex);
                        black.figure_map.put("rook" + targetIndex, removedFigure);
                        black.removed_figures.remove("rook" + targetIndex);
                    }
                }
                case "n" -> {
                    if (!isColorWhite) {
                        Figure removedFigure = white.removed_figures.get("knight" + targetIndex);
                        white.figure_map.put("knight" + targetIndex, removedFigure);
                        white.removed_figures.remove("knight" + targetIndex);
                    } else {
                        Figure removedFigure = black.removed_figures.get("knight" + targetIndex);
                        black.figure_map.put("knight" + targetIndex, removedFigure);
                        black.removed_figures.remove("knight" + targetIndex);
                    }
                }
                case "b" -> {
                    if (!isColorWhite) {
                        Figure removedFigure = white.removed_figures.get("bishop" + targetIndex);
                        white.figure_map.put("bishop" + targetIndex, removedFigure);
                        white.removed_figures.remove("bishop" + targetIndex);
                    } else {
                        Figure removedFigure = black.removed_figures.get("bishop" + targetIndex);
                        black.figure_map.put("bishop" + targetIndex, removedFigure);
                        black.removed_figures.remove("bishop" + targetIndex);
                    }
                }
                case "q" -> {
                    if (!isColorWhite) {
                        Figure removedFigure = white.removed_figures.get("queen" + targetIndex);
                        white.figure_map.put("queen" + targetIndex, removedFigure);
                        white.removed_figures.remove("queen" + targetIndex);
                    } else {
                        Figure removedFigure = black.removed_figures.get("queen" + targetIndex);
                        black.figure_map.put("queen" + targetIndex, removedFigure);
                        black.removed_figures.remove("queen" + targetIndex);
                    }
                }
                case "k" -> {
                    if (!isColorWhite) {
                        Figure removedFigure = white.removed_figures.get("king" + targetIndex);
                        white.figure_map.put("king" + targetIndex, removedFigure);
                        white.removed_figures.remove("king" + targetIndex);
                    } else {
                        Figure removedFigure = black.removed_figures.get("king" + targetIndex);
                        black.figure_map.put("king" + targetIndex, removedFigure);
                        black.removed_figures.remove("king" + targetIndex);
                    }
                }
                default -> {
                    if (!isColorWhite) {
                        Figure removedFigure = white.removed_figures.get("pawn" + targetIndex);
                        white.figure_map.put("pawn" + targetIndex, removedFigure);
                        white.removed_figures.remove("pawn" + targetIndex);
                    } else {
                        Figure removedFigure = black.removed_figures.get("pawn" + targetIndex);
                        black.figure_map.put("pawn" + targetIndex, removedFigure);
                        black.removed_figures.remove("pawn" + targetIndex);
                    }
                }
            }
            
        }
    
        // Restore original figure position and state
        if (!wasPromotion && figure != null) {
            figure.setMoved(wasMoved);
            figure.setPosition_ignore_rest(convertTool.convert_chess_notation_to_long(figurePos));
        }
    
        change_isTurn();
    }
    
    public void resetGame(Game game){
        //reset variables
        isGameEnded = false;

        //reset board
        board.change_yellow_highlighted_squares(0L);
        for(int i = 0; i < gameHistory.size(); i++){
            gameHistory.remove(i);
        }

        gameHistory.clear();
        white.isTurn = true;
        black.isTurn = false;
        game.historyInDatabase = true;

        //delete old figures
        white.figure_map.clear();
        black.figure_map.clear();
        white.removed_figures.clear();
        black.removed_figures.clear();
        white.hasCastled = false;
        black.hasCastled = false;

        //initialize new figures
        String[] converted_piece_positions = convertTool.String_to_two_strings(board_string);
        white.initializePieces(converted_piece_positions[1]);
        black.initializePieces(converted_piece_positions[0]);
        this.board.update_board(convertTool.convert_map_to_name_both(game.white.figure_map, game.black.figure_map));

        white.isMainPlayer = isWhiteMainPlayer;
        black.isMainPlayer = !isWhiteMainPlayer;
    }
    
    public int getGameEvaluation(Game game){
        if(white.figure_map.size() == 1 && black.figure_map.size() == 1) return 0;
        
        String map_name = convertTool.convert_map_to_name_both(white.figure_map, black.figure_map);
        //check if evaluation is already saved in transposition table
        if(transpositionTable.containsKey(map_name)) return transpositionTable.get(map_name);

        int evaluation = white.getPosEvaluation(game) - black.getPosEvaluation(game);

        //save evaluation in transposition table
        transpositionTable.put(map_name, evaluation);
        

        return evaluation;
    }

    public boolean isRepetitionDraw() {
        if(gameHistory.size() > 6){
            String lastMove = gameHistory.get(gameHistory.size()-1);
            String otherPlayersLastMove = gameHistory.get(gameHistory.size()-2);
            //big if block that checks for repition draw
            if((lastMove.equals(gameHistory.get(gameHistory.size() - 5))&&lastMove.equals(gameHistory.get(gameHistory.size() - 9)))&& otherPlayersLastMove.equals(gameHistory.get(gameHistory.size() - 6))&&otherPlayersLastMove.equals(gameHistory.get(gameHistory.size() - 10))) return true;
        }
        return false;
    }

    public String getPGN_gameHistory(){
        String returnString = "";

        for(Map.Entry<Integer, String> entry : gameHistory.entrySet()) {
            if(entry.getKey() != 0) returnString += " ";
            String value = entry.getValue();

            switch (value.substring(0, 8)) { //castle notation
                case "k1e100g1" -> {
                    returnString += "O-O";
                    continue;
                }
                case "k1e800g8" -> {
                    returnString += "O-O";
                    continue;
                    
                }
                case "k1e100c1" -> {
                    returnString += "O-O-O";
                    continue;
                    
                }
                case "k1e800c8" -> {
                    returnString += "O-O-O";
                    continue;
                }
            }
            if(value.substring(0,1).equals("p")){ // for pawns
                if(value.substring(4,6).equals("00")){ //if there was no capture
                    returnString +=  value.substring(6,8); //example: e4
                }
                else{ //for captures
                    returnString += value.substring(2,3) + "x" + value.substring(6,8); //example dxe4
                }
                if(value.substring(6,8).equals("8") || value.substring(6,8).equals("1")) returnString += "=Q"; //(you can only promote to queen) example: fxg8=Q



            } else { //for every other figure
                if(value.substring(4,6).equals("00")){ //if there was no capture
                    returnString += value.substring(0,1).toUpperCase() + value.substring(6,8); //example: Qf6
                }
                else{ //for captures
                    returnString += value.substring(0,1).toUpperCase() + "x" + value.substring(6,8); //example Qxd7
                }
            }
        }

        return returnString;
    }
    

}