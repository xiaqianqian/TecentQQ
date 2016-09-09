package com.xqq.tecentqq.drag;

import android.app.Notification;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.xqq.tecentqq.entity.Status;

/**
 * Created by xqq on 2015/11/8.
 */
public class MyRelativeLayout extends RelativeLayout {

    private DragLayout mDragLayout;

    public MyRelativeLayout(Context context) {
        super(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //获取当前的状态
    public void setDragLayout(DragLayout mDragLayout) {
        this.mDragLayout = mDragLayout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDragLayout.getStatus() == Status.Close) {//当左面板关闭的时候
            return super.onInterceptTouchEvent(ev);
        } else {//不准触摸
            if (MotionEventCompat.getActionMasked(ev) == MotionEvent.ACTION_UP) {
                mDragLayout.close(true);
            }
            return true;
        }
    }
}
