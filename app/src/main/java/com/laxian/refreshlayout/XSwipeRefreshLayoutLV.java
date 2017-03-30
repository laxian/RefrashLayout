package com.laxian.refreshlayout;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

/**
 * Created by zhouweixian on 2016/2/1.
 * 添加下拉加载功能
 */
public class XSwipeRefreshLayoutLV extends SwipeRefreshLayout implements AbsListView.OnScrollListener {
    private static final float MIN_HEIGHT = 200;
    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private final int FOOTER_VIEW_TAG = 123;

    private ListView mListView;
    private View mFooterView;

    private float rawX;
    private float rawY;
    private float rawX_UP;
    private float rawY_UP;

    private OnLoadListener mLoadListener;
    private boolean mLoading;
    private boolean mCanLoad;
    private FooterStyle mFooterStyle;
    private boolean hasFooterView;

    /**
     * footer 样式，默认FLOAT
     */
    public enum FooterStyle {
        FLOAT, BELOW;
    }

    public XSwipeRefreshLayoutLV(Context context) {
        super(context);
        this.mContext = context;
    }

    public XSwipeRefreshLayoutLV(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    // 拦截触摸事件，保存按下位置和离开位置
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                rawX = ev.getRawX();
                rawY = ev.getRawY();
                Log.d(TAG, "(rawX,rawY)->(" + rawX + "," + rawY + ")");
                break;
            case MotionEvent.ACTION_MOVE:
                rawX_UP = ev.getRawX();
                rawY_UP = ev.getRawY();
                if (isPullUp() && isLastItem()) {
                    Log.d(TAG, "已到底部");
                    addFooterView();
                }
                break;
            case MotionEvent.ACTION_UP:
                rawX_UP = ev.getRawX();
                rawY_UP = ev.getRawY();
                Log.d(TAG, "(rawX_UP,rawY_UP)->(" + rawX_UP + "," + rawY_UP + ")");
                if (canLoad()) {
                    mLoadListener.onLoad();
                } else {
                    removeFooterView();
                }
                cleanPosition();
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    public void setFooterStyle(FooterStyle style) {
        mFooterStyle = style;
    }

    /**
     * 是否上拉操作
     *
     * @return
     */
    private boolean isPullUp() {
        Log.d(TAG, "isPullUp:" + (rawY > rawY_UP));
        return rawY > rawY_UP;
    }

    /**
     * 判断滑动高度
     *
     * @return
     */
    private float getPullHeight() {
        Log.d(TAG, "PullUpHeight:" + Math.abs(rawY - rawY_UP));
        return Math.abs(rawY - rawY_UP);
    }

    /**
     * 判断是否到了listview 最后一项
     *
     * @return
     */
    private boolean isLastItem() {
        if (mListView == null) return false;
        Log.d(TAG, "isLastItem:" + (mListView.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1));
        return mListView.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1;
    }

    /**
     * 清楚保存的位置信息
     */
    private void cleanPosition() {
        rawX = 0;
        rawY = 0;
        rawX_UP = 0;
        rawY_UP = 0;
    }

    /**
     * 设置自定义的footer，代替默认的footer
     *
     * @param footerView
     */
    public void setFooterView(View footerView) {
        mFooterView = footerView;
    }

    /**
     * 添加footer到屏幕
     */
    private void addFooterView() {
        if (mFooterView == null) {
            mFooterView = getDefaultFooter();
        }
        if (!hasFooterView()) {
            if (mFooterStyle == null) {
                mFooterStyle = FooterStyle.FLOAT;
            }
            if (mFooterStyle == FooterStyle.FLOAT) {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                getDecorView().addView(mFooterView, params);
            } else {
                mListView.addFooterView(mFooterView);
            }
        }
    }

    /**
     * 获取屏幕root view
     *
     * @return
     */
    private FrameLayout getDecorView() {
        return (FrameLayout) ((Activity) mContext).getWindow().getDecorView();
    }

    /**
     * 判断是否已经添加footer
     *
     * @return
     */
    private boolean hasFooterView() {
        if (mFooterStyle == null || mFooterStyle == FooterStyle.FLOAT) {
            return getDecorView().findViewWithTag(FOOTER_VIEW_TAG) != null;
        } else {
            return mListView.getFooterViewsCount() > 0;
        }
    }

    /**
     * 默认footer
     *
     * @return
     */
    @NonNull
    private View getDefaultFooter() {
        ProgressBar defaultFooter = new ProgressBar(mContext);
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//        defaultFooter.setLayoutParams(params);
        defaultFooter.setTag(FOOTER_VIEW_TAG);
        return defaultFooter;
    }

    /**
     * 移除footer
     */
    public void removeFooterView() {
        if (mFooterView != null) {
            if (mFooterStyle == null) {
                mFooterStyle = FooterStyle.FLOAT;
            }
            if (mFooterStyle == FooterStyle.FLOAT) {
                getDecorView().removeView(mFooterView);
            } else {
                mListView.removeFooterView(mFooterView);
            }
        }
    }

        /**
         * 下拉加载的listener
         *
         * @param listener
         */

    public void setOnLoadListener(OnLoadListener listener) {
        mLoadListener = listener;
    }

    /**
     * 是否正在加载
     *
     * @return
     */
    public boolean isLoading() {
        return mLoading;
    }

    /**
     * 显示或隐藏加载
     *
     * @param loading true显示footer，false隐藏footer
     */
    public void setLoading(boolean loading) {
        mLoading = loading;
        if (loading) {
            showLoadingView();
        } else {
            hideLoadingView();
        }
    }

    private void showLoadingView() {
        addFooterView();
    }

    private void hideLoadingView() {
        removeFooterView();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mListView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof ListView) {
                    mListView = (ListView) child;
                    mListView.setOnScrollListener(this);
                    break;
                }
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_IDLE://静止状态
                if (canLoad()) {
//                    cleanPosition();
//                    Log.d(TAG, "上拉");
//                    if (mLoadListener != null) {
//                        mLoadListener.onLoad();
//                    }
                }
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING://滚动状态

                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸后滚动

                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    private boolean canLoad() {
        return isPullUp() && isLastItem() && getPullHeight() > MIN_HEIGHT;
    }

    public interface OnLoadListener {
        void onLoad();
    }
}
