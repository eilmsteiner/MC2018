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
    Switch vibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = new Settings(this);

        String[] difficulties = new String[]{"Easy", "Medium", "Hard"};

        difficulty = findViewById(R.id.spnDifficulty);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficulty.setAdapter(adapter);
        int position = -1;
        for (int i = 0; i < difficulties.length; i++){
            if (difficulties[i].equals(settings.getDifficulty())) {
                position = i;
            }
        }
        Toast.makeText(this, "Position: "+position, Toast.LENGTH_SHORT).show();
        difficulty.setSelection(position);

        difficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setDifficulty((String) difficulty.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getBaseContext(), "HOW DID YOU DO THAT?!", Toast.LENGTH_SHORT).show();
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
