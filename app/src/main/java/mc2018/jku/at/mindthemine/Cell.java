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

    public Cell(int rowCoord, int colCoord, boolean hasMine) {
        this.colCoord = colCoord;
        this.rowCoord = rowCoord;
        this.hasMine = hasMine;
        this.hasFlag = false;
        this.isOpen = false;
        this.isActive = false;
        this.surroundingMines = 0;
    }

    public boolean hasMine() { return this.hasMine; }
    public boolean hasFlag() { return this.hasFlag; }
    public boolean isBlank() { return (getSurroundingMines() == 0 && !hasMine()); }
    public void changeFlagged() { this.hasFlag = !this.hasFlag; }

    public boolean isActive() { return this.isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public boolean isOpen() { return this.isOpen; }
    public void reveal() { this.isOpen = true; }

    public void setSurroundingMines(int val) { this.surroundingMines = val; }
    public int getSurroundingMines() { return this.surroundingMines; }

    public int getRowCoord() { return this.rowCoord; }
    public int getColCoord() { return this.colCoord; }

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
