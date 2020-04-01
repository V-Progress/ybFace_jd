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
import android.view.animation.Animation;
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
    private TextView tvTitle;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_editemploy;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_editemploy_h;
    }

    private Animation animation;

    @Override
    protected void initView() {
        tvTitle = findViewById(R.id.tv_title_edt);
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
        public boolean onFaceDetection(boolean hasFace, FacePreviewInfo facePreviewInfo) {
            if (hasFace) {
                mHasFace = facePreviewInfo != null ? 1 : -1;
            } else {
                mHasFace = -1;
            }
            return false;
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

            tvTitle.setText(getResources().getString(R.string.act_editEmploy_zjyg));
            d("类型：新增");
            initAddLogic();
        } else {
            tvTitle.setText(getResources().getString(R.string.act_editEmploy_xgxx));
            d("类型：修改");
            initEditLogic();
        }
    }

    private String departName = "";
    private long departId = -1;

    //初始化新增逻辑
    private void initAddLogic() {
        if (departNames.size() > 0) {
            departName = departNames.get(0);
        }
        if (departIds.size() > 0) {
            departId = departIds.get(0);
        }
        rgSex.check(R.id.rb_male);
    }

    private void submitAddUser() {
        if (TextUtils.isEmpty(mCurrPhotoPath)) {
            UIUtils.showShort(this, "请拍照");
            return;
        }

        String name = et_name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            UIUtils.showShort(this, "请输入姓名");
            return;
        }

        // TODO: 2020/3/18 离线功能
        int comid = SpUtils.getCompany().getComid();
        if(comid != Constants.NOT_BIND_COMPANY_ID){
            if (departId == -1) {
                UIUtils.showShort(this, "请选择部门");
                return;
            }
        }

        int sex;
        int checkedRadioButtonId = rgSex.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.rb_male) {
            sex = 1;
        } else {
            sex = 0;
        }

        String number = et_num.getText().toString();
        if (TextUtils.isEmpty(number)) {
            UIUtils.showShort(this, "请输入编号");
            return;
        }

        String birthday = tv_birth.getText().toString();
        String position = et_job.getText().toString();
        String signature = et_sign.getText().toString();

        final User addUser = new User();
        addUser.setDepartId(departId);
        addUser.setNumber(number);
        addUser.setSex(sex);
        addUser.setDepartName(departName);
        addUser.setName(name);
        addUser.setHeadPath(mCurrPhotoPath);
        addUser.setCompanyId(comid);
        addUser.setBirthday(birthday);
        addUser.setPosition(position);
        addUser.setAutograph(signature);

        // TODO: 2020/3/18 离线功能 
        if (comid == Constants.NOT_BIND_COMPANY_ID) {
            UIUtils.showNetLoading(EditEmployActivity.this);
            List<User> users = DaoManager.get().queryAll(User.class);
            //第一次进入没有绑定过公司的情况
            if (users == null || users.size() <= 0) {
                addUser.setId(1);
                addUser.setFaceId("1");
            } else {
                long id = 1;
                long faceId = 1;
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    //先取出最大的Id
                    if (user.getId() > id) {
                        id = user.getId();
                    }
                    //再取出最大的FaceId
                    String face = user.getFaceId();
                    int i1 = Integer.parseInt(face);
                    if (i1 > faceId) {
                        faceId = i1;
                    }
                }
                id += 1;
                faceId += 1;
                addUser.setId(id);
                addUser.setFaceId(faceId + "");
            }

            Log.e(TAG, "submitAddUser: " + addUser.getHeadPath());

            boolean b = FaceManager.getInstance().addUser(addUser.getFaceId(), addUser.getHeadPath());
            if (b) {
                DaoManager.get().add(addUser);
                UIUtils.showShort(EditEmployActivity.this, "添加成功");
            } else {
                UIUtils.showShort(EditEmployActivity.this, "添加人脸库失败");
            }
            UIUtils.dismissNetLoading();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("depId", addUser.getDepartId() + "");
        params.put("name", addUser.getName());
        params.put("number", addUser.getNumber());
        params.put("sex", addUser.getSex() + "");
        params.put("comId", addUser.getCompanyId() + "");

        if (!TextUtils.isEmpty(addUser.getAutograph())) {
            params.put("autograph", addUser.getAutograph());
        }
        if (!TextUtils.isEmpty(addUser.getPosition())) {
            params.put("position", addUser.getPosition());
        }
        if (!TextUtils.isEmpty(addUser.getBirthday())) {
            params.put("birthday", addUser.getBirthday());
        }

        File file = new File(addUser.getHeadPath());
        params.put("headName", file.getName());

        String addstaff = ResourceUpdate.ADDSTAFF;
        OkHttpUtils.post()
                .url(addstaff)
                .params(params)
                .addFile("head", file.getName(), file)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
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

                        addUser.setId(addStaffResponse.getEntryId());
                        addUser.setFaceId(addStaffResponse.getFaceId());

                        boolean b = FaceManager.getInstance().addUser(addUser.getFaceId(), addUser.getHeadPath());
                        if (!b) {
                            UIUtils.showShort(EditEmployActivity.this, "添加人脸库失败");
                        } else {
                            DaoManager.get().add(addUser);
                        }
                        faceView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                UIUtils.dismissNetLoading();
                                finish();
                            }
                        }, 1000);
                    }
                });


    }

    private void initDepart() {
        departNames.clear();
        departIds.clear();

        Company company = SpUtils.getCompany();
        List<Depart> departs = DaoManager.get().queryDepartByCompId(company.getComid());
        if (departs == null || departs.size() <= 0) {
            UIUtils.showShort(EditEmployActivity.this, "请先设置部门");
        }

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
            departId = departIds.get(position);
            departName = departNames.get(position);

            mUpdateDepartId = departIds.get(position);
            mUpdateDepartName = departNames.get(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
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
                showDialog("是否重置所有信息？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetInfo();
                    }
                });
                break;
            case R.id.tv_birth:
                showCalendar();
                break;
            case R.id.btn_submit:
                if (type == TYPE_ADD) {
                    submitAddUser();
                } else {
                    submitUpdateUser();
                }
                break;
            case R.id.btn_cancle:
            case R.id.iv_back:
                finish();
                break;
        }
    }

    private void resetInfo() {

    }

    private String mUpdatePhotoPath;
    private User user;
    private long mUpdateDepartId = -1;
    private String mUpdateDepartName;

    private void initEditLogic() {
        long userId = getIntent().getLongExtra(EditEmployActivity.KEY_ID, -1);
        if (userId == -1) {
            UIUtils.showShort(this, "未知错误");
            finish();
            return;
        }

        user = DaoManager.get().queryUserById(userId);
        if (user == null) {
            UIUtils.showShort(this, "用户不存在");
            finish();
            return;
        }

        et_name.setText(user.getName());
        et_sign.setText(user.getAutograph());
        rgSex.check(user.getSex() == 1 ? R.id.rb_male : R.id.rb_female);
        et_job.setText(user.getPosition());
        et_num.setText(user.getNumber());
        Glide.with(this).load(user.getHeadPath()).asBitmap().into(iv_capture);
        mUpdatePhotoPath = user.getHeadPath();

        long departId = user.getDepartId();
        mUpdateDepartId = departId;
        int index = departIds.indexOf(departId);
        sp_depart.setSelection(index);
    }

    private void submitUpdateUser() {
        if (TextUtils.isEmpty(mUpdatePhotoPath)) {
            UIUtils.showShort(this, "请拍照");
            return;
        }
        final String name = et_name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            UIUtils.showShort(this, "请输入姓名");
            return;
        }
        int checkedRadioButtonId = rgSex.getCheckedRadioButtonId();
        final int sex = checkedRadioButtonId == R.id.rb_male ? 1 : 0;

        // TODO: 2020/3/18 离线功能
        if(user.getCompanyId() != Constants.NOT_BIND_COMPANY_ID){
            if (departId == -1) {
                UIUtils.showShort(this, "请选择部门");
                return;
            }
        }

        final String number = et_num.getText().toString();
        if (TextUtils.isEmpty(number)) {
            UIUtils.showShort(this, "请输入编号");
            return;
        }

        final String birthday = tv_birth.getText().toString();

        final String position = et_job.getText().toString();

        final String sign = et_sign.getText().toString();

        final File currFile = new File(mUpdatePhotoPath);
        File oldFile = new File(user.getHeadPath());
        final boolean isHeadUpdated = !TextUtils.equals(currFile.getName(), oldFile.getName());

        // TODO: 2020/3/18 离线功能
        if(user.getCompanyId() == Constants.NOT_BIND_COMPANY_ID){
            UIUtils.showNetLoading(this);
            user.setName(name);
            user.setSex(sex);
            user.setDepartId(mUpdateDepartId);
            user.setDepartName(mUpdateDepartName);
            user.setNumber(number);
            user.setPosition(position);
            user.setBirthday(birthday);
            user.setAutograph(sign);

            if(isHeadUpdated){
                user.setHeadPath(currFile.getPath());
                boolean b1 = FaceManager.getInstance().removeUser(user.getFaceId());
                Log.e(TAG, "submitUpdateUser: 删除旧特征：" + b1);
                boolean b = FaceManager.getInstance().addUser(user.getFaceId(), user.getHeadPath());
                if(b){
                    DaoManager.get().addOrUpdate(user);
                    UIUtils.showShort(this,"更新成功");
                } else {
                    UIUtils.showShort(this,"更新头像失败");
                }
            } else {
                DaoManager.get().addOrUpdate(user);
                UIUtils.showShort(this,"更新信息成功");
            }

            UIUtils.dismissNetLoading();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("id", user.getId() + "");
        params.put("depId", mUpdateDepartId + "");

        if (!TextUtils.equals(name, user.getName())) {
            params.put("name", name);
        }
        if (sex != user.getSex()) {
            params.put("sex", sex + "");
        }
        if (!TextUtils.equals(position, user.getPosition())) {
            params.put("position", position);
        }
        if (!TextUtils.equals(birthday, user.getBirthday())) {
            params.put("birthday", birthday);
        }
        if (!TextUtils.equals(sign, user.getAutograph())) {
            params.put("autograph", sign);
        }
        if (!TextUtils.equals(number, user.getNumber())) {
            params.put("number", number);
        }

        PostFormBuilder builder = OkHttpUtils.post().url(ResourceUpdate.UPDATSTAFF).params(params);
        if (isHeadUpdated) {
            params.put("headName", currFile.getName());
            builder.addFile("head", currFile.getName(), currFile);
        } else {
            File file = new File(Environment.getExternalStorageDirectory(), "1.txt");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            builder.addFile("head", file.getName(), file);
        }

        builder.build().execute(new StringCallback() {
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

                user.setHeadPath(currFile.getPath());
                user.setName(name);
                user.setSex(sex);
                user.setDepartId(mUpdateDepartId);
                user.setDepartName(mUpdateDepartName);
                user.setNumber(number);
                user.setPosition(position);
                user.setBirthday(birthday);
                user.setAutograph(sign);

                //判断是否需要更新头像
                if (isHeadUpdated) {
                    boolean b = FaceManager.getInstance().addUser(user.getFaceId(), user.getHeadPath());
                    if (!b) {
                        UIUtils.showShort(EditEmployActivity.this, "更新人脸库失败");
                    } else {

                        long l = DaoManager.get().addOrUpdate(user);
                        Log.e(TAG, "onResponse: 更新用户库：" + l);
                    }
                } else {
                    long l = DaoManager.get().addOrUpdate(user);
                    Log.e(TAG, "onResponse: 更新用户库：" + l);
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

    private String mCurrPhotoPath;

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
                mCurrPhotoPath = saveBitmap(bitmap);
                mUpdatePhotoPath = mCurrPhotoPath;
                Glide.with(EditEmployActivity.this).load(mCurrPhotoPath).asBitmap().override(100, 100).into(iv_capture);
            } else {
                UIUtils.showTitleTip(EditEmployActivity.this, "失败，请重试");
            }

            pbTakePhoto.setVisibility(View.GONE);
            btn_TakePhoto.setVisibility(View.VISIBLE);
        }
    };

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
