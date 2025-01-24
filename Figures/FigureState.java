package Figures;

public class FigureState {

    public String color;    
    public long position;
    public boolean isMoved;
    public int index;


    public FigureState(String color, long position, boolean isMoved, int index) {
        this.color = color;
        this.position = position;
        this.isMoved = isMoved;
        this.index = index;
    }
    
}
