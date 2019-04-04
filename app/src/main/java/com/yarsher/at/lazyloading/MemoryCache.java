package com.yarsher.at.lazyloading;

import android.graphics.Bitmap;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryCache {

    private Map<String, Bitmap> cache = Collections.synchronizedMap(
            //Last argument true for LRU ordering
            new LinkedHashMap<String, Bitmap>(10, 0.75f, true)
    );

    private long size = 0; //curent alocated size
    private long limit = 2000000; //max memory in bytes

    public MemoryCache(){
        setLimit(Runtime.getRuntime().maxMemory()/4);
    }

    public void setLimit(long newLimit){
        limit = newLimit;
    }

    public Bitmap get(String id){
        try{
            if (!cache.containsKey(id)) return null;
            return cache.get(id);
        }catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }

    public void put(String id, Bitmap bitmap){
        try {
            if (cache.containsKey(id)) size -= getSizeInBytes(cache.get(id));
            cache.put(id, bitmap);
            size += getSizeInBytes(bitmap);
            checkSize();
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private void checkSize() {
        if (size>limit){
            //least recently accessed item will be the first one iterated
            Iterator<Map.Entry<String, Bitmap>> iterator = cache.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Bitmap> entry = iterator.next();
                size -= getSizeInBytes(entry.getValue());
                iterator.remove();
                if (size<=limit) break;
            }
        }
    }




    long getSizeInBytes(Bitmap bitmap){
        if (bitmap == null) return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }


}
