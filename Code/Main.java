package Code;

public class Main {
	public static Game game;
	public static String board_string = "RNBQKBNRPPPPPPPP--------------------------------pppppppprnbqkbnr"; //standard start position: "RNBQKBNRPPPPPPPP--------------------------------pppppppprnbqkbnr"
	public static AI ai = new AI();
	public static int coreCount = Runtime.getRuntime().availableProcessors(); //if you dont want multithreading set this to 0 (or set it to the amount of extra threads you want)



	public static void main(String[] args) {
		game = new Game(board_string, "white", 104); //104 (depth 4) works best
		game.startGame();

		//use following code to create a database (youll need a sample in pgn format)
		//manipulator.saveSample(filePathToSample);
	}
}
