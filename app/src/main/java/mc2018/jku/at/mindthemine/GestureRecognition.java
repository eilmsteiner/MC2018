package mc2018.jku.at.mindthemine;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.LinkedList;
import java.util.Queue;

public class GestureRecognition implements SensorEventListener {

    private float actualYGravity;
    private Queue<Float> xWindow, yWindow;
    private long timeFlag = 0L, timeRight = 0L, timeLeft = 0L, lock = 0L;
    private final MainActivity mainActivity;
    private static final long SECOND = 1000000000L;

    GestureRecognition(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        xWindow = new LinkedList<>();
        yWindow = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            xWindow.add(0f);
            yWindow.add(0f);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
       if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            actualYGravity = event.values[1];
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if (actualYGravity >= 8.3 && event.timestamp - lock > SECOND) {
                xWindow.poll();
                yWindow.poll();
                xWindow.add(event.values[0]);
                yWindow.add(event.values[1]);
                float sumX = 0, sumY = 0;
                for (int i = 0; i < xWindow.size(); i++) {
                    sumX += (float) xWindow.toArray()[i];
                    sumY += (float) yWindow.toArray()[i];
                }
                double avgX = sumX / xWindow.size();
                double avgY = sumY / yWindow.size();
                if (avgY <= -13) {
                    timeFlag = event.timestamp;
                } else if (event.timestamp - timeFlag < 0.6 * SECOND && avgY >= 9) {
                    mainActivity.setFlag();
                    resetTimes();
                    lock = event.timestamp;
                }
                if (avgX <= -8 && avgY < 6 && avgY > -7) {
                    timeLeft = event.timestamp;
                } else if (event.timestamp - timeLeft < 0.6 * SECOND && avgX >= 8.5) {
                    mainActivity.reveal();
                    resetTimes();
                    lock = event.timestamp;
                }
                if (avgX >= 8 && avgY < 7 && avgY > -7) {
                    timeRight = event.timestamp;
                } else if (event.timestamp - timeRight < 0.6 * SECOND && avgX <= -8.5) {
                    mainActivity.reveal();
                    resetTimes();
                    lock = event.timestamp;
                }
            }
        }
    }

    private void resetTimes() {
        timeFlag = 0L;
        timeRight = 0L;
        timeLeft = 0L;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }
}
