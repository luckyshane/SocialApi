package com.github.luckyshane.social.qq;

import com.github.luckyshane.social.IAuthorizeData;
import com.google.gson.annotations.SerializedName;

/*
 * @author: luckyShane
 */
public class QQAuthorizeData implements IAuthorizeData {
    @SerializedName("openid")
    public String openId;
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("pay_token")
    public String payToken;
    @SerializedName("expires_in")
    public long expiresIn;
}
