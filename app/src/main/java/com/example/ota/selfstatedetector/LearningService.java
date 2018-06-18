package com.example.ota.selfstatedetector;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ota on 17/07/26.
 */

public class LearningService extends IntentService {
    GlobalValues globalValues;
    private int label;

    public LearningService() {
        super("IntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            Log.d("onHandleIntent", "learning success");
            globalValues = (GlobalValues) getApplication();
            label = intent.getIntExtra("label", -1);
            handleActionLearn();
        }
    }

    private void handleActionLearn() {
        globalValues.setModel(label);
    }
}
