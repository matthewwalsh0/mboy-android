package com.matthew.mboy;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

public class DelayedButtonView extends AppCompatImageButton {

    private static final int BUTTON_UP_DELAY = 20;

    private TouchListener m_listener;

    public DelayedButtonView(Context context) {
        super(context);
        init();
    }

    public DelayedButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DelayedButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(TouchListener listener) {
        m_listener = listener;
    }

    private void init() {
        setOnTouchListener(new DelayedListener());
    }

    public interface TouchListener {
        void onStateChange(boolean down);
    }

    private class DelayedListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch ( event.getAction() ) {
                case MotionEvent.ACTION_DOWN: {
                    m_listener.onStateChange(true);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            m_listener.onStateChange(false);
                        }
                    }, BUTTON_UP_DELAY);
                    break;
                }
            }
            return true;
        }
    }
}
