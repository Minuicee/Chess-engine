package Tools;

import Figures.*;
import java.util.Arrays;
import java.util.Map;

public class convertTool{

    public static long[] convert_name_to_bitboard(String board){
        long[] bitboard = new long[6];
        bitboard[0] = 0L; //pawns
        bitboard[1] = 0L; //rooks
        bitboard[2] = 0L; //knights
        bitboard[3] = 0L; //bishops
        bitboard[4] = 0L; //queens
        bitboard[5] = 0L; //king

        for (int i = 0; i < 64; i++) {
            char c = Character.toLowerCase(board.charAt(i));
            switch(c){
                case 'p' -> bitboard[0] = setBit(bitboard[0], i);
                case 'r' -> bitboard[1] = setBit(bitboard[1], i);
                case 'n' -> bitboard[2] = setBit(bitboard[2], i);
                case 'b' -> bitboard[3] = setBit(bitboard[3], i);
                case 'q' -> bitboard[4] = setBit(bitboard[4], i);
                case 'k' -> bitboard[5] = setBit(bitboard[5], i);
            }
        }
        return bitboard;
    }
    
    public static String convert_bitboard_to_name_single(String color, long[] bitboard){
        String return_string = "";
        for (int i = 0; i < 64; i++) {
            if((bitboard[0] & (1L << i))!= 0){
                return_string += "p";
            }
            else if((bitboard[1] & (1L << i))!= 0){
                return_string += "r";
            }
            else if((bitboard[2] & (1L << i))!= 0){
                return_string += "n";
            }
            else if((bitboard[3] & (1L << i))!= 0){
                return_string += "b";
            }
            else if((bitboard[4] & (1L << i))!= 0){
                return_string += "q";
            }
            else if((bitboard[5] & (1L << i))!= 0){
                return_string += "k";
            }
            else{
                return_string += "-";
            }
        }
        if(color.equals("white")){
            return return_string;
        }
        else{
            return return_string.toUpperCase();
        }

    }

    public static long setBit(long bitboard, int index){
        return bitboard | (1L << index);
    }


    public static String convert_map_to_name_both(Map<String, Figure> white_bitboard, Map<String, Figure> black_bitboard) {
        char[] board = new char[64];
        Arrays.fill(board, '-'); // Standardmäßig leere Felder

        // Mapping von Figuren-Typen zu Zeichen
        Map<String, Character> whiteMapping = Map.of(
            "p", 'p', "r", 'r', "n", 'n', "b", 'b', "q", 'q', "k", 'k'
        );
        Map<String, Character> blackMapping = Map.of(
            "p", 'P', "r", 'R', "n", 'N', "b", 'B', "q", 'Q', "k", 'K'
        );

        // Weiße Figuren einfügen
        for (Figure figure : white_bitboard.values()) {
            if (figure == null) continue;
            int position = Long.numberOfTrailingZeros(figure.getPosition());
            board[position] = whiteMapping.getOrDefault(figure.getName(), '-');
        }

        // Schwarze Figuren einfügen
        for (Figure figure : black_bitboard.values()) {
            if (figure == null) continue;
            int position = Long.numberOfTrailingZeros(figure.getPosition());
            board[position] = blackMapping.getOrDefault(figure.getName(), '-');
        }

        return new String(board);
    }

    public static long convert_int_to_bitboard(int num){
        return (1L << num);
    }
    public static String[] String_to_two_strings(String board){
        String[] return_strings =  new String[2];
        return_strings[0] = "";
        return_strings[1] = "";

        for(int i = 0; i < 64; i++){
            char c = board.charAt(i);
            if(Character.isUpperCase(c)&&c!='-'){
                return_strings[0] += c;
                return_strings[1] += "-";
            }
            else if(Character.isLowerCase(c)&&c!='-'){
                return_strings[1] += c;
                return_strings[0] += "-";
            }
            else{
                return_strings[0] += "-";
                return_strings[1] += "-";
            }
        }
        return return_strings;
    }
    public static String convert_chess_notation(long square){
        int squareIndex = Long.numberOfTrailingZeros(square);
        String[] letters = {"a","b","c","d","e","f","g","h"};
        String[] numbers = {"8","7","6","5","4","3","2","1"};
        return letters[squareIndex%8]+numbers[squareIndex/8];
    }
    public static long convert_chess_notation_to_long(String square){
        String[] letters = {"a","b","c","d","e","f","g","h"};
        String[] numbers = {"8","7","6","5","4","3","2","1"};
        int index = 0;
        for(int i = 0; i < 8; i++){
            if(letters[i].equals(square.substring(0,1))){
                index += i;
            }
            if(numbers[i].equals(square.substring(1,2))){
                index += i*8;
            }
        }
        return 1L << index;
    }
    public static int convert_chess_notation_to_int(String square){
        String[] letters = {"a","b","c","d","e","f","g","h"};
        String[] numbers = {"8","7","6","5","4","3","2","1"};
        int index = 0;
        for(int i = 0; i < 8; i++){
            if(letters[i].equals(square.substring(0,1))){
                index += i;
            }
            if(numbers[i].equals(square.substring(1,2))){
                index += i*8;
            }
        }
        return index;
    }
    public static int convert_long_to_int(long square){
        return Long.numberOfTrailingZeros(square);
    }
    
}