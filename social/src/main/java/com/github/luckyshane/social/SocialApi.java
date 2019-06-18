package com.github.luckyshane.social;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import com.github.luckyshane.social.qq.QQPlatform;
import com.github.luckyshane.social.wechat.WechatPlatform;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;


/*
 * @author: luckyShane
 */
public class SocialApi implements IPlatformConfigGetter {
    private static volatile SocialApi instance;
    private static PlatformConfig config;
    private static Context appContext;
    private IPlatform qqPlatform;
    private IPlatform wechatPlatform;
    private SparseArray<IPlatform> sharePlatforms = new SparseArray<>(2);


    private SocialApi() {
        checkInit();
        qqPlatform = new QQPlatform(this, appContext);
        wechatPlatform = new WechatPlatform(this, appContext);
        sharePlatforms.put(IPlatform.SHARE_QQ, qqPlatform);
        sharePlatforms.put(IPlatform.SHARE_QZONE, qqPlatform);
        sharePlatforms.put(IPlatform.SHARE_WECHAT, wechatPlatform);
        sharePlatforms.put(IPlatform.SHARE_WECHAT_TIMELINE, wechatPlatform);
        Util.clearCacheDir(appContext);
    }

    public static SocialApi getInstance() {
        if (instance == null) {
            synchronized (SocialApi.class) {
                if (instance == null) {
                    instance = new SocialApi();
                }
            }
        }
        return instance;
    }

    public static void init(@NonNull Context appContext, @NonNull PlatformConfig config) {
        SocialApi.config = config;
        SocialApi.appContext = appContext.getApplicationContext();
    }

    private static void checkInit() {
        if (SocialApi.config == null) {
            throw new IllegalStateException("must invoke init() method and pass correct config before you do any other operations");
        }
    }

    public IPlatform getPlatform(@IPlatform.PlatformType int platformType) {
        if (platformType == IPlatform.PLATFORM_QQ) {
            return qqPlatform;
        } else if (platformType == IPlatform.PLATFORM_WECHAT) {
            return wechatPlatform;
        }
        return null;
    }

    @MainThread
    public void login(Activity activity, @IPlatform.PlatformType int platformType, Callback<IAuthorizeData> callback) {
        IPlatform platform = getPlatform(platformType);
        if (platform == null) {
            throw new IllegalArgumentException("platformType invalid");
        }
        platform.authorize(activity, callback);
    }

    @MainThread
    public void share(Activity activity, @IPlatform.ShareType int sharePlatformType, @NonNull ShareObject shareObject, Callback callback) {
        IPlatform sharePlatform = sharePlatforms.get(sharePlatformType);
        if (sharePlatform == null) {
            throw new IllegalArgumentException("sharePlatformType invalid");
        }
        sharePlatform.share(activity, sharePlatformType, shareObject, callback);
    }

    @MainThread
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (qqPlatform != null) {
            qqPlatform.onActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean isAppInstalled(@IPlatform.PlatformType int platformType) {
        if (platformType == IPlatform.PLATFORM_QQ) {
            return qqPlatform.isAppInstalled();
        } else if (platformType == IPlatform.PLATFORM_WECHAT) {
            return wechatPlatform.isAppInstalled();
        }
        return false;
    }

    @Override
    public PlatformConfig getConfig() {
        return config;
    }





}
