package com.github.luckyshane.social.wechat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.luckyshane.social.Callback;
import com.github.luckyshane.social.ErrorCode;
import com.github.luckyshane.social.IAuthorizeData;
import com.github.luckyshane.social.IPlatform;
import com.github.luckyshane.social.IPlatformConfigGetter;
import com.github.luckyshane.social.ShareObject;
import com.github.luckyshane.social.Util;
import com.github.luckyshane.social.exception.SocialException;
import com.github.luckyshane.social.share.ImageParam;
import com.github.luckyshane.social.share.ShareImageObject;
import com.github.luckyshane.social.share.ShareTextObject;
import com.github.luckyshane.social.share.ShareWebObject;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @author: luckyShane
 */
public class WechatPlatform implements IPlatform, IWXAPIEventHandler {
    private static final String TAG = "WechatPlatform";
    private IPlatformConfigGetter configGetter;
    private IWXAPI wxapi;
    private WeakReference<Callback<IAuthorizeData>> loginCallback;
    private Context appContext;
    private String lastTransaction = "";
    private Map<String, WeakReference<Callback>> callbackMap = new HashMap<>();

    public WechatPlatform(IPlatformConfigGetter configGetter, @NonNull Context context) {
        this.configGetter = configGetter;
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void authorize(@NonNull Activity activity, @NonNull Callback<IAuthorizeData> callback) {
        wxapi = getWxapi(activity);
        loginCallback = new WeakReference<>(callback);

        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "authorize";
        req.transaction = buildTransaction("authorize");
        wxapi.sendReq(req);
    }

    @Override
    public void share(@NonNull Activity activity, @ShareType int shareType, @NonNull ShareObject shareObject, @Nullable Callback callback) {
        wxapi = getWxapi(activity);
        try {
            SendMessageToWX.Req req = makeReq(shareObject, shareType);
            if (req != null) {
                String tag = buildTransaction("share");
                req.transaction = tag;
                boolean ok = wxapi.sendReq(req);
                if (ok) {
                    if (callback != null) {
                        WeakReference<Callback> callbackWeakReference = new WeakReference<>(callback);
                        callbackMap.put(tag, callbackWeakReference);
                    }
                } else {
                    if (callback != null) {
                        callback.onError(getPlatform(), ErrorCode.ERR_UNKNOWN, 0, "send api error");
                    }
                }
            } else {
                if (callback != null) {
                    callback.onError(getPlatform(), ErrorCode.ERR_NOT_SUPPORT, 0, "not support for this");
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
        // nothing to do
    }

    @Override
    public boolean isAppInstalled() {
        return getWxapi(appContext).isWXAppInstalled();
    }

    private String getAppId() {
        String appId = configGetter.getConfig().getWechatAppId();
        if (TextUtils.isEmpty(appId)) {
            throw new IllegalStateException("wechat app id not config");
        }
        return appId;
    }


    private IWXAPI onCreate(Context context) {
        IWXAPI iwxapi = WXAPIFactory.createWXAPI(context, getAppId());
        iwxapi.registerApp(getAppId());
        return iwxapi;
    }

    public IWXAPI getWxapi(Context context) {
        // TODO 测试是否符合要求
        if (wxapi == null) {
            return onCreate(context.getApplicationContext());
        }
        return wxapi;
    }

    public IWXAPIEventHandler getEventHandler() {
        return this;
    }


    @Override
    public void onReq(BaseReq baseReq) {


    }

    private SendMessageToWX.Req makeReq(ShareObject shareObject, int shareType) {
        int scene;
        if (shareType == IPlatform.SHARE_WECHAT) {
            scene = SendMessageToWX.Req.WXSceneSession;
        } else if (shareType == IPlatform.SHARE_WECHAT_TIMELINE) {
            scene = SendMessageToWX.Req.WXSceneTimeline;
        } else {
            throw new IllegalArgumentException("shareType wrong");
        }
        if (shareObject instanceof ShareWebObject) {
            ShareWebObject shareWebObject = (ShareWebObject) shareObject;
            WXWebpageObject webPageObject = new WXWebpageObject();
            webPageObject.webpageUrl = shareWebObject.getUrl();
            WXMediaMessage msg = new WXMediaMessage(webPageObject);
            msg.title = shareWebObject.getTitle();
            msg.description = shareWebObject.getDesc();
            List<ImageParam> imageParamList = shareWebObject.getImageList();
            if (imageParamList != null && imageParamList.size() > 0) {
                ImageParam imageParam = imageParamList.get(0);
                if (!TextUtils.isEmpty(imageParam.url) && !Util.isWebUrl(imageParam.url)) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeFile(imageParam.url);
                        Bitmap thumbBitmap = Util.createThumbBitmap(bitmap);
                        bitmap.recycle();
                        msg.thumbData = Util.bitmapToByteArray(thumbBitmap, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (imageParam.bitmap != null && msg.thumbData == null) {
                    Bitmap thumbBitmap = Util.createThumbBitmap(imageParam.bitmap);
                    imageParam.bitmap.recycle();
                    msg.thumbData = Util.bitmapToByteArray(thumbBitmap, true);
                }
            }
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.message = msg;
            req.scene = scene;
            return req;
        } else if (shareObject instanceof ShareTextObject) {
            ShareTextObject shareTextObject = (ShareTextObject) shareObject;
            WXTextObject wxTextObject = new WXTextObject();
            wxTextObject.text = shareTextObject.text;

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = wxTextObject;
            msg.description = shareTextObject.text;
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.message = msg;
            req.scene = scene;
            return req;
        } else if (shareObject instanceof ShareImageObject) {
            ShareImageObject shareImageObject = (ShareImageObject) shareObject;
            WXImageObject imageObject;
            Bitmap bitmap = shareImageObject.bitmap;
            if (bitmap == null && !TextUtils.isEmpty(shareImageObject.url)) {
                File imageFile = new File(shareImageObject.url);
                if (imageFile.exists()) {
                    bitmap = BitmapFactory.decodeFile(shareImageObject.url);
                } else {
                    Log.e(TAG, "image file: " + shareImageObject.url + " not exists!");
                }
            }
            if (bitmap != null) {
                imageObject = new WXImageObject(bitmap);
                Bitmap thumb = Util.createThumbBitmap(bitmap);
                WXMediaMessage msg = new WXMediaMessage(imageObject);
                msg.thumbData = Util.bitmapToByteArray(thumb, true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.message = msg;
                req.scene = scene;
                return req;
            } else {
                throw new SocialException(SocialException.CODE_INVALID_PARMAS, "image not exists or decode failed!");
            }
        }

        return null;
    }


    @Override
    public void onResp(BaseResp baseResp) {
        if (!lastTransaction.equals(baseResp.transaction)) {
            Log.e(TAG, "onResp wrong transaction resp");
            callbackMap.remove(baseResp.transaction);
            return;
        }

        if (baseResp instanceof SendAuth.Resp) {
            SendAuth.Resp resp = (SendAuth.Resp) baseResp;
            onAuthCallback(resp);
        } else if (baseResp instanceof SendMessageToWX.Resp) {
            SendMessageToWX.Resp resp = (SendMessageToWX.Resp) baseResp;
            WeakReference<Callback> callbackRef = callbackMap.remove(resp.transaction);
            if (callbackRef != null) {
                Callback callback = callbackRef.get();
                if (callback != null) {
                    switch (resp.errCode) {
                        case BaseResp.ErrCode.ERR_OK:
                            callback.onComplete(getPlatform(), null);
                            break;
                        case BaseResp.ErrCode.ERR_USER_CANCEL:
                            callback.onCancel(getPlatform());
                            break;
                        case BaseResp.ErrCode.ERR_AUTH_DENIED:
                            callback.onError(getPlatform(), ErrorCode.ERR_AUTH_DENY, resp.errCode, resp.errStr);
                            break;
                        default:
                            callback.onError(getPlatform(), ErrorCode.ERR_PLATFORM, resp.errCode, resp.errStr);
                    }
                }
            }
        }
    }

    private void onAuthCallback(SendAuth.Resp resp) {
        Callback<IAuthorizeData> callback;
        if (loginCallback == null || (callback = loginCallback.get()) == null) {
            return;
        }
        loginCallback = null;
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                WechatAuthorizeData data = new WechatAuthorizeData();
                data.code = resp.code;
                data.country = resp.country;
                data.url = resp.url;
                data.lang = resp.lang;
                data.state = resp.state;
                callback.onComplete(getPlatform(), data);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                callback.onCancel(getPlatform());
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                callback.onError(getPlatform(), ErrorCode.ERR_AUTH_DENY, resp.errCode, resp.errStr);
                break;
            default:
                callback.onError(getPlatform(), ErrorCode.ERR_PLATFORM, resp.errCode, resp.errStr);
        }
    }

    private String buildTransaction(String type) {
        String transaction = type == null ? String.valueOf(System.currentTimeMillis()) : (type + System.currentTimeMillis());
        lastTransaction = transaction;
        return transaction;
    }

    private static int getPlatform() {
        return IPlatform.PLATFORM_WECHAT;
    }


}
