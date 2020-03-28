package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.view.View;
import android.widget.ListView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.VisitorUpdateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.VisitorAdapter;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Visitor;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ThermalVisitorActivity extends BaseActivity {

    private ListView lvVisitor;
    private VisitorAdapter visitorAdapter;
    private List<Visitor> visitorList = new ArrayList<>();
    private View loading;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_visitor;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_visitor;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        visitorAdapter = new VisitorAdapter(this,visitorList);
        loading = findViewById(R.id.avl_loading);
        lvVisitor = findViewById(R.id.lv_visitor_List);
        lvVisitor.setAdapter(visitorAdapter);
    }

    @Override
    protected void initData() {
        setData();
    }

    private void setData(){
        visitorList.clear();
        visitorAdapter.notifyDataSetChanged();
        loading.setVisibility(View.VISIBLE);

        Company company = SpUtils.getCompany();
        List<Visitor> visitors = DaoManager.get().queryVisitorsByCompId(company.getComid());
        visitorList.addAll(visitors);
        visitorAdapter.notifyDataSetChanged();
        loading.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(VisitorUpdateEvent event){
        setData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
