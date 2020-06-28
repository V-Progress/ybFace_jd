package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.FlavorType;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.Event.UpdateInfoEvent;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.business.NoticeManager;
import com.yunbiao.ybsmartcheckin_live_id.business.ResourceCleanManager;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.CommonUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.NetWorkChangReceiver;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ThermalSignFragment extends Fragment implements NetWorkChangReceiver.NetWorkChangeListener/* implements SignManager.SignEventListener*/ {

    private TextView tvTotal;
    private RecyclerView rlv;
    private List<Sign> mSignList = new ArrayList<>();
    private SignAdapter signAdapter;
    private static final String TAG = "SignFragment";
    private int mCurrentOrientation;
//    private ImageView ivQRCode;
    private PieChart pieChart;
    private View rootView;
    private TextView tvSignMale;
    private TextView tvSignFemale;
    private LinearLayoutManager linearLayoutManager;
    private TextView tvModel;
    private float mCurrWarningThreshold = 0.0f;
    private TextView tvCompanyName;
    private TextView tvDeviceNo;
    private TextView tvNetState;
    private TextView tvVer;
    private TextView tvAlready;
    private boolean mFEnabled;
    private NetWorkChangReceiver netWorkChangReceiver;
    private View llInfoSignList;
    private GifImageView gifImageView;
    private boolean faceEnabled;
    private boolean temperEnabled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        mCurrentOrientation = APP.getContext().getResources().getConfiguration().orientation;

        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            rootView = inflater.inflate(R.layout.fragment_sign_list_thermal, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_sign_list_thermal_h, container, false);
        }

        /*公用*/
        tvCompanyName = rootView.findViewById(R.id.tv_company_name_sign_fragment);//公司名称
        tvDeviceNo = rootView.findViewById(R.id.tv_device_no_sign_fragment);//设备编号
        tvNetState = rootView.findViewById(R.id.tv_net_state_sign_fragment);//网路状态
        tvVer = rootView.findViewById(R.id.tv_ver_sign_fragment);//版本号
        tvModel = rootView.findViewById(R.id.tv_model_sign);//模式
//        ivQRCode = rootView.findViewById(R.id.iv_qrcode_sign_list);//二维码
        tvTotal = rootView.findViewById(R.id.tv_total_sign_list);//签到总人数
        llInfoSignList = rootView.findViewById(R.id.ll_info_sign_list);

        gifImageView = rootView.findViewById(R.id.iv_qrcode_sign_list);
        try {
            GifDrawable drawable = new GifDrawable(getResources(),R.mipmap.splash2);
            drawable.setLoopCount(0);
            drawable.setSpeed(2.0f);
            gifImageView.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(Constants.FLAVOR_TYPE == FlavorType.SOFT_WORK_Z){
            gifImageView.setImageResource(R.mipmap.soft_workz_qrcode);
        }

        setModelText(mModel);

        //竖屏砖用
        tvAlready = rootView.findViewById(R.id.tv_already_sign_list);

        //横屏专用
        tvSignMale = rootView.findViewById(R.id.tv_sign_number_male);//男数
        tvSignFemale = rootView.findViewById(R.id.tv_sign_number_female);//女数
        pieChart = rootView.findViewById(R.id.pie_chart);//统计图表
        linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        rlv = rootView.findViewById(R.id.rlv_sign_list);//签到列表

        initPieChart();

        /*只在横屏初始化公告*/
        NoticeManager.getInstance().init(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        netWorkChangReceiver = new NetWorkChangReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(netWorkChangReceiver, filter);

        if(rlv != null){
            signAdapter = new SignAdapter(getActivity(), mSignList, mCurrentOrientation);
            rlv.setLayoutManager(linearLayoutManager);
            rlv.setAdapter(signAdapter);
            rlv.setItemAnimator(new DefaultItemAnimator());
            rlv.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.left = 10;
                    outRect.right = 10;
                }
            });
        }

        //版本号
        if (tvVer != null) {
            tvVer.setText(APP.getContext().getResources().getString(R.string.fment_sign_version) + CommonUtils.getAppVersion(getActivity()));
        }
        if (tvDeviceNo != null) {
            tvDeviceNo.setText(APP.getContext().getResources().getString(R.string.fment_sign_device_no) + SpUtils.getStr(SpUtils.DEVICE_NUMBER));
        }
        if (tvCompanyName != null) {
            tvCompanyName.setText(APP.getContext().getResources().getString(R.string.fment_sign_bind_code) + SpUtils.getStr(SpUtils.BIND_CODE));
        }

        if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            //公告
            NoticeManager.getInstance().initSignData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean qrCodeEnabled = SpUtils.getBoolean(Constants.Key.QRCODE_ENABLED, Constants.Default.QRCODE_ENABLED);
        gifImageView.setVisibility(qrCodeEnabled ? View.VISIBLE : View.GONE);

        boolean isPrivacy = SpUtils.getBoolean(Constants.Key.PRIVACY_MODE,Constants.Default.PRIVACY_MODE);
        if(rlv != null){
            rlv.setVisibility(isPrivacy ? View.INVISIBLE : View.VISIBLE);
        }
        Log.e(TAG, "onResume: 重加载数据");
        float warningThreshold = SpUtils.getFloat(ThermalConst.Key.TEMP_WARNING_THRESHOLD, ThermalConst.Default.TEMP_WARNING_THRESHOLD);
        faceEnabled = SpUtils.getBoolean(ThermalConst.Key.FACE_ENABLED,ThermalConst.Default.FACE_ENABLED);
        temperEnabled = SpUtils.getBoolean(ThermalConst.Key.TEMPER_ENABLED,ThermalConst.Default.TEMPER_ENABLED);
        boolean fEnabled = SpUtils.getBoolean(ThermalConst.Key.THERMAL_F_ENABLED,ThermalConst.Default.THERMAL_F_ENABLED);
        if (mCurrWarningThreshold != warningThreshold || mFEnabled != fEnabled) {
            mCurrWarningThreshold = warningThreshold;
            mFEnabled = fEnabled;
            loadSignData();
        }

        boolean showMainInfo = SpUtils.getBoolean(ThermalConst.Key.SHOW_MAIN_INFO, ThermalConst.Default.SHOW_MAIN_INFO);
        if(showMainInfo){
            llInfoSignList.setVisibility(View.VISIBLE);
        } else {
            llInfoSignList.setVisibility(View.GONE);
        }

        if(rlv != null){
            boolean isMainSignList = SpUtils.getBoolean(Constants.Key.MAIN_SIGN_LIST,Constants.Default.MAIN_SIGN_LIST);
            if(isMainSignList){
                if(!rlv.isShown())
                    rlv.setVisibility(View.VISIBLE);
            } else {
                if(rlv.isShown())
                    rlv.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void connect() {
        Log.e(TAG, "connect: 网络已连接");
        if(mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT){
            tvNetState.setText(APP.getContext().getResources().getString(R.string.smt_main_net_normal2));
        } else {
            tvNetState.setText(APP.getContext().getResources().getString(R.string.smt_main_net_normal));
        }
        tvNetState.setTextColor(Color.GREEN);
    }

    @Override
    public void disConnect() {
        Log.e(TAG, "connect: 网络未连接");
        tvNetState.setText(APP.getContext().getResources().getString(R.string.smt_main_net_no));
        tvNetState.setTextColor(Color.RED);
    }

    private void loadSignData() {
        mSignList.clear();
        if(rlv != null){
            signAdapter.notifyDataSetChanged();
        }
        //加载已存在的记录
        List<Sign> todaySignData = SignManager.instance().getTodaySignData();
        if (todaySignData != null) {
            mSignList.addAll(todaySignData);
        }
        if(rlv != null){
            signAdapter.notifyDataSetChanged();
        }
        updateNumber();
    }

    /*private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (btnBulu != null) {
                btnBulu.setEnabled(false);
                btnBulu.setVisibility(View.GONE);
                aivBulu.setVisibility(View.VISIBLE);
            }
            SignManager.instance().startBulu();
        }
    };
*/
    private String mModel = "";

    public void setModelText(String model) {
        if (TextUtils.isEmpty(model)) {
            return;
        }
        mModel = model;
        if (tvModel != null) {
            tvModel.setText(mModel);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(UpdateInfoEvent event) {
        if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            //公告
            NoticeManager.getInstance().initSignData();
        }

        Company company = SpUtils.getCompany();
        String codeUrl = company.getCodeUrl();
        if(!TextUtils.isEmpty(codeUrl)){
            Glide.with(getActivity()).load(codeUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    gifImageView.setImageBitmap(resource);
                }
            });
        }

        if (tvCompanyName != null) {
            if (TextUtils.isEmpty(company.getComname())) {
                tvCompanyName.setText(APP.getContext().getResources().getString(R.string.fment_sign_bind_code) + SpUtils.getStr(SpUtils.BIND_CODE));
            } else {
                tvCompanyName.setText(APP.getContext().getResources().getString(R.string.fment_sign_company) + company.getComname());
            }
        }
        if (tvDeviceNo != null) {
            tvDeviceNo.setText(APP.getContext().getResources().getString(R.string.fment_sign_device_no) + SpUtils.getStr(SpUtils.DEVICE_NUMBER));
        }

        loadSignData();

        //自动清理服务
        ResourceCleanManager.instance().startAutoCleanService();
    }

    //设置饼图属性
    private void initPieChart() {
        if (pieChart == null) {
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

    public void addSignData(Sign sign) {
        mSignList.add(0, sign);
        if(rlv != null){
            signAdapter.notifyItemInserted(0);
            rlv.scrollToPosition(0);
        }
        updateNumber();
    }

    /*@Override
    public void onMakeUped(Sign sign, boolean makeUpSuccess) {
        mSignList.add(0, sign);
        signAdapter.notifyItemInserted(0);
        rlv.scrollToPosition(0);
        updateNumber();

        VipDialogManager.showBuluDialog(getMainActivity(), sign.getHeadPath(), makeUpSuccess);
        KDXFSpeechManager.instance().playText(makeUpSuccess ? getString(R.string.fment_sign_tip_blcg) + "！" : getString(R.string.fment_sign_tip_blsb) + "！");
        btnBulu.setEnabled(true);
        btnBulu.setVisibility(View.VISIBLE);
        aivBulu.setVisibility(View.GONE);
    }*/

    //更新签到列表
    private void updateNumber() {
        List<Sign> mNewSignList = removeDuplicateCase(mSignList);

        int male = 0;
        int female = 0;
        int total = 0;
        for (Sign signBean : mNewSignList) {
            if (signBean.getType() == -9) {
                continue;
            }
            total++;
            if (signBean.getSex() == 1) {
                male++;
            } else {
                female++;
            }
        }

        Company company = SpUtils.getCompany();
        List<User> users = DaoManager.get().queryUserByCompId(company.getComid());

        int totalUserNum = users == null ? 0 : users.size();
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            tvTotal.setText(totalUserNum + "");
            tvAlready.setText(total + "");
        } else {
            tvTotal.setText("" + total);
            tvSignMale.setText(getString(R.string.base_male) + ": " + male + getString(R.string.base_people));
            tvSignFemale.setText(getString(R.string.base_female) + ": " + female + getString(R.string.base_people));

            //设置饼图数据
            List<PieEntry> dataEntry = new ArrayList<>();
            List<Integer> dataColors = new ArrayList<>();

            if (male == 0 && female == 0) {
                dataEntry.add(new PieEntry(100, ""));
                dataColors.add(APP.getContext().getResources().getColor(R.color.white));
            } else {
                dataEntry.add(new PieEntry(male, getString(R.string.base_male)));
                dataEntry.add(new PieEntry(female, getString(R.string.base_female)));
                dataColors.add(APP.getContext().getResources().getColor(R.color.horizontal_chart_male));
                dataColors.add(APP.getContext().getResources().getColor(R.color.horizontal_chart_female));
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
                return (o1.getEmpId() + "").compareTo(o2.getEmpId() + "");
            }
        });
        set.addAll(cases);
        return new ArrayList<>(set);
    }


    protected void bindImageView(String urlOrPath, final ImageView iv) {
        if (TextUtils.isEmpty(urlOrPath)) {
            return;
        }
        Glide.with(getActivity()).load(urlOrPath).skipMemoryCache(true).crossFade(500).into(iv);
    }

    class SignAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        private List<Sign> signBeanList;
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
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.item_sign_thermal, null));
            } else {
                return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.item_sign_thermal_h, null));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ViewHolder vh = (ViewHolder) viewHolder;
            Sign signBean = signBeanList.get(i);
            vh.bindData(mContext, signBean);
        }

        @Override
        public int getItemCount() {
            return signBeanList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivHead;
            TextView tvName;
            TextView tvTime;
            TextView tvTemp;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivHead = itemView.findViewById(R.id.iv_head_item);
                tvName = itemView.findViewById(R.id.tv_name_item);
                tvTime = itemView.findViewById(R.id.tv_time_item);
                tvTemp = itemView.findViewById(R.id.tv_temp_item);
            }

            public void bindData(Context context, Sign signBean) {
                /*String headPath = signBean.getHeadPath();
                Bitmap imgBitmap = signBean.getImgBitmap();
                if(imgBitmap != null){
                    ivHead.setImageBitmap(imgBitmap);
                } else if(!TextUtils.isEmpty(headPath)){
                    Glide.with(mContext).load(headPath).asBitmap().override(60, 60).into(ivHead);
                }*/
                Glide.with(mContext).load(signBean.getHeadPath()).asBitmap().override(60, 60).into(ivHead);

                tvName.setText(signBean.getType() != -9 ? signBean.getName() : APP.getContext().getResources().getString(R.string.fment_sign_visitor_name));
                tvTime.setText(df.format(signBean.getTime()));

                String temper;
                if(mFEnabled){
                    temper = formatF((float) (signBean.getTemperature() * 1.8 + 32)) + "℉";
                } else {
                    temper = signBean.getTemperature() + "℃";
                }
                tvTemp.setText(temper);

                if ((faceEnabled && !temperEnabled) || signBean.getTemperature() == 0.0f) {
                    tvTemp.setVisibility(View.INVISIBLE);
                } else {
                    tvTemp.setVisibility(View.VISIBLE);
                }

                if (signBean.getTemperature() >= mCurrWarningThreshold) {
                    ivHead.setBackgroundResource(R.drawable.shape_record_img_bg_main_warning);
                    tvName.setTextColor(APP.getContext().getResources().getColor(R.color.horizontal_item_visitor_name_warning));
                    tvTime.setTextColor(APP.getContext().getResources().getColor(R.color.horizontal_item_visitor_name_warning));
                    tvTemp.setTextColor(APP.getContext().getResources().getColor(R.color.horizontal_item_visitor_name_warning));
                } else {
                    ivHead.setBackgroundResource(R.drawable.shape_record_img_bg_main_normal);
                    tvName.setTextColor(APP.getContext().getResources().getColor(R.color.horizontal_item_visitor_name_normal2));
                    tvTime.setTextColor(APP.getContext().getResources().getColor(R.color.horizontal_item_visitor_name_normal2));
                    tvTemp.setTextColor(APP.getContext().getResources().getColor(R.color.horizontal_item_visitor_name_normal2));
                }
            }
        }
    }

    private float formatF(float fValue) {
        return (float) (Math.round(fValue * 10)) / 10;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(netWorkChangReceiver != null){
            getActivity().unregisterReceiver(netWorkChangReceiver);
        }
        EventBus.getDefault().unregister(this);
    }
}
