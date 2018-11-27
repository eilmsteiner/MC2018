package mc2018.jku.at.mindthemine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StartScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        Button sp = findViewById(R.id.btnSinglePlayer);
        sp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent toSinglePlayer = new Intent(getBaseContext(), MainActivity.class);
                startActivity(toSinglePlayer);
            }
        });

        Button coopMp = findViewById(R.id.btnCoopMP);
        coopMp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "To be implemented!", Toast.LENGTH_SHORT).show();
            }
        });

        Button compMp = findViewById(R.id.btnCompMP);
        compMp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "To be implemented!", Toast.LENGTH_SHORT).show();
            }
        });

        Button settings = findViewById(R.id.btnSettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSettings = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(toSettings);
            }
        });
    }
}
