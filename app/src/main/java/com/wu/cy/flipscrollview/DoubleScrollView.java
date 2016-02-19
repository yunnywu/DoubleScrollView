package com.wu.cy.flipscrollview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * Created by wcy8038 on 2016/2/17.
 */
public class DoubleScrollView extends ViewGroup {

    private static final int SCROLL_THRESHOLD =  300;

    private static final int SLIDING_ANIMATION_DURATION = 500;

    boolean isMeasured;

    int mViewHeight;

    int mViewWidth;

    View mTopView;

    View mBottomView;

    private int mCurrentViewIndex;

    private boolean canPullUp;

    private  boolean canPullDown;

    private float mMoveLen;

    private float mLastY;

    private float mDownY;

    OnScrollEndListener mListener;

    private int mTouchSlop;

    public interface OnScrollEndListener {
        /**
         *  跳转结束
         * @param toFirst 是否滑动到第一页 true 滑到第一页， false 滑到第二页
         */
        void onScrollEnd(boolean toFirst);
    }

    public DoubleScrollView(Context context) {
        super(context);
        initView();
    }

    public DoubleScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DoubleScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mTouchSlop  =  ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mCurrentViewIndex = 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mTopView.layout(0, (int) mMoveLen, mViewWidth,
                mTopView.getMeasuredHeight() + (int) mMoveLen);
        mBottomView.layout(0, mTopView.getMeasuredHeight() + (int) mMoveLen,
                mViewWidth, mTopView.getMeasuredHeight() + (int) mMoveLen
                        + mBottomView.getMeasuredHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(!isMeasured) {
            isMeasured = true;

            mTopView = getChildAt(0);
            mBottomView = getChildAt(1);

            mTopView.setOnTouchListener(topViewTouchListener);
            mBottomView.setOnTouchListener(bottomViewTouchListener);
        }

        mViewHeight = getMeasuredHeight();
        mViewWidth = getMeasuredWidth();

        int size = getChildCount();
        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.measure(widthMeasureSpec, heightMeasureSpec);
            }
        }

    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()){
            case  MotionEvent.ACTION_DOWN:
                mDownY  = mLastY = ev.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                float y = ev.getY();
                float deltaY = y - mDownY;
                if(Math.abs(deltaY) < mTouchSlop){
                    return false;
                }
                if ((y < mDownY && canPullUp && mCurrentViewIndex == 0) || (y > mDownY && canPullDown && mCurrentViewIndex == 1)){
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                float deltaY = y - mLastY;
                if((deltaY > 0 && canPullDown && mCurrentViewIndex == 1) ||
                        (deltaY < 0 && canPullUp && mCurrentViewIndex == 0 )) {
                    mMoveLen += deltaY;
                    requestLayout();
                }
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                float endY = event.getY();
                if(endY > mDownY && canPullDown && mCurrentViewIndex == 1){
                    if(mMoveLen > -mViewHeight + SCROLL_THRESHOLD){
                        startAnimation(true);
                    }else{
                        startAnimation(false);
                    }
                }else if(endY < mDownY && canPullUp && mCurrentViewIndex == 0){
                    if(mMoveLen > -SCROLL_THRESHOLD) {
                        startAnimation(true);
                    }else{
                        startAnimation(false);
                    }
                }
                mDownY = mLastY = 0;
        }

        return super.onTouchEvent(event);
    }

    private void startAnimation(final boolean toFirst) {
        ValueAnimator animator = ValueAnimator.ofFloat(0,1).setDuration(SLIDING_ANIMATION_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction  = animation.getAnimatedFraction();
                if(toFirst) {
                    mMoveLen = mMoveLen * (1 - fraction);
                }else{
                    mMoveLen = mMoveLen - ((mMoveLen + mViewHeight) * fraction);
                }
                requestLayout();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mMoveLen = toFirst ? 0 :  -mViewHeight;
                mCurrentViewIndex =  toFirst ? 0 : 1;
                requestLayout();
                if(mListener != null){
                    mListener.onScrollEnd(toFirst);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private OnTouchListener topViewTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ScrollView sv = (ScrollView) v;
            if ((sv.getScrollY() == (sv.getChildAt(0).getMeasuredHeight() - sv
                    .getMeasuredHeight()) || (sv.getScrollY() == 0 &&
                    sv.getChildAt(0).getMeasuredHeight() < sv.getMeasuredHeight())) && mCurrentViewIndex == 0) {
                canPullUp = true;
            }else {
                canPullUp = false;
            }
            return false;
        }
    };
    private OnTouchListener bottomViewTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getScrollY() == 0 && mCurrentViewIndex == 1) {
                canPullDown = true;
            }else {
                canPullDown = false;
            }
            return false;
        }
    };

    public void setOnScoolEndListener(OnScrollEndListener listener){
        mListener = listener;
    }
}
