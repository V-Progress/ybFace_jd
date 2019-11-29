package com.yunbiao.ybsmartcheckin_live_id.activity.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.gson.reflect.TypeToken;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateQRCodeEvent;
import com.yunbiao.ybsmartcheckin_live_id.activity.WelComeActivity;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.KDXFSpeechManager;
import com.yunbiao.ybsmartcheckin_live_id.business.NoticeManager;
import com.yunbiao.ybsmartcheckin_live_id.business.ResourceCleanManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.business.VipDialogManager;
import com.yunbiao.ybsmartcheckin_live_id.business.sign.MultipleSignDialog;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.ThreadUitls;
import com.yunbiao.ybsmartcheckin_live_id.views.ImageFileLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.jdjr.risk.face.local.thread.ThreadHelper.runOnUiThread;

public class SignFragment extends Fragment implements SignManager.SignEventListener {

    private TextView tvTotal;
    private TextView tvTotal1;
    private RecyclerView rlv;
    private List<Sign> mSignList = new ArrayList<>();
    private SignAdapter signAdapter;
    private static final String TAG = "SignFragment";
    private int mCurrentOrientation;
    private ImageView ivQRCode;
    private PieChart pieChart;
    private View rootView;
    private TextView tvSignMale;
    private TextView tvSignFemale;
    private LinearLayoutManager linearLayoutManager;
    private View aivBulu;
    private Button btnBulu;
    private TextView tvTotalSex;
    private TextView tvMale1;
    private TextView tvFemale1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        mCurrentOrientation = getActivity().getResources().getConfiguration().orientation;

        int orientation;
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT){
            rootView = inflater.inflate(R.layout.fragment_sign_list, container, false);
            orientation = LinearLayoutManager.HORIZONTAL;
        } else {
            rootView = inflater.inflate(R.layout.fragment_sign_list_h, container, false);
            orientation = LinearLayoutManager.VERTICAL;
            /*只在横屏初始化公告*/
            NoticeManager.getInstance().init(rootView);
        }

        linearLayoutManager = new LinearLayoutManager(getActivity(),orientation,false);

        tvTotal1 = rootView.findViewById(R.id.tv_total_number);
        tvMale1 = rootView.findViewById(R.id.tv_male_number);
        tvFemale1 = rootView.findViewById(R.id.tv_female_number);

        //公用
        btnBulu = rootView.findViewById(R.id.btn_bulu_sign_list);
        aivBulu = rootView.findViewById(R.id.aiv_bulu_sign_list);
        btnBulu.setOnClickListener(onClickListener);

        rlv = rootView.findViewById(R.id.rlv_sign_list);
        ivQRCode = rootView.findViewById(R.id.iv_qrcode_sign_list);
        tvTotal = rootView.findViewById(R.id.tv_total_sign_list);
        tvTotalSex = rootView.findViewById(R.id.tv_total_sex);

        //横屏专用
        pieChart = rootView.findViewById(R.id.pie_chart);
        tvSignMale = rootView.findViewById(R.id.tv_sign_number_male);
        tvSignFemale = rootView.findViewById(R.id.tv_sign_number_female);
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT){
            rlv.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.right = 5;
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

        if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            //公告
            NoticeManager.getInstance().initSignData();
        }

        yuyin = getActivity().getResources().getString(R.string.fment_sign_tip_hello);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        SignManager.instance().init(getActivity(),this);
        if(mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            //公告
            NoticeManager.getInstance().initSignData();
        }

        ivQRCode.setVisibility(View.VISIBLE);
        Company company = SpUtils.getCompany();
        ImageFileLoader.i().loadAndSave(getActivity(),company.getCodeUrl(), Constants.DATA_PATH,ivQRCode);
    }

    private String yuyin ="";
    //语音播报
    private void speak(int signType, String signerName) {
        String speakStr =  getString(R.string.fment_sign_tip_welcome);
        String goTips =  getString(R.string.fment_sign_tip_goTips);
        String downTips =  getString(R.string.fment_sign_tip_downTips);
        Company company = SpUtils.getCompany();

        switch (signType) {
            case 0:
                speakStr = String.format(yuyin, signerName);
                break;
            case 1:
                if (company != null && !TextUtils.isEmpty(company.getGotips())) {
                    goTips = company.getGotips();
                }
                speakStr = String.format(" %s " + goTips, signerName);
                break;
            case 2:
                if (company != null && !TextUtils.isEmpty(company.getDowntips())) {
                    downTips = company.getDowntips();
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

    @Override
    public void onPrepared(List<Sign> mList) {
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
    public void onSigned(Sign sign, int signType) {
        mSignList.add(0,sign);
        signAdapter.notifyItemInserted(0);
        rlv.scrollToPosition(0);
        updateNumber();

        ((WelComeActivity)getActivity()).openDoor();

        speak(signType, sign.getName());

        MultipleSignDialog.instance().sign(sign);
    }

    @Override
    public void onMakeUped(Sign sign, boolean makeUpSuccess) {
        mSignList.add(0,sign);
        signAdapter.notifyItemInserted(0);
        rlv.scrollToPosition(0);
        updateNumber();

        VipDialogManager.showBuluDialog(getActivity(), sign.getHeadPath(),makeUpSuccess);
        KDXFSpeechManager.instance().playText(makeUpSuccess ?getString(R.string.fment_sign_tip_blcg)+"！":getString(R.string.fment_sign_tip_blsb)+"！");
        btnBulu.setEnabled(true);
        btnBulu.setVisibility(View.VISIBLE);
        aivBulu.setVisibility(View.GONE);
    }

    //更新签到列表
    private void updateNumber() {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {

                List<Sign> mNewSignList=  removeDuplicateCase(mSignList);

                int male = 0;
                for (Sign signBean : mNewSignList) {
                    if(signBean.getSex() == 1){
                        male = male + 1;
                    }
                }
                
                final int total = mNewSignList.size();
                final int female = total - male;
                final int maleNum = male;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            String signTips = getString(R.string.fment_sign_tip_yqd)+"   <font color='#fff600'>" + total + "</font>   "+getString(R.string.base_people);
                            String totalSex = "（"+getString(R.string.base_male)+" <font color='#fff600'>" + maleNum + "</font>   "+getString(R.string.base_female)+"  <font color='#fff600'>" + female + "</font> ）";
                            tvTotal.setText(Html.fromHtml(signTips));
                            tvTotalSex.setText(Html.fromHtml(totalSex));

                            if(tvTotal1 != null){
                                tvTotal1.setText(total + "");
                            }
                            if(tvMale1 != null){
                                tvMale1.setText(maleNum + "");
                            }
                            if(tvFemale1 != null){
                                tvFemale1.setText(female + "");
                            }
                        } else {
                            tvTotal.setText("" + total);
                            tvSignMale.setText(getString(R.string.base_male)+": " + maleNum + getString(R.string.base_people));
                            tvSignFemale.setText(getString(R.string.base_female)+": " + female + getString(R.string.base_people));

                            //设置饼图数据
                            List<PieEntry> dataEntry = new ArrayList<>();
                            List<Integer> dataColors = new ArrayList<>();

                            if (maleNum == 0 && female == 0) {
                                dataEntry.add(new PieEntry(100, ""));
                                dataColors.add(getResources().getColor(R.color.white));
                            } else {
                                dataEntry.add(new PieEntry(maleNum, getString(R.string.base_male)));
                                dataEntry.add(new PieEntry(female, getString(R.string.base_female)));
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


    /**
     * @param
     * @return
     * @description 根据员工ID去重
     * @date 14:39 2018/6/19
     * @author zhenghao
     */
    private List<Sign> removeDuplicateCase(List<Sign> cases) {
        Set<Sign> set = new TreeSet<>(new Comparator<Sign>() {
            @Override
            public int compare(Sign o1, Sign o2) {
                //字符串,则按照asicc码升序排列
                return (o1.getEmpId()+"").compareTo(o2.getEmpId()+"");
            }
        });
        set.addAll(cases);
        return new ArrayList<>(set);
    }


    protected void bindImageView(String urlOrPath, final ImageView iv){
        if(TextUtils.isEmpty(urlOrPath)){
            return;
        }
        Glide.with(getActivity()).load(urlOrPath).skipMemoryCache(true).crossFade(500).into(iv);
    }

    class SignAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private List<Sign> signBeanList ;
        private Context mContext;
        private int orientation;
        private int id;

        public SignAdapter(Context context, List<Sign> signBeanList, int currOrientation) {
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
            Sign signBean = signBeanList.get(i);

            byte[] imgBytes = signBean.getImgBytes();
            String headPath = signBean.getHeadPath();
            if(imgBytes != null){
                Glide.with(mContext).load(imgBytes).asBitmap().override(100,100).into(vh.ivHead);
            } else if(!TextUtils.isEmpty(headPath)){
                Glide.with(mContext).load(headPath).asBitmap().override(100,100).into(vh.ivHead);
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
