package com.yunbiao.ybsmartcheckin_live_id.activity_certificates;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.activity.base.BaseActivity;
import com.yunbiao.ybsmartcheckin_live_id.business.SignManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.VertifyRecord;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

import timber.log.Timber;

public class CollectPhoneActivity extends BaseActivity implements View.OnClickListener {

    private EditText edtPhone;
    private VertifyRecord vertifyRecord;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_collect_phone;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_collect_phone;
    }

    @Override
    protected void initView() {
        Button btnConfirm = findViewById(R.id.btn_confirm);
        edtPhone = findViewById(R.id.edt_phone);
        edtPhone.addTextChangedListener(textWatcher);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        try {
            vertifyRecord = (VertifyRecord) intent.getSerializableExtra("verifyRecord");
        }catch (Exception e){
            e.printStackTrace();
            Timber.e(e);
        }

        if(vertifyRecord == null){
            UIUtils.showShort(this,"数据异常，请重新验证");
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onClick(View v) {
        String string = edtPhone.getText().toString();
        if(TextUtils.isEmpty(string)){
            edtPhone.setError("请输入手机号码");
            return;
        }

        int length = string.length();
        if(length != 11){
            edtPhone.setError("号码位数不正确，请检查");
            return;
        }

        if(!isValidPhoneNumber(string)){
            edtPhone.setError("手机号码格式不正确，请检查");
            return;
        }
        vertifyRecord.setPhoneNumber(string);

        SignManager.instance().uploadIdCardAndReImage(vertifyRecord);
        finish();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if ((phoneNumber != null) && (!phoneNumber.isEmpty())) {
            return Pattern.matches("^1[3-9]\\d{9}$", phoneNumber);
        }
        return false;
    }
}
