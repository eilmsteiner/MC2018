package mc2018.jku.at.mindthemine;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    // test
    static final int DIM = 6;
    static final int MARGIN = 5;

    Board board;
    ImageView[][] cells;

    public int cellColor;

    public int chosenRow, chosenCol;

    private TextView remainingCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TableLayout tl = findViewById(R.id.table);
        remainingCounter = findViewById(R.id.remaingCounter);

        int margin = convertToDp(MARGIN);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int whole_width = size.x - (DIM * 2) * margin;

        int width = (int) Math.floor(((double) whole_width) / ((double) DIM));

        cellColor = Color.rgb(127, 78, 35);

        cells = new ImageView[DIM][DIM];

        board = new Board(DIM, DIM);

        Cell c = board.getActive();
        chosenCol = c.getColCoord();
        chosenRow = c.getRowCoord();

        for (int i = 0; i < DIM; i++) {
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

            row.setLayoutParams(lp);

            for (int j = 0; j < DIM; j++) {
                //ImageView Setup
                ImageView iv = new ImageView(this);

                // margin
                TableRow.LayoutParams tr_lp = new TableRow.LayoutParams(width, width);

                tr_lp.setMargins(margin, margin, margin, margin);

                //setting image position
                iv.setLayoutParams(tr_lp);

                iv.setId(i * DIM + j);

                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int id = v.getId();

                        int row = id % DIM;
                        int col = id / DIM;

                        chosenRow = row;
                        chosenCol = col;

                        board.setActive(row, col);

                        setActive();
                    }
                });

                cells[i][j] = iv;

                row.addView(iv);
            }

            //row.setBackgroundColor(backgrounds[i]);

            ImageButton flagButton = findViewById(R.id.flagButton);
            flagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (board.isNotRunning()) return;

                    Cell cell = board.flagField(chosenRow, chosenCol);

                    /*try {
                        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        // Vibrate for 100 milliseconds
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vib.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            vib.vibrate(100);
                        }
                    } catch (Exception e) {
                        // nothing to do
                    }*/

                    checkBoard();

                    // show news
                    String s;
                    while ((s = board.getNews()) != null) {
                        Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
                    }

                    setActive();
                    remainingCounter.setText(String.format("%s",board.getRemainingCells()));

                }
            });

            ImageButton revealButton = findViewById(R.id.revealButton);
            revealButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (board.isNotRunning()) return;

                    board.revealCell(chosenRow, chosenCol);

                    checkBoard();

                    // show news
                    String s;
                    while ((s = board.getNews()) != null) {
                        Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
                    }

                    setActive();
                    remainingCounter.setText(String.format("%s",board.getRemainingCells()));
                }
            });


            Button restartButton = findViewById(R.id.restartButton);
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("Board state", "Board gets newly initialized");

                    board = new Board(DIM, DIM);

                    Cell c = board.getActive();
                    chosenCol = c.getColCoord();
                    chosenRow = c.getRowCoord();

                    setActive();

                    //Toast.makeText(getBaseContext(), "New game started!", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.restartButton).setVisibility(View.GONE);
                    remainingCounter.setText(String.format("%s",board.getRemainingCells()));
                }
            });

            tl.addView(row, i);
        }

        setActive();
        remainingCounter.setText(String.format("%s",board.getRemainingCells()));
        //Toast.makeText(getBaseContext(), "DONE", Toast.LENGTH_SHORT).show();
    }

    private int convertToDp(int pixelValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (pixelValue * scale + 0.5f);
    }

    private void setActive() {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                //setting image resource
                cells[j][i].setImageResource(getCellDrawable(board.getCell(i, j)));
                cells[j][i].setBackgroundColor(getCellColor(board.getCell(i, j)));
            }
        }
    }

    public void checkBoard() {
        if (board.isNotRunning()) {
            if (board.isWon()) {
                Toast.makeText(getBaseContext(), "YOU WON!", Toast.LENGTH_LONG).show();
                vibrate(true);
            } else {
                for (Cell mine : board.getMines()) mine.reveal();

                Cell c = board.getActive();
                cells[c.getRowCoord()][c.getColCoord()].setImageResource(R.drawable.ic_explosion);

                //setActive(DIM, DIM);

                Toast.makeText(getBaseContext(), "GAME OVER", Toast.LENGTH_SHORT).show();

                vibrate(false);
            }
            findViewById(R.id.restartButton).setVisibility(View.VISIBLE);


        }
    }

    private void vibrate(boolean win) {
        long[] vibrationPattern;
        if (win)
            vibrationPattern = new long[]{
                    0, 100,
                    500,100,
                    500,100,
                    500,100,
                    500,100
            };
        else
            vibrationPattern = new long[]{
                    0, 700,
                    50, 75,
                    50, 70,
                    50, 65,
                    50, 60,
                    50, 55,
                    50, 50,
                    50, 45,
                    50, 40,
                    50, 35,
                    50, 30,
                    50, 25,
                    50, 20,
                    50, 15,
                    50, 10
            };

        try {
            Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1));
            } else {
                //deprecated in API 26
                vib.vibrate(vibrationPattern, -1); // -1 ... no repeat
            }
        } catch (Exception e) {
            // nothing to do
        }
    }

    private int getCellDrawable(Cell c) {
        if (c.isActive()) return R.drawable.ic_player_c;
        if (c.hasFlag()) return R.drawable.ic_flag;
        if (!c.isOpen()) return R.drawable.ic_action_name;
        if (c.hasMine()) return R.drawable.ic_mine;
        return R.drawable.ic_action_name;
    }

    private int getCellColor(Cell c) {
        int iconColor;
        if (c.isOpen()) {
            if (c.hasMine()) {
                iconColor = Color.BLACK;
            } else {
                switch (c.getSurroundingMines()) {
                    case 0:
                        iconColor = Color.WHITE;
                        break;
                    case 1:
                        iconColor = Color.BLUE;
                        break;
                    case 2:
                        iconColor = Color.GREEN;
                        break;
                    case 3:
                        iconColor = Color.YELLOW;
                        break;
                    case 4:
                        iconColor = Color.MAGENTA;
                        break;
                    default:
                        iconColor = Color.RED;
                        break;
                }
            }
        } else {
            if (c.hasFlag()) {
                iconColor = Color.DKGRAY;
            } else {
                iconColor = Color.LTGRAY;
            }
        }

        return iconColor;
    }
}
