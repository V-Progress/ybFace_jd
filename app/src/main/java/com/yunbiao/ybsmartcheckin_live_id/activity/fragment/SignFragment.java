package com.yunbiao.ybsmartcheckin_live_id.activity.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
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

public class SignFragment extends Fragment/* implements SignManager.SignEventListener*/ {

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
    private TextView tvModel;
    private int mCurrModel = -1;
    private float mCurrWarningThreshold = 0.0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        mCurrentOrientation = getActivity().getResources().getConfiguration().orientation;

        int orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (Constants.SCREEN_TYPE == Constants.ScreenType.TYPE_PORTRAIT_8_800_1280) {
                rootView = inflater.inflate(R.layout.fragment_sign_list_fake_landscape, container, false);
                orientation = LinearLayoutManager.HORIZONTAL;
                /*只在横屏初始化公告*/
                NoticeManager.getInstance().init(rootView);
            } else {
                rootView = inflater.inflate(R.layout.fragment_sign_list, container, false);
                orientation = LinearLayoutManager.HORIZONTAL;
            }
        } else {
            rootView = inflater.inflate(R.layout.fragment_sign_list_h, container, false);
            orientation = LinearLayoutManager.VERTICAL;
            /*只在横屏初始化公告*/
            NoticeManager.getInstance().init(rootView);
        }

        linearLayoutManager = new LinearLayoutManager(getActivity(), orientation, false);

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

        tvModel = rootView.findViewById(R.id.tv_model_sign);
        setModelText(mModel);

        //横屏专用
        pieChart = rootView.findViewById(R.id.pie_chart);
        tvSignMale = rootView.findViewById(R.id.tv_sign_number_male);
        tvSignFemale = rootView.findViewById(R.id.tv_sign_number_female);
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            rlv.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.right = 5;
                }
            });
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

        initPieChart();

        if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            //公告
            NoticeManager.getInstance().initSignData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume: 重加载数据");
        float warningThreshold = SpUtils.getFloat(SpUtils.TEMP_WARNING_THRESHOLD, Constants.DEFAULT_TEMP_WARNING_THRESHOLD_VALUE);
        int newModel = SpUtils.getIntOrDef(SpUtils.MODEL_SETTING, Constants.DEFAULT_TEMP_MODEL);
        if (newModel != mCurrModel || mCurrWarningThreshold != warningThreshold) {
            mCurrWarningThreshold = warningThreshold;
            mCurrModel = newModel;
            loadSignData();
        }

        boolean qrCodeEnabled = SpUtils.getBoolean(SpUtils.QRCODE_ENABLED, Constants.DEFAULT_QRCODE_ENABLED);
        ivQRCode.setVisibility(qrCodeEnabled ? View.VISIBLE : View.GONE);
    }

    private void loadSignData() {
        mSignList.clear();
        signAdapter.notifyDataSetChanged();
        //加载已存在的记录
        List<Sign> todaySignData = SignManager.instance().getTodaySignData();
        if (todaySignData != null) {
            mSignList.addAll(todaySignData);
        }

        Log.e(TAG, "loadSignData: " + mSignList.size());
        signAdapter.notifyDataSetChanged();
        updateNumber();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
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
        ImageFileLoader.i().loadAndSave(getActivity(), company.getCodeUrl(), Constants.DATA_PATH, ivQRCode);

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
        signAdapter.notifyItemInserted(0);
        rlv.scrollToPosition(0);
        updateNumber();
    }

    /*@Override
    public void onMakeUped(Sign sign, boolean makeUpSuccess) {
        mSignList.add(0, sign);
        signAdapter.notifyItemInserted(0);
        rlv.scrollToPosition(0);
        updateNumber();

        VipDialogManager.showBuluDialog(getActivity(), sign.getHeadPath(), makeUpSuccess);
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
            String signTips = "已到   <font color='#fff600'>" + total + "</font>   " + getString(R.string.base_people);
            String totalSex = "应到   <font color='#fff600'>" + totalUserNum + "</font>   " + getString(R.string.base_people);
//          String totalSex = "（" + getString(R.string.base_male) + " <font color='#fff600'>" + maleNum + "</font>   " + getString(R.string.base_female) + "  <font color='#fff600'>" + female + "</font> ）";
            tvTotal.setText(Html.fromHtml(signTips));
            tvTotalSex.setText(Html.fromHtml(totalSex));

            if (tvTotal1 != null) {
                tvTotal1.setText(total + "");
            }
            if (tvMale1 != null) {
                tvMale1.setText(male + "");
            }
            if (tvFemale1 != null) {
                tvFemale1.setText(female + "");
            }
        } else {
            tvTotal.setText("" + total);
            tvSignMale.setText(getString(R.string.base_male) + ": " + male + getString(R.string.base_people));
            tvSignFemale.setText(getString(R.string.base_female) + ": " + female + getString(R.string.base_people));

            //设置饼图数据
            List<PieEntry> dataEntry = new ArrayList<>();
            List<Integer> dataColors = new ArrayList<>();

            if (male == 0 && female == 0) {
                dataEntry.add(new PieEntry(100, ""));
                dataColors.add(getResources().getColor(R.color.white));
            } else {
                dataEntry.add(new PieEntry(male, getString(R.string.base_male)));
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
                return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.item_sign_main, null));
            } else {
                return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.item_sign_h, null));
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
                byte[] imgBytes = signBean.getImgBytes();
                String headPath = signBean.getHeadPath();
                Bitmap imgBitmap = signBean.getImgBitmap();
                if (imgBytes != null) {
                    Glide.with(mContext).load(imgBytes).asBitmap().override(100, 100).into(ivHead);
                } else if (!TextUtils.isEmpty(headPath)) {
                    Glide.with(mContext).load(headPath).asBitmap().override(100, 100).into(ivHead);
                } else if (imgBitmap != null) {
                    ivHead.setImageBitmap(imgBitmap);
                }

                tvName.setText(signBean.getType() != -9 ? signBean.getName() : "访客");
                tvTime.setText(df.format(signBean.getTime()));

                tvTemp.setText(signBean.getTemperature() + "℃");
                if (mCurrModel == Constants.Model.MODEL_FACE_ONLY || signBean.getTemperature() == 0.0f) {
                    tvTemp.setVisibility(View.INVISIBLE);
                } else {
                    tvTemp.setVisibility(View.VISIBLE);
                }

                if (signBean.getTemperature() >= mCurrWarningThreshold) {
                    ivHead.setBackgroundResource(R.drawable.shape_record_img_bg_main_warning);
                    tvName.setTextColor(mContext.getResources().getColor(R.color.horizontal_item_visitor_name_warning));
                    tvTime.setTextColor(mContext.getResources().getColor(R.color.horizontal_item_visitor_name_warning));
                    tvTemp.setTextColor(mContext.getResources().getColor(R.color.horizontal_item_visitor_name_warning));
                } else {
                    ivHead.setBackgroundResource(R.drawable.shape_record_img_bg_main_normal);
                    tvName.setTextColor(mContext.getResources().getColor(R.color.horizontal_item_visitor_name_normal2));
                    tvTime.setTextColor(mContext.getResources().getColor(R.color.horizontal_item_visitor_name_normal2));
                    tvTemp.setTextColor(mContext.getResources().getColor(R.color.horizontal_item_visitor_name_normal2));
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
