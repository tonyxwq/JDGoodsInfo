package com.rx.jdgoodsinfo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView footer = findViewById(R.id.footer);
        RecyclerView header = findViewById(R.id.header);
        footer.setLayoutManager(new LinearLayoutManager(this));
        header.setLayoutManager(new LinearLayoutManager(this));
        List list = new ArrayList();
        for (int i = 0; i < 30; i++)
        {
            list.add("我是商品" + i);
        }
        List list2 = new ArrayList();
        for (int i = 0; i < 30; i++)
        {
            list2.add("我是头部" + i);
        }

        QuickAdapter<String> baseAdapter = new QuickAdapter<String>(list2)
        {
            @Override
            public int getLayoutId(int viewType)
            {
                return R.layout.item;
            }

            @Override
            public void convert(VH holder, String data, int position)
            {
                holder.setText(R.id.tv_name,data);
            }
        };

        QuickAdapter<String> baseAdapterbottom = new QuickAdapter<String>(list)
        {
            @Override
            public int getLayoutId(int viewType)
            {
                return R.layout.itembottom;
            }

            @Override
            public void convert(VH holder, String data, int position)
            {
                holder.setText(R.id.tv_name,data);
            }
        };

        header.setAdapter(baseAdapter);
        footer.setAdapter(baseAdapterbottom);
    }
}
