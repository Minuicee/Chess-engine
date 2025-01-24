package Code;

import Figures.Figure;
import Figures.move;
import java.util.List;
import java.util.Map;

public class threadclass {
    public Thread thread;
    public Map.Entry<move, Integer> bestmove;
    public boolean ready = false; // if calculations are done


    public void startThread(List<Map.Entry<move, Integer>> moveList, int depth, Map<String, Figure> white_map, Map<String, Figure> black_map, boolean isMaximizing, Map<Integer, String> gameHistory){
        thread = new Thread(() -> {
            searchMoves(moveList, depth, white_map, black_map, isMaximizing, gameHistory);
        }
        );
        thread.start();
    }

    public void searchMoves(List<Map.Entry<move, Integer>> moveList, int depth, Map<String, Figure> white_map, Map<String, Figure> black_map, boolean isMaximizing, Map<Integer, String> gameHistory){
        Game game = new Game("----------------------------------------------------------------", "white", 1);
        game.white.figure_map = white_map;
        game.black.figure_map = black_map;
        game.gameHistory = gameHistory;
        game.white.isTurn = isMaximizing;
        game.black.isTurn = !isMaximizing;
        int bestMove = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        move bestMoveObject = null;

        for(Map.Entry<move, Integer> entry : moveList){
            move pseudoMove = entry.getKey();
            move realMove;
            if(isMaximizing){
                realMove = new move(game.white.figure_map.get(pseudoMove.figure.getFullName()), pseudoMove.position);
            }
            else {
                realMove = new move(game.black.figure_map.get(pseudoMove.figure.getFullName()), pseudoMove.position);
            }
            realMove.figure.setPosition(game, realMove.position, false, false);
            int evaluation = Main.ai.minimax(game, !isMaximizing, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, entry.getValue(), depth, 0);
            game.undoMove(); 

            if(isMaximizing){
                if(evaluation > bestMove){
                    bestMove = evaluation;
                    bestMoveObject = realMove;
                }
            } else {
                if(evaluation < bestMove){
                    bestMove = evaluation;
                    bestMoveObject = realMove;
                }
            }
        }
        if(bestMoveObject != null){
        bestmove = Map.entry(bestMoveObject, bestMove);
        } 
        this.ready = true;
    }
}
