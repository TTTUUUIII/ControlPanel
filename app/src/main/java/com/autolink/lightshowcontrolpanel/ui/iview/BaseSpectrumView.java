package com.autolink.lightshowcontrolpanel.ui.iview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class BaseSpectrumView extends View{
    protected byte[] magnitudes;
    public BaseSpectrumView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }
    public void refresh(byte[] magnitudes){
        this.magnitudes = magnitudes;
        this.invalidate();
    }
}
