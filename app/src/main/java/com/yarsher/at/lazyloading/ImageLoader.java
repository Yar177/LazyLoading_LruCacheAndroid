package com.yarsher.at.lazyloading;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import android.os.Handler;

public class ImageLoader {

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    Handler handler = new Handler();

    public ImageLoader(FileCache fileCache, ExecutorService executorService) {
        this.fileCache = fileCache;
        this.executorService = executorService;
    }

    final int stub_id = R.drawable.blank150x225;

    public void DisplayImage(String url, ImageView imageView){
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }


    private class PhotoToLoad {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }
    }

    private Bitmap getBitmap(String url){
        File file = fileCache.getFiles(url);

        int connectTimeout = 5000;
        int readTimeout = 5000;

        Bitmap bitmap = decodeFile(file);


        return null;
    }




    private class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        public PhotosLoader(PhotoToLoad p) {
            this.photoToLoad = p;
        }

        @Override
        public void run() {
            try {
                if (imageViewsReused(photoToLoad))return;
                Bitmap bitmap = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bitmap);
                if (imageViewsReused(photoToLoad))return;
                BitmapDisplay bitmapDisplay = new BitmapDisplay(bitmap, photoToLoad);
                handler.post(bitmapDisplay);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    class BitmapDisplay implements Runnable{
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplay(Bitmap bitmap, PhotoToLoad photoToLoad) {
            this.bitmap = bitmap;
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if(imageViewsReused(photoToLoad))return;
            if (bitmap != null) photoToLoad.imageView.setImageBitmap(bitmap);
            else photoToLoad.imageView.setImageResource(stub_id);
        }
    }


    boolean imageViewsReused(PhotoToLoad photoToLoad){
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))return true;

        return false;
    }
}
