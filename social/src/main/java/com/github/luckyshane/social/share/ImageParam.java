package com.github.luckyshane.social.share;

import android.graphics.Bitmap;

/*
 * @author: luckyShane
 */
public class ImageParam {
    /**
     * 本地图片地址或者网络图片地址。<br>
     * 分享纯图片时都不支持网络地址。而分享网页时，微信不支持网络地址。
     */
    public String url;
    public Bitmap bitmap;
}
