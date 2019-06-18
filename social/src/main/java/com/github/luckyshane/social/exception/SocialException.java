package com.github.luckyshane.social.exception;

/*
 * @author: luckyShane
 */
public class SocialException extends RuntimeException {
    private int code;

    public static final int CODE_NOT_SUPPORT = 1;
    public static final int CODE_INVALID_PARMAS = 2;

    public SocialException() {

    }

    public SocialException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public SocialException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
