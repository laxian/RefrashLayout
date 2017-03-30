package com.laxian.refreshlayout;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;


import com.laxian.RefreshLayout.R;

import java.util.ArrayList;

/**
 * 自定义View继承SwipeRefreshLayout，添加上拉加载更多的功能
 * 参考了https://github.com/PingerOne/SwipeRefreshDemo
 * 和https://github.com/huxq17/XRefreshView
 * Created by zwx on 2017/03/30
 */

public class XSwipeRefreshLayout extends SwipeRefreshLayout {

    private final int mScaledTouchSlop;
    private final View mFooterView;

    private ArrayList<View> mHeaderViews = new ArrayList<>();

    private ArrayList<View> mFootViews = new ArrayList<>();
    private RecyclerView mListView;
    private OnLoadListener mOnLoadListener;

    /**
     * 正在加载状态
     */
    private boolean isLoading;

    public XSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 填充底部加载布局
        mFooterView = View.inflate(context, R.layout.view_footer, null);
        mFootViews.add(mFooterView);

        // 表示控件移动的最小距离，手移动的距离大于这个距离才能拖动控件
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        Log.d("abc", "====" + mScaledTouchSlop);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 获取ListView,设置ListView的布局位置
        if (mListView == null) {
            // 判断容器有多少个孩子
            if (getChildCount() > 0) {
                // 判断第一个孩子是不是ListView
                if (getChildAt(0) instanceof RecyclerView) {
                    // 创建ListView对象
                    mListView = (RecyclerView) getChildAt(0);
                    mListView.setAdapter(new WrapAdapter(mHeaderViews, mFootViews,mListView.getAdapter()));

                    // 设置ListView的滑动监听
                    setListViewOnScroll();
                }
            }
        }
    }

    /**
     * 在分发事件的时候处理子控件的触摸事件
     *
     * @param ev
     * @return
     */
    private float mDownY, mUpY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 移动的起点
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 移动过程中判断时候能下拉加载更多
                if (canLoadMore()) {
                    // 加载数据
                    loadData();
                }

                break;
            case MotionEvent.ACTION_UP:
                // 移动的终点
                mUpY = getY();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断是否满足加载更多条件
     *
     * @return
     */
    private boolean canLoadMore() {
        // 1. 是上拉状态
        boolean condition1 = (mDownY - mUpY) >= mScaledTouchSlop;
        if (condition1) {
            Log.d("abc", "是上拉状态");
        }

        // 2. 当前页面可见的item是最后一个条目
        boolean condition2 = false;
        if (mListView != null && mListView.getAdapter() != null) {
            condition2 = ((LinearLayoutManager)mListView.getLayoutManager())
                    .findLastVisibleItemPosition() == (mListView.getAdapter().getItemCount() - 1);
        }

        if (condition2) {
            Log.d("abc", "是最后一个条目");
        }
        // 3. 正在加载状态
        boolean condition3 = !isLoading;
        if (condition3) {
            Log.d("abc", "不是正在加载状态");
        }
        return condition1 && condition2 && condition3;
    }

    /**
     * 处理加载数据的逻辑
     */
    private void loadData() {
        Log.d("abc", "加载数据...");
        if (mOnLoadListener != null) {
            // 设置加载状态，让布局显示出来
            setLoading(true);
            mOnLoadListener.onLoad();
        }

    }

    /**
     * 设置加载状态，是否加载传入boolean值进行判断
     *
     * @param loading
     */
    public void setLoading(boolean loading) {
        // 修改当前的状态
        isLoading = loading;
        if (isLoading) {
            // 显示布局
//            mListView.addFooterView(mFooterView);
            showFooter();
        } else {
            // 隐藏布局
//            mListView.removeFooterView(mFooterView);
            hideFooter();
            // 重置滑动的坐标
            mDownY = 0;
            mUpY = 0;
        }
    }

    private void hideFooter() {
        isLoading = false;
        View footView = mFootViews.get(0);
        footView.setVisibility(View.GONE);
    }

    private void showFooter() {
        isLoading = true;
        View footView = mFootViews.get(0);
        footView.setVisibility(View.VISIBLE);
    }


    /**
     * 设置ListView的滑动监听
     */
    private void setListViewOnScroll() {

        mListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
                // 移动过程中判断时候能下拉加载更多
                if (canLoadMore()) {
                    // 加载数据
                    loadData();
                }
            }
        });
    }


    /**
     * 上拉加载的接口回调
     */

    public interface OnLoadListener {
        void onLoad();
    }

    public void setOnLoadListener(OnLoadListener listener) {
        this.mOnLoadListener = listener;
    }


    private class WrapAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private RecyclerView.Adapter adapter;

        private ArrayList<View> mHeaderViews;

        private ArrayList<View> mFootViews;

        private int headerPosition = 1;

        public WrapAdapter(ArrayList<View> headerViews, ArrayList<View> footViews, RecyclerView.Adapter adapter) {
            this.adapter = adapter;
            this.mHeaderViews = headerViews;
            this.mFootViews = footViews;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if(manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isHeader(position)||  isFooter(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            LayoutParams lp = holder.itemView.getLayoutParams();
            if(lp != null
                    && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    &&  (isHeader( holder.getLayoutPosition()) || isFooter( holder.getLayoutPosition())) ) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }

        public boolean isHeader(int position) {
            return position >= 0 && position < mHeaderViews.size();
        }

        public boolean isFooter(int position) {
            return position < getItemCount() && position >= getItemCount() - mFootViews.size();
        }

        public boolean isRefreshHeader(int position) {
            return position < getHeadersCount() ;
        }

        public int getHeadersCount() {
            return mHeaderViews == null ? 0 : mHeaderViews.size();
        }

        public int getFootersCount() {
            return mFootViews == null ? 0 : mFootViews.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER) {
                return new SimpleViewHolder(mHeaderViews.get(0));
            } else if (viewType == TYPE_HEADER) {
                return new SimpleViewHolder(mHeaderViews.get(headerPosition++ ));
            } else if (viewType == TYPE_FOOTER) {
                return new SimpleViewHolder(mFootViews.get(0));
            }
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (isHeader(position)) {
                return;
            }
            int adjPosition = position - getHeadersCount();
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    adapter.onBindViewHolder(holder, adjPosition);
                    return;
                }
            }
        }

        @Override
        public int getItemCount() {
            if (adapter != null) {
                return getHeadersCount() + getFootersCount() + adapter.getItemCount();
            } else {
                return getHeadersCount() + getFootersCount();
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(isRefreshHeader(position)){
                return TYPE_REFRESH_HEADER;
            }
            if (isHeader(position)) {
                return TYPE_HEADER;
            }
            if(isFooter(position)){
                return TYPE_FOOTER;
            }
            int adjPosition = position - getHeadersCount();;
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    return adapter.getItemViewType(adjPosition);
                }
            }
            return TYPE_NORMAL;
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null && position >= getHeadersCount()) {
                int adjPosition = position - getHeadersCount();
                int adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    return adapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        @Override
        public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
            if (adapter != null) {
                adapter.unregisterAdapterDataObserver(observer);
            }
        }

        @Override
        public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
            if (adapter != null) {
                adapter.registerAdapterDataObserver(observer);
            }
        }

        private class SimpleViewHolder extends RecyclerView.ViewHolder {
            public SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
    private static final int TYPE_REFRESH_HEADER =  -5;
    private static final int TYPE_HEADER =  -4;
    private static final int TYPE_NORMAL =  0;
    private static final int TYPE_FOOTER =  -3;
}