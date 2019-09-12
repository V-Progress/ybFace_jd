package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.Config;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.DepartAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.BaseResponse;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.faceview.face_new.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import timber.log.Timber;


/**
 * Created by Administrator on 2018/8/7.
 */

public class EditEmployActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "EditEmployActivity";
    public static final String KEY_ID = "entryId";
    public static final String KEY_TYPE = "type";
    public static final int TYPE_ADD = 0;
    public static final int TYPE_EDIT = 1;

    private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String SCREEN_BASE_PATH = sdPath + "/mnt/sdcard/photo/";

    private Button btn_submit;
    private Button btn_TakePhoto;
    private Button btn_ReTakePhoto;
    private Button btn_cancle;
    private ImageView iv_back;

    private ImageView iv_capture;
    private EditText et_name;
    private EditText et_num;
    private Spinner sp_depart;
    private EditText et_sign;
    private EditText et_job;
    private TextView tv_birth;

    private TextView tv_takephoto_time;
    private TextView tv_takephoto_tips;
    private View pbTakePhoto;
    private FaceView faceView;

    private List<String> departNames = new ArrayList<>();
    private List<Long> departIds = new ArrayList<>();
    private long mCurrDepartId = 0;
    private User mCurrUser = null;
    private String mCurrHeadPath = null;
    private int type;
    private RadioGroup rgSex;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_editemploy;
    }

    @Override
    protected int getLandscapeLayout() {
        if (Config.deviceType == Config.DEVICE_SMALL_FACE) {
            return R.layout.activity_editemploy_h_small;
        } else {
            return R.layout.activity_editemploy_h;
        }
    }

    @Override
    protected void initView() {
        faceView = findViewById(R.id.face_view);
        et_name = findViewById(R.id.et_name);
        sp_depart = findViewById(R.id.sp_depart);
        et_sign = findViewById(R.id.et_sign);
        et_job = findViewById(R.id.et_job);
        et_num = findViewById(R.id.et_num);
        tv_birth = findViewById(R.id.tv_birth);
        btn_submit = findViewById(R.id.btn_submit);
        iv_capture = findViewById(R.id.iv_capture);
        btn_TakePhoto = findViewById(R.id.btn_TakePhoto);
        btn_cancle = findViewById(R.id.btn_cancle);
        btn_ReTakePhoto = findViewById(R.id.btn_ReTakePhoto);
        iv_back = findViewById(R.id.iv_back);
        tv_takephoto_time = findViewById(R.id.tv_takephoto_time);
        tv_takephoto_tips = findViewById(R.id.tv_takephoto_tips);
        pbTakePhoto = findViewById(R.id.alv_take_photo);
        rgSex = findViewById(R.id.rg_sex);

        btn_TakePhoto.setOnClickListener(this);
        btn_ReTakePhoto.setOnClickListener(this);
        tv_birth.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        if (getIntent() == null) {
            finish();
            return;
        }
        type = getIntent().getIntExtra(KEY_TYPE, -1);
        if (type == -1) {
            finish();
            return;
        }
        //如果是修改，则只初始化部门
        if (type == TYPE_ADD) {
            d("类型：新增");
            initDepart(0);
            return;
        }

        d("类型：修改");
        long entryId = getIntent().getLongExtra(KEY_ID, 0);
        if (entryId == 0) {
            finish();
            return;
        }
        initUser(entryId);
        initDepart(departIds.indexOf(mCurrDepartId));
        setUserInfo();
    }

    private void initUser(long entryId) {
        User user = DaoManager.get().queryUserById(entryId);
        if (user == null) {
            finish();
            return;
        }
        mCurrUser = user;
        mCurrDepartId = mCurrUser.getDepartId();
        mCurrHeadPath = user.getHeadPath();
    }

    private void initDepart(int index) {
        departNames.clear();
        departIds.clear();
        List<Depart> departs = DaoManager.get().queryAll(Depart.class);
        for (Depart depart : departs) {
            departNames.add(depart.getDepName());
            departIds.add(depart.getDepId());
        }
        DepartAdapter departAdapter = new DepartAdapter(this, departNames);
        sp_depart.setAdapter(departAdapter);
        Drawable drawable = getResources().getDrawable(R.drawable.shape_employ_button);
        sp_depart.setPopupBackgroundDrawable(drawable);
        sp_depart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemSelected------> " + departIds.get(position));
                mCurrDepartId = departIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sp_depart.setSelection(index);
    }

    private void setUserInfo() {
        Glide.with(this).load(mCurrHeadPath).asBitmap().into(iv_capture);
        et_name.setText(mCurrUser.getName());
        et_num.setText(mCurrUser.getNumber());
        et_job.setText(mCurrUser.getPosition());
        et_sign.setText(mCurrUser.getAutograph());
        rgSex.check(mCurrUser.getSex() == 0 ? R.id.rb_female : R.id.rb_male);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            byte[] faceImageBytes = faceView.getFaceImage();
            if (faceImageBytes == null || faceImageBytes.length <= 0) {
                handler.sendEmptyMessageDelayed(0, 200);
                return;
            }
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap faceImage = BitmapFactory.decodeByteArray(faceImageBytes, 0, faceImageBytes.length, options);
            mCurrHeadPath = saveBitmap(faceImage);
            iv_capture.setImageBitmap(faceImage);

            pbTakePhoto.setVisibility(View.GONE);
            btn_TakePhoto.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_TakePhoto:
                pbTakePhoto.setVisibility(View.VISIBLE);
                btn_TakePhoto.setVisibility(View.GONE);
                handler.sendEmptyMessage(0);
                break;
            case R.id.btn_ReTakePhoto:
                reset();
                break;
            case R.id.btn_submit:
                if (type == TYPE_ADD) {
                    checkAddInfo();
                } else {
                    checkEditInfo();
                }
                break;
            case R.id.tv_birth:
                showCalendar();
                break;
            case R.id.btn_cancle:
            case R.id.iv_back:
                finish();
                break;

        }
    }

    private void reset() {
        mCurrHeadPath = null;
        tv_takephoto_tips.setText("");
        tv_takephoto_time.setText("");
        pbTakePhoto.setVisibility(View.GONE);
        btn_TakePhoto.setVisibility(View.VISIBLE);
        iv_capture.setImageResource(R.mipmap.avatar);
    }

    private void showCalendar() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                tv_birth.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void checkAddInfo() {
        if (TextUtils.isEmpty(mCurrHeadPath)) {
            UIUtils.showShort(this, "请先拍照");
            return;
        }

        final String name = et_name.getText().toString();
        final String position = et_job.getText().toString();
        final String number = et_num.getText().toString();
        final String autograph = et_sign.getText().toString();
        String birthday = tv_birth.getText().toString();
        int checkSex = rgSex.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(name)) {
            UIUtils.showShort(this, "请填写姓名");
            return;
        }
        if (checkSex == -1) {
            UIUtils.showShort(this, "请选择性别");
            return;
        }
        if (TextUtils.isEmpty(number)) {
            UIUtils.showShort(this, "请填写编号");
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("depId", mCurrDepartId + "");
        params.put("comId", SpUtils.getInt(SpUtils.COMPANYID) + "");
        params.put("sex", checkSex == R.id.rb_male ? "1" : "0");
        params.put("number",number);
        if (!TextUtils.isEmpty(autograph)) {
            params.put("autograph", autograph);
        }
        if (!TextUtils.isEmpty(position)) {
            params.put("position", position);
        }
        if(!TextUtils.isEmpty(birthday)){
            params.put("birthday",birthday);
        }
        File headFile = new File(mCurrHeadPath);
        if (!headFile.exists()) {
            UIUtils.showShort(this, "未找到头像文件，请重新拍照后再试");
            return;
        }

        submitEdit(ResourceUpdate.ADDSTAFF, params, headFile);
    }

    private void checkEditInfo() {
        final String name = et_name.getText().toString();
        final String position = et_job.getText().toString();
        final String number = et_num.getText().toString();
        final String autograph = et_sign.getText().toString();
        String birthday = tv_birth.getText().toString();
        int sex = rgSex.getCheckedRadioButtonId() == R.id.rb_male ? 1 : 0;

        Map<String, String> params = new HashMap<>();
        if (!TextUtils.isEmpty(name) && !TextUtils.equals(name, mCurrUser.getName())) {
            params.put("name", name);
        }
        if(sex != mCurrUser.getSex()){
            params.put("sex", sex+"");
        }
        if (!TextUtils.isEmpty(position) && !TextUtils.equals(position, mCurrUser.getPosition())) {
            params.put("position", position);
        }
        if (!TextUtils.isEmpty(number) && !TextUtils.equals(number, mCurrUser.getNumber())) {
            params.put("number", number);
        }
        if (!TextUtils.isEmpty(autograph) && !TextUtils.equals(autograph, mCurrUser.getAutograph())) {
            params.put("autograph", autograph);
        }
        if (!TextUtils.isEmpty(birthday) && !TextUtils.equals(birthday, mCurrUser.getBirthday())) {
            params.put("birthday", birthday);
        }

        final boolean isHeadNotEdit = TextUtils.equals(mCurrHeadPath, mCurrUser.getHeadPath());
        if (!TextUtils.isEmpty(mCurrHeadPath) && !isHeadNotEdit) {
            params.put("autograph", autograph);
        }

        if (mCurrDepartId != mCurrUser.getDepartId()) {
            params.put("depId", mCurrUser.getDepartId() + "");
        }

        if (params.size() <= 0) {
            UIUtils.showShort(this, "未做任何修改");
            return;
        }
        params.put("id", mCurrUser.getId() + "");
        params.put("depId", mCurrUser.getDepartId() + "");

        File headFile;
        if (isHeadNotEdit) {
            headFile = new File(Environment.getExternalStorageDirectory() + "/1.txt");
            if (!headFile.exists()) {
                try {
                    headFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            headFile = new File(mCurrHeadPath);
        }

        submitEdit(ResourceUpdate.UPDATSTAFF, params, headFile);
    }

    private void submitEdit(String url, Map<String, String> params, File headFile) {
        d("地址：" + url);
        d("参数：" + params.toString());
        d("头像：" + headFile.getPath());
        OkHttpUtils.post()
                .url(url)
                .params(params)
                .addFile("head", headFile.getName(), headFile)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        btn_submit.setEnabled(false);
                        UIUtils.showNetLoading(EditEmployActivity.this);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d(e);
                        UIUtils.showTitleTip(EditEmployActivity.this, "添加失败：\n" + (e != null ? e.getMessage() : "NULL"));
                        UIUtils.dismissNetLoading();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        d(response);
                        BaseResponse baseResponse = new Gson().fromJson(response, BaseResponse.class);
                        if (baseResponse.getStatus() != 1) {
                            String errMsg;
                            switch (baseResponse.getStatus()) {
                                case 2://添加失败
                                    errMsg = "添加失败";
                                    break;
                                case 3://员工不存在
                                    errMsg = "该员工不存在";
                                    break;
                                case 6://部门不存在
                                    errMsg = "不存在该部门";
                                    break;
                                case 7://不存在公司部门关系
                                    errMsg = "公司没有这个部门";
                                    break;
                                case 8://不存在员工的公司部门信息
                                    errMsg = "公司没有这位员工";
                                    break;
                                default://参数错误
                                    errMsg = "参数错误";
                                    break;
                            }
                            UIUtils.showTitleTip(EditEmployActivity.this, "" + errMsg);
                            return;
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    }

                    @Override
                    public void onAfter(int id) {
                        btn_submit.setEnabled(true);
                        UIUtils.dismissNetLoading();
                    }
                });
    }

    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Bitmap mBitmap) {
        String savePath;
        File filePic;

        try {
            //格式化时间
            long time = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(SCREEN_BASE_PATH + sdfTime + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }

    @Override
    protected void onResume() {
        super.onResume();
        faceView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        faceView.pause();
    }

    @Override
    protected void d(String log) {
        Timber.tag(getClass().getSimpleName());
        Timber.d(log);
    }

    private void d(Throwable t) {
        Timber.tag(getClass().getSimpleName());
        Timber.d(t);
    }
}
