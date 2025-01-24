package Code;

import Figures.*;
import Tools.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class Board extends JFrame implements MouseListener{

    //variables
    public int chess_board_size = 640;
    public int frame_height = 680;
    public int frame_width = 1000;

    public long yellow_highlighted_squares = 0L;
    public long red_highlighted_squares = 0L;
    private long targetSquare = 0L;
    private Figure targetFigure = null;
    public boolean newTurn = false;

    //make images public
    int pieceSize = chess_board_size/9;      
    String[] pieceNames = {"kw", "kb", "qw", "qb", "rw", "rb", "bw", "bb", "nw", "nb", "pw", "pb"};
    public Map<String, ImageIcon> pieceImages = new HashMap<>();
    
    //make squares public
    JPanel squarePanel = new JPanel(new GridLayout(8, 8));
    JPanel[] squarePanels = new JPanel[64];
    JLabel[] squares = new JLabel[64];
    JPanel controlPanel = new JPanel();
    public JLabel testDisplay1 = new JLabel("");
    JButton restartButton = new JButton("restart");
    

    

    public void init_board(String board){
        //update board state
        //init image
        for (String pieceName : pieceNames) {
            ImageIcon imgIcon = new ImageIcon("images/" + pieceName + ".png");
            imgIcon = new ImageIcon(imgIcon.getImage().getScaledInstance(pieceSize, pieceSize, Image.SCALE_SMOOTH));
            pieceImages.put(pieceName, imgIcon);
        }
        
        //panel with game squares
        int squareSize = chess_board_size/8;
        for (int i = 0; i < 64; i++) {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(squareSize, squareSize));
            panel.setBackground((return_square_color(i).equals("white") ? Color.decode("#fade91") : Color.decode("#96703b")));
            squarePanels[i] = panel;
            squarePanel.add(squarePanels[i]);
            squares[i] = new JLabel();
            squarePanels[i].add(squares[i]);
        } squarePanel.addMouseListener(this);

        //control panel
        testDisplay1.setPreferredSize(new Dimension(frame_width-chess_board_size-200, 50));
        testDisplay1.setOpaque(true);
        testDisplay1.setBackground(Color.white);
        // restartButton.setPreferredSize(new Dimension(frame_width-chess_board_size-250, 40));
        // restartButton.addActionListener(e -> {Main.game.resetGame(Main.game);});
        // restartButton.setBackground(Color.decode("#dd9dff"));
        // restartButton.setFocusable(false);
        controlPanel.setOpaque(true);
        controlPanel.setBackground(Color.decode("#2d2630"));
        // controlPanel.add(testDisplay1);
        controlPanel.add(restartButton);
        controlPanel.setPreferredSize(new Dimension(frame_width-chess_board_size, 0));

        //init main board
        this.setTitle("Minuicee's Chess Engine");
        this.setSize(frame_width,frame_height);
        this.getContentPane().setBackground(Color.lightGray);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        this.add(squarePanel, BorderLayout.WEST);
        this.add(controlPanel, BorderLayout.EAST);

        this.update_board(board);

    }
 
    public void update_board(String board){
        for (int i = 0; i < 64; i++) {
            char c = board.charAt(i);
            if(!String.valueOf(c).equals("-")){
            	if(Character.isUpperCase(c)) {
            		squares[i].setIcon(pieceImages.get(Character.toLowerCase(c)+"b"));
            	} else {
            		squares[i].setIcon(pieceImages.get(String.valueOf(c)+"w"));
            	}
            }
            else{
                squares[i].setIcon(null);
            }
        } 
    }


    public void change_yellow_highlighted_squares(long squares){
        yellow_highlighted_squares = squares;
        for (int i = 0; i < 64; i++) {
            if((squares & (1L << i))!= 0){
                this.squarePanels[i].setBackground(return_square_color(i).equals("white")? Color.decode("#ffff78") : Color.decode("#f7f75c"));
            }
            else {
                this.squarePanels[i].setBackground(return_square_color(i).equals("white")? Color.decode("#fade91") : Color.decode("#96703b"));
            }
        }
    }
    public void change_clicked_square(long squares){
        for (int i = 0; i < 64; i++) {
            if((squares & (1L << i))!= 0){
                this.squarePanels[i].setBackground(return_square_color(i).equals("white")? Color.decode("#ffff78") : Color.decode("#f7f75c"));
            }
            
        }
    }

    public String return_square_color(int i){
        return (i / 8 % 2 == i % 8 % 2)? "white" : "black";
    }

    @Override
    public void mouseClicked(MouseEvent e) { 

    }


    @Override
    public void mousePressed(MouseEvent e) { 
        if(newTurn) newTurn = false;
       interact_with_board(e);
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(newTurn) return;
        interact_with_board(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
       
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
       
    }
    public void interact_with_board(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();
        int squareSize = chess_board_size / 8; 
        int column = mouseX / squareSize;
        int row = mouseY / squareSize;
        long clickedSquare = convertTool.convert_int_to_bitboard(row * 8 + column);
        if (Main.game.white.isTurn) {
            handlePlayerMove(Main.game.white, Main.game.black, clickedSquare);
        } else if (Main.game.black.isTurn) {
            handlePlayerMove(Main.game.black, Main.game.white, clickedSquare);
        }
    }
    
    private void handlePlayerMove(Player player, Player opponent, long clickedSquare) {
        if(!Main.game.isGameEnded && !AI.isThinking){


            long own_squares = player.get_squares(Main.game);
            int firstSetBitPosition = Long.numberOfTrailingZeros(clickedSquare);
        
            if ((own_squares & (1L << firstSetBitPosition)) != 0) {
                // Select a piece
                Map<String, Figure> temporary_figure_map = new HashMap<>(player.figure_map);    
                for (Figure figure : temporary_figure_map.values()) {
                    if(figure == null) continue;
                    if ((figure.getPosition() & (1L << firstSetBitPosition)) != 0) {
                        change_yellow_highlighted_squares(yellow_highlighted_squares);
                        change_clicked_square(clickedSquare);
                        targetSquare = clickedSquare;
                        targetFigure = figure;
                    }
                } 
            } else if ((own_squares & clickedSquare) == 0 && targetFigure != null && (targetFigure.getValidMoves(Main.game, own_squares, opponent.get_squares(Main.game)) & clickedSquare) != 0) {
                // Do a move 

                targetFigure.setPosition(Main.game, clickedSquare, true, true); //set pos
                update_board(convertTool.convert_map_to_name_both(Main.game.white.figure_map, Main.game.black.figure_map));
                change_yellow_highlighted_squares(combineTool.combine_longs_to_bitboard(clickedSquare, targetSquare));
                targetSquare = 0L;
                targetFigure = null;
                SwingUtilities.invokeLater(() -> {
                    if(!Main.game.mainPlayer_isTurn()){
                        if(Main.game.gameMode == 1){
                            Main.ai.doRandomMove();
                        }
                        else if(Main.game.gameMode > 100){
                            Main.ai.doMinimaxMove(Main.game.gameMode - 100);
                        }
                    }
                });
                newTurn = true;
                
            }
        }
    }




}
