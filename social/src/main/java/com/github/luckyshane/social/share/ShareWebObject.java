package com.github.luckyshane.social.share;

import android.graphics.Bitmap;

import com.github.luckyshane.social.ShareObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

/*
 * @author: luckyShane
 */
public class ShareWebObject implements ShareObject {
    /**
     * 要跳转的网页地址
     */
    private String url;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String desc;

    /**
     * 缩略图或者QZone上传的图片列表
     */
    private List<ImageParam> imageList;

    public static class Builder {
        private String title;
        private String desc;
        private String url;
        private List<ImageParam> params = new ArrayList<>();

        public Builder(@NonNull String url) {
            this.url = url;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder addImage(@NonNull String imageUrl) {
            ImageParam param = new ImageParam();
            param.url = imageUrl;
            params.add(param);
            return this;
        }

        public Builder addImage(@NonNull Bitmap bitmap) {
            ImageParam param = new ImageParam();
            param.bitmap = bitmap;
            params.add(param);
            return this;
        }

        public ShareWebObject build() {
            ShareWebObject object = new ShareWebObject();
            object.title = title;
            object.desc = desc;
            object.url = url;
            object.imageList = Collections.unmodifiableList(params);
            return object;
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getUrl() {
        return url;
    }

    public List<ImageParam> getImageList() {
        return imageList;
    }
}
