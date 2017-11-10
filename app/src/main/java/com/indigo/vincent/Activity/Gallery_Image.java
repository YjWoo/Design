package com.indigo.vincent.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Miracle on 2017/11/9.
 */

public class Gallery_Image extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_image);

        PhotoView image = findViewById(R.id.grid_image);
        String path = getIntent().getStringExtra("path");

        PhotoViewAttacher mAttacher=new PhotoViewAttacher(image);
        image.setImageURI(Uri.parse(path));
        mAttacher.update();
    }
}
