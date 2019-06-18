package com.github.luckyshane.social;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.luckyshane.social.wechat.WechatPlatform;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

/*
 * @author: luckyShane
 */
public class WXCallbackActivity extends AppCompatActivity implements IWXAPIEventHandler {
    private String TAG = getClass().getSimpleName();

    private IWXAPI iwxapi;
    private WechatPlatform wechatPlatform;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        wechatPlatform = (WechatPlatform) SocialApi.getInstance().getPlatform(IPlatform.PLATFORM_WECHAT);
        iwxapi = wechatPlatform.getWxapi(this);
        iwxapi.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d(TAG, "onReq");
        try {
            if (wechatPlatform.getEventHandler() != null) {
                wechatPlatform.getEventHandler().onReq(baseReq);
            }
        } catch (Exception e) {
        }
        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d(TAG, "onResp");
        try {
            if (wechatPlatform.getEventHandler() != null) {
                wechatPlatform.getEventHandler().onResp(baseResp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        setIntent(intent);
        iwxapi.handleIntent(intent, this);
    }


}
