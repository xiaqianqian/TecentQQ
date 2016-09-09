package com.xqq.tecentqq.reminder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.xqq.tecentqq.utils.GeometryUtil;
import com.xqq.tecentqq.utils.Utils;

/**
 * Created by xqq on 2015/11/14.
 */
public class GooView extends View {
    PointF[] mStickPoints = new PointF[]{//固定圆两点坐标
            new PointF(250f, 250f),
            new PointF(250f, 350f)
    };
    PointF[] mDragPoints = new PointF[]{//拖拽圆两点坐标
            new PointF(50f, 250f),
            new PointF(50f, 350f)
    };
    PointF mControlPoint = new PointF(150f, 300f);//控制点坐标
    PointF mStickCenter = new PointF(150f, 150f);//固定圆半径以及圆心坐标
    float mStickRadius = 12f;
    PointF mDragCenter = new PointF(100f, 100f);//拖拽圆半径以及圆心坐标
    private Paint mPaint;//画刷
    private float mDragRadius = 16f;
    private float mStatusBarHeight;

    private Paint mTextPaint;//文本画刷

    private float mFarthest = 80f;//拖拽圆与固定圆之间的最大距离
    private boolean isOutOfRange = false;//判断是否超出最大范围
    private boolean isDisappear = false;//拖拽时是否消失

    private OnStateChangeListener mOnStateChangeListener;//状态监听器

    public interface  OnStateChangeListener{
        /**
         * 当我们要清除时进行回调
         */
        void onDisappear();

        /**
         * 当恢复时进行回调
         * @param isOutOfRange
         */
        void onReset(boolean isOutOfRange);
    }

    public void setOnStateChangeListener(OnStateChangeListener mOnStateChangeListener) {
        this.mOnStateChangeListener = mOnStateChangeListener;
    }

    public OnStateChangeListener getOnStateChangeListener() {
        return mOnStateChangeListener;
    }

    public GooView(Context context) {
        this(context, null);
    }

    public GooView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(18f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                //更新拖拽圆的圆心坐标
                float x = event.getRawX();
                float y = event.getRawY();
                updateDragCenter(x, y);
                isOutOfRange = false;//判断是否超出最大范围
                isDisappear = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                updateDragCenter(rawX, rawY);

                //当拖拽出范围时  断开（move)
                float distanceBetween2Points = GeometryUtil.getDistanceBetween2Points(
                        mDragCenter, mStickCenter);
                if (distanceBetween2Points > mFarthest) {
                    isOutOfRange = true;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isOutOfRange) {
                    //当拖拽没有超出范围， 放手 弹回去
                    onViewReset();
                } else {
                    float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
                    if(distance < mFarthest){
                        //当拖拽超出范围时，又放回去 松手，恢复
                        updateDragCenter(mStickCenter.x, mStickCenter.y);
                        isDisappear = false;
                        if(mOnStateChangeListener != null){
                            mOnStateChangeListener.onReset(isOutOfRange);
                        }
                    } else {
                        //当拖拽超出范围时，没放回去，松手，清除
                        isDisappear = true;
                        invalidate();
                        if(mOnStateChangeListener != null){
                            mOnStateChangeListener.onDisappear();
                        }
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 当拖拽没有超出范围， 放手 弹回去
     */
    private void onViewReset() {
        ValueAnimator mAnim = ValueAnimator.ofFloat(1.0f);
        final PointF startP = new PointF(mDragCenter.x, mDragCenter.y);
        final PointF endP = new PointF(mStickCenter.x, mStickCenter.y);
        mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                PointF pointByPercent = GeometryUtil.getPointByPercent(
                        startP, endP, fraction);
                updateDragCenter(pointByPercent.x, pointByPercent.y);
            }
        });
        mAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(mOnStateChangeListener != null){
                    mOnStateChangeListener.onReset(isOutOfRange);
                }
            }
        });
        mAnim.setInterpolator(new OvershootInterpolator(4.0f));//弹回去
        mAnim.setDuration(500);
        mAnim.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mStatusBarHeight = Utils.getStatusBarHeight(this);
    }

    /**
     * 更新拖拽圆的圆心坐标， 同时绘制界面
     *
     * @param x
     * @param y
     */
    private void updateDragCenter(float x, float y) {
        mDragCenter.set(x, y);
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //平移画布
        canvas.save();
        canvas.translate(0, -mStatusBarHeight);
        //计算坐标
        // 1 、根据两圆心间距，计算固定圆半径
        float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
        float mTempStickRadius = getRadiusByDistance(distance);

        if(!isDisappear) {
            if (isOutOfRange == false) {
                // 2、计算四个附着点坐标
                float offsetX = mStickCenter.x - mDragCenter.x;
                float offsetY = mStickCenter.y - mDragCenter.y;
                Double lineK = null;
                if (offsetX != 0) {
                    lineK = (double) (offsetY / offsetX);
                }
                mDragPoints = GeometryUtil.getIntersectionPoints(mDragCenter, mDragRadius, lineK);
                mStickPoints = GeometryUtil.getIntersectionPoints(mStickCenter, mTempStickRadius, lineK);

                // 3、计算控制点坐标
                mControlPoint = GeometryUtil.getPointByPercent(mDragCenter, mStickCenter, 0.618f);

                //画连接部分
                Path path = new Path();
                path.moveTo(mStickPoints[0].x, mStickPoints[0].y);
                path.quadTo(mControlPoint.x, mControlPoint.y, mDragPoints[0].x, mDragPoints[0].y);
                path.lineTo(mDragPoints[1].x, mDragPoints[1].y);
                path.quadTo(mControlPoint.x, mControlPoint.y, mStickPoints[1].x, mStickPoints[1].y);
                path.close();
                ;
                canvas.drawPath(path, mPaint);
            }
            //画固定圆
            canvas.drawCircle(mStickCenter.x, mStickCenter.y, mTempStickRadius, mPaint);

            //或一个拖拽圆
            canvas.drawCircle(mDragCenter.x, mDragCenter.y, mDragRadius, mPaint);

            //在拖拽圆上添加文本
            canvas.drawText("66", mDragCenter.x, mDragCenter.y + mDragRadius / 2.0f, mTextPaint);
        }
        canvas.restore();
    }

    /**
     * 根据两圆间距，计算固定圆半径
     *
     * @param distance
     */
    private float getRadiusByDistance(float distance) {
        distance = Math.min(distance, mFarthest);
        float percent = distance / mFarthest;
        //原始半径缩放至0.2， 最小0.2
        return evaluate(percent, mStickRadius, mStickRadius * 0.2);
    }

    private float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }
}
