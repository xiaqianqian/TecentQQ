package com.xqq.tecentqq.drag;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;
import com.xqq.tecentqq.entity.Status;

/**
 * Created by xqq on 2015/11/7.
 * <p/>
 * 自定义控件
 */
public class DragLayout extends FrameLayout {

    private ViewDragHelper myDragHelper;
    private ViewGroup mMainContent;//主孩子
    private ViewGroup mLeftContent;//左孩子
    private int mWidth;//屏幕宽度
    private int mHeight;//屏幕高度
    private int mDragRange;//拖拽宽度
    private Status mStatus = Status.Close;//当前状态
    private OnDragStateChangeListener mOnDragStateChangeListener;//拖拽监听器

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //通过静态方法得到一个viewDragHelper辅助类
        myDragHelper = ViewDragHelper.create(this, 1.0f, myCallBack);
    }

    public static interface OnDragStateChangeListener {
        void onOpen();

        void onClose();

        void onDraging(float percent);
    }

    public void setmStatus(Status mStatus) {
        this.mStatus = mStatus;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setOnDragStateChangeListener(OnDragStateChangeListener mOnDragStateChangeListener) {
        this.mOnDragStateChangeListener = mOnDragStateChangeListener;
    }

    public OnDragStateChangeListener getOnDragStateChangeListener() {
        return mOnDragStateChangeListener;
    }

    /**
     * 回调方法
     */
    ViewDragHelper.Callback myCallBack = new ViewDragHelper.Callback() {

        //决定是否要拖拽当前的child
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            Log.d("TAG", "tryCaptureView");
            return child == mMainContent || child == mLeftContent;
        }

        //设置拖拽的横向范围
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragRange;
        }

        /**
         * 决定了View将要防止的位置， 在这里进行位置的修正
         * left：只是建议移动到的位置   dx是水平移动的距离
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int oldLeft = child.getLeft();
            Log.d("TAG", "clampViewPositionHorizontal:  oldLeft:" + oldLeft + "left:" + left + " dx:" + dx);

            if (child == mMainContent) {
                left = fixLeft(left);
                return left;
            }
            return left;
        }

        /**
         * 决定了当前View 位置被改变时，要做的其他事情（伴随动画）
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            Log.d("TAG", "onViewPositionChanged");
            //拖动左面板 主面板也作相应的移动
            int mMainLeft = mMainContent.getLeft();
            if (changedView != mMainContent) {
                mMainLeft += dx;
            }
            //对 mMainLeft进行修正
            mMainLeft = fixLeft(mMainLeft);

            //如果是左边的View 则把他又放回左边 所以左面板看起来没有动
            if (changedView == mLeftContent) {
                mLeftContent.layout(0, 0, mWidth, mHeight);
                mMainContent.layout(mMainLeft, 0, mWidth + mMainLeft, mHeight);
            }

            //执行伴随动画
            dispatchDragEvent(mMainLeft);

            invalidate();//手动重绘
        }

        /**
         * 决定了松手之后要处理的事情VIew被释放，做动画
         * xvel : x轴的速度  yvel:y轴的速度  向左为负值  向右为正值
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (xvel > 0) {
                open(true);
            } else if (xvel == 0 && releasedChild.getLeft() > mDragRange * 0.5) {
                open(true);
            } else {
                close(true);
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        //设置拖拽的
        @Override
        public int getViewVerticalDragRange(View child) {
            return super.getViewVerticalDragRange(child);
        }
    };

    /**
     * 分发拖拽时间， 执行伴随动画
     */
    protected void dispatchDragEvent(int mMainLeft) {
        float percent = mMainLeft * 1.0f / mDragRange;

        Log.d("TAG", "percent:" + percent);
        //执行动画
        animViews(percent);

        if (mOnDragStateChangeListener != null) {
            mOnDragStateChangeListener.onDraging(percent);
        }
        //记录上一次的状态
        Status lastStatus = mStatus;
        //更新状态
        getUpdateStatus(percent);
        if (mStatus != lastStatus && mOnDragStateChangeListener != null) {
            if (mStatus == Status.Open) {
                mOnDragStateChangeListener.onOpen();
            } else if (mStatus == Status.Close) {
                mOnDragStateChangeListener.onClose();
            }
        }
    }

    private void getUpdateStatus(float percent) {
        if (percent == 0) {
            mStatus = Status.Close;
        } else if (percent == 1) {
            mStatus = Status.Open;
        } else {
            mStatus = Status.Draging;
        }
    }

    private void animViews(float percent) {
        //主面板：缩放动画   添加   nineoldandroids-2.4.0.jar
        //不让主面板  缩放太小
        ViewHelper.setScaleX(mMainContent, (1 - percent) * 0.2f + 0.8f);
        ViewHelper.setScaleY(mMainContent, evaluate(percent, 1.0f, 0.8f));//另一种计算方式

        //左面板： 缩放动画、平移动画、透明度变化
        ViewHelper.setScaleX(mLeftContent, (percent) * 0.5f + 0.5f);
        ViewHelper.setScaleY(mLeftContent, evaluate(percent, 0.5f, 1.0f));//另一种计算方式

        ViewHelper.setTranslationX(mLeftContent, evaluate(percent, -mWidth / 2.0f, 0.0f));
        ViewHelper.setAlpha(mLeftContent, evaluate(percent, 0.0f, 1.0f));

        //背景：亮度变化
        getBackground().setColorFilter(evaluateColor(
                        percent, Color.BLACK, Color.TRANSPARENT),
                PorterDuff.Mode.SRC_OVER);
    }

    //颜色估值器
    private int evaluateColor(float fraction, int startValue, int endValue) {

        int startInt = ((Integer) startValue).intValue();
        int startA = startInt >> 24;
        int startR = startInt >> 16 & 255;
        int startG = startInt >> 8 & 255;
        int startB = startInt & 255;
        int endInt = ((Integer) endValue).intValue();
        int endA = endInt >> 24;
        int endR = endInt >> 16 & 255;
        int endG = endInt >> 8 & 255;
        int endB = endInt & 255;

        return Integer.valueOf(startA + (int) (fraction * (float) (endA - startA)) << 24
                | startR + (int) (fraction * (float) (endR - startR)) << 16
                | startG + (int) (fraction * (float) (endG - startG)) << 8
                | startB + (int) (fraction * (float) (endB - startB)));
    }

    //FloatEvaluator 估值器
    private Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /**
     * 打开主面板动画
     */
    public void open(boolean isSmooth) {
        int mLeft = mDragRange;
        if (isSmooth) {
            //触发一个动画，平滑的移动到指定位置
            if (myDragHelper.smoothSlideViewTo(mMainContent, mLeft, 0)) {
                //如果是true 当前还没到指定位置
                ViewCompat.postInvalidateOnAnimation(this); //一定得与computeScroll()一起使用
            }
        } else {
            mMainContent.layout(mLeft, 0, mLeft + mWidth, mHeight);
        }
    }

    /**
     * 关闭主面板动画
     * isSmooth:  平滑动画
     */
    public void close(boolean isSmooth) {
        int mLeft = 0;
        if (isSmooth) {
            //触发一个动画，平滑的移动到指定位置
            if (myDragHelper.smoothSlideViewTo(mMainContent, mLeft, 0)) {
                //如果是true 当前还没到指定位置
                ViewCompat.postInvalidateOnAnimation(this);  //一定得与computeScroll()一起使用
            }
        } else {
            mMainContent.layout(mLeft, 0, mLeft + mWidth, mHeight);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //高频率的调用
        if (myDragHelper.continueSettling(true)) {
            //如果是true 当前还没到指定位置
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 修正左偏移量
     */
    private int fixLeft(int left) {
        if (left < 0) {//不能往左边拖动
            return 0;
        } else if (left > mDragRange) {
            return mDragRange;
        }
        return left;
    }

    /**
     * 由myDragHelper决定是否需要拦截事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return myDragHelper.shouldInterceptTouchEvent(ev);
    }

    /**
     * 由myDragHelper决定如何处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        try {
            myDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //填充完毕之后  调用此方法
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //健壮性
        int childCount = getChildCount();
        if (childCount < 2) {
            throw new IllegalStateException("你需要两个字View！You need 2 children at least!");
        }
        //孩子必须是ViewGroup的子类
        if (!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalStateException("You children must be instanceof ViewGroup!");
        }
        mLeftContent = (ViewGroup) getChildAt(0);
        mMainContent = (ViewGroup) getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //得到屏幕宽度
        mWidth = mMainContent.getMeasuredWidth();
        mHeight = mMainContent.getMeasuredHeight();

        //得到拖拽的一个范围
        mDragRange = (int) (mWidth * 0.6f);
    }
}
