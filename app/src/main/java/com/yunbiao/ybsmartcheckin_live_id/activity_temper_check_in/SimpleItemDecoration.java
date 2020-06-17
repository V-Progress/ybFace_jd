package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;


import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

class SimpleItemDecoration extends RecyclerView.ItemDecoration{
    private int space;

    public SimpleItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildPosition(view) != 0)
            outRect.top = space;
    }
}