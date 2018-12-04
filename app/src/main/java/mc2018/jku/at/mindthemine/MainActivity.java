package mc2018.jku.at.mindthemine;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // test
    static final int DIM = 6;
    static final int MARGIN = 5;

    Board board;
    ImageView[][] cells;

    public int cellColor;

    public int chosenRow, chosenCol;

    private TextView remainingCounter;


    private LocationManager mLocationManager_gps;
    private LocationManager mLocationManager_net;
    private static int LOCATION_REFRESH_DISTANCE = 2000;
    private static int LOCATION_REFRESH_TIME = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_FINE = 2;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_COARSE = 1;
    private String provider_gps = LocationManager.GPS_PROVIDER;
    private String provider_net = LocationManager.NETWORK_PROVIDER;
    private Chronometer cmTimer;
    private TextView txtAcc;
    private Button btnIndicator;
    private int timeShow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // initialize the whole location stuff

        mLocationManager_gps = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager_net = (LocationManager) getSystemService(LOCATION_SERVICE);

        while (!checkLocationPermission()) {
        }

        mLocationManager_gps.requestLocationUpdates(provider_gps, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
        mLocationManager_net.requestLocationUpdates(provider_net, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);

        cmTimer = findViewById(R.id.txtAge);
        cmTimer.setText(R.string.lblAccInitVal);

        txtAcc = findViewById(R.id.txtAcc);
        btnIndicator = findViewById(R.id.btnIndicator);
        timeShow = 0;

        cmTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer arg0) {
                long time = SystemClock.elapsedRealtime() - cmTimer.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;

                if (s % 5 == 0)
                    timeShow += 5;
                cmTimer.setText(String.format("< %3d", timeShow));
            }
        });

        // minesweeper-specific stuff

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
                    remainingCounter.setText(String.format("%s", board.getRemainingCells()));

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
                    remainingCounter.setText(String.format("%s", board.getRemainingCells()));
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
                    remainingCounter.setText(String.format("%s", board.getRemainingCells()));
                }
            });

            tl.addView(row, i);
        }

        setActive();
        remainingCounter.setText(String.format("%s", board.getRemainingCells()));
        //Toast.makeText(getBaseContext(), "DONE", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager_gps.requestLocationUpdates(provider_gps, 400, 1, mLocationListener);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager_gps.requestLocationUpdates(provider_net, 400, 1, mLocationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager_gps.removeUpdates(mLocationListener);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager_gps.requestLocationUpdates(provider_net, 400, 1, mLocationListener);
        }
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
                    200, 100,
                    200, 100,
                    200, 100,
                    200, 100
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

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            Location pos = new Location("");
            pos.setLatitude(location.getLatitude());
            pos.setLongitude(location.getLongitude());

            float acc = location.getAccuracy();
            txtAcc.setText(String.format("%.1f", acc));

            updateIndicator(btnIndicator, acc);

            cmTimer.setBase(SystemClock.elapsedRealtime());
            cmTimer.start();
            cmTimer.setText(R.string.lblAccInitVal);
            timeShow = 0;


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };

    private void updateIndicator(Button btnIndicator, float acc) {
        if (acc > 10)
            btnIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark, this.getTheme()));
        else if (acc > 5)
            btnIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light, this.getTheme()));
        else
            btnIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, this.getTheme()));

//          for testing during development
//        if (acc > 30)
//            btnIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark, this.getTheme()));
//        else if (acc > 25)
//            btnIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light, this.getTheme()));
//        else
//            btnIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, this.getTheme()));

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.app_name)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION_FINE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION_FINE);
            }
            return false;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.app_name)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION_COARSE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION_COARSE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION_FINE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        mLocationManager_gps.requestLocationUpdates(provider_gps, 400, 1, mLocationListener);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getBaseContext(), "Location permission denied. \nNeed location permissions for the game.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_LOCATION_COARSE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        mLocationManager_net.requestLocationUpdates(provider_net, 400, 1, mLocationListener);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getBaseContext(), "Location permission denied. \nNeed location permissions for the game.", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

}
