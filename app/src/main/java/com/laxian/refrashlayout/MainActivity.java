package com.laxian.refrashlayout;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private RefreshLayout mRefreshLayout;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mRefreshLayout.setLoading(false);
                    break;
            }
        }
    };
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRefreshLayout = (RefreshLayout) findViewById(R.id.view_refrash);
        mListView = (ListView) findViewById(R.id.lv_test);

        final List<String> datas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            datas.add("laxian-" + i);
        }
        myAdapter = new MyAdapter(datas);
        mListView.setAdapter(myAdapter);

        mRefreshLayout.setFooterStyle(RefreshLayout.FooterStyle.FLOAT);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i < 10; i++) {
                            datas.add("pull_to_refrash_" + i);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                                mListView.smoothScrollByOffset(0);
                                mRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }.start();
            }
        });

        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
//                mRefreshLayout.setLoading(true);

                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        for (int i = 0; i < 2; i++) {
                            datas.add("laxian_added-" + i);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                                mListView.smoothScrollToPosition(myAdapter.getCount());
                                mHandler.sendEmptyMessage(0);
                            }
                        });
                    }
                }.start();

//                mRefrashLayout.setLoading(false);
            }
        });
    }

    class MyAdapter extends BaseAdapter {
        List<String> mDatas;

        public MyAdapter(List<String> datas) {
            mDatas = datas;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                holder = new Holder();
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_list, null);
                holder.textView = (TextView) convertView.findViewById(R.id.tv_item);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.textView.setText((CharSequence) getItem(position));

            return convertView;
        }

        class Holder {
            TextView textView;
        }
    }
}
