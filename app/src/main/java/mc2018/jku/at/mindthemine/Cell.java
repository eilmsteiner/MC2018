package mc2018.jku.at.mindthemine;

public class Cell {
    private boolean hasMine;
    private boolean hasFlag;
    private boolean isOpen;
    private boolean isActive;
    private int surroundingMines;
    private int rowCoord;
    private int colCoord;

    public Cell(int rowCoord, int colCoord) {
        this(rowCoord, colCoord, false);
    }

    Cell(int rowCoord, int colCoord, boolean hasMine) {
        this.colCoord = colCoord;
        this.rowCoord = rowCoord;
        this.hasMine = hasMine;
        this.hasFlag = false;
        this.isOpen = false;
        this.isActive = false;
        this.surroundingMines = 0;
    }

    boolean hasMine() { return this.hasMine; }
    boolean hasFlag() { return this.hasFlag; }
    void changeFlagged() { this.hasFlag = !this.hasFlag; }

    boolean isActive() { return this.isActive; }
    void setActive(boolean active) { this.isActive = active; }

    boolean isOpen() { return this.isOpen; }
    void reveal() { this.isOpen = true; }

    void setSurroundingMines(int val) { this.surroundingMines = val; }
    int getSurroundingMines() { return this.surroundingMines; }

    int getRowCoord() { return this.rowCoord; }
    int getColCoord() { return this.colCoord; }

    /*@Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Field (");
        sb.append(rowCoord);
        sb.append("/");
        sb.append(colCoord);
        sb.append(")");
        sb.append("\nOpen: ");
        if(isOpen)  sb.append("TRUE");
        else        sb.append("FALSE");
        sb.append("\nHas Mine: ");
        if(hasMine) sb.append("TRUE");
        else        sb.append("FALSE");
        sb.append("\nHas Flag: ");
        if(hasFlag) sb.append("TRUE");
        else        sb.append("FALSE");
        sb.append("\nSurrounding mines: ")
        sb.append(surroundingMines);
        return sb.toString();
    }*/
}
