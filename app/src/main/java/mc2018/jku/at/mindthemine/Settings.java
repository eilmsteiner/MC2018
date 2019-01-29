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

    public static final int DIM_easy = 6;
    public static final int MARGIN_easy = 6;
    public static final double PROBABILITY_easy = 0.95;

    public static final int DIM_medium = 8;
    public static final int MARGIN_medium = 6;
    public static final double PROBABILITY_medium = 0.8;

    public static final int DIM_hard = 10;
    public static final int MARGIN_hard = 6;
    public static final double PROBABILITY_hard = 0.5;

    public static final int DIM_impossible = 20;
    public static final int MARGIN_impossible = 2;
    public static final double PROBABILITY_impossible = .0;

    private final static String FILENAME = "settings";

    private Context context;
    private String filePath;

    private String difficulty = "Easy";
    private int vibrationEnabled = 1;
    private int distance = 10;
    private int gestureEnabled = 1;

    private String uid;

    String getDifficulty(){ return difficulty; }
    String getUid(){ return uid; }
    int getDistance(){ return distance; }
    boolean isVibrationEnabled() { return vibrationEnabled == 1; }
    boolean isGestureEnabled() { return gestureEnabled == 1; }

    Settings(Context context){
        this.context = context;
        this.filePath = context.getFilesDir().toString()+"/"+FILENAME;
        this.uid = Identifier.id(context);
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

    void setDistance(int distance){
        this.distance = distance;
        saveSettings();
    }

    void setGestureEnabled(int gestureEnabled){
        this.gestureEnabled = gestureEnabled;
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
            StringBuilder uidBuilder = new StringBuilder();

            this.distance = 0;
            int separator = 0;
            for(int i=0; i<len; i++) {
                if(readString.charAt(i) == ';') {
                    separator++;
                } else {
                    switch(separator){
                        case 0:
                            difficultyBuilder.append(readString.charAt(i));
                            break;
                        case 1:
                            vibrationEnabled = readString.charAt(i) - '0';
                            break;
                        case 2:
                            if(Character.isDigit(readString.charAt(i)))
                                distance = distance*10 + (readString.charAt(i) - '0');
                            break;
                        case 3:
                            this.gestureEnabled = readString.charAt(i) - '0';
                            break;
                        case 4:
                            uidBuilder.append(readString.charAt(i));
                            break;
                        default:
                            break;
                    }
                }
            }

            if(difficultyBuilder.length() > 0) {
                this.difficulty = difficultyBuilder.toString();
            }
            if(uidBuilder.length() > 0) {
                this.uid = uidBuilder.toString();
            }

        } catch(IOException ioe) {
            //show("File could not be read.\n"+ioe.getMessage());
            // maybe no such file exists
            if(!(new File(filePath)).exists()) {
                //show("No such file found.");
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
        writeString.append(";");
        writeString.append(this.distance);
        writeString.append(";");
        writeString.append(Integer.toString(this.gestureEnabled));
        writeString.append(";");
        writeString.append(this.uid);
        try {
            FileOutputStream fOut = context.openFileOutput(FILENAME, MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            osw.write(writeString.toString());

            /* ensure that everything is really written out and close */
            osw.flush();
            osw.close();

        } catch (IOException ioe) {
            show("Could not save settings.\n"+ioe.getMessage());
        }
    }

    private void show(String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public int getDimOfDifficulty(){
        if(difficulty.equals("Easy"))
            return DIM_easy;
        if(difficulty.equals("Medium"))
            return DIM_medium;
        if(difficulty.equals("Hard"))
            return DIM_hard;
        if(difficulty.equals("Impossible"))
            return DIM_impossible;
        return DIM_easy;
    }
}
