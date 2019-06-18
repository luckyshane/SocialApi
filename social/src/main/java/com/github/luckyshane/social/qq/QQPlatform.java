package com.github.luckyshane.social.qq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.github.luckyshane.social.exception.SocialException;
import com.github.luckyshane.social.share.ShareImageObject;
import com.github.luckyshane.social.share.ShareWebObject;
import com.github.luckyshane.social.Callback;
import com.github.luckyshane.social.ErrorCode;
import com.github.luckyshane.social.IAuthorizeData;
import com.github.luckyshane.social.IPlatform;
import com.github.luckyshane.social.IPlatformConfigGetter;
import com.github.luckyshane.social.ShareObject;
import com.github.luckyshane.social.Util;
import com.github.luckyshane.social.share.ImageParam;
import com.google.gson.Gson;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/*
 * @author: luckyShane
 */
public class QQPlatform implements IPlatform {
    private static final String TAG = "QQPlatform";
    private static final String PACKAGE_NAME = "com.tencent.mobileqq";
    private IPlatformConfigGetter configGetter;
    private Tencent tencent;
    private Context context;

    public QQPlatform(@NonNull IPlatformConfigGetter configGetter, Context context) {
        this.configGetter = configGetter;
        this.context = context;
    }

    private static int getPlatform() {
        return PLATFORM_QQ;
    }

    @Override
    public void authorize(@NonNull Activity activity, @NonNull final Callback<IAuthorizeData> callback) {
        getTencent(activity).login(activity, "all", new IUiListener() {
            @Override
            public void onComplete(Object o) {
                try {
                    QQAuthorizeData data = new Gson().fromJson(o.toString(), QQAuthorizeData.class);
                    callback.onComplete(getPlatform(), data);
                } catch (Exception e) {
                    callback.onError(getPlatform(), ErrorCode.ERR_RESOLVE, 0, "返回数据解析失败");
                }
            }

            @Override
            public void onError(UiError uiError) {
                callback.onError(getPlatform(), ErrorCode.ERR_PLATFORM, uiError.errorCode, uiError.errorMessage);
            }

            @Override
            public void onCancel() {
                callback.onCancel(PLATFORM_QQ);
            }
        });
    }

    @Override
    public void share(@NonNull Activity activity, @ShareType int shareType, @NonNull ShareObject shareObject, @Nullable final Callback callback) {
        ParamsMaker paramsMaker;
        if (shareType == SHARE_QQ) {
            paramsMaker = new QQParamsMaker();
        } else if (shareType == SHARE_QZONE) {
            paramsMaker = new QzoneParamsMaker();
        } else {
            throw new IllegalArgumentException("shareType invalid for QQ platform");
        }
        try {
            Bundle params = paramsMaker.makeParams(shareObject);
            if (params != null) {
                if (shareType == SHARE_QQ) {
                    getTencent(activity).shareToQQ(activity, params, new ShareListener(callback));
                } else {
                    getTencent(activity).shareToQzone(activity, params, new ShareListener(callback));
                }
            } else {
                if (callback != null) {
                    callback.onError(getPlatform(), ErrorCode.ERR_NOT_SUPPORT, 0, "not support for this share object");
                }
            }
        } catch (SocialException e) {
            Log.e(TAG, "share", e);
            if (callback != null) {
                int errorCode = ErrorCode.ERR_UNKNOWN;
                if (e.getCode() == SocialException.CODE_INVALID_PARMAS) {
                    errorCode = ErrorCode.ERR_INVALID_PARMAS;
                } else if (e.getCode() == SocialException.CODE_NOT_SUPPORT) {
                    errorCode = ErrorCode.ERR_NOT_SUPPORT;
                }
                callback.onError(getPlatform(), errorCode, 0, e.getMessage());
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, null);
    }

    @Override
    public boolean isAppInstalled() {
        return Util.isAppInstalled(context, PACKAGE_NAME);
    }

    private Tencent getTencent(Context context) {
        if (tencent == null) {
            tencent = Tencent.createInstance(getAppId(), context);
        }
        return tencent;
    }

    private String getAppId() {
        String appId = configGetter.getConfig().getQQAppId();
        if (TextUtils.isEmpty(appId)) {
            throw new IllegalStateException("QQ app id not config");
        }
        return appId;
    }

    interface ParamsMaker {
        Bundle makeParams(ShareObject shareObject) throws SocialException;
    }

    private class QQParamsMaker implements ParamsMaker {

        @Override
        public Bundle makeParams(ShareObject shareObject) {
            Bundle params = null;
            if (shareObject instanceof ShareWebObject) {
                params = new Bundle();
                ShareWebObject shareWebObject = (ShareWebObject) shareObject;
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                params.putString(QQShare.SHARE_TO_QQ_TITLE, shareWebObject.getTitle());
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareWebObject.getDesc());
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareWebObject.getUrl());
                List<ImageParam> imageParamList = shareWebObject.getImageList();
                if (imageParamList != null && imageParamList.size() > 0) {
                    ImageParam imageParam = imageParamList.get(0);
                    if (imageParam != null) {
                        if (Util.isWebUrl(imageParam.url)) {
                            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageParam.url);
                        } else if (!TextUtils.isEmpty(imageParam.url)) {
                            // TODO: 判断是否为内部文件，如果是，则复制到外部目录后执行分享。
                            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageParam.url);
                        } else if (imageParam.bitmap != null) {
                            File cacheDir = Util.getCacheDir(context);
                            if (cacheDir != null) {
                                String path = cacheDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".png";
                                File imageFile = Util.saveBitmapFile(imageParam.bitmap, path);
                                if (imageFile != null && imageFile.exists()) {
                                    Log.d(TAG, "share qq image path: " + path);
                                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path);
                                }
                            }
                        }
                    }
                }
            } else if (shareObject instanceof ShareImageObject) {
                ShareImageObject shareImageObject = (ShareImageObject) shareObject;
                params = new Bundle();
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, getImageUrl(context, shareImageObject));
            } else {
                throw new SocialException(SocialException.CODE_NOT_SUPPORT, "QQ not support for this share object type");
            }
            return params;
        }
    }

    private class QzoneParamsMaker implements ParamsMaker {

        @Override
        public Bundle makeParams(ShareObject shareObject) {
            Bundle params = null;
            if (shareObject instanceof ShareWebObject) {
                ShareWebObject shareWebObject = (ShareWebObject) shareObject;
                params = new Bundle();
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                params.putString(QzoneShare.SHARE_TO_QQ_TITLE, shareWebObject.getTitle());
                params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, shareWebObject.getDesc());
                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, shareWebObject.getUrl());
                List<ImageParam> imageList = shareWebObject.getImageList();
                ArrayList<String> imageShareList = new ArrayList<>();
                for (ImageParam imageParam : imageList) {
                    String imageUrl = getImageUrl(context, imageParam);
                    if (imageUrl != null) {
                        imageShareList.add(imageUrl);
                    }
                }
                if (!imageShareList.isEmpty()) {
                    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageShareList);
                }
            }

            return params;
        }
    }

    private static class ShareListener implements IUiListener {
        Callback callback;

        ShareListener(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void onComplete(Object o) {
            if (callback != null) {
                callback.onComplete(getPlatform(), o);
            }
        }

        @Override
        public void onError(UiError uiError) {
            if (callback != null) {
                callback.onError(getPlatform(), ErrorCode.ERR_PLATFORM, uiError.errorCode, uiError.errorMessage);
            }
        }

        @Override
        public void onCancel() {
            if (callback != null) {
                callback.onCancel(getPlatform());
            }
        }
    }

    private static String getImageUrl(Context context, ImageParam param) {
        if (param != null) {
            if (param.url != null) {
                return param.url;
            } else if (param.bitmap != null) {
                File cacheDir = Util.getCacheDir(context);
                if (cacheDir != null) {
                    String path = cacheDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".png";
                    File imageFile = Util.saveBitmapFile(param.bitmap, path);
                    if (imageFile != null && imageFile.exists()) {
                        return path;
                    }
                }
            }
        }
        return null;
    }




}
