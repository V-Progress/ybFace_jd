package com.yunbiao.ybsmartcheckin_live_id.business;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import java.util.List;

public class NoticeManager {
    private static final String TAG = "NoticeManager";
    private TextView tvNotice;
    private View noticeLayout;
    private static NoticeManager instance = new NoticeManager();
    public static NoticeManager getInstance(){
        return instance;
    }

    public void init(View rootView){
        /*公告*/
        tvNotice = rootView.findViewById(R.id.tv_notice_sign_list);
        noticeLayout = rootView.findViewById(R.id.layout_subTitle);
    }

    public void initSignData(){
        Company company = SpUtils.getCompany();
        String notice = company.getNotice();
        if(TextUtils.isEmpty(notice)){
            Log.e(TAG, "initSignData: notice为空");
            noticeLayout.setVisibility(View.GONE);
            return;
        }

        notices = new Gson().fromJson(notice, new TypeToken<List<String>>(){}.getType());
        if(notices == null || notices.size() <= 0){
            Log.e(TAG, "initSignData: notices为空");
            noticeLayout.setVisibility(View.GONE);
        } else {
            noticeIndex = 0;
            noticeLayout.setVisibility(View.VISIBLE);
            if(notices.size() < 2){

                tvNotice.setText(notices.get(0));
            } else {
                noticeHandler.sendEmptyMessage(0);
            }
        }
    }

    private List<String> notices;
    private int noticeIndex = 0;
    private Handler noticeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String s = notices.get(noticeIndex);
            tvNotice.setText(s);
            noticeIndex++;
            if(noticeIndex >= notices.size()){
                noticeIndex = 0;
            }
            noticeHandler.removeMessages(0);
            noticeHandler.sendEmptyMessageDelayed(0,10 * 1000);
        }
    };


}
