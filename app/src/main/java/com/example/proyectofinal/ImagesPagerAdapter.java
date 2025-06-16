// ImagesPagerAdapter.java
package com.example.proyectofinal;

import android.content.Context;
import android.view.*;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;

import java.util.List;

public class ImagesPagerAdapter extends PagerAdapter {
    private final Context ctx;
    private final List<String> urls;

    public ImagesPagerAdapter(Context c, List<String> u) {
        ctx = c; urls = u;
    }

    @Override public int getCount() { return urls.size(); }

    @Override public boolean isViewFromObject(@NonNull View v, @NonNull Object o) {
        return v == o;
    }

    @NonNull @Override
    public Object instantiateItem(@NonNull ViewGroup container, int pos) {
        ImageView iv = new ImageView(ctx);
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(ctx).load(urls.get(pos)).into(iv);
        container.addView(iv);
        return iv;
    }

    @Override public void destroyItem(@NonNull ViewGroup c, int pos, @NonNull Object obj) {
        c.removeView((View)obj);
    }
}
