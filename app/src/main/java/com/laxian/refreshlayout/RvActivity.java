package com.laxian.refreshlayout;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.laxian.RefreshLayout.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RvActivity extends AppCompatActivity {
    private List<String> mList;
    private int mCount;
    private StringAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);

        final XSwipeRefreshLayout XSwipeRefreshLayout = (XSwipeRefreshLayout) findViewById(R.id.srl);
        RecyclerView listView = (RecyclerView) findViewById(R.id.lv);

        // 设置适配器数据
        mList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            mList.add("position" + i);
            mCount++;
        }
        mAdapter = new StringAdapter();
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(mAdapter);


        // 不能在onCreate中设置，这个表示当前是刷新状态，如果一进来就是刷新状态，SwipeRefreshLayout会屏蔽掉下拉事件
        //swipeRefreshLayout.setRefreshing(true);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        XSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        XSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        // 下拉时触发SwipeRefreshLayout的下拉动画，动画完毕之后就会回调这个方法
        XSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // 开始刷新，设置当前为刷新状态
                //swipeRefreshLayout.setRefreshing(true);

                // 这里是主线程
                // 一些比较耗时的操作，比如联网获取数据，需要放到子线程去执行
                // TODO 获取数据
                final Random random = new Random();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mList.add(0, "我是天才" + random.nextInt(100) + "号");
                        mAdapter.notifyDataSetChanged();

                        Toast.makeText(RvActivity.this, "刷新了一条数据", Toast.LENGTH_SHORT).show();

                        // 加载完数据设置为不刷新状态，将下拉进度收起来
                        XSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1200);

                // System.out.println(Thread.currentThread().getName());

                // 这个不能写在外边，不然会直接收起来
                //swipeRefreshLayout.setRefreshing(false);
            }
        });


        // 设置下拉加载更多
        XSwipeRefreshLayout.setOnLoadListener(new XSwipeRefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // 添加数据
                        for (int i = 30; i < 35; i++) {
                            mList.add("我是天才" + i + "号");
                            // 这里要放在里面刷新，放在外面会导致刷新的进度条卡住
                            mAdapter.notifyDataSetChanged();
                        }

                        Toast.makeText(RvActivity.this, "加载了" + 5 + "条数据", Toast.LENGTH_SHORT).show();

                        // 加载完数据设置为不加载状态，将加载进度收起来
                        XSwipeRefreshLayout.setLoading(false);
                    }
                }, 1200);
            }
        });


    }


    /**
     * 适配器
     */
    private class StringAdapter extends RecyclerView.Adapter<StringAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(View.inflate(RvActivity.this, android.R.layout.simple_list_item_1, null));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {

            holder.textView.setText(mList.get(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            public TextView textView;

            public VH(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
