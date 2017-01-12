package com.marc.marclibs.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.marc.marclibs.R;
import com.marc.marclibs.utils.glide.GlideRoundTransform;

/**
 * Description : 图片加载工具类
 * Date   : 15/12/21
 */
public class ImageLoaderUtils {

    public static void display(Context context, ImageView imageView, String url, int placeholder, int error) {
        if (imageView == null) {
            throw new IllegalArgumentException("argument error");
        }
        Glide.with(context).load(url).placeholder(placeholder)
                .error(error).crossFade().dontAnimate().into(imageView);
    }

    public static void displayRound(Context context,ImageView imageView,String url,int placeHolder){
        if (imageView == null) {
            throw new IllegalArgumentException("imageview是空的");
        }
        Glide.with(context).load(url).placeholder(placeHolder) .transform(new GlideRoundTransform(context)).into(imageView);
    }

    public static void display(Context context, ImageView imageView, String url) {
        if (imageView == null) {
            throw new IllegalArgumentException("argument error");
        }
        Glide.with(context).load(url).placeholder(R.drawable.c_head)
                .error(R.drawable.c_head).crossFade().dontAnimate().into(imageView);
    }

    public static void display(Context context, ImageView imageView, int resID) {
        if (imageView == null) {
            throw new IllegalArgumentException("argument error");
        }
        Glide.with(context).load(resID).placeholder(R.drawable.c_head)
                .error(R.drawable.c_head).crossFade().dontAnimate().into(imageView);
    }



}
