package com.rx.jdgoodsinfo;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author:XWQ
 * Time   2019/1/29
 * Descrition: this is DragSlideLayout
 */

public class DragSlideLayout extends ViewGroup
{

    private static final int VEL_THRESHOLD = 200; // 滑动速度的阈值，超过这个绝对值认为是上下
    private static final int DISTANCE_THRESHOLD = 100; // 单位是像素，当上下滑动速度不够时，通过这个阈值来判定是应该粘到顶部还是底部
    private int downTop; // 手指按下的时候，frameView1的getTop值
    private ShowNextPageNotifier nextPageListener; // 手指松开是否加载下一页的notifier
    /* 拖拽工具类 */
    private ViewDragHelper mDragHelper;
    private View frameView1, frameView2;
    private int viewHeight;

    public DragSlideLayout(Context context)
    {
        super(context);
    }

    public DragSlideLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mDragHelper = ViewDragHelper
                .create(this, 10f, new DragHelperCallback());
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // 统一交给mDragHelper处理，由DragHelperCallback实现拖动效果
        try
        {
            mDragHelper.processTouchEvent(event);

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return true;
    }

    /**
     * 滑动时view位置改变协调处理
     *
     * @param viewIndex 滑动view的index(1或2)
     * @param posTop    滑动View的top位置
     */
    private void onViewPosChanged(int viewIndex, int posTop)
    {
        if (viewIndex == 1)
        {
            int offsetTopBottom = viewHeight + frameView1.getTop() - frameView2.getTop();
            frameView2.offsetTopAndBottom(offsetTopBottom);
        } else if (viewIndex == 2)
        {
            int offsetTopBottom = frameView2.getTop() - (viewHeight + frameView1.getTop());
            frameView1.offsetTopAndBottom(offsetTopBottom);
        }

        // 有的时候会默认白板，这个很恶心。后面有时间再优化
        invalidate();
    }

    /**
     * 这是拖拽效果的主要逻辑
     */
    private class DragHelperCallback extends ViewDragHelper.Callback
    {

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy)
        {
            int childIndex = 1;
            if (changedView == frameView2)
            {
                childIndex = 2;
            }
            // 一个view位置改变，另一个view的位置要跟进
            onViewPosChanged(childIndex, top);
        }

        //这个地方实际上函数返回值为true就代表可以滑动 为false 则不能滑动
        // 决定了是否需要捕获这个 child，只有捕获了才能进行下面的拖拽行为
        @Override
        public boolean tryCaptureView(View child, int pointerId)
        {
            // 两个子View都需要跟踪，返回true
            return true;
        }

        @Override
        public int getViewVerticalDragRange(View child)
        {
            // 这个用来控制拖拽过程中松手后，自动滑行的速度，暂时给一个随意的数值
            return 1;
        }

        // 手指释放时的回调
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel)
        {
            // 滑动松开后，需要向上或者乡下粘到特定的位置
            animTopOrBottom(releasedChild, yvel);
        }

        // 修整 child 垂直方向上的坐标，top 指 child 要移动到的坐标，dy 相对上次的偏移量
        @Override
        public int clampViewPositionVertical(View child, int top, int dy)
        {
            int finalTop = top;
            if (child == frameView1)
            {
                // 拖动的时第一个view
                if (top > 0)
                {
                    // 不让第一个view往下拖，因为顶部会白板
                    finalTop = 0;
                }
            } else if (child == frameView2)
            {
                // 拖动的时第二个view
                if (top < 0)
                {
                    // 不让第二个view网上拖，因为底部会白板
                    finalTop = 0;
                }
            }
            // finalTop代表的是理论上应该拖动到的位置。此处计算拖动的距离除以一个参数(3)，是让滑动的速度变慢。数值越大，滑动的越慢
            return child.getTop() + (finalTop - child.getTop()) / 3;
        }
    }

    private void animTopOrBottom(View releasedChild, float yvel)
    {
        int finalTop = 0; // 默认是粘到最顶端
        if (releasedChild == frameView1)
        {
            // 拖动第一个view松手
            if ((downTop == 0 && frameView1.getTop() < -DISTANCE_THRESHOLD))
            {
                // 向上的速度足够大，就滑动到顶端
                // 向上滑动的距离超过某个阈值，就滑动到顶端
                finalTop = -viewHeight;

            }
        } else
        {
            // 拖动第二个view松手
            if (releasedChild.getTop() > DISTANCE_THRESHOLD)
            {
                // 保持原地不动
                finalTop = viewHeight;
            }
        }
        boolean smooth = mDragHelper.smoothSlideViewTo(releasedChild, 0, finalTop);
        //Log.d("mmp", "============" + finalTop + "=====smooth=======" + smooth);
        if (smooth)
        {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 添加此方法回弹才可生效
     */
    @Override
    public void computeScroll()
    {
        if (mDragHelper.continueSettling(true))
        {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        int whdth = resolveSizeAndState(maxWidth, widthMeasureSpec, 0);
        int height = resolveSizeAndState(maxHeight, heightMeasureSpec, 0);
        Log.d("mmp", "height" + height);
        setMeasuredDimension(whdth, height);


    }

    /**
     * 这是View的方法，该方法不支持android低版本（2.2、2.3）的操作系统，所以手动复制过来以免强制退出
     */
    public static int resolveSizeAndState(int size, int measureSpec,
                                          int childMeasuredState)
    {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode)
        {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < size)
                {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else
                {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {

        if (frameView1.getTop() == 0)
        {
            // 只在初始化的时候调用
            // 一些参数作为全局变量保存起来
            frameView1.layout(l, 0, r, b - t);
            frameView2.layout(l, 0, r, b - t);
            viewHeight = frameView1.getMeasuredHeight();
            frameView2.offsetTopAndBottom(viewHeight);
        } else
        {

            // 如果已被初始化，这次onLayout只需要将之前的状态存入即可
            frameView1.layout(l, frameView1.getTop(), r, frameView1.getBottom());
            frameView2.layout(l, frameView2.getTop(), r, frameView2.getBottom());
        }
    }

    @Override
    protected void onFinishInflate()
    {
        frameView1 = getChildAt(0);
        frameView2 = getChildAt(1);
        super.onFinishInflate();
    }

    public interface ShowNextPageNotifier
    {
        public void onDragNext();
    }

}
