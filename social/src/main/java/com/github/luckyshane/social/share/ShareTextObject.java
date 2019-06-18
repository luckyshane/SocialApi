package com.github.luckyshane.social.share;

import com.github.luckyshane.social.ShareObject;

import androidx.annotation.NonNull;

/*
 * @author: luckyShane
 */
public class ShareTextObject implements ShareObject {
    public final String text;

    public ShareTextObject(@NonNull String text) {
        this.text = text;
    }
}
