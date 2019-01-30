package mc2018.jku.at.mindthemine;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Lobby extends AppCompatActivity {

    Settings s;
    static Board b;

    final static String TAG = "LobbyLog";

    TextView titleTextView;
    EditText matchIdEditText;
    Button enterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        s = new Settings(this.getApplicationContext());

        titleTextView = findViewById(R.id.titleTextView);
        matchIdEditText = findViewById(R.id.matchIdEditText);
        enterBtn = findViewById(R.id.btnAddToWaitingroom);

        matchIdEditText.setEnabled(false);
        enterBtn.setEnabled(false);

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                titleTextView.setText("Creating board...");
                do {
                    b = new Board(Settings.DIM_medium, Settings.DIM_medium, Settings.PROBABILITY_medium);
                    titleTextView.setText(titleTextView.getText()+".");
                }while(b.getNumberOfBlankCells() < 2);

                enterWaitingRoom();
            }
        });

        checkForGameOnce();
    }

    private void checkForGameOnce(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://master.picup.cc/index.php?MtmRequestHandler/activeGame";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(response.startsWith("NONE FOUND")){ // no game found -> start a new one and wait
                        titleTextView.setText("No active game found.\nLet's start one up.");

                        checkForWaitingRoom();
                    }else{
                        titleTextView.setText("Game found!");
                        try {
                            int gameId = Integer.parseInt(response);
                            nextStep(gameId);
                        }catch(Exception e){
                            titleTextView.setText("An error occurred.\nPlease try again later!\n(Error code: E001a)");
                        }
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Failed with error msg:\t" + error.getMessage());
                    // edited here
                    try {
                        byte[] htmlBodyBytes = error.networkResponse.data;
                        Log.e(TAG, new String(htmlBodyBytes), error);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    titleTextView.setText("An error occurred.\nPlease try again later!\n(Error code: E001b)");
                }
            }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();

                map.put("pId", s.getUid());
                map.put("type", "comp");

                return map;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void checkForWaitingRoom() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://master.picup.cc/index.php?MtmRequestHandler/getMatchId";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.startsWith("NONE FOUND")){ // no game found -> start a new one and wait
                    titleTextView.setText("You have no open registration at the moment.\nPlease enter a Match-ID.");

                    matchIdEditText.setEnabled(true);
                    enterBtn.setEnabled(true);
                }else{
                    titleTextView.setText("You have an open registration at the moment.\nWaiting...");
                    matchIdEditText.setText(response);

                    matchIdEditText.setEnabled(false);
                    enterBtn.setEnabled(false);

                    checkForGameUntilFound();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Failed with error msg:\t" + error.getMessage());
                        // edited here
                        try {
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e(TAG, new String(htmlBodyBytes), error);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        titleTextView.setText("An error occurred.\nPlease try again later!\n(Error code: E002)");
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();

                map.put("pId", s.getUid());

                return map;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void enterWaitingRoom(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://master.picup.cc/index.php?MtmRequestHandler/addToWaitingRoom";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                if(response.startsWith("WAIT")){
                    matchIdEditText.setEnabled(false);
                    enterBtn.setEnabled(false);

                    checkForGameUntilFound();
                } else {
                    try {
                        int gameId = Integer.parseInt(response);
                        nextStep(gameId);
                    }catch(Exception e){
                        titleTextView.setText("An error occurred.\nPlease try again later!\n(Error code: E003a)");
                    }
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Failed with error msg:\t" + error.getMessage());
                        // edited here
                        try {
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            Log.e(TAG, new String(htmlBodyBytes), error);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        titleTextView.setText("An error occurred.\nPlease try again later!\n(Error code: E003b)");
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();

                String sfp1, sfp2;

                Cell c = b.getActive();

                sfp1 = c.getRowCoord()+","+c.getColCoord();

                Cell[] others = b.getNonActiveBlankCells();

                Cell c2 = others[(int) (Math.random() * others.length)];

                sfp2 = c2.getRowCoord()+","+c2.getColCoord();

                map.put("mId", matchIdEditText.getText().toString());
                map.put("pId", s.getUid());
                map.put("sfp1", sfp1);
                map.put("sfp2", sfp2);
                map.put("gb", b.getGSON());

                return map;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void checkForGameUntilFound(){
        titleTextView.setText("You have registered.\nPlease wait for an opponent.");
        Timer t = new Timer();
        t.scheduleAtFixedRate(
                new TimerTask(){
                    public void run() {
                        // Instantiate the RequestQueue.
                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        String url ="http://master.picup.cc/index.php?MtmRequestHandler/activeGame";

                        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if(!response.startsWith("NONE FOUND")){ // no game found -> start a new one and wait
                                    titleTextView.setText("Game found!");
                                    int gameId = Integer.parseInt(response);
                                    nextStep(gameId);
                                }
                            }
                        },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d(TAG, "Failed with error msg:\t" + error.getMessage());
                                        // edited here
                                        try {
                                            byte[] htmlBodyBytes = error.networkResponse.data;
                                            Log.e(TAG, new String(htmlBodyBytes), error);
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                        titleTextView.setText("An error occurred.\nPlease try again later!\n(Error code: E004)");
                                    }
                                }
                        ){
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> map = new HashMap<>();

                                map.put("pId", s.getUid());
                                map.put("type", "comp");

                                return map;
                            }
                        };

                        // Add the request to the RequestQueue.
                        queue.add(request);
                    }
                },
                0,      // run first occurrence immediatetly
                500); // run every 0.5 seconds
    }

    private void nextStep(int gameId){
        Intent intent = new Intent();
        intent.putExtra("gameId", gameId);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("gameId", -1);
        setResult(RESULT_OK, intent);
        finish();
    }
}
