package com.github.luckyshane.social;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;

/*
 * @author: luckyShane
 */
public class Util {
    private static final int THUMB_SIZE = 150;
    private static File cacheDir;

    public static boolean isAppInstalled(@NonNull Context context, @NonNull String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getApplicationInfo(packageName, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isWebUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public static Bitmap createThumbBitmap(@NonNull Bitmap src) {
        return Bitmap.createScaledBitmap(src, THUMB_SIZE, THUMB_SIZE, true);
    }

    public static byte[] bitmapToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static File saveBitmapFile(Bitmap bitmap, String path) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(path);
    }

    public static File getCacheDir(Context context) {
        if (cacheDir == null) {
            File temp = context.getExternalCacheDir(); // 必须放到外部存储空间
            if (temp != null) {
                cacheDir = new File(temp, "social_share");
            }
        }
        File dir = cacheDir;
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    static void clearCacheDir(Context context) {
        File dir = getCacheDir(context);
        if (dir != null && dir.exists()) {
            dir.delete();
        }
    }





}
