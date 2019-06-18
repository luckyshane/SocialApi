package com.github.luckyshane.social;

import android.app.Activity;
import android.content.Intent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/*
 * @author: luckyShane
 */
public interface IPlatform {
    int PLATFORM_QQ = 0;
    int PLATFORM_WECHAT = 1;

    int SHARE_QQ = 0;
    int SHARE_QZONE = 1;
    int SHARE_WECHAT = 2;
    /**
     * 朋友圈
     */
    int SHARE_WECHAT_TIMELINE = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PLATFORM_QQ, PLATFORM_WECHAT})
    @interface PlatformType {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SHARE_QQ, SHARE_QZONE, SHARE_WECHAT, SHARE_WECHAT_TIMELINE})
    @interface ShareType {
    }

    @MainThread
    void authorize(@NonNull Activity activity, @NonNull Callback<IAuthorizeData> callback);

    @MainThread
    void share(@NonNull Activity activity, @ShareType int shareType, @NonNull ShareObject shareObject, @Nullable Callback callback);

    @MainThread
    void onActivityResult(int requestCode, int resultCode, Intent data);

    boolean isAppInstalled();

}
