package com.startek.fm220;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private Integer[] imgThumbIds;
    // 建構子
    public GridAdapter(Context c, Integer[] thumbIds) {
        context = c;
        imgThumbIds = thumbIds;
    }
    // 傳回圖片數
    @Override
    public int getCount() {
        return imgThumbIds.length;
    }
    // 傳回圖片物件
    @Override
    public Object getItem(int position) {
        return null;
    }
    // 傳回是哪一張圖片
    @Override
    public long getItemId(int position) {
        return position;
    }
    // 傳回ImageView物件
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // 是否需初始ImageView元件
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(220, 180));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(imgThumbIds[position]);
        return imageView;
    }
}
