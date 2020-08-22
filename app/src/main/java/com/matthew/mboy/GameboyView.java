package com.matthew.mboy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class GameboyView extends SurfaceView {

    public static final int GAME_WIDTH = 160;
    public static final int GAME_HEIGHT = 144;
    public static final float ASPECT_RATIO = GAME_HEIGHT / (float) GAME_WIDTH;

    public GameboyView(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int requestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int requestedHeight = MeasureSpec.getSize(heightMeasureSpec);
        boolean landscape = requestedWidth > requestedHeight;

        int width = landscape ? new Float(requestedHeight / ASPECT_RATIO).intValue() : requestedWidth;
        int height = landscape ? requestedHeight : new Float(requestedWidth * ASPECT_RATIO).intValue();

        setMeasuredDimension(width, height);
    }
}

