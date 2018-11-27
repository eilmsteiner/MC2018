package mc2018.jku.at.mindthemine;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

class Settings {
    private final static String FILENAME = "settings";

    private Context context;
    private String filePath;

    private String difficulty = "Easy";
    private int vibrationEnabled = 1;

    String getDifficulty(){ return difficulty; }
    boolean isVibrationEnabled() { return vibrationEnabled == 1; }

    Settings(Context context){
        this.context = context;
        this.filePath = context.getFilesDir().toString()+"/"+FILENAME;
        this.loadSettings();
    }

    void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        saveSettings();
    }

    void setVibrationEnabled(int vibrationEnabled){
        this.vibrationEnabled = vibrationEnabled;
        saveSettings();
    }

    private void loadSettings(){
        try {
            FileInputStream fIn = context.openFileInput(FILENAME);
            InputStreamReader isr = new InputStreamReader(fIn);

            /* Prepare a char-Array that will
             * hold the chars we read back in. */
            char[] inputBuffer = new char[100];

            // Fill the Buffer with data from the file
            int len = isr.read(inputBuffer);

            // Transform the chars to a String
            String readString = new String(inputBuffer);

            StringBuilder difficultyBuilder = new StringBuilder();

            boolean beforeSeparator = true;
            for(int i=0; i<len; i++) {
                if(readString.charAt(i) == ';') {
                    beforeSeparator = false;
                } else {
                    if(beforeSeparator){
                        difficultyBuilder.append(readString.charAt(i));
                    }else{
                        vibrationEnabled = readString.charAt(i);
                    }
                }
            }

            this.difficulty = difficultyBuilder.toString();

            show("Elements found: "+difficulty+", "+vibrationEnabled);
        } catch(IOException ioe) {
            //show("File could not be read.\n"+ioe.getMessage());
            // maybe no such file exists
            if(!(new File(filePath)).exists()) {
                show("No such file found.");
                saveSettings(); // create the file and save the standard settings
                loadSettings();
            }
        }
    }

    private void saveSettings(){
        StringBuilder writeString = new StringBuilder();
        writeString.append(this.difficulty);
        writeString.append(";");
        writeString.append(Integer.toString(this.vibrationEnabled));
        try {
            FileOutputStream fOut = context.openFileOutput(FILENAME, MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            osw.write(writeString.toString());

            /* ensure that everything is really written out and close */
            osw.flush();
            osw.close();

            show("Elements written: "+difficulty+", "+vibrationEnabled);
        } catch (IOException ioe) {
            show("Could not write file.\n"+ioe.getMessage());
        }
    }

    private void show(String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}