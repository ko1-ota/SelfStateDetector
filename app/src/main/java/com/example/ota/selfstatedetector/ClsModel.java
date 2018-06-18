package com.example.ota.selfstatedetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ota on 17/07/26.
 */

public class ClsModel {
    // List of int
    private List<Integer> labelList = new ArrayList<>();
    // List of float[9]
    private List<float[]> dataList = new ArrayList<>();

    public void addLabel(int label, float[] data) {
        labelList.add(label);
        dataList.add(data);
    }

    public List<Integer> getLabelList() { return labelList; }

    public List<float[]> getDataList() { return  dataList; }
}
