package com.github.luckyshane.social;

/*
 * @author: luckyShane
 */
public class ErrorCode {
    public static final int ERR_OK = 0;
    /**
     * 系统未知异常
     */
    public static final int ERR_UNKNOWN = -1;
    /**
     * 用户取消
     */
    public static final int ERR_CANCEL = -2;
    /**
     * 授权被用户拒绝
     */
    public static final int ERR_AUTH_DENY = -3;
    /**
     * 接口不支持
     */
    public static final int ERR_NOT_SUPPORT = -4;
    /**
     * 各个SDK分享平台抛出的错误
     */
    public static final int ERR_PLATFORM = -5;
    /**
     * 解析错误
     */
    public static final int ERR_RESOLVE = -6;
    /**
     * 参数错误
     */
    public static final int ERR_INVALID_PARMAS = -7;

}
