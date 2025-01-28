package Database;

import Code.Main;
import Code.Player;
import Figures.move;
import Tools.convertTool;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class manipulator{
    public static String url = "jdbc:sqlite:Database/chessgames.db";


    public static void saveSample(String sampleName){
        int progression = 0;
        try(Connection conn = DriverManager.getConnection(url)){
            if(conn != null){
                System.out.println("Succesfully connected to database: " + url);

                PreparedStatement pstmtCreateTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS gameHistory (history TEXT NOT NULL UNIQUE)");
                pstmtCreateTable.executeUpdate();

                PreparedStatement pstmtInsert = conn.prepareStatement("INSERT OR IGNORE INTO gameHistory (history) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM gameHistory WHERE history = ?);");

                try (BufferedReader br = new BufferedReader(new FileReader(sampleName))) {
                    String line;
                    StringBuilder game = new StringBuilder();
                    
                    while ((line = br.readLine()) != null) {

                        if (line.trim().isEmpty()) {
                            if(!game.toString().startsWith("[") && !game.toString().contains("{")){
                                String cleanedString = cleanString(game.toString());
                                pstmtInsert.setString(1, cleanedString);
                                pstmtInsert.setString(2, cleanedString); //to check if its not already in the database
                                pstmtInsert.executeUpdate();

                                progression++;
                                if(progression%1000==0)System.out.println(progression);
                            }

                            game.setLength(0); //empty string builder
                        } else {
                            game.append(line).append("\n");
                        }
                    }
        

                    if (game.length() > 0) {
                        if(!game.toString().startsWith("[") && !game.toString().contains("{")){
                            String cleanedString = cleanString(game.toString());
                            pstmtInsert.setString(1, cleanedString);
                            pstmtInsert.setString(2, cleanedString); //to check if its not already in the database
                            pstmtInsert.executeUpdate();

                            progression++;
                                if(progression%1000==0)System.out.println(progression);
                        }
                    }





                } catch (IOException e) {
                    System.out.println("Couldnt find sample: " + sampleName);
                }
            }
            else{
                System.out.println("No connection to database: " + url);
            }
        } catch (Exception e){
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static String cleanString(String string) {
        String[] list = string.split(" ");
        StringBuilder result = new StringBuilder();
    

        
        for (String word : list) {
            if (!word.isEmpty() && !Character.isDigit(word.charAt(0))) { // cancels out who won (example: 1-0) and also which turn it is (example: 43.)
                String wordToAdd = word.replace("#","").replace("+", ""); //remove hashtags and plus
                result.append(wordToAdd).append(" ");
            }
        }
    
        return result.toString().trim();
    }

    public static void displayAllEntries() {
        String query = "SELECT * FROM gameHistory;";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("Saved entries:");

            int counter = 1; 
            while (rs.next()) {
                System.out.println("Entry " + counter + ":");
                System.out.println(rs.getString("history"));
                System.out.println("----------------------------");
                counter++;
            }

            if (counter == 1) {
                System.out.println("Table is empty");
            }

        } catch (Exception e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static void clearTable() {
        String query = "DELETE FROM gameHistory;";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(query);
            System.out.println(rowsAffected + " entries removed");

        } catch (Exception e) {

        }
    }
    public static String getMostPlayedMoveText(){
        String mostPlayedMove = "";

        try (Connection conn = DriverManager.getConnection(url)) {
        String query = "SELECT * FROM gameHistory WHERE history LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, Main.game.getPGN_gameHistory() + "%");


        //returns all games in database that match the main game history
        //and then counts how many times each move appears in all these games.
        //Then it returns the move that appears the most.
        int mainHistoryLength = Main.game.gameHistory.size();
        Map<String, Integer> moveCounts = new HashMap<>();

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String[] initialString = rs.getString("history").split(" ");


            if (mainHistoryLength < initialString.length - 1) {
                String nextMove = initialString[mainHistoryLength];
                moveCounts.put(nextMove, moveCounts.getOrDefault(nextMove, 0) + 1);
            }
        }


            int maxCount = 0;
            for (Map.Entry<String, Integer> entry : moveCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    mostPlayedMove = entry.getKey();
                    maxCount = entry.getValue();
                }
            } 
        

        } 
        catch (Exception e) {
            System.out.println("Error connecting to database");
        }
        return mostPlayedMove;
    }

    public static move getMostPlayedMove(){
            String databaseMove = getMostPlayedMoveText();
            if(databaseMove != null){
                Player player = Main.game.white.isTurn ? Main.game.white : Main.game.black;
                boolean isPawnMove = !(databaseMove.startsWith("O") || databaseMove.startsWith("R") || databaseMove.startsWith("N") || databaseMove.startsWith("B") || databaseMove.startsWith("Q") || databaseMove.startsWith("K"));
                List<move> moveList = new ArrayList<>(player.getMoves(Main.game));

                if(databaseMove.equals("O-O")){
                    if(Main.game.white.isTurn){
                        return new move(Main.game.white.figure_map.get("king1"), convertTool.convert_int_to_bitboard(62));
                    } else {
                        return new move(Main.game.black.figure_map.get("king1"), convertTool.convert_int_to_bitboard(6));
                    }
                }
                if(databaseMove.equals("O-O-O")){
                    if(Main.game.white.isTurn){
                        return new move(Main.game.white.figure_map.get("king1"), convertTool.convert_int_to_bitboard(58));
                    } else {
                        return new move(Main.game.black.figure_map.get("king1"), convertTool.convert_int_to_bitboard(2));
                    }
                }

                if(databaseMove.contains("x")){ //in case of capture
                    if(isPawnMove){
                        for(Figures.move move : moveList){
                            if(convertTool.convert_chess_notation(move.figure.state.position).substring(0,1).equals(databaseMove.substring(0,1)) && databaseMove.substring(2,4).equals(convertTool.convert_chess_notation(move.position))) return move;
                        } 
                    }else{
                        for(Figures.move move : moveList){
                            if(move.figure.getName().equals(databaseMove.substring(0,1).toLowerCase()) && databaseMove.substring(2,4).equals(convertTool.convert_chess_notation(move.position))) return move;
                        } 
                    }
                } else {
                    if(isPawnMove){
                        String targetSquare = databaseMove;
                        for(move move : moveList){
                            if(move.figure.getName().equals("p") && targetSquare.equals(convertTool.convert_chess_notation(move.position))) return move;
                        } 
                    } else {
                        String targetSquare = databaseMove.substring(1,3);
                        for(move move : moveList){
                            if(move.figure.getName().equals(databaseMove.substring(0,1).toLowerCase()) && targetSquare.equals(convertTool.convert_chess_notation(move.position))) return move;
                        }
                    }
                }

            } else {
                Main.game.historyInDatabase = false;
            }
        return null;
    }

}
