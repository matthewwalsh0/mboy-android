package com.matthew.mboy;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DpadView extends androidx.appcompat.widget.AppCompatImageButton {

    private static final int BUTTON_UP_DELAY = 20;

    private DirectionListener m_upListener;
    private DirectionListener m_leftListener;
    private DirectionListener m_downListener;
    private DirectionListener m_rightListener;

    public DpadView(Context context) {
        super(context);
        init();
    }

    public DpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DpadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnUpListener(DirectionListener listener) {
        m_upListener = listener;
    }

    public void setOnLeftListener(DirectionListener listener) {
        m_leftListener = listener;
    }

    public void setOnDownListener(DirectionListener listener) {
        m_downListener = listener;
    }

    public void setOnRightListener(DirectionListener listener) {
        m_rightListener = listener;
    }

    private void init() {
        setOnTouchListener(new DpadTouchListener());
    }

    public interface DirectionListener {
        void onStateChange(boolean down);
    }

    private class DpadTouchListener implements View.OnTouchListener {

        private DirectionListener m_listener;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch ( event.getAction() ) {
                case MotionEvent.ACTION_DOWN: {
                    m_listener = getListener(event.getX(), event.getY());
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

        private DirectionListener getListener(float x, float y) {
            float upDistance = y;
            float bottomDistance = getHeight() - y;
            float leftDistance = x;
            float rightDistance = getWidth() - x;

            DirectionListener closest = null;
            float distance = Integer.MAX_VALUE;

            if(upDistance < distance) {
                closest = m_upListener;
                distance = upDistance;
            }

            if(bottomDistance < distance) {
                closest = m_downListener;
                distance = bottomDistance;
            }

            if(leftDistance < distance) {
                closest = m_leftListener;
                distance = leftDistance;
            }

            if(rightDistance < distance) {
                closest = m_rightListener;
            }

            return closest;
        }
    }
}
