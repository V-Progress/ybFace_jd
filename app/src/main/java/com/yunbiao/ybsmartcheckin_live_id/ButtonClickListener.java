package com.yunbiao.ybsmartcheckin_live_id;


import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public abstract class ButtonClickListener implements View.OnTouchListener {
    List<Integer> longClickList = new ArrayList<>();
    private long longClickTime = 800;
    private long cacheTime = 0;


    public void onShortClick(int viewId){}
    public void onLongClick(int viewId){}
    public void onLongClickFinish(int viewId){}

    public void setLongClickTime(long time){
        longClickTime = time;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Integer viewId = v.getId();
        long currentTimeMillis = System.currentTimeMillis();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if(cacheTime == 0){
                    cacheTime = currentTimeMillis;
                } else if(currentTimeMillis - cacheTime > longClickTime){
                    if(!longClickList.contains(viewId)){
                        longClickList.add(viewId);
                        onLongClick(viewId);
                    } else {
                        onLongClick(viewId);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(currentTimeMillis - cacheTime < longClickTime){
                    onShortClick(viewId);
                } else {
                    if(longClickList.contains(viewId)){
                        longClickList.remove(viewId);
                        onLongClickFinish(viewId);
                    }
                }
                cacheTime = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                if(longClickList.contains(viewId)){
                    longClickList.remove(viewId);
                }
                cacheTime = 0;
                break;
        }
        return true;
    }
}
