package mc2018.jku.at.mindthemine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

class Board {
    private Cell[][] board;
    private int colCount;
    private int rowCount;
    private boolean isRunning;
    private boolean isWon;

    private LinkedList<String> news;

    Board(int rows, int cols){
        this(rows, cols, 0.95);
    }


    // ratio probability of containing a mine (is 1-alpha like)
    Board(int rows, int cols, double ratio) {
        news = new LinkedList<>();

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

    boolean isWon() {
        return this.isWon;
    }

    public Cell[][] getBoard() {
        return this.board;
    }

    Cell getCell(int row, int col) {
        return this.board[row][col];
    }

    String getNews() {
        if (news.isEmpty()) return null;
        return news.remove();
    }

    private void setRandomCellActive() {
        ArrayList<Cell> blanks = new ArrayList<>();
        for (Cell[] row : board) {
            for (Cell c : row) {
                if (c.isBlank())
                    blanks.add(c);
            }
        }

        Cell c = blanks.get((int) (Math.random() * blanks.size()));
        c.setActive(true);
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

    boolean hasBlanks(){
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
}
