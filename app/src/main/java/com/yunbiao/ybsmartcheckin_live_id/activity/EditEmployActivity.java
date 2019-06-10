package com.yunbiao.ybsmartcheckin_live_id.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.ybsmartcheckin_live_id.APP;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.adapter.DepartAdapter;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartBean;
import com.yunbiao.ybsmartcheckin_live_id.db.DepartDao;
import com.yunbiao.ybsmartcheckin_live_id.db.UserDao;
import com.yunbiao.ybsmartcheckin_live_id.db.VIPDetail;
import com.yunbiao.ybsmartcheckin_live_id.faceview.FaceSDK;
import com.yunbiao.ybsmartcheckin_live_id.faceview.FaceView;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;


import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Request;


/**
 * Created by Administrator on 2018/8/7.
 */

public class EditEmployActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "EditEmployActivity";
    private static final int REQUEST_CODE_1 = 0x001;

    private Button btn_submit;
    private Button btn_TakePhoto;
    private Button btn_ReTakePhoto;
    private Button btn_cancle;
    private ImageView iv_back;

    //    private FaceCanvasView mFaceOverlay;
    private ImageView iv_capture;

    private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private EditText et_name;
    private EditText et_num;
    private Spinner sp_depart;
    private EditText et_sign;
    private EditText et_job;
    private TextView tv_birth;

    private String strFileAdd;
    private String strFileSource;
    private String age = "0";
    private String sex = "男";
    private String depart;//部门
    private String empNum;//员工编号
    private int faceId;//人脸id
    private int empId;//员工id
    private int departId;//部门id
    private List<String> mDepartList;
    private DepartDao departDao;
    private List<DepartBean> mDepartlist;
    private UserDao userDao;
    private MediaPlayer shootMP;
    private TextView tv_takephoto_time;
    private TextView tv_takephoto_tips;
    private View pbTakePhoto;
    private Bitmap currFaceBitmap = null;
    private View faceFrame;

    public static String SCREEN_BASE_PATH = sdPath + "/mnt/sdcard/photo/";
    private FaceView faceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_editemploy);
        } else {
            setContentView(R.layout.activity_editemploy_h);
        }

        userDao = new UserDao(EditEmployActivity.this);
        initViews();
        initSpinnerData();
    }

    private void initViews() {
        faceView = findViewById(R.id.face_view);
        faceFrame = findViewById(R.id.fl_face_frame);
        et_name = (EditText) findViewById(R.id.et_name);
        sp_depart = (Spinner) findViewById(R.id.sp_depart);
        et_sign = (EditText) findViewById(R.id.et_sign);
        et_job = (EditText) findViewById(R.id.et_job);
        et_num = (EditText) findViewById(R.id.et_num);
        tv_birth = (TextView) findViewById(R.id.tv_birth);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        iv_capture = (ImageView) findViewById(R.id.iv_capture);
        btn_TakePhoto = (Button) findViewById(R.id.btn_TakePhoto);
        btn_cancle = (Button) findViewById(R.id.btn_cancle);
        btn_ReTakePhoto = (Button) findViewById(R.id.btn_ReTakePhoto);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_takephoto_time = (TextView) findViewById(R.id.tv_takephoto_time);
        tv_takephoto_tips = (TextView) findViewById(R.id.tv_takephoto_tips);
        pbTakePhoto = findViewById(R.id.alv_take_photo);

        btn_TakePhoto.setOnClickListener(this);
        btn_ReTakePhoto.setOnClickListener(this);
        tv_birth.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }


    private void initSpinnerData() {
        mDepartList = new ArrayList<>();
        departDao = new DepartDao(EditEmployActivity.this);
        mDepartlist = departDao.selectAll();
        for (int i = 0; i < mDepartlist.size(); i++) {
            mDepartList.add(mDepartlist.get(i).getName());
        }
        if (mDepartList.size() > 0) {
            depart = mDepartList.get(0);
        }

        DepartAdapter departAdapter = new DepartAdapter(this, mDepartList);
        sp_depart.setAdapter(departAdapter);

        sp_depart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemSelected------> " + mDepartList.get(position));
                depart = mDepartList.get(position);
                departId = mDepartlist.get(position).getDepartId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String name = getIntent().getStringExtra("name");
        String depart = getIntent().getStringExtra("depart");
        if (name != null && depart != null) {
            List<VIPDetail> mlist = userDao.queryByDepartAndName(depart, name);
            if (mlist != null && mlist.size() > 0) {
                VIPDetail vipDetail = mlist.get(0);

                et_name.setText(name);
                et_sign.setText(vipDetail.getSignature());
                et_job.setText(vipDetail.getJob());
                et_num.setText(vipDetail.getEmployNum());
                tv_birth.setText(vipDetail.getBirthday());
                x.image().bind(iv_capture, vipDetail.getImgUrl());
                strFileAdd = vipDetail.getImgUrl();
                strFileSource = vipDetail.getImgUrl();
                faceId = vipDetail.getFaceId();
                empId = vipDetail.getEmpId();
                departId = vipDetail.getDepartId();

                for (int i = 0; i < mDepartList.size(); i++) {
                    if (depart.equals(mDepartList.get(i))) {
                        depart = mDepartList.get(i);
                        sp_depart.setSelection(i);
                    }
                }
            }
        }

        Log.e(TAG, "initSpinnerData: " + strFileAdd);
        Log.e(TAG, "initSpinnerData: " + strFileSource);
    }

    private void chooseBitmap(int requestCode) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, requestCode);
    }

    /**
     * 播放系统拍照声音
     */
    public void shootSound() {
        AudioManager meng = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume(AudioManager.STREAM_ALARM);

        if (volume != 0) {
            if (shootMP == null)
                shootMP = MediaPlayer.create(this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            if (shootMP != null)
                shootMP.start();
        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            byte[] faceImageBytes = faceView.getFaceImage();
            if(faceImageBytes == null || faceImageBytes.length<=0){
                handler.sendEmptyMessageDelayed(0,200);
                return;
            }
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap faceImage = BitmapFactory.decodeByteArray(faceImageBytes, 0, faceImageBytes.length, options);
            strFileAdd = saveBitmap(faceImage);
            iv_capture.setImageBitmap(faceImage);

            pbTakePhoto.setVisibility(View.GONE);
            btn_TakePhoto.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_choose:
                chooseBitmap(REQUEST_CODE_1);
                break;
            case R.id.btn_TakePhoto:
                pbTakePhoto.setVisibility(View.VISIBLE);
                btn_TakePhoto.setVisibility(View.GONE);
                handler.sendEmptyMessage(0);
                break;
            case R.id.btn_ReTakePhoto:
                currFaceBitmap = null;
                tv_takephoto_tips.setText("");
                tv_takephoto_time.setText("");
                pbTakePhoto.setVisibility(View.GONE);
                btn_TakePhoto.setVisibility(View.VISIBLE);
                iv_capture.setImageResource(R.mipmap.avatar);
                break;
            case R.id.btn_submit:
                final String name = et_name.getText().toString();
                final String job = et_job.getText().toString();
                final String birthday = tv_birth.getText().toString();
                final String signature = et_sign.getText().toString();
                final String empNum = et_num.getText().toString();

                if (TextUtils.isEmpty(strFileAdd)) {
                    UIUtils.showTitleTip("请先拍照！");
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    UIUtils.showTitleTip("名字不能为空！");
                    return;
                }
                if (TextUtils.isEmpty(depart)) {
                    UIUtils.showTitleTip("请返回增加部门！");
                    return;
                }

                int index = strFileAdd.lastIndexOf("/");
                String str = strFileAdd.substring(index + 1, strFileAdd.length());

                Map<String, String> params = new HashMap<>();
                params.put("id", empId + "");
                params.put("name", name + "");
                params.put("sex", (sex.equals("男") ? 1 : 0) + "");
                params.put("headName", str);
                params.put("position", job);
                params.put("birthday", birthday);
                params.put("age", age);
                params.put("autograph", signature);
                params.put("number", empNum);
                params.put("depId", departId + "");

                File imgFile = null;
                if (strFileSource.equals(strFileAdd)) {
                    String path = Environment.getExternalStorageDirectory() + "";
                    imgFile = new File(path + "/1.txt");
                    try {
                        if (!imgFile.exists())
                            imgFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    imgFile = new File(strFileAdd);
                }

                OkHttpUtils.post()
                        .url(ResourceUpdate.UPDATSTAFF)
                        .params(params)
                        .addFile("head", imgFile.getName(), imgFile)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onBefore(Request request, int id) {
                                btn_submit.setEnabled(false);
                                UIUtils.showNetLoading(EditEmployActivity.this);
                            }

                            @Override
                            public void onError(Call call, Exception e, int id) {
                                UIUtils.showTitleTip("添加失败：\n" + (e != null ? e.getMessage() : "NULL"));
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.d(TAG, "editStaffInfo....." + response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    int status = jsonObject.getInt("status");
                                    if (status != 1) {
                                        String errMsg;
                                        switch (status) {
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
                                        UIUtils.showTitleTip("" + errMsg);
                                        return;
                                    }

                                    if (!TextUtils.equals(strFileSource, strFileAdd)) {
                                        FaceSDK.instance().removeUser(0,String.valueOf(faceId));
                                        FaceSDK.instance().addUser(String.valueOf(faceId), strFileAdd, new FaceUserManager.FaceUserCallback() {
                                            @Override
                                            public void onUserResult(boolean b, int i) {
                                                if(b){
                                                    userDao.deleteByFaceId(faceId);
                                                    VIPDetail countDetail = new VIPDetail(departId, empId, faceId, sex, age, name, depart, job, empNum, birthday, signature, strFileAdd);
                                                    userDao.insert(countDetail);

                                                    UIUtils.showTitleTip("修改成功！");
                                                    finish();
                                                } else {
                                                    UIUtils.showTitleTip("修改失败！");
                                                }
                                            }
                                        });
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onAfter(int id) {
                                btn_submit.setEnabled(true);
                                UIUtils.dismissNetLoading();
                            }
                        });
                break;
            case R.id.tv_birth:
                Calendar now = Calendar.getInstance();
                new android.app.DatePickerDialog(
                        EditEmployActivity.this,
                        new android.app.DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("Orignal", "Got clicked");

                                tv_birth.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                ).show();

                break;
            case R.id.btn_cancle:
                finish();

                break;
            case R.id.iv_back:
                finish();
                break;

        }
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
}
