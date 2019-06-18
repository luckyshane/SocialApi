# SocialApi
util for third party share and authorize.  currently include qq and wechat platform

### 用法
-  在Application onCreate方法中调用SocialApi的init方法配置各个平台的APP ID
```
public class App extends Application {
  
  void onCreate() {
    SocialApi.init(this, new PlatformConfig.Builder().setWechat(BuildConfig.WECHAT_APP_ID, "")
                .setQQ(BuildConfig.QQ_APP_ID, "").build());
  }
 
}
```
- 配置WXEntryActivity
按照微信平台规范，在<package>/wxapi目录下创建WXEntryActivityl类，直接继承SocialApi中的WXCallbackActivity即可。然后在manifest中进行声明。

WXEntryActivity
```
@Keep
public class WXEntryActivity extends WXCallbackActivity {
}
```
manifest配置。
```
 <activity
            android:name=".wxapi.WXEntryActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:launchMode="singleTask"
            android:theme="@android:style/Theme.Translucent.NoTitleBar" />
```
针对QQ，还需要配置app module的buld.gradle文件：
```
android {
  defaultConfig {
   manifestPlaceholders = [
                QQ_APP_ID: QQ_APP_ID       // 填写QQ互联申请的app id
        ]
  }
}
```
- authorize调用
```
public class LoginActivity extends Activity {
    
   void onQQLoginBtnClick() {
        SocialApi.getInstance().login(this, IPlatform.PLATFORM_QQ, new Callback<IAuthorizeData>() {
            @Override
            public void onComplete(int platform, IAuthorizeData data) {
                QQAuthorizeData authorizeData = (QQAuthorizeData) data;
                // do your things
            }

            @Override
            public void onCancel(int platform) {

            }

            @Override
            public void onError(int platform, int code, int subCode, String msg) {

            }
        });
   }
   
   void onWechatBtnClick() {
      SocialApi.getInstance().login(this, IPlatform.PLATFORM_WECHAT, new Callback<IAuthorizeData>() {
              @Override
              public void onComplete(int platform, IAuthorizeData data) {
                  WechatAuthorizeData authorizeData = (WechatAuthorizeData) data;
                  // do your things
              }

              @Override
              public void onCancel(int platform) {

              }

              @Override
              public void onError(int platform, int code, int subCode, String msg) {

              }
          });
   }
   
  
   protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data)
        SocialApi.getInstance().onActivityResult(requestCode, resultCode, data)
   }


}


```

- 分享调用
```
  public void share(Activity activity, @IPlatform.ShareType int sharePlatformType, @NonNull ShareObject shareObject, Callback callback)
```





