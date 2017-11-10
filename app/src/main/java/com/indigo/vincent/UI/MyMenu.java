package com.indigo.vincent.UI;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.dk.view.folder.ResideMenu;
import com.dk.view.folder.ResideMenuItem;
import com.indigo.vincent.Activity.Gallery;
import com.indigo.vincent.Activity.MainActivity;
import com.indigo.vincent.Activity.R;

/**
 * Created by Miracle on 2017/11/8.
 */

public class MyMenu {
    public ResideMenu resideMenu;

    //设置菜单
    public MyMenu(Activity context) {
        // attach to current activity;
        resideMenu = new ResideMenu(context);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(context);
        // create menu items;
        String titles[] = {"首页", "图集", "退出"};
        int icon[] = {R.drawable.icon_home, R.drawable.icon_profile, R.drawable.icon_exit};

        for (int i = 0; i < titles.length; i++) {
            ResideMenuItem item = new ResideMenuItem(context, icon[i], titles[i]);
            item.setOnClickListener((View.OnClickListener) context);
            resideMenu.addMenuItem(item, ResideMenu.DIRECTION_RIGHT);
        }
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT);
    }

    //设置点击操作
    public void menuClick(Activity context, View v) {
        Intent it;
        if (v == resideMenu.getMenuItems(1).get(0)) {
            it = new Intent(context, MainActivity.class);
            context.startActivity(it);
        } else if (v == resideMenu.getMenuItems(1).get(1)) {
            it = new Intent(context, Gallery.class);
            context.startActivity(it);
        } else if (v == resideMenu.getMenuItems(1).get(2)) {
            System.exit(0);
        }
        resideMenu.closeMenu();
    }
}
