package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.DepartListAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.xutil.MyXutils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2018/10/8.
 */

public class DepartListActivity extends BaseActivity implements  DepartListAdapter.InnerItemOnclickListener{

    private static final String TAG = "DepartListActivity";

    private ListView lv_depart_List;
    private Button btn_addDepart;
    private DepartListAdapter mDepartAdapter;
    private List<String> mDepartList;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_departlist;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_departlist_h;
    }

    @Override
    protected void initView() {
        lv_depart_List= findViewById(R.id.lv_depart_List);
        btn_addDepart= findViewById(R.id.btn_addDepart);
        mDepartList=new ArrayList<>();
//        mDepartlist = departDao.selectAll();
//        if (mDepartlist!=null){
//            for (int i = 0; i <mDepartlist.size() ; i++) {
//                mDepartList.add(mDepartlist.get(i).getName());
//            }
//        }
        mDepartAdapter=new DepartListAdapter(this,mDepartList);
        lv_depart_List.setAdapter(mDepartAdapter);
        mDepartAdapter.setOnInnerItemOnClickListener(this);

        btn_addDepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DepartListActivity.this);

                builder.setTitle(getString(R.string.act_departList_tip_qsrbmmc));
                //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
                View view = LayoutInflater.from(DepartListActivity.this).inflate(R.layout.dialog_depart, null);
                //    设置我们自己定义的布局文件作为弹出框的Content
                builder.setView(view);

                final EditText et_departName = (EditText)view.findViewById(R.id.et_departName);

                builder.setPositiveButton(getString(R.string.base_ensure), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        final  String name = et_departName.getText().toString().trim();
                        mDepartList.add(name);
                        mDepartAdapter.notifyDataSetChanged();
//                        mDepartlist   =	 departDao.selectAll();

//                        int companyid= SpUtils.getInt(DepartListActivity.this,SpUtils.COMPANYID,0);
                        int companyid= SpUtils.getInt(SpUtils.COMPANYID);
                        final Map<String, String> map = new HashMap<String, String>();
                        map.put("comId", companyid+"");
                        map.put("name", name+"");
                        MyXutils.getInstance().post(ResourceUpdate.ADDDEPART, map, new MyXutils.XCallBack() {
                            @Override
                            public void onSuccess(String result) {
                                Log.e(TAG, "增加部门--------------->"+result );
                                try {
                                    JSONObject jsonObject=new JSONObject(result);
                                    int status=jsonObject.getInt("status");
                                    if (status==1){
                                        int    departId=jsonObject.getInt("depId");
//                                        List<DepartBean> mlist=	 departDao.queryByName(name);
//                                        if (mlist!=null&&mlist.size()>0){
//                                        }else {
////                                            departDao.insert(new DepartBean(name,departId));
//                                        }
                                    }else {
//                                        List<DepartBean> mlist=	 departDao.queryByName(name);
//                                        if (mlist!=null&&mlist.size()>0){
//                                        }else {
//                                            departDao.insert(new DepartBean(name,0));
//                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(Throwable ex) {
                                Log.e(TAG, "onError-------> "+ex.getMessage().toString() );
//                                List<DepartBean> mlist=	 departDao.queryByName(name);
//                                if (mlist!=null&&mlist.size()>0){
//                                }else {
//                                    departDao.insert(new DepartBean(name,0));
//                                }
                            }

                            @Override
                            public void onFinish() {

                            }
                        });

                    }
                });
                builder.setNegativeButton(getString(R.string.base_cancel), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void initData() {
//        departDao= APP.getDepartDao();
    }

    @Override
    public void itemClick(View v, final int postion) {

        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(DepartListActivity.this);

        //    设置Title的内容
        builder.setTitle(getString(R.string.base_tip));
        //    设置Content来显示一个信息
        builder.setMessage(getString(R.string.act_departList_tip_qdscm));
        //    设置一个PositiveButton
        builder.setPositiveButton(getString(R.string.base_ensure), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
//                final Map<String, String> map = new HashMap<String, String>();
//                Log.e(TAG, "depId---------------> "+mDepartlist.get(postion).getDepartId()+"" );
//                map.put("depId", mDepartlist.get(postion).getDepartId()+"");
//                MyXutils.getInstance().post(ResourceUpdate.DELETEDEPART, map, new MyXutils.XCallBack() {
//                    @Override
//                    public void onSuccess(String result) {
//                        Log.e(TAG, "删除部门---------> "+result );
//                        try {
//                            JSONObject jsonObject=new JSONObject(result);
//                            int status=jsonObject.getInt("status");
//                            if (status==1){
//                                departDao.delete(mDepartlist.get(postion));
//                                mDepartList.remove(postion);
//                                mDepartAdapter.notifyDataSetChanged();
//                                mDepartlist   =	 departDao.selectAll();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//
//                    @Override
//                    public void onError(Throwable ex) {
//                        Log.e(TAG, "onError-------> "+ex.getMessage() );
//                    }
//
//                    @Override
//                    public void onFinish() {
//
//                    }
//                });



            }


        });
        //    设置一个NegativeButton
        builder.setNegativeButton(getString(R.string.base_cancel), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        //    显示出该对话框
        builder.show();

    }
}
