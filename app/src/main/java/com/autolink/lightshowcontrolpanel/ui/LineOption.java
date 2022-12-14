package com.autolink.lightshowcontrolpanel.ui;

import com.autolink.lightshowcontrolpanel.MainActivity;

import java.util.HashMap;

public class LineOption {
   private String backgroundColor = "";
   private boolean animation = true;

   private HashMap<String, Object> xAxis = new HashMap();
   private HashMap<String, Object> yAxis = new HashMap();
   private HashMap<String, Object> series = new HashMap();

    public LineOption(int xSize){
        String[] xData = new String[xSize];
        for (int i = 0; i < xData.length; i++){
            xData[i] = "频段" + i;
        }
        yAxis.put("type", "category");
        yAxis.put("data", xData);
        xAxis.put("type", "value");
        series.put("type", "line");
    }

    public LineOption setData(byte[] data){
        series.put("data", data);
        return this;
    }

    public LineOption setAnimation(boolean animation) {
        this.animation = animation;
        return this;
    }

    @Override
    public String toString() {
        return MainActivity.GSON_INSTANCE.toJson(this);
    }
}
