package com.github.luckyshane.social.wechat;

import com.github.luckyshane.social.IAuthorizeData;

/*
 * @author: luckyShane
 */
public class WechatAuthorizeData implements IAuthorizeData {
    public String code;
    public String state;
    public String url;
    public String lang;
    public String country;

}
