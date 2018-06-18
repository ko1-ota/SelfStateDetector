package com.example.ota.selfstatedetector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.constraint.solver.Goal;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {
    private GlobalValues globalValues;

//    private float[] pressure;
//    private float[] illiminance;
//    private float[] gravity;

    TextView pressureTextView;
    TextView illuminanceTextView;
    TextView gravityTextView;
    TextView stateTextView;
    TextView learningTextView;

    private Button teacherButton;
    private Button modeSwitchButton;
    private int mode = 0;
//    private Vibrator vibrator;

    private String LABEL_0 = "OnDesk";
    private String INSTRUCTION_0 = "Put on desk and press button.";
    private String LABEL_1 = "InHand";
    private String INSTRUCTION_1 = "Hold in hand and press button";
    private String LABEL_2 = "OverHead";
    private String INSTRUCTION_2 = "Raise over head and press button";
    private String LABEL_3 = "OnEar";
    private String INSTRUCTION_3 = "Put to your ear and press button";
    private List<String> LABEL_LIST = new ArrayList<String>() {{add(LABEL_0); add(LABEL_1); add(LABEL_2); add(LABEL_3);}};
    private List<String> INSTRUCTION_LIST = new ArrayList<String>() {{add(INSTRUCTION_0); add(INSTRUCTION_1); add(INSTRUCTION_2); add(INSTRUCTION_3);}};
    private int labelI = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        globalValues = (GlobalValues) this.getApplication();

//        // get vibrator control
//        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // set text view to show each sensor value
        pressureTextView = (TextView) findViewById(R.id.pressure_text_view);
        illuminanceTextView = (TextView) findViewById(R.id.illuminance_text_view);
        gravityTextView = (TextView) findViewById(R.id.gravity_text_vew);

        // learning instruction
        learningTextView = (TextView) findViewById(R.id.learning_text_view);

        // set text view to show classified self state
        stateTextView = (TextView) findViewById(R.id.state_text_view);

        // make teacher Button
        teacherButton = (Button) findViewById(R.id.teacher_button);
        learningTextView.setText(INSTRUCTION_LIST.get(labelI));
        teacherButton.setText(LABEL_LIST.get(labelI));
        teacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent learnIntent = new Intent(MainActivity.this, LearningService.class);
                learnIntent.putExtra("label", labelI);
                startService(learnIntent);
                labelI = (labelI + 1) % LABEL_LIST.size();
                learningTextView.setText(INSTRUCTION_LIST.get(labelI));
                teacherButton.setText(LABEL_LIST.get(labelI));
            }
        });

        // make mode switch button
        modeSwitchButton = (Button) findViewById(R.id.mode_switch_button);
        setModeSwitchButtonText();
        modeSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = (mode + 1) % 2;
                Log.d("mode is", String.valueOf(mode));
                setModeSwitchButtonText();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start measuring
        Intent intent = new Intent(this, MeasuringService.class);
        startService(intent);

        // do in every 1000 ms
        final android.os.Handler handler = new android.os.Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                switch (mode) {
                    case 0:
                        // show present sensor values
                        textView();
                        break;
                    case 1:
                        // classify present state
                        int selfState = clsSelfState();
                        if (selfState > -1) { stateTextView.setText(LABEL_LIST.get(selfState)); }
//                        if (selfState == 3) { vibrator.vibrate(500); }
                        break;
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(r);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // rewrite button text with mode
    public void setModeSwitchButtonText () {
        switch (mode) {
            case 0:
                modeSwitchButton.setText("FINISH LEARN");
                modeSwitchButton.setBackgroundColor(Color.parseColor("#AAAAAA"));
                stateTextView.setText("Learning Mode");
                break;
            case 1:
                modeSwitchButton.setText("DETECTING");
                modeSwitchButton.setBackgroundColor(Color.parseColor("#FF4136"));
                break;
        }
    }

    // show sensor values
    public void textView() {
        List<float[]> sensorValues = globalValues.getSensorValues();
        pressureTextView.setText(String.format("%.3f", sensorValues.get(0)[0]));
        illuminanceTextView.setText(String.format("%.3f", sensorValues.get(1)[0]));
        gravityTextView.setText(String.format("%.3f", sensorValues.get(2)[0]));
    }

    // classify and show self state
    public int clsSelfState() {
        Log.d("clsSelfState", "start");
        // how many neighbours
        int K = 10;

        // load model
        ClsModel clsModel = globalValues.getClsModel();
        Log.d("clsModel label size", String.valueOf(clsModel.getLabelList().size()));

        // load present sensor values
        List<float[]> sensorValues = globalValues.getSensorValues();
        // convert values to 1-dim vector
        float[] sensorValuesVector = new float[5];
        System.arraycopy(sensorValues.get(0), 0, sensorValuesVector, 0, 1);
        System.arraycopy(sensorValues.get(1), 0, sensorValuesVector, 1, 1);
        System.arraycopy(sensorValues.get(2), 0, sensorValuesVector, 2, 3);

        // classify with model: kNN
        // classification result
        int clsResult = -1;
        // 1st-Kth largest distances: 1st to Kth
        float[] tmpDistance = new float[K];
        for (int i = 0; i < K; i++) { tmpDistance[i] = 1000000; }
        int[] tmpLabel = new int[K];
        for (int i = 0; i < K; i++) { tmpLabel[i] = -1; }

        for (int i = 0; i < clsModel.getLabelList().size(); i++) {
            // teacher sensor values: float vector of 9-dim
            float[] dataVector = clsModel.getDataList().get(i);
            // teacher label: int (0, 1, 2)
            int label = clsModel.getLabelList().get(i);
//            Log.d("label", String.valueOf(label));

            // calculate distance
            float distance = dist(sensorValuesVector, dataVector);

            // compare distances
            for (int j = 0; j < K; j++) {
               if (tmpDistance[j] > distance) {
                   for (int m = K-1; m > j; m--) {
                       tmpDistance[m] = tmpDistance[m-1];
                       tmpLabel[m] = tmpLabel[m-1];
                   }
                   tmpDistance[j] = distance;
                   tmpLabel[j] = label;
                   break;
               }
            }
        }

        // find major label
        int[] labelCount = new int[LABEL_LIST.size()];
        for (int i = 0; i < K; i++) {
            if (tmpLabel[i] > -1) { labelCount[tmpLabel[i]] += 1; }
        }
        int tmpCount = 0;
        for (int i = 0; i < LABEL_LIST.size(); i++) {
            if (labelCount[i] > 0) {
                if (tmpCount < labelCount[i]) {
                    clsResult = i;
                    tmpCount = labelCount[i];
                }
            }
        }
        for (int i = 0; i < tmpDistance.length; i++) {
            Log.d("clsSelfState labelCount", String.valueOf(tmpDistance[i]));
        }
        Log.d("clsSelfState Result", String.valueOf(clsResult));
        return clsResult;
    }

    public float dist(float[] vec1, float[] vec2) {
        float[] vecDiff = new float[Math.min(vec1.length, vec2.length)];
        for (int i = 0; i < vecDiff.length; i++) {
            vecDiff[i] = vec1[i] - vec2[i];
        }
        double norm = 0;
        for (int j = 0; j < vecDiff.length; j++) {
            norm += Math.pow((double) vecDiff[j], (double) 2);
        }
        return (float) Math.sqrt(norm);
    }
}
