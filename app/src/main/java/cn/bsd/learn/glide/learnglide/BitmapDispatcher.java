package cn.bsd.learn.glide.learnglide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

import cn.bsd.learn.glide.MyApplication;
import cn.bsd.learn.glide.cache.DoubleLruCache;

public class BitmapDispatcher extends Thread {
    private Handler handler = new Handler(Looper.getMainLooper());
    //创建一个阻塞队列
    private LinkedBlockingQueue<BitmapRequest> requestQueue;

    //获取三级缓存对象
    private DoubleLruCache doubleLruCache = new DoubleLruCache(MyApplication.getInstance());

    public BitmapDispatcher(LinkedBlockingQueue<BitmapRequest> requestQueue){
        this.requestQueue=requestQueue;
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()){
            try {
                BitmapRequest br = requestQueue.take();

                //设置占位图片
                showLoadingImg(br);

                //加载图片
                Bitmap bitmap = findBitMap(br);

                //把图片显示到ImageView
                showImageView(br,bitmap);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void showImageView(final BitmapRequest br, final Bitmap bitmap) {
        if(bitmap!=null&&br.getImageView()!=null
        &&br.getUrlMd5().equals(br.getImageView().getTag())){
            final ImageView imageView = br.getImageView();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                    if(br.getRequestListener()!=null){
                        RequestListener listener = br.getRequestListener();
                        listener.onSuccess(bitmap);
                    }
                }
            });
        }
    }

    private Bitmap findBitMap(BitmapRequest br) {
        Bitmap bitmap = null;
        bitmap = doubleLruCache.get(br);
        if(bitmap == null){
            bitmap = downloadImage(br.getUrl());
            if(bitmap!=null){
                doubleLruCache.put(br,bitmap);
            }
        }
        return bitmap;
    }

    private void showLoadingImg(BitmapRequest br) {
        if(br.getResID()>0
            &&br.getImageView()!=null){
            final int resID = br.getResID();
            final ImageView imageView = br.getImageView();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageResource(resID);
                }
            });

        }
    }

    private Bitmap downloadImage(String uri) {
        FileOutputStream fos = null;
        InputStream is = null;
        Bitmap bitmap = null;
        try
        {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            is = conn.getInputStream();
            bitmap= BitmapFactory.decodeStream(is);
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (is != null)
                    is.close();
            } catch (IOException e)
            {
            }

            try
            {
                if (fos != null)
                    fos.close();
            } catch (IOException e)
            {
            }
        }
        return bitmap;
    }
}
