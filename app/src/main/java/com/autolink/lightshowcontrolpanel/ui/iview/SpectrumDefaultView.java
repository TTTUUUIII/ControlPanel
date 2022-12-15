package com.autolink.lightshowcontrolpanel.ui.iview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class SpectrumDefaultView extends BaseSpectrumView {
    private Paint paint = new Paint();
    public SpectrumDefaultView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStrokeWidth(7.0f);
        if (magnitudes != null){
            for (int i = 0; i < magnitudes.length; i++){
                paint.setColor(choicePaintColor(magnitudes[i]));
                float aStartX = 0.0f;
                float aStartY = i * (getHeight() / magnitudes.length) + 10.0f;
                float aStopX = (float)magnitudes[i] / 15 * getHeight();
                float aStopY = i * (getHeight() / magnitudes.length) + 10.0f;
                canvas.drawLine(aStartX, aStartY, aStopX, aStopY, paint);
            }
        }
    }

    private int choicePaintColor(float m){
        int color;
        if (m < 5){
            color = 0xff000000;
        }else if (m < 8){
            color = 0xff00ff00;
        }else if (m < 12){
            color = 0xff0000ff;
        }else {
            color = 0xffff0000;
        }
        return color;
    }
}
