package com.iptv.hn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.iptv.hn.utility.Callback;

/**
 * Created by Administrator on 2017/3/12.
 */

public class AdsView extends FrameLayout {

    private Callback mKeyboardCallback;

    public AdsView(Context context) {
        super(context);

        setBackground(new ColorDrawable(getResources().getColor(R.color.transparent)));
    }

    public AdsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackground(new ColorDrawable(getResources().getColor(R.color.transparent)));
    }

    public AdsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackground(new ColorDrawable(getResources().getColor(R.color.transparent)));
    }

    public AdsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setBackground(new ColorDrawable(getResources().getColor(R.color.transparent)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawButton(canvas);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setKeyBoardCallback(Callback callback){
        this.mKeyboardCallback = callback;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (mKeyboardCallback != null) {
                mKeyboardCallback.onFinish(keyCode);
                return true;
            }

        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            if (mKeyboardCallback != null) {
//                mKeyboardCallback.onFail(keyCode);
//            }
        } else if(keyCode == KeyEvent.KEYCODE_BACK) {
            if (mKeyboardCallback != null) {
                mKeyboardCallback.onFail(keyCode);
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }
    private void drawButton(Canvas canvas) {

       /* Rect rect = new Rect((int)40, (int)40, 50, 30);

        String testString = "测试：wanghj:4568";

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(1);
        paint.setTextSize(10);
        canvas.drawRect(rect, paint);
        paint.setColor(Color.RED);
        canvas.drawText(testString, rect.left, rect.bottom, paint);*/
//
    }

}
