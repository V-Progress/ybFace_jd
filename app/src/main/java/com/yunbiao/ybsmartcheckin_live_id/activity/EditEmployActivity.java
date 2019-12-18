package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import com.yunbiao.faceview.CompareResult;
import com.yunbiao.faceview.FaceManager;
import com.yunbiao.faceview.FacePreviewInfo;
import com.yunbiao.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.Config;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.adapter.DepartAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.bean.AddStaffResponse;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    private View pbTakePhoto;
    private FaceView faceView;
    private RadioGroup rgSex;

    private List<String> departNames = new ArrayList<>();
    private List<Long> departIds = new ArrayList<>();

    private User mCurrUser = null;
    private User mTempUser = null;

    private int type;

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
        pbTakePhoto = findViewById(R.id.alv_take_photo);
        rgSex = findViewById(R.id.rg_sex);

        btn_TakePhoto.setOnClickListener(this);
        btn_ReTakePhoto.setOnClickListener(this);
        tv_birth.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
        iv_back.setOnClickListener(this);

        faceView.setCallback(faceCallback);
    }

    private int mHasFace = -1;
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {
            if (hasFace) {
                if (facePreviewInfoList != null) {
                    if (facePreviewInfoList.size() == 1) {
                        mHasFace = 1;
                    } else if (facePreviewInfoList.size() > 1) {
                        mHasFace = -2;
                    }
                } else {
                    mHasFace = -1;
                }
            } else {
                mHasFace = -1;
            }
        }

        @Override
        public void onFaceVerify(CompareResult faceAuth) {

        }
    };

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

        initDepart();

        //如果是修改，则只初始化部门
        if (type == TYPE_ADD) {
            d("类型：新增");
            initAddLogic();
        } else {
            d("类型：修改");
            initEditLogic();
        }
    }

    private void initDepart() {
        departNames.clear();
        departIds.clear();

        Company company = SpUtils.getCompany();
        List<Depart> departs = DaoManager.get().queryDepartByCompId(company.getComid());
        for (Depart depart : departs) {
            departNames.add(depart.getDepName());
            departIds.add(depart.getDepId());
        }
        DepartAdapter departAdapter = new DepartAdapter(this, departNames);
        sp_depart.setAdapter(departAdapter);
        Drawable drawable = getResources().getDrawable(R.drawable.shape_employ_button);
        sp_depart.setPopupBackgroundDrawable(drawable);
        sp_depart.setOnItemSelectedListener(onItemSelectedListener);
    }

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.e(TAG, "onItemSelected------> " + departIds.get(position));
            mTempUser.setDepartId(departIds.get(position));
            mTempUser.setDepartName(departNames.get(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    //初始化修改逻辑
    private void initEditLogic() {
        long entryId = getIntent().getLongExtra(KEY_ID, 0);
        if (entryId == 0) {
            finish();
            return;
        }
        mCurrUser = DaoManager.get().queryUserById(entryId);
        if (mCurrUser == null) {
            finish();
            return;
        }
        mTempUser = new User();
        mTempUser.setUser(mCurrUser);

        setUserInfo();
    }

    //初始化新增逻辑
    private void initAddLogic() {
        mTempUser = new User();
        mTempUser.setDepartName(departNames.get(0));
        mTempUser.setDepartId(departIds.get(0));

        setUserInfo();
    }

    //设置页面显示
    private void setUserInfo() {
        sp_depart.setSelection(departIds.indexOf(mTempUser.getDepartId()));
        rgSex.check(mTempUser.getSex() == -1 ? R.id.rb_male : (mTempUser.getSex() == 0 ? R.id.rb_female : R.id.rb_male));
        et_name.setText(TextUtils.isEmpty(mTempUser.getName()) ? "" : mTempUser.getName());
        et_num.setText(TextUtils.isEmpty(mTempUser.getNumber()) ? "" : mTempUser.getNumber());
        et_job.setText(TextUtils.isEmpty(mTempUser.getPosition()) ? "" : mTempUser.getPosition());
        et_sign.setText(TextUtils.isEmpty(mTempUser.getAutograph()) ? "" : mTempUser.getAutograph());
        tv_birth.setText(TextUtils.isEmpty(mTempUser.getBirthday()) ? "" : mTempUser.getBirthday());
        Glide.with(this).load(TextUtils.isEmpty(mTempUser.getHeadPath()) ? R.mipmap.avatar : mTempUser.getHeadPath()).asBitmap().into(iv_capture);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_TakePhoto:
                pbTakePhoto.setVisibility(View.VISIBLE);
                btn_TakePhoto.setVisibility(View.GONE);
                handler.sendEmptyMessage(0);
                break;
            case R.id.btn_ReTakePhoto:
                showDialog("是否重置所有信息？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetInfo();
                    }
                });
                break;
            case R.id.btn_submit:
                submit();
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

    private void submit() {
        if (TextUtils.isEmpty(mTempUser.getHeadPath())) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qxpz));
            return;
        }

        final String name = et_name.getText().toString();
        final String number = et_num.getText().toString();
        int checkSex = rgSex.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(name)) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qtxxm));
            return;
        }
        if (checkSex == -1) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qxzxb));
            return;
        }
        if (TextUtils.isEmpty(number)) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qtxbh));
            return;
        }

        if (type == TYPE_ADD) {
            addStaff();
        } else {
            updateStaff();
        }
    }

    private void updateStaff() {
        final String name = et_name.getText().toString();
        final String number = et_num.getText().toString();
        int checkSex = rgSex.getCheckedRadioButtonId();
        final String position = et_job.getText().toString();
        String birthday = tv_birth.getText().toString();
        final String autograph = et_sign.getText().toString();

        mTempUser.setName(name);
        mTempUser.setNumber(number);
        mTempUser.setSex(checkSex == R.id.rb_male ? 1 : 0);
        mTempUser.setPosition(position);
        mTempUser.setBirthday(birthday);
        mTempUser.setAutograph(autograph);

        if (mTempUser.equals(mCurrUser)) {
            UIUtils.showTitleTip(this, getString(R.string.edit_not_edit));
            return;
        }

        Log.e(TAG, "原User：" + mCurrUser.toString());
        Log.e(TAG, "后User：" + mTempUser.toString());
        submitParams(ResourceUpdate.UPDATSTAFF);
    }

    private void addStaff() {
        final String name = et_name.getText().toString();
        final String number = et_num.getText().toString();
        int checkSex = rgSex.getCheckedRadioButtonId();
        final String position = et_job.getText().toString();
        String birthday = tv_birth.getText().toString();
        final String autograph = et_sign.getText().toString();

        mTempUser.setName(name);
        mTempUser.setNumber(number);
        mTempUser.setSex(checkSex == R.id.rb_male ? 1 : 0);
        mTempUser.setPosition(position);
        mTempUser.setBirthday(birthday);
        mTempUser.setAutograph(autograph);
        mTempUser.setCompanyId(SpUtils.getCompany().getComid());

        UIUtils.showTitleTip(this, "可以提交添加了");
        Log.e(TAG, "后User：" + mTempUser.toString());
        submitParams(ResourceUpdate.ADDSTAFF);
    }

    private void submitParams(String url) {
        Map<String, String> params = new HashMap<>();
        params.put("depId", mTempUser.getDepartId() + "");
        params.put("name", mTempUser.getName());
        params.put("number", mTempUser.getNumber());
        params.put("sex", mTempUser.getSex() + "");

        if (!TextUtils.isEmpty(mTempUser.getAutograph())) {
            params.put("autograph", mTempUser.getAutograph());
        }
        if (!TextUtils.isEmpty(mTempUser.getPosition())) {
            params.put("position", mTempUser.getPosition());
        }
        if (!TextUtils.isEmpty(mTempUser.getBirthday())) {
            params.put("birthday", mTempUser.getBirthday());
        }

        PostFormBuilder builder = OkHttpUtils.post().url(url);
        if (type == TYPE_ADD) {
            params.put("comId",mTempUser.getCompanyId()+"");
            File file = new File(mTempUser.getHeadPath());
            builder.addFile("head", file.getName(), file);
        } else {
            params.put("id", mTempUser.getId()+"");
            if (!TextUtils.equals(mCurrUser.getHeadPath(), mTempUser.getHeadPath())) {
                Log.e(TAG, "submitParams: 头像不相同，需要更新头像");
                File file = new File(mTempUser.getHeadPath());
                builder.addFile("head", file.getName(), file);
            } else {
                File file = new File(Environment.getExternalStorageDirectory(),"1.txt");
                if(!file.exists()){
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                builder.addFile("head", file.getName(), file);
            }
        }

        d("地址：" + url);
        d("参数：" + params.toString());
        builder.params(params).build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                d("开始提交");
                UIUtils.showNetLoading(EditEmployActivity.this);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                d("提交失败：" + (e == null ? "NULL" : e.getMessage()));
                UIUtils.showTitleTip(EditEmployActivity.this, "提交失败：" + (e == null ? "NULL" : e.getClass().getSimpleName() + ": " + e.getMessage()));
                UIUtils.dismissNetLoading();
            }

            @Override
            public void onResponse(String response, int id) {
                d(response);
                AddStaffResponse addStaffResponse = new Gson().fromJson(response, AddStaffResponse.class);
                if (addStaffResponse.getStatus() != 1) {
                    String errMsg;
                    switch (addStaffResponse.getStatus()) {
                        case 2://添加失败
                            errMsg = getString(R.string.act_editEmploy_tip_tjsb);
                            break;
                        case 3://员工不存在
                            errMsg = getString(R.string.act_editEmploy_tip_gygbcz);
                            break;
                        case 6://部门不存在
                            errMsg = getString(R.string.act_editEmploy_tip_bczgbm);
                            break;
                        case 7://不存在公司部门关系
                            errMsg = getString(R.string.act_editEmploy_tip_gsmyzgbm);
                            break;
                        case 8://不存在员工的公司部门信息
                            errMsg = getString(R.string.act_editEmploy_tip_gsmyzgbm);
                            break;
                        default://参数错误
                            errMsg = getString(R.string.act_editEmploy_tip_cscw);
                            break;
                    }
                    UIUtils.showTitleTip(EditEmployActivity.this, "" + errMsg);
                    UIUtils.dismissNetLoading();
                    return;
                }

                if (type == TYPE_ADD) {
                    mTempUser.setId(addStaffResponse.getEntryId());
                    mTempUser.setFaceId(addStaffResponse.getFaceId());
                    long l = DaoManager.get().addOrUpdate(mTempUser);
                    Log.e(TAG, "onResponse: 添加用户库" + l);
                    boolean b = FaceManager.getInstance().addUser(mTempUser.getFaceId(), mTempUser.getHeadPath());
                    Log.e(TAG, "onResponse: 添加人脸库：" + b);
                } else {
                    long l = DaoManager.get().addOrUpdate(mTempUser);
                    Log.e(TAG, "onResponse: 更新用户库：" + l);
                    //判断是否需要更新头像
                    if (!TextUtils.equals(mTempUser.getHeadPath(), mCurrUser.getHeadPath())) {
                        boolean b = FaceManager.getInstance().addUser(mTempUser.getFaceId(), mTempUser.getHeadPath());
                        Log.e(TAG, "onResponse: 更新人脸库：" + b);
                    }
                }

                faceView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UIUtils.dismissNetLoading();
                        finish();
                    }
                }, 1 * 1000);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mHasFace == -1) {
                UIUtils.showTitleTip(EditEmployActivity.this, "未检测到人脸");
                pbTakePhoto.setVisibility(View.GONE);
                btn_TakePhoto.setVisibility(View.VISIBLE);
                return;
            } else if (mHasFace == -2) {
                UIUtils.showTitleTip(EditEmployActivity.this, "请保持屏幕中只有一张人脸");
                pbTakePhoto.setVisibility(View.GONE);
                btn_TakePhoto.setVisibility(View.VISIBLE);
                return;
            }

            Bitmap bitmap = faceView.takePicture();
            if (bitmap != null) {
                String headPath = saveBitmap(bitmap);
                mTempUser.setHeadPath(headPath);
                Glide.with(EditEmployActivity.this).load(mTempUser.getHeadPath()).asBitmap().override(100, 100).into(iv_capture);
            } else {
                UIUtils.showTitleTip(EditEmployActivity.this, "失败，请重试");
            }

            pbTakePhoto.setVisibility(View.GONE);
            btn_TakePhoto.setVisibility(View.VISIBLE);
        }
    };

    private void resetInfo() {
        if (type == TYPE_ADD) {
            mTempUser = new User();
        } else {
            mTempUser.setUser(mCurrUser);
        }
        setUserInfo();
    }

    private void showDialog(String msg, DialogInterface.OnClickListener confirm) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.base_tip) + "!");
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.base_ensure), confirm);
        builder.setNegativeButton(getString(R.string.base_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /***
     * 保存图片到本地
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Bitmap mBitmap) {
        File filePic;
        try {
            //格式化时间
            long time = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(Constants.HEAD_PATH + sdfTime + "_m.jpg");
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

    private void showCalendar() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String birthDay = year + "-" + (month + 1) + "-" + dayOfMonth;
                tv_birth.setText(birthDay);
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


   /* private void checkAddInfo() {
        if (TextUtils.isEmpty(mCurrHeadPath)) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qxpz));
            return;
        }

        final String name = et_name.getText().toString();
        final String position = et_job.getText().toString();
        final String number = et_num.getText().toString();
        final String autograph = et_sign.getText().toString();
        String birthday = tv_birth.getText().toString();
        int checkSex = rgSex.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(name)) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qtxxm));
            return;
        }
        if (checkSex == -1) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qxzxb));
            return;
        }
        if (TextUtils.isEmpty(number)) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qtxbh));
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
        Log.e(TAG, "mCurrHeadPath------------->"+mCurrHeadPath);
        if (!headFile.exists()) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_wzdtxwjqcxpzhzs));
            return;
        }
        params.put("headName",headFile.getName());
        Log.e(TAG, "headName------------>"+headFile.getName());
        submitEdit(ResourceUpdate.ADDSTAFF, params, headFile);
    }*/
/*

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
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qxpz));
            return;
        }

        final String name = et_name.getText().toString();
        final String position = et_job.getText().toString();
        final String number = et_num.getText().toString();
        final String autograph = et_sign.getText().toString();
        String birthday = tv_birth.getText().toString();
        int checkSex = rgSex.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(name)) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qtxxm));
            return;
        }
        if (checkSex == -1) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qxzxb));
            return;
        }
        if (TextUtils.isEmpty(number)) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_qtxbh));
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
        Log.e(TAG, "mCurrHeadPath------------->"+mCurrHeadPath);
        if (!headFile.exists()) {
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_wzdtxwjqcxpzhzs));
            return;
        }
        params.put("headName",headFile.getName());
        Log.e(TAG, "headName------------>"+headFile.getName());
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
            UIUtils.showShort(this, getString(R.string.act_editEmploy_tip_wzrhxg));
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

    private void submitEdit(String url, final Map<String, String> params, final File headFile) {
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
                        UIUtils.showTitleTip(EditEmployActivity.this, getString(R.string.act_editEmploy_tip_tjsb)+"：\n" + (e != null ? e.getMessage() : "NULL"));
                        UIUtils.dismissNetLoading();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        d(response);
                        AddStaffResponse addStaffResponse = new Gson().fromJson(response, AddStaffResponse.class);
                        if (addStaffResponse.getStatus() != 1) {
                            String errMsg;
                            switch (addStaffResponse.getStatus()) {
                                case 2://添加失败
                                    errMsg = getString(R.string.act_editEmploy_tip_tjsb);
                                    break;
                                case 3://员工不存在
                                    errMsg = getString(R.string.act_editEmploy_tip_gygbcz);
                                    break;
                                case 6://部门不存在
                                    errMsg = getString(R.string.act_editEmploy_tip_bczgbm);
                                    break;
                                case 7://不存在公司部门关系
                                    errMsg =getString(R.string.act_editEmploy_tip_gsmyzgbm);
                                    break;
                                case 8://不存在员工的公司部门信息
                                    errMsg = getString(R.string.act_editEmploy_tip_gsmyzgbm);
                                    break;
                                default://参数错误
                                    errMsg = getString(R.string.act_editEmploy_tip_cscw);
                                    break;
                            }
                            UIUtils.showTitleTip(EditEmployActivity.this, "" + errMsg);
                            UIUtils.dismissNetLoading();
                            return;
                        }

                        if (type == TYPE_ADD) {
                            User user = new User();
                            user.setId(addStaffResponse.getEntryId());
                            user.setFaceId(addStaffResponse.getFaceId());
                            user.setName(params.get("name"));

                            user.setDepartId(Long.parseLong(params.get("depId")));
                            user.setCompanyId(Integer.parseInt(params.get("comId")));
                            user.setSex(Integer.parseInt(params.get("sex")));
                            user.setNumber(params.get("number"));
                            user.setAutograph(params.get("autograph"));
                            user.setPosition(params.get("position"));
                            user.setBirthday(params.get("birthday"));
                            user.setHeadPath(headFile.getPath());
                            long l = DaoManager.get().addOrUpdate(user);
                            Log.e(TAG, "onResponse: 添加用户库" + l);

                            boolean b = FaceManager.getInstance().addUser(user.getFaceId(), user.getHeadPath());
                            Log.e(TAG, "onResponse: 添加人脸库：" + b);
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
                        if(isFinishing()){
                            return;
                        }
                    }
                });
    }



    */

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
    protected void onDestroy() {
        super.onDestroy();
        faceView.destory();
        UIUtils.dismissNetLoading();
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
