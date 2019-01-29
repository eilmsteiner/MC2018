package mc2018.jku.at.mindthemine;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

public class MultiplayerCompetitive extends AppCompatActivity {

    TextView mTextView;

    Board b;

    Settings s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_competitive);

        mTextView = findViewById(R.id.textViewToWrite);

        b = new Board(2, 2);

        s = new Settings(this.getApplicationContext());

        Timer t = new Timer();

        t.scheduleAtFixedRate(
            new TimerTask(){
                public void run() {
                    //communicate();
                    communicateJSON();
                }
            },
            0,      // run first occurrence immediatetly
            2000); // run every two seconds
    }

    void communicate(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://www.google.com";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Display the first 500 characters of the response string.
                    mTextView.setText("Response is: "+ response.substring(0,500));
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mTextView.setText("That didn't work!");
                }
            }
        );

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    void communicateJSON(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://http://master.picup.cc/mtm/index.php?mtmrequesthandler/activeGame";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Display the first 500 characters of the response string.
                    mTextView.setText("Response is: "+ response.substring(0,500));
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //mTextView.setText("That didn't work!");
                    //mTextView.setText(">"+s.getUid()+"< what");
                    mTextView.setText(error.getMessage());
                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<>();

                    map.put("id", s.getUid());
                    map.put("obj", b.getGSON());

                    return map;
                }
            };

        // Add the request to the RequestQueue.
        queue.add(request);

        Log.d("MulitplayerLog", s.getUid());
        Log.d("MulitplayerLog", b.getGSON());
    }
}
