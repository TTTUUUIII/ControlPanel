package com.autolink.lightshowcontrolpanel.ui;

import com.autolink.lightshowcontrolpanel.MainActivity;

import java.util.HashMap;

public class LineOption {
   private String backgroundColor = "";
   private HashMap<String, Object> xAxis = new HashMap();
   private HashMap<String, Object> yAxis = new HashMap();
   private HashMap<String, Object> series = new HashMap();

    public LineOption(){
        String[] xData = new String[31];
        for (int i = 1; i <= xData.length; i++){
            xData[i - 1] = "频段" + i;
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

    @Override
    public String toString() {
        return MainActivity.GSON_INSTANCE.toJson(this);
    }
}
