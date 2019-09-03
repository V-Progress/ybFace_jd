package com.yunbiao.ybsmartcheckin_live_id.activity.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.MyStringCallback;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.PageUpdateEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.AddQRCodeBean;
import com.yunbiao.ybsmartcheckin_live_id.business.ApiManager;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.ResourceCleanManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.VipDialogManager;
import com.yunbiao.ybsmartcheckin_live_id.business.sign.MultipleSignDialog;
import com.yunbiao.ybsmartcheckin_live_id.db.CompBean;
import com.yunbiao.ybsmartcheckin_live_id.db.SignBean;
import com.yunbiao.ybsmartcheckin_live_id.utils.FileUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.jdjr.risk.face.local.thread.ThreadHelper.runOnUiThread;

public class SignFragment extends Fragment implements SignManager.SignEventListener {

    private TextView tvTotal;
    private RecyclerView rlv;
    private List<SignBean> mSignList = new ArrayList<>();
    private SignAdapter signAdapter;
    private static final String TAG = "SignFragment";
    private int mCurrentOrientation;
    private ImageView ivQRCode;
    private PieChart pieChart;
    private View rootView;
    private TextView tvSignMale;
    private TextView tvSignFemale;
    private LinearLayoutManager linearLayoutManager;
    private TextView tvNotice;
    private View aivBulu;
    private Button btnBulu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        mCurrentOrientation = getActivity().getResources().getConfiguration().orientation;

        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT){
            rootView = inflater.inflate(R.layout.fragment_sign_list, container, false);
            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_sign_list_h, container, false);
            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        }

        //公用
        btnBulu = rootView.findViewById(R.id.btn_bulu_sign_list);
        aivBulu = rootView.findViewById(R.id.aiv_bulu_sign_list);
        btnBulu.setOnClickListener(onClickListener);

        rlv = rootView.findViewById(R.id.rlv_sign_list);
        ivQRCode = rootView.findViewById(R.id.iv_qrcode_sign_list);
        tvTotal = rootView.findViewById(R.id.tv_total_sign_list);
        tvNotice = rootView.findViewById(R.id.tv_notice_sign_list);

        //横屏专用
        pieChart = rootView.findViewById(R.id.pie_chart);
        tvSignMale = rootView.findViewById(R.id.tv_sign_number_male);
        tvSignFemale = rootView.findViewById(R.id.tv_sign_number_female);
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT){
            rlv.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.right = 25;
                }
            });
        }
        return rootView;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(btnBulu != null){
                btnBulu.setEnabled(false);
                btnBulu.setVisibility(View.GONE);
                aivBulu.setVisibility(View.VISIBLE);
            }
            SignManager.instance().startBulu();
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        signAdapter = new SignAdapter(getActivity(), mSignList,mCurrentOrientation);
        rlv.setLayoutManager(linearLayoutManager);
        rlv.setAdapter(signAdapter);
        rlv.setItemAnimator(new DefaultItemAnimator());

        initPieChart();

        MultipleSignDialog.instance().init(getActivity());

        //初始化语音系统//todo 7.0以上无法加载讯飞语音库
        KDXFSpeechManager.instance().init(getActivity()).welcome();

        initSignData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(PageUpdateEvent updateEvent) {
        SignManager.instance().init(getActivity(),this);
        initSignData();
    }

    private void initSignData(){
        CompBean compBean = APP.getCompBean();
        if(compBean != null){
            String qrCodePath = compBean.getQRCodePath();
            if(!TextUtils.isEmpty(qrCodePath)){
                File qrCode = new File(qrCodePath);
                if(!qrCode.exists()){
                    loadQrCode(qrCodePath);
                } else {
                    ivQRCode.setVisibility(View.VISIBLE);
                    bindImageView(qrCode.getPath(),ivQRCode);
                }
            }
            String notice = compBean.getNotice();
            if(tvNotice != null){
                tvNotice.setText(notice);
            }
        }
    }

    private String yuyin = " 您好 %s";
    //语音播报
    private void speak(int signType, String signerName) {
        String speakStr = " 您好 %s ，欢迎光临";
        String goTips = "上班签到成功";
        String downTips = "下班签到成功";
        CompBean compBean = APP.getCompBean();

        switch (signType) {
            case 0:
                speakStr = String.format(yuyin, signerName);
                break;
            case 1:
                if (compBean != null && !TextUtils.isEmpty(compBean.getGotips())) {
                    goTips = compBean.getGotips();
                }
                speakStr = String.format(" %s " + goTips, signerName);
                break;
            case 2:
                if (compBean != null && !TextUtils.isEmpty(compBean.getDowntips())) {
                    downTips = compBean.getDowntips();
                }
                speakStr = String.format(" %s " + downTips, signerName);
                break;
        }
        KDXFSpeechManager.instance().playText(speakStr);
    }
    //设置饼图属性
    private void initPieChart() {
        if(pieChart == null){
            return;
        }
        Description description = pieChart.getDescription();
        description.setText("");//关闭饼图描述
        pieChart.setDrawEntryLabels(false);//关闭环中显示的lable
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);//关闭色块指示
        pieChart.setHoleColor(Color.TRANSPARENT);//中心圆透明
        pieChart.setHoleRadius(70f);//中心圆半径
        pieChart.setHighlightPerTapEnabled(false);//点击高亮
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);// 设置pieChart图表展示动画效果
        pieChart.setNoDataText("");//不显示无数据提醒
    }

    /*加载二维码*/
    private void loadQrCode(final String localPath) {
        int comId = SpUtils.getInt(SpUtils.COMPANYID);
        Map<String, String> params = new HashMap();
        params.put("comId", comId + "");
        OkHttpUtils.post().url(ResourceUpdate.QRCODE_ADD).params(params).build().execute(new MyStringCallback() {
            @Override
            public void onResponse(String response, int id) {
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                AddQRCodeBean addQRCodeBean = new Gson().fromJson(response, AddQRCodeBean.class);
                if (!TextUtils.equals(addQRCodeBean.status, "1")) {
                    return;
                }
                if (addQRCodeBean == null || TextUtils.isEmpty(addQRCodeBean.codeurl)) {
                    return;
                }

                OkHttpUtils.get().url(addQRCodeBean.codeurl).build().execute(new BitmapCallback() {
                    @Override public void onError(Call call, Exception e, int id) { }
                    @Override public void onResponse(final Bitmap response, int id) {
                        final File file = FileUtils.saveBitmap(response, localPath);
                        ivQRCode.setVisibility(View.VISIBLE);
                        bindImageView(file.getPath(),ivQRCode);
                    }
                });
            }
        });
    }


    @Override
    public void onPrepared(List<SignBean> mList) {
        if(mList == null){
            return;
        }
        mSignList.addAll(mList);
        signAdapter.notifyDataSetChanged();
        updateNumber();

        //自动清理服务
        ResourceCleanManager.instance().startAutoCleanService();
    }

    @Override
    public void onSigned(SignBean signBean, int signType) {
        mSignList.add(0,signBean);
        signAdapter.notifyItemInserted(0);
        rlv.scrollToPosition(0);
        updateNumber();

//        ApiManager.instance().onGate();
//        if (mGateIsAlive) {
//            mGateConnection.writeCom(GateCommands.GATE_OPEN_DOOR);
//        }
//
        ((WelComeActivity)getActivity()).openDoor();

        speak(signType, signBean.getName());

        MultipleSignDialog.instance().sign(signBean);
    }

    @Override
    public void onMakeUped(SignBean signBean, boolean makeUpSuccess) {
        mSignList.add(0,signBean);
        signAdapter.notifyItemInserted(0);
        rlv.scrollToPosition(0);
        updateNumber();

        VipDialogManager.showBuluDialog(getActivity(), signBean.getImgUrl(),makeUpSuccess);
        KDXFSpeechManager.instance().playText(makeUpSuccess ?"补录成功！":"补录失败！");
        btnBulu.setEnabled(true);
        btnBulu.setVisibility(View.VISIBLE);
        aivBulu.setVisibility(View.GONE);
    }

    //更新签到列表
    private void updateNumber() {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                int male = 0;
                for (SignBean signBean : mSignList) {
                    boolean empty = TextUtils.isEmpty(signBean.getSex());
                    if (empty || signBean.getSex().equals("1") || signBean.getSex().equals("男")) {
                        male = male + 1;
                    }
                }

                final int total = mSignList.size();
                final int female = total - male;
                final int maleNum = male;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            String signTips = "已签到   <font color='#fff600'>" + total + "</font>   人 （男 <font color='#fff600'>" + maleNum + "</font>   女  <font color='#fff600'>" + female + "</font> ） ";
                            tvTotal.setText(Html.fromHtml(signTips));
                        } else {
                            tvTotal.setText("" + total);
                            tvSignMale.setText("男: " + maleNum + "人");
                            tvSignFemale.setText("女: " + female + "人");

                            //设置饼图数据
                            List<PieEntry> dataEntry = new ArrayList<>();
                            List<Integer> dataColors = new ArrayList<>();

                            if (maleNum == 0 && female == 0) {
                                dataEntry.add(new PieEntry(100, ""));
                                dataColors.add(getResources().getColor(R.color.white));
                            } else {
                                dataEntry.add(new PieEntry(maleNum, "男"));
                                dataEntry.add(new PieEntry(female, "女"));
                                dataColors.add(getResources().getColor(R.color.horizontal_chart_male));
                                dataColors.add(getResources().getColor(R.color.horizontal_chart_female));
                            }

                            pieChart.clear();
                            PieDataSet pieDataSet = new PieDataSet(dataEntry, null);
                            pieDataSet.setColors(dataColors);
                            PieData pieData = new PieData(pieDataSet);
                            pieData.setDrawValues(false);//环中value显示
                            pieChart.setData(pieData);
                            pieChart.notifyDataSetChanged();
                            pieChart.invalidate();
                        }
                    }
                });
            }
        });
    }

    protected void bindImageView(String urlOrPath, final ImageView iv){
        if(TextUtils.isEmpty(urlOrPath)){
            return;
        }
        Glide.with(this).load(urlOrPath).skipMemoryCache(true).crossFade(500).into(iv);
    }

    class SignAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private List<SignBean> signBeanList ;
        private Context mContext;
        private int orientation;
        private int id;

        public SignAdapter(Context context,List<SignBean> signBeanList,int currOrientation) {
            this.mContext = context;
            this.signBeanList = signBeanList;
            this.orientation = currOrientation;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            if(orientation == Configuration.ORIENTATION_PORTRAIT){
                return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.item_sign_main, null));
            } else {
                return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.item_visitor_h, null));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ViewHolder vh = (ViewHolder) viewHolder;
            SignBean signBean = signBeanList.get(i);

            String imgUrl = signBean.getImgUrl();
            if(!TextUtils.isEmpty(imgUrl)){
                Glide.with(mContext).load(imgUrl).asBitmap().override(100,100).into(vh.ivHead);
            }

            vh.tvName.setText(signBean.getName());
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            vh.tvTime.setText(df.format(signBean.getTime()));
        }

        @Override
        public int getItemCount() {
            return signBeanList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageView ivHead;
            TextView tvName;
            TextView tvTime;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivHead = itemView.findViewById(R.id.iv_head_item);
                tvName = itemView.findViewById(R.id.tv_name_item);
                tvTime = itemView.findViewById(R.id.tv_time_item);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
