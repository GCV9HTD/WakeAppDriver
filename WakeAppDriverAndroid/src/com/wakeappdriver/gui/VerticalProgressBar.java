package com.wakeappdriver.gui;

import com.wakeappdriver.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * see:
 * https://github.com/mgrzechocinski/AndroidClipDrawableExample
 */
public class VerticalProgressBar extends ImageView {

    public VerticalProgressBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setImageResource(R.drawable.progressbar);
    }

    public void setCurrentValue(int value){
        setImageLevel(value);
    }
    
}