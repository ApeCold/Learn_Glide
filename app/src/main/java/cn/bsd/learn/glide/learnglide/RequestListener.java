package cn.bsd.learn.glide.learnglide;

import android.graphics.Bitmap;

//请求结果的监听对象
public interface RequestListener {
    boolean onSuccess(Bitmap bitmap);

    boolean onFailure();
}
