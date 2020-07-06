package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.BR;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db2.Company;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/***
 * 导入流程
 * 点击选择文件———————选中文件————————设置文件名————————下方开始加载列表
 */
public class BatchImportActivity extends Activity {
    private BatchContent batchContent = new BatchContent();
    private ActionPresenter actionPresenter = new ActionPresenter();
    private List<UserCheckBean> userCheckList = new ArrayList();
    private RecyclerView rlvData;
    private Uri mExcelFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewDataBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_batch_import);

        viewDataBinding.setVariable(BR.batchContent, batchContent);
        viewDataBinding.setVariable(BR.actionPresenter, actionPresenter);

        rlvData = viewDataBinding.getRoot().findViewById(R.id.rlv_data);
        rlvData.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        BatchImportAdapter batchImportAdapter = new BatchImportAdapter(this, userCheckList);
        batchImportAdapter.setOnItemCheckedListener((position, checked) -> {
            userCheckList.get(position).setChecked(checked);
        });
        rlvData.setAdapter(batchImportAdapter);

        batchContent.setFileName(getString(R.string.import_please_select_excel));
        batchContent.setCheckedAvailable(true);
    }

    final String XLSX = "application/x-excel";
    final String XLSX1 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    final String XLS = "application/vnd.ms-excel";

    public class ActionPresenter {
        public void onBack(View view){
            finish();
        }
        public void chooseFile(View view) {
            //调用系统文件管理器打开指定路径目录
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String[] mimeTypes = {XLS, XLSX1, XLSX};
            intent.setType("application/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(intent, 1111);
        }

        public void onAllChecked(CompoundButton buttonView, boolean isChecked) {
            selectAll(isChecked, SpUtils.getCompany().getComid() == Constants.NOT_BIND_COMPANY_ID);
        }

        public void onReverseChecked(CompoundButton buttonView, boolean isChecked) {
            selectReverse(isChecked);
        }

        public void importUsers(View view) {
            Company company = SpUtils.getCompany();
            if (company.getComid() == Constants.NOT_BIND_COMPANY_ID) {
                ReadExcel.importDataToLocal(company.getComid(), userCheckList, new ReadExcel.ImportCallback() {
                    @Override
                    public void onStartImport() {
                        UIUtils.showNetLoading(BatchImportActivity.this);
                    }

                    @Override
                    public void onImportComplete(ReadExcel.ImportResult importResult) {
                        UIUtils.showLong(BatchImportActivity.this,
                                getString(R.string.import_add_success) + importResult.successNum
                                        + "\n"
                                        + getString(R.string.import_add_failed) + importResult.failedNum
                                        + "\n"
                                        + getString(R.string.import_add_exists) + importResult.alreadyExists
                                        + "\n"
                                        + getString(R.string.import_add_skip) + importResult.skipNum);
                        UIUtils.dismissNetLoading();
                    }
                });
            } else {
                ReadExcel.submitExcelToServer(BatchImportActivity.this, mExcelFileUri, new ReadExcel.SubmitCallback() {
                    @Override
                    public void onStart() {
                        UIUtils.showNetLoading(BatchImportActivity.this);
                    }

                    @Override
                    public void onSubmitResult(int result, ReadExcel.ImportResult importResult, String errMsg) {
                        runOnUiThread(() -> {
                            switch (result) {
                                case -1:
                                    UIUtils.showLong(BatchImportActivity.this, getString(R.string.import_submit_failed) + errMsg);
                                    break;
                                case -2:
                                    UIUtils.showLong(BatchImportActivity.this, getString(R.string.import_sync_failed) + errMsg);
                                    break;
                            }
                            if (result != 1) {
                                return;
                            }

                            UIUtils.showLong(BatchImportActivity.this,
                                    getString(R.string.import_import_complete)
                                            + "\n"
                                            + getString(R.string.import_add_success) + importResult.successNum
                                            + "\n"
                                            + getString(R.string.import_add_failed) + importResult.failedNum
                                            + "\n"
                                            + getString(R.string.import_add_exists) + importResult.alreadyExists);
                        });
                    }

                    @Override
                    public void onFinish() {
                        UIUtils.dismissNetLoading();
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111 && resultCode == RESULT_OK) {
            mExcelFileUri = data.getData();
            Timber.d("文件地址：" + (mExcelFileUri == null ? "NULL" : mExcelFileUri.getPath()));

            ReadExcel.readEntries(this, mExcelFileUri, new ReadExcel.ReadCallback() {
                @Override
                public void onStartRead() {
                    UIUtils.showNetLoading(BatchImportActivity.this);
                }

                @Override
                public void onReadData(List<ReadExcel.Entry> entryList, ReadExcel.ErrorInfo errorInfo) {
                    UIUtils.dismissNetLoading();
                    if (errorInfo.errCode == ReadExcel.ErrorCode.COMPLETE) {
                        batchContent.setFileName(getFileRealNameFromUri(BatchImportActivity.this, mExcelFileUri));
                        setUserList(entryList);
                    } else {
                        runOnUiThread(() -> UIUtils.showShort(BatchImportActivity.this, errorInfo.errMsg));
                    }
                }
            });
        }
    }

    //设置显示用户列表
    private void setUserList(List<ReadExcel.Entry> entryList) {
        if (!userCheckList.isEmpty()) {
            userCheckList.clear();
        }
        for (ReadExcel.Entry entry : entryList) {
            UserCheckBean checkBean = new UserCheckBean();
            checkBean.setEntry(entry);
            userCheckList.add(checkBean);
            rlvData.getAdapter().notifyItemInserted(userCheckList.size() - 1);
        }
        Company company = SpUtils.getCompany();

        Timber.d("绑定结果：" + company.getComid());
        selectAll(true, company.getComid() == Constants.NOT_BIND_COMPANY_ID);
    }

    //全选
    private void selectAll(boolean check, boolean enabled) {
        Timber.d("是否可用：" + enabled);

        for (UserCheckBean checkBean : userCheckList) {
            checkBean.setChecked(check);
            checkBean.setCheckEnabled(enabled);
        }
        rlvData.getAdapter().notifyDataSetChanged();
        batchContent.setSelectAll(check);
        batchContent.setCheckedAvailable(enabled);
    }

    //反选
    private void selectReverse(boolean check) {
        for (UserCheckBean checkBean : userCheckList) {
            checkBean.setChecked(!checkBean.isChecked());
        }
        rlvData.getAdapter().notifyDataSetChanged();
        batchContent.setSelectReverse(check);
    }

    //通过Uri获得名称
    public static String getFileRealNameFromUri(Context context, Uri fileUri) {
        if (context == null || fileUri == null) return null;
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, fileUri);
        if (documentFile == null) return null;
        return documentFile.getName();
    }

    private class BatchImportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<UserCheckBean> datas;
        private Context mContext;
        private onItemCheckedListener onItemCheckedListener;

        public BatchImportAdapter(Context context, List<UserCheckBean> datas) {
            this.datas = datas;
            this.mContext = context;
        }

        public void setOnItemCheckedListener(BatchImportActivity.onItemCheckedListener onItemCheckedListener) {
            this.onItemCheckedListener = onItemCheckedListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BatchHolder(getLayoutInflater().inflate(R.layout.item_batch_import, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            BatchHolder batchHolder = (BatchHolder) holder;
            batchHolder.bindData(userCheckList.get(position), position);
        }

        @Override
        public int getItemCount() {
            return datas == null ? 0 : datas.size();
        }

        class BatchHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextView tvDepart;
            TextView tvSex;
            TextView tvPosition;
            ImageView ivHead;
            CheckBox checkBox;

            public BatchHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_name);
                tvDepart = itemView.findViewById(R.id.tv_depart);
                tvSex = itemView.findViewById(R.id.tv_sex);
                tvPosition = itemView.findViewById(R.id.tv_position);
                ivHead = itemView.findViewById(R.id.iv_head);
                checkBox = itemView.findViewById(R.id.cb_checked);
            }

            public void bindData(UserCheckBean userCheckBean, int position) {
                ReadExcel.Entry entry = userCheckBean.getEntry();
                tvName.setText(entry.getName());
                tvDepart.setText(entry.getDepName());
                tvSex.setText(getString(entry.getSex() == 1 ? R.string.base_male : R.string.base_female));
                tvPosition.setText(entry.getPosition());
                Glide.with(mContext).load(entry.getPic()).asBitmap().into(ivHead);

                checkBox.setEnabled(userCheckBean.isCheckEnabled());
                checkBox.setChecked(userCheckBean.isChecked());
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (onItemCheckedListener != null) {
                        onItemCheckedListener.onItemChecked(position, isChecked);
                    }
                });
            }
        }
    }

    public interface onItemCheckedListener {
        void onItemChecked(int position, boolean checked);
    }

    public class BatchContent extends BaseObservable {
        String fileName;
        String filePath;
        boolean checkedAvailable;
        boolean isSelectAll;
        boolean isSelectReverse;

        @Bindable
        public boolean isSelectAll() {
            return isSelectAll;
        }

        public void setSelectAll(boolean selectAll) {
            isSelectAll = selectAll;
            notifyPropertyChanged(BR.selectAll);
        }

        @Bindable
        public boolean isSelectReverse() {
            return isSelectReverse;
        }

        public void setSelectReverse(boolean selectReverse) {
            isSelectReverse = selectReverse;
            notifyPropertyChanged(BR.selectReverse);
        }

        @Bindable
        public boolean isCheckedAvailable() {
            return checkedAvailable;
        }

        public void setCheckedAvailable(boolean available) {
            checkedAvailable = available;
            notifyPropertyChanged(BR.checkedAvailable);
        }

        @Bindable
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
            notifyPropertyChanged(BR.fileName);
        }

        @Bindable
        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
            notifyPropertyChanged(BR.filePath);
        }
    }

    public class UserCheckBean {
        private boolean isChecked;
        private boolean isCheckEnabled;
        private ReadExcel.Entry entry;

        public boolean isCheckEnabled() {
            return isCheckEnabled;
        }

        public void setCheckEnabled(boolean checkEnabled) {
            isCheckEnabled = checkEnabled;
        }

        public ReadExcel.Entry getEntry() {
            return entry;
        }

        public void setEntry(ReadExcel.Entry entry) {
            this.entry = entry;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }


}
