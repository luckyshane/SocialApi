package com.github.luckyshane.social;

/*
 * @author: luckyShane
 */
public interface Callback<T> {
    /**
     * 成功时调用
     *
     * @param data 返回结果
     */
    void onComplete(int platform, T data);

    /**
     * 用户取消时候调用
     */
    void onCancel(int platform);

    /**
     * 发生错误时调用。
     *
     * @param code    Social API针对各个平台统一封装的code，参见{@link ErrorCode}
     * @param subCode 不同平台返回的实际code
     * @param msg     错误消息
     */
    void onError(int platform, int code, int subCode, String msg);
}
