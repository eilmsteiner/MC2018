package mc2018.jku.at.mindthemine;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

class Board implements Serializable {
    private Cell[][] board;
    private int colCount;
    private int rowCount;
    private boolean isRunning;
    private boolean isWon;


    Board(int rows, int cols){
        this(rows, cols, 0.95);
    }

    // ratio probability of containing a mine (is 1-alpha like)
    Board(int rows, int cols, double ratio) {

        colCount = cols;
        rowCount = rows;

        isRunning = true;
        isWon = false;

        board = new Cell[rowCount][colCount];

        do {
            boolean hasMine;
            Random r = new Random();
            r.setSeed(System.currentTimeMillis());
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < colCount; j++) {
                    hasMine = (r.nextGaussian() > ratio);
                    board[i][j] = new Cell(i, j, hasMine);
                }
            }

            int surroundingMineCount;
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < colCount; j++) {
                    if (!board[i][j].hasMine()) {
                        surroundingMineCount = 0;

                        // check surrounding fields
                        for (int m = -1; m <= 1; m++) {
                            for (int n = -1; n <= 1; n++) {
                                if (m != 0 || n != 0) { // ignore same cell
                                    if (i + m >= 0 && i + m < rowCount
                                            && j + n >= 0 && j + n < colCount) {
                                        if (board[i + m][j + n].hasMine()) surroundingMineCount++;
                                    }
                                }
                            }
                        }

                        board[i][j].setSurroundingMines(surroundingMineCount);
                    }
                }
            }

        } while(!hasBlanks());

        setRandomCellActive();
    }

    Cell revealCell(int row, int col) {
        if (col < 0 || col >= colCount || row < 0 || row >= rowCount) return null;

        Cell cell = board[row][col];

        // news.addLast(cell.toString());

        if (cell.hasFlag()) {
            return cell;
        }

        if (cell.isOpen()) {
            return cell;
        }

        cell.reveal();

        if (cell.hasMine()) {
            isRunning = false; // game over
        } else {
            if (cell.getSurroundingMines() == 0) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i != 0 || j != 0)
                            this.revealCell(row + i, col + j);
                    }
                }
            }
        }

        setActive(row, col);

        checkBoardState();

        return cell;
    }

    Cell flagField(int row, int col) {
        board[row][col].changeFlagged();

        setActive(row, col);

        checkBoardState();

        return board[row][col];
    }

    private void checkBoardState() {
        boolean winning = true;
        for (Cell[] row : board) {
            for (Cell c : row) {
                if (c.isOpen()) {
                    if (c.hasMine()) {
                        winning = false;
                    } // if field is no mine and is open then good!
                } else {
                    if (c.hasMine()) {
                        if (!c.hasFlag()) {
                            winning = false;
                        } // if mine has flag then yay!
                    } else {
                        winning = false;
                    }
                }
            }
        }
        this.isWon = winning;
        if (this.isWon) this.isRunning = false;
    }

    Cell[] getMines() {
        ArrayList<Cell> mines = new ArrayList<>();

        for (Cell[] row : board) {
            for (Cell cell : row) {
                if (cell.hasMine() && !cell.isOpen())
                    mines.add(cell);
            }
        }

        return mines.toArray(new Cell[0]);
    }

    boolean isNotRunning() {
        return !this.isRunning;
    }

    void setDone() { this.isRunning = false; }

    boolean isWon() {
        return this.isWon;
    }

    Cell[][] getBoard() {
        return this.board;
    } //TODO sinlose Methode?

    Cell getCell(int row, int col) {
        return this.board[row][col];
    }

    private void setRandomCellActive() {
        Cell[] blanks = getBlankCells(true);

        Cell c = blanks[(int) (Math.random() * blanks.length)];
        c.setActive(true);
    }

    private Cell[] getBlankCells(boolean inclusiveActive) {
        ArrayList<Cell> blanks = new ArrayList<>();
        for (Cell[] row : board) {
            for (Cell c : row) {
                if (c.isBlank())
                    if(inclusiveActive) {
                        blanks.add(c);
                    } else {
                        if (!c.isActive()) {
                            blanks.add(c);
                        }
                    }
            }
        }

        return blanks.toArray(new Cell[0]);
    }

    int getNumberOfBlankCells(){
        return getBlankCells(true).length;
    }

    Cell[] getNonActiveBlankCells(){
        return getBlankCells(false);
    }

    void setActive(int rowCoord, int colCoord) {
        for (Cell[] row : board) {
            for (Cell c : row) {
                if (c.getRowCoord() == rowCoord && c.getColCoord() == colCoord) {
                    c.setActive(true);
                } else {
                    c.setActive(false);
                }
            }
        }
    }

    Cell getActive() {
        for (Cell[] row : board) {
            for (Cell c : row) {
                if (c.isActive())
                    return c;
            }
        }
        return null;
    }

    /*@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Board (");
        sb.append(rowCount);
        sb.append("/");
        sb.append(colCount);
        sb.append("):");

        for(int i=0; i<rowCount; i++) {
            sb.append("\n---------------------\n");
            for(int j=0; j<colCount; j++) {
                sb.append(board[i][j].toString());
            }
        }

        return sb.toString();
    }*/
    int getRemainingCells() {
        int count = 0;
        for (Cell[] row : board) for (Cell c : row) if (!c.isOpen() && !c.hasFlag()) count++;
        return count;
    }

    private boolean hasBlanks(){
        for(Cell[] row : board){
            for(Cell c : row){
                if(c.isBlank()) return true;
            }
        }
        return false;
    }


    int getColCount() {
        return colCount;
    }

    int getRowCount() {
        return rowCount;
    }

    String getGSON(){
        Gson gson = new Gson();

        // 2. Java object to JSON, and assign to a String
        return gson.toJson(this);
    }
}
