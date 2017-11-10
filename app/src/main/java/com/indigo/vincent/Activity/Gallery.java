package com.indigo.vincent.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.indigo.vincent.UI.MyMenu;
import com.indigo.vincent.Util.GetFilename;

import java.io.File;
import java.util.List;

/**
 * Created by Miracle on 2017/11/9.
 */

public class Gallery extends AppCompatActivity implements View.OnClickListener {

    GridView mGrid;
    List<String> paths;
    MyMenu myMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        myMenu = new MyMenu(this);
        mGrid = (GridView) findViewById(R.id.grid);
        paths = new GetFilename("SimpleWord").getImagePathFromSD();
        MyGridViewAdapter adapter = new MyGridViewAdapter(this);
        mGrid.setAdapter(adapter);

        //监听GridView下的item
        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = paths.get(position);
                Intent it = new Intent(Gallery.this, Gallery_Image.class);
                it.putExtra("path", path);
//                ToastDebug.showToast(Gallery.this, "图片位置：" + path);
                startActivity(it);
            }
        });
    }

    @Override
    public void onClick(View v) {
        myMenu.menuClick(this, v);
    }

    class MyGridViewAdapter extends BaseAdapter {
        private Context context;

        public MyGridViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return paths.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SquaredImageView view = (SquaredImageView) convertView;
            if (view == null) {
                view = new SquaredImageView(context);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            //当前item要加载的图片路径
            String path = paths.get(position);
            Glide.with(context).load(new File(path)).into(view);
            return view;
        }
    }

    @SuppressLint("AppCompatCustomView")
    final class SquaredImageView extends ImageView {
        public SquaredImageView(Context context) {
            super(context);
        }

        public SquaredImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
        }
    }
}