package mc2018.jku.at.mindthemine;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

public class SinglePlayer extends AppCompatActivity implements SensorEventListener, Game {
    //TODO vl können wir die Klasse noch ein bisschen zerteilen 900 Zeilen sind schon sehr viel :D
    //TODO z.B. SensorEventListener, LocationListner , Board initialization auslagern, ...
    // test

    private int DIM = Settings.DIM_easy;
    private double PROBABILITY = Settings.PROBABILITY_easy;
    private int MARGIN = Settings.MARGIN_easy; // always set one higher than needed

    private final static String FILENAME = "singleplayer";

    Settings settings;

    private Board board;
    private ImageView[][] cells;

    public int cellColor;

    public int chosenRow, chosenCol;

    private TextView remainingCounter;

    private LocationManager mLocationManager_gps;
    private LocationManager mLocationManager_net;
    private static final int LOCATION_REFRESH_DISTANCE = 2000;
    private static final int LOCATION_REFRESH_TIME = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_FINE = 2;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_COARSE = 1;
    private Chronometer cmTimer;
    private TextView txtAcc;
    private Button btnIndicator;
    private int timeShow;

    private Location activePosLocation;

    // define the display assembly compass picture
    private ImageView compassImageView;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    //Motion sensors
    private Sensor gSensor, aSensor;
    private GestureRecognition gestureRecognition;
    private boolean gestureDetectionActive = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            getSupportActionBar().hide();
        }catch(NullPointerException e){
            Toast.makeText(this, "Failure hiding Action Bar element.", Toast.LENGTH_SHORT).show();
        }

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // initialize the whole location stuff
        mLocationManager_gps = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager_net = (LocationManager) getSystemService(LOCATION_SERVICE);

        while (!checkLocationPermission()) ;

        mLocationManager_gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
        mLocationManager_net.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);

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
                if (timeShow == 0)
                    cmTimer.setText(String.format(Locale.getDefault(), "< %3d", 5));
                else
                    cmTimer.setText(String.format(Locale.getDefault(), "< %3d", timeShow));
            }
        });

        // minesweeper-specific stuff

        TableLayout tl = findViewById(R.id.table);
        remainingCounter = findViewById(R.id.remaingCounter);

        int whole_width, width, margin;

        settings = new Settings(this);

        switch (settings.getDifficulty()) {
            case "Easy":
                DIM = Settings.DIM_easy;
                MARGIN = Settings.MARGIN_easy;
                PROBABILITY = Settings.PROBABILITY_easy;
                break;
            case "Medium":
                DIM = Settings.DIM_medium;
                MARGIN = Settings.MARGIN_medium;
                PROBABILITY = Settings.PROBABILITY_medium;
                break;
            case "Hard":
                PROBABILITY = Settings.PROBABILITY_hard;
                MARGIN = Settings.MARGIN_hard;
                DIM = Settings.DIM_hard;
                break;
            case "Impossible":
                PROBABILITY = Settings.PROBABILITY_impossible;
                MARGIN = Settings.MARGIN_impossible;
                DIM = Settings.DIM_impossible;
                break;
            default:
                DIM = 2;
                MARGIN = 6;
                PROBABILITY = 0.99;
                break;
        }

        do {
            MARGIN--;

            margin = convertToDp(MARGIN);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            whole_width = size.x - (DIM * 2) * margin;

            width = (int) Math.floor(((double) whole_width) / ((double) DIM));
        } while (width <= 2 * margin);

        cellColor = Color.rgb(127, 78, 35);

        cells = new ImageView[DIM][DIM];

        board = readSave();
        if(board == null)
            board = new Board(DIM, DIM, PROBABILITY);

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
                        moveToCell(row, col);
                    }
                });

                cells[i][j] = iv;

                row.addView(iv);
            }

            ImageButton flagButton = findViewById(R.id.flagButton);
            flagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFlag();
                    saveGame();
                }
            });

            ImageButton revealButton = findViewById(R.id.revealButton);
            revealButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reveal();
                    saveGame();
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

                    drawBoard();
                    saveGame();

                    remainingCounter.setText(String.format("%s", board.getRemainingCells()));
                }
            });

            tl.addView(row, i);
        }

        drawBoard();
        remainingCounter.setText(String.format("%s", board.getRemainingCells()));

        // our compass image
        compassImageView = findViewById(R.id.compassImageView);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // initialize motion sensors
        gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gestureDetectionActive = settings.isGestureEnabled();
        if(gSensor == null || aSensor == null) {
            Toast.makeText(getBaseContext(), "Gesture detection is not possible on your phone.", Toast.LENGTH_LONG).show();
            gestureDetectionActive = false;
        } else gestureRecognition = new GestureRecognition(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager_gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, mLocationListener);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager_gps.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, mLocationListener);
        }
        //noinspection deprecation
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        //register motion Sensors
        if(gestureDetectionActive) {
            mSensorManager.registerListener(gestureRecognition, gSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(gestureRecognition, aSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() { //TODO ToDiscuss: wird GPS abgeschaltet wenn man das Display sperrt?
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager_gps.removeUpdates(mLocationListener);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager_gps.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, mLocationListener);
        }

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);

        //stop motion sensor
        mSensorManager.unregisterListener(gestureRecognition);
    }

    public void setFlag() {
        if (board.isNotRunning()) return;
        board.flagField(chosenRow, chosenCol);
        checkBoard();
        drawBoard();
        remainingCounter.setText(String.format("%s", board.getRemainingCells()));
    }

    public void reveal() {
        if (board.isNotRunning()) return;
        board.revealCell(chosenRow, chosenCol);
        checkBoard();
        drawBoard();
        remainingCounter.setText(String.format("%s", board.getRemainingCells()));
    }

    private void moveToCell(int row, int col) {
        if (board.isNotRunning()) return;

        chosenCol = col;
        chosenRow = row;

        board.setActive(row, col);
        saveGame();

        drawBoard();
    }

    private void moveToCellDir(String dir) {
        if (board.isNotRunning()) return;

        int row = 0, col = 0;

        switch (dir) {
            case "N":
                if (chosenCol > 0)
                    col = chosenCol - 1;
                else
                    col = chosenCol;
                row = chosenRow;
                break;
            case "E":
                if (chosenRow < board.getRowCount() - 1)
                    row = chosenRow + 1;
                else
                    row = chosenRow;
                col = chosenCol;
                break;
            case "S":
                if (chosenCol < board.getColCount() - 1)
                    col = chosenCol + 1;
                else
                    col = chosenCol;
                row = chosenRow;
                break;
            case "W":
                if (chosenRow > 0)
                    row = chosenRow - 1;
                else
                    row = chosenRow;
                col = chosenCol;
                break;
        }


        Log.d("MainActivityLogger", "chosen row/col: " + chosenRow + "/" + chosenCol);
        Log.d("MainActivityLogger", "new row/col: " + row + "/" + col);

        chosenRow = row;
        chosenCol = col;

        board.setActive(chosenRow, chosenCol);

        drawBoard();
    }

    private int convertToDp(int pixelValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (pixelValue * scale + 0.5f);
    }

    private void drawBoard() {
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
                for (Cell mine : board.getMines()) if (!mine.hasFlag()) mine.reveal();

                Cell c = board.getActive();
                cells[c.getRowCoord()][c.getColCoord()].setImageResource(R.drawable.ic_explosion);

                Toast.makeText(getBaseContext(), "GAME OVER", Toast.LENGTH_SHORT).show();

                vibrate(false);
            }
        }
    }

    private void vibrate(boolean win) {
        if (!settings.isVibrationEnabled()) return;

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
        if (c.isOpen()) {
            if (c.hasMine()) {
                if (c.isActive())
                    return R.drawable.ic_explosion;
                else
                    return R.drawable.ic_mine;
            } else {
                if (c.isActive()) {
                    switch (c.getSurroundingMines()) {
                        case 1:
                            return R.drawable.ic_1_player;
                        case 2:
                            return R.drawable.ic_2_player;
                        case 3:
                            return R.drawable.ic_3_player;
                        case 4:
                            return R.drawable.ic_4_player;
                        case 5:
                            return R.drawable.ic_5_player;
                        case 6:
                            return R.drawable.ic_6_player;
                        case 7:
                            return R.drawable.ic_7_player;
                        case 8:
                            return R.drawable.ic_8_player;
                        default:
                            return R.drawable.ic_player_c;
                    }
                } else {
                    switch (c.getSurroundingMines()) {
                        case 1:
                            return R.drawable.ic_1;
                        case 2:
                            return R.drawable.ic_2;
                        case 3:
                            return R.drawable.ic_3;
                        case 4:
                            return R.drawable.ic_4;
                        case 5:
                            return R.drawable.ic_5;
                        case 6:
                            return R.drawable.ic_6;
                        case 7:
                            return R.drawable.ic_7;
                        case 8:
                            return R.drawable.ic_8;
                        default:
                            return R.drawable.ic_blank;
                    }
                }
            }
        } else {
            if (c.hasFlag()) {
                if (c.isActive()) {
                    return R.drawable.ic_flag_player;
                } else {
                    if (board.isNotRunning() && !c.hasMine())
                        return R.drawable.ic_wrong_flag;
                    else
                        return R.drawable.ic_flag;
                }
            } else {
                if (c.isActive())
                    return R.drawable.ic_player_c;
                else
                    return R.drawable.ic_blank;
            }
        }
    }

    private int getCellColor(Cell c) {
        int iconColor;
        if (c.isOpen()) {
            iconColor = Color.WHITE;
        } else {
            iconColor = Color.LTGRAY;
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
            if (activePosLocation == null) {
                activePosLocation = new Location("");
                activePosLocation.setLatitude(pos.getLatitude());
                activePosLocation.setLongitude(pos.getLongitude());
            }

            float acc = location.getAccuracy();
            txtAcc.setText(String.format(Locale.getDefault(), "%.1f", acc));

            updateIndicator(btnIndicator, acc);

            cmTimer.setBase(SystemClock.elapsedRealtime());
            cmTimer.start();
            cmTimer.setText(R.string.lblAccInitVal);
            timeShow = 0;

            // TODO remove multiplicator for real life usage
            if (acc < settings.getDistance()) {
                float[] distance = new float[2];
                Location.distanceBetween(activePosLocation.getLatitude(), activePosLocation.getLongitude(), pos.getLatitude(), pos.getLongitude(), distance);

                float dist = distance[0];
                float bear = -1;
                if (distance.length == 2)
                    bear = distance[1];
                else if (distance.length == 3)
                    bear = distance[2];

                int dim = settings.getDistance();
                int dir = -2; // -1...inside; 0...N; 1...E; 2...S; 3...W
                double coeff;
                double diag = Math.sqrt(dim / 2.0 * dim / 2.0 + dim / 2.0 * dim / 2.0);
                if (dist < dim / 2) {
                    dir = -1;
                } else if (0 <= bear && bear < 45) {
                    coeff = (diag - dim / 2.0) / 45;
                    if (dist > dim / 2 + (Math.abs(bear) % 45) * coeff) {
                        dir = 0;
                    }
                } else if (45 <= bear && bear < 90) {
                    if (dist > diag - ((bear - 45) / 45) * ((diag - dim / 2))) {
                        dir = 1;
                    }
                } else if (90 <= bear && bear < 135) {
                    coeff = (diag - dim / 2.0) / 45;
                    if (dist > dim / 2 + (Math.abs(bear) % 45) * coeff) {
                        dir = 1;
                    }
                } else if (135 <= bear && bear < 180) {
                    if (dist > diag - ((bear - 135) / 45) * ((diag - dim / 2.0))) {
                        dir = 2;
                    }
                } else if (-180 <= bear && bear < -135) {
                    if (dist > diag - ((bear * (-1) - 135) / 45) * ((diag - dim / 2.0))) {
                        dir = 2;
                    }
                } else if (-135 <= bear && bear < -90) {
                    coeff = (diag - dim / 2.0) / 45;
                    if (dist > dim / 2.0 + (Math.abs(bear) % 45) * coeff) {
                        dir = 3;
                    }
                } else if (-90 <= bear && bear < -45) {
                    if (dist > diag - ((bear * (-1) - 45) / 45) * ((diag - dim / 2))) {
                        dir = 3;
                    }
                } else if (-45 <= bear && bear < 0) {
                    coeff = (Math.sqrt(dim / 2.0 * dim / 2.0 + dim / 2.0 * dim / 2.0) - dim / 2.0) / 45;
                    if (dist > dim / 2 + (Math.abs(bear) % 45) * coeff) {
                        dir = 0;
                    }
                }

                switch (dir) {
                    case -1:
                        Log.d("MainActivityLogger", "DON'T MOVE " + dist);
                        break;
                    case 0:
                        Log.d("MainActivityLogger", "MOVE NORTH " + dist);
                        moveToCellDir("N");
                        activePosLocation = pos;
                        break;
                    case 1:
                        Log.d("MainActivityLogger", "MOVE EAST " + dist);
                        moveToCellDir("E");
                        activePosLocation = pos;
                        break;
                    case 2:
                        Log.d("MainActivityLogger", "MOVE SOUTH " + dist);
                        moveToCellDir("S");
                        activePosLocation = pos;
                        break;
                    case 3:
                        Log.d("MainActivityLogger", "MOVE WEST " + dist);
                        moveToCellDir("W");
                        activePosLocation = pos;
                        break;
                }

                if(dir >= 0 && dir <= 3)
                    saveGame();
            } else {
                Log.d("MainActivityLogger", "accurracy too bad");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}

    };

    private void updateIndicator(Button btnIndicator, float acc) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (acc > settings.getDistance()/2) {
                btnIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark, this.getTheme()));
            }else if (acc > settings.getDistance()/3) {
                btnIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light, this.getTheme()));
            }else {
                btnIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, this.getTheme()));
            }
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.app_name)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(SinglePlayer.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION_FINE);
                            }
                        }).create().show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION_FINE);
            }
            return false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.app_name)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(SinglePlayer.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION_COARSE);
                            }
                        }).create().show();
            } else { // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION_COARSE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION_FINE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        mLocationManager_gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, mLocationListener);
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(getBaseContext(), "Location permission denied. \nNeed location permissions for the game.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_LOCATION_COARSE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        mLocationManager_net.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, mLocationListener);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getBaseContext(), "Location permission denied. \nNeed location permissions for the game.", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        compassImageView.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void saveGame(){
        try {
            FileOutputStream fos = this.getBaseContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.board);
            oos.flush();
            oos.close();
        } catch (IOException ioe) {
            //show("Could not save settings.\n"+ioe.getMessage());
            Log.d("MainActivitySaveLogger", "File could not be written");
        }
    }

    private Board readSave(){
        Board b = null;
        try {
            FileInputStream fin = this.getBaseContext().openFileInput(FILENAME);
            ObjectInputStream in = new ObjectInputStream(fin);
            b = (Board) in.readObject();
            in.close();
            fin.close();
            if(b.getRowCount() != settings.getDimOfDifficulty())
                return null;
        } catch (IOException i) {
            // what to do?
            Log.d("MainActivitySaveLogger", "File could not be read.");
        } catch (ClassNotFoundException c) {
            // what to do?
            Log.d("MainActivitySaveLogger", "Board not found");
        }
        return b;
    }
}
