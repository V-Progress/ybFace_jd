package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.White;
import com.yunbiao.ybsmartcheckin_live_id.utils.IDCardReader;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class CertificatesWhiteListActivity extends BaseActivity {

    private ListView lvWhiteList;
    private TextView tvDataState;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_certificates_white_list;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_certificates_white_list;
    }

    @Override
    protected void initView() {
        lvWhiteList = findViewById(R.id.lv_white_list_certificates);
        tvDataState = findViewById(R.id.tv_data_state);
    }

    @Override
    protected void initData() {
        initWhiteList();

        initWhiteListSwitch();
    }

    private void initWhiteListSwitch() {
        boolean whiteList = SpUtils.getBoolean(CertificatesConst.Key.WHITE_LIST, CertificatesConst.Default.WHITE_LIST);
        Switch swWhiteList = findViewById(R.id.sw_white_list_setting);
        swWhiteList.setChecked(whiteList);
        swWhiteList.setOnCheckedChangeListener((buttonView, isChecked) -> SpUtils.saveBoolean(CertificatesConst.Key.WHITE_LIST, isChecked));
    }

    private void initWhiteList() {
        List<White> whites = DaoManager.get().queryAll(White.class);
        List<String> whiteList = new ArrayList<>();
        if (whites != null) {
            for (White white : whites) {
                String num = white.getNum();
                if (TextUtils.isEmpty(num) || num.length() < 2) {
                    continue;
                }
                String sheng = "";
                String shi = "";
                String qu = "";
                if (num.length() >= 2) {
                    sheng = IDCardReader.getPlace(num.substring(0, 2));
                }
                if (num.length() >= 4) {
                    shi = IDCardReader.getPlace(num.substring(0, 4));
                }
                if (num.length() >= 6) {
                    qu = IDCardReader.getPlace(num.substring(0, 6));
                }

                whiteList.add(sheng + shi + qu + "（" + num + "）");
            }
        }
        WhiteListAdapter whiteListAdapter = new WhiteListAdapter(whiteList);
        lvWhiteList.setAdapter(whiteListAdapter);
        lvWhiteList.setDividerHeight(10);

        if (whiteListAdapter.getCount() <= 0) {
            if (!tvDataState.isShown()) {
                tvDataState.setVisibility(View.VISIBLE);
            }
        } else {
            if (tvDataState.isShown()) {
                tvDataState.setVisibility(View.GONE);
            }
        }
    }

    public void syncWhiteList(View view) {
        SyncManager.instance().requestWhiteList(new SyncManager.WhiteListSyncListener() {
            @Override
            public void onSuccess() {
                initWhiteList();
            }

            @Override
            public void onFailed(String msg) {
                UIUtils.showShort(CertificatesWhiteListActivity.this, "白名单同步失败，错误信息：（" + msg + "）");
            }
        });
    }

    class WhiteListAdapter extends BaseAdapter {
        private List<String> whiteList;

        public WhiteListAdapter(List<String> whiteList) {
            this.whiteList = whiteList;
        }

        @Override
        public int getCount() {
            return whiteList == null ? 0 : whiteList.size();
        }

        @Override
        public Object getItem(int position) {
            return whiteList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), android.R.layout.simple_list_item_1, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.bindData(whiteList.get(position));
            return convertView;
        }

        class ViewHolder {
            TextView tv;

            public ViewHolder(View convertView) {
                tv = convertView.findViewById(android.R.id.text1);
            }

            public void bindData(String data) {
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(26);
                tv.setText(data);
            }
        }
    }
}
