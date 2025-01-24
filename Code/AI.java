package Code;

import Database.manipulator;
import Figures.*;
import Tools.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AI{
    public static boolean isThinking = false;
    public static LocalDateTime thinkingStartingTime;

    public void startLoop(int mode){
        while(!Main.game.isGameEnded){
            if(!isThinking){
                if(mode == 1){
                        doRandomMove();
                }
                else if(mode > 10 && mode <= 100){
                    doRandomMove();
                    doMinimaxMove(mode-10);
                }
                else if(mode > 100){
                    doMinimaxMove(mode-100);
                }
            }
        }
    }
    public void doRandomMove(){
        Random rand = new Random();
        List<move> possibleMoves = Main.game.white.isTurn ? Main.game.white.getMoves(Main.game) : Main.game.black.getMoves(Main.game);
        if(possibleMoves.isEmpty()){
            return;
        }
        int randomIndex = rand.nextInt(possibleMoves.size());
        move randomMove = possibleMoves.get(randomIndex);
        doMove(randomMove);
    }

    public void doMinimaxMove(int depth){
        System.out.println();
        isThinking = true;
        thinkingStartingTime = LocalDateTime.now();

        if(Main.game.white.figure_map.size()+Main.game.black.figure_map.size() <= 10) depth += 1;
        move bestMove = getBestMove(depth-1); //-1 because if you type 0 in main class that would otherwise correspond to depth 1
        long thinkingTime = java.time.Duration.between(thinkingStartingTime, LocalDateTime.now()).toMillis();

        //if it was very quick, try again with more depth
        if(thinkingTime < 100) bestMove = getBestMove(depth+2);
        else if(thinkingTime < 600) bestMove = getBestMove(depth+1);
        else if(thinkingTime < 3000) bestMove = getBestMove(depth);

        //update thinking time
        thinkingTime = java.time.Duration.between(thinkingStartingTime, LocalDateTime.now()).toMillis();

        System.out.println("Thinking time: " + thinkingTime + " ms (depth: " + depth + ")");

        if(bestMove != null) doMove(bestMove);
        isThinking = false;
    }

    public void doMove(move pseudomove){
        Player player = Main.game.white.isTurn ? Main.game.white : Main.game.black;
        move move = new move(player.figure_map.get(pseudomove.figure.getFullName()), pseudomove.position); // you cant just access move.figure because it might be a figure from a copy of the game from a created thread
        long currentPos = move.figure.getPosition();
        move.figure.setPosition(Main.game , move.position, true, true);
        Main.game.board.change_yellow_highlighted_squares(combineTool.combine_longs_to_bitboard(currentPos, move.position));
        Main.game.transpositionTable.clear();
    }

    public int minimax(Game game, boolean isMaximizing, int depth, int alpha, int beta, int lastMoveEvaluation, int maxDepth, int searchedDepth){
        int currentEvaluation = game.getGameEvaluation(game);

        //if change in evaluation is large, search deeper (quiescence search)
        //Also extend search if not alot of time has passed
        long passedTime = java.time.Duration.between(thinkingStartingTime, LocalDateTime.now()).toMillis();
        if(lastMoveEvaluation - currentEvaluation > 200 || currentEvaluation - lastMoveEvaluation < -200) depth += 1;
        else if(searchedDepth == maxDepth - 1 ){
            if(passedTime < 300) depth += 2;
            else if(passedTime < 1000 ) depth += 1;
        }
        


        if(depth == 0 || searchedDepth == maxDepth + 3) return currentEvaluation;
        else if(game.isRepetitionDraw()) return 0;

        if(isMaximizing){
            int bestMove = Integer.MIN_VALUE;
            List<move> possibleMoves = game.white.getMoves(game);
            if(possibleMoves.isEmpty()){ 
                if(game.white.isCheck(game)) return -100000 + searchedDepth; // for checkmate
                return 0; // in case of stalemate or draw
            }
            for(move move : possibleMoves){
                move.figure.setPosition(game, move.position, false, false);
                int evaluation = minimax(game, !isMaximizing, depth - 1, alpha, beta, currentEvaluation, maxDepth, searchedDepth +1);
                game.undoMove();
                bestMove = Math.max(bestMove, evaluation);
                alpha = Math.max(alpha, bestMove);
                if(beta <= alpha && depth > 2) depth = 2;
            }
            return bestMove;


        } else {
            int bestMove = Integer.MAX_VALUE;
            List<move> possibleMoves = game.black.getMoves(game); // for checkmate
            if(possibleMoves.isEmpty()){
                if(game.black.isCheck(game)) return 100000 - searchedDepth;
                return 0; // in case of stalemate or draw
            }

            for(move move : possibleMoves){
                move.figure.setPosition(game, move.position, false, false);
                int evaluation = minimax(game, !isMaximizing, depth - 1, alpha, beta, currentEvaluation, maxDepth, searchedDepth +1);
                game.undoMove();
                bestMove = Math.min(bestMove, evaluation);
                beta = Math.min(beta,evaluation);
                if(beta <= alpha && depth > 2) depth = 2;
            }
            return bestMove;
        }
    }
    public move getBestMove(int depth){
        move bestMoveObject = null;
        if(Main.game.historyInDatabase){ //if previous searches succeeded search again for the most played move in the database (based on the current position)
            move dataBaseMove = manipulator.getMostPlayedMove();
            if(dataBaseMove != null){
                return dataBaseMove;
            }
        }

        Game game = new Game("----------------------------------------------------------------", "white", 0);
        game.white.figure_map = Main.game.white.createCopyOfMap();
        game.black.figure_map = Main.game.black.createCopyOfMap();
        game.gameHistory = new HashMap<>(Main.game.gameHistory);
        boolean isMaximizing =  Main.game.white.isTurn;
        game.white.isTurn = isMaximizing;
        game.black.isTurn = !isMaximizing;

        int bestMove = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        List<Map.Entry<move, Integer>> possibleMoves = isMaximizing ? game.white.getOrderedMoves(game) : game.black.getOrderedMoves(game);
        if(possibleMoves.isEmpty()){
            return null;
        }
        else if(possibleMoves.size() == 1){ // in case of forced moves
            return possibleMoves.get(0).getKey();
        }


        int threadcount = Main.coreCount; 

        //init lists
        List<List<Map.Entry<move, Integer>>> moveLists = new ArrayList<>();
        for (int i = 0; i < threadcount+ 1; i++){
            moveLists.add(new ArrayList<>());
        }
        
        //fill lists with moves
        int j = 0;
        for(Map.Entry<move, Integer> move : possibleMoves){
            moveLists.get(j).add(move);
            j++;
            if(j >= threadcount + 1) j = 0;
        }
        List<Map.Entry<move, Integer>> mainThreadList = moveLists.remove(0);
        
        //create threads
        List<threadclass> threads = new ArrayList<>();
        for(int i = 0; i < threadcount; i++){
            threads.add(new threadclass());
        }

        //start threads
        for(threadclass thread : threads){
                Map<String, Figure> whiteCopy = Main.game.white.createCopyOfMap(); //create copy so threads cant access original
                Map<String, Figure> blackCopy = Main.game.black.createCopyOfMap();
                Map<Integer, String> gameHistoryCopy = new HashMap<>(Main.game.gameHistory);
            thread.startThread(moveLists.remove(0), depth, whiteCopy, blackCopy,isMaximizing, gameHistoryCopy);
        }
        //start main thread
        for(Map.Entry<move, Integer> entry : mainThreadList){
            move move = entry.getKey();
            move.figure.setPosition(game, move.position, false, false);
            int evaluation = minimax(game, !isMaximizing, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, entry.getValue(), depth, 0);
            game.undoMove();
            
            if(isMaximizing){
                if(evaluation > bestMove){
                    bestMove = evaluation;
                    bestMoveObject = move;
                }
            } else {
                if(evaluation < bestMove){
                    bestMove = evaluation;
                    bestMoveObject = move;
                }
            }
        } 

        //wait for all threads to finish
        boolean allThreadsFinished = false;
        while (!allThreadsFinished) {
            allThreadsFinished = true;
            for (threadclass thread : threads) {
                try {
                    if (!thread.ready) {
                        thread.thread.join();
                        allThreadsFinished = false;
                    }
                } catch (InterruptedException e) {
                }
            }
        }

        //get best move 
        for(threadclass thread : threads){
            Map.Entry<move, Integer> entry = thread.bestmove;
            if(entry == null) continue;
            if(isMaximizing){
                if(entry.getValue() > bestMove){
                    bestMove = entry.getValue();
                    bestMoveObject = entry.getKey();
                }
            } else {
                if(entry.getValue() < bestMove){
                    bestMove = entry.getValue();
                    bestMoveObject = entry.getKey();
                }
            }
        }


        return bestMoveObject;
    }
}