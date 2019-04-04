package com.yarsher.at.lazyloading;

import android.graphics.Bitmap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryCache {

    private Map<String, Bitmap> chache = Collections.synchronizedMap(
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


}
