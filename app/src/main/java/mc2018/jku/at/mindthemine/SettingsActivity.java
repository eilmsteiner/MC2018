package mc2018.jku.at.mindthemine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    Settings settings;
    Spinner difficulty;
    Spinner distance;
    Switch vibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = new Settings(this);

        String[] distances = new String[]{"10m", "25m", "50m", "75m", "100m", "125m", "150m", "175m", "200m", "250m"};

        distance = findViewById(R.id.spnDistance);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distances);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distance.setAdapter(adapter);
        int position = -1;
        for (int i = 0; i < distances.length; i++){
            if (distances[i].equals(settings.getDistance())) {
                position = i;
            }
        }
        //Toast.makeText(this, "Position: "+position, Toast.LENGTH_SHORT).show();
        distance.setSelection(position);

        distance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setDistance((String) distance.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getBaseContext(), "HOW DID YOU DO THAT?! (distance)", Toast.LENGTH_SHORT).show();
            }
        });

        String[] difficulties = new String[]{"Easy", "Medium", "Hard", "Impossible"};

        difficulty = findViewById(R.id.spnDifficulty);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficulties);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficulty.setAdapter(adapter2);
        position = -1;
        for (int i = 0; i < difficulties.length; i++){
            if (difficulties[i].equals(settings.getDifficulty())) {
                position = i;
            }
        }
        //Toast.makeText(this, "Position: "+position, Toast.LENGTH_SHORT).show();
        difficulty.setSelection(position);

        difficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setDifficulty((String) difficulty.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getBaseContext(), "HOW DID YOU DO THAT?! (difficulty)", Toast.LENGTH_SHORT).show();
            }
        });

        vibration = findViewById(R.id.swtVibration);
        vibration.setChecked(settings.isVibrationEnabled());
        vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setVibrationEnabled((isChecked)?1:0);
            }
        });
    }
}
