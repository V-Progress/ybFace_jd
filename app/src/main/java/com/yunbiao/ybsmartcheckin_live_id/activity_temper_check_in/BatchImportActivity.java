package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
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
import com.yunbiao.ybsmartcheckin_live_id.databinding.ActivityBatchImportBinding;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;

import java.util.ArrayList;
import java.util.List;

/***
 * 导入流程
 * 点击选择文件———————选中文件————————设置文件名————————下方开始加载列表
 */
public class BatchImportActivity extends Activity {
    private BatchContent batchContent = new BatchContent();
    private ActionPresenter actionPresenter = new ActionPresenter();
    private List<UserCheckBean> userCheckList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewDataBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_batch_import);

        viewDataBinding.setVariable(BR.batchContent,batchContent);
        viewDataBinding.setVariable(BR.actionPresenter,actionPresenter);

        RecyclerView rlvData = viewDataBinding.getRoot().findViewById(R.id.rlv_data);
        rlvData.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        BatchImportAdapter batchImportAdapter = new BatchImportAdapter(this,userCheckList);
        batchImportAdapter.setOnItemCheckedListener((position, checked) -> {
            userCheckList.get(position).setCheckked(checked);
        });
        rlvData.setAdapter(batchImportAdapter);
    }


    private class BatchImportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<UserCheckBean> datas;
        private Context mContext;
        private onItemCheckedListener onItemCheckedListener;

        public BatchImportAdapter(Context context,List<UserCheckBean> datas) {
            this.datas = datas;
            this.mContext = context;
        }

        public void setOnItemCheckedListener(BatchImportActivity.onItemCheckedListener onItemCheckedListener) {
            this.onItemCheckedListener = onItemCheckedListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BatchHolder(getLayoutInflater().inflate(R.layout.item_batch_import,null));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            BatchHolder batchHolder = (BatchHolder) holder;
            batchHolder.bindData(userCheckList.get(position),position);
        }

        @Override
        public int getItemCount() {
            return datas == null ? 0 : datas.size();
        }

        class BatchHolder extends RecyclerView.ViewHolder{
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

            public void bindData(UserCheckBean userCheckBean,int position) {
                tvName.setText(userCheckBean.getName());
                tvDepart.setText(userCheckBean.getDepart());
                tvSex.setText(userCheckBean.getSexStr());
                tvPosition.setText(userCheckBean.getPosition());
                Glide.with(mContext).load(userCheckBean.getImageBytes()).asBitmap().into(ivHead);
                checkBox.setChecked(userCheckBean.isCheckked());
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if(onItemCheckedListener != null){
                        onItemCheckedListener.onItemChecked(position,isChecked);
                    }
                });
            }
        }
    }

    public interface onItemCheckedListener{
        void onItemChecked(int position,boolean checked);
    }

    public class BatchContent extends BaseObservable {
        String fileName;
        String filePath;
        boolean checkedAvailable;

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

    public class ActionPresenter{
        public void chooseFile(View view){

        }

        public void onAllChecked(CompoundButton buttonView, boolean isChecked) {

        }

        public void onReverseChecked(CompoundButton buttonView, boolean isChecked) {

        }
    }

    public class UserCheckBean{
        private String name;
        private String depart;
        private String sexStr;
        private String position;
        private byte[] imageBytes;
        private boolean isCheckked;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDepart() {
            return depart;
        }

        public void setDepart(String depart) {
            this.depart = depart;
        }

        public String getSexStr() {
            return sexStr;
        }

        public void setSexStr(String sexStr) {
            this.sexStr = sexStr;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public byte[] getImageBytes() {
            return imageBytes;
        }

        public void setImageBytes(byte[] imageBytes) {
            this.imageBytes = imageBytes;
        }

        public boolean isCheckked() {
            return isCheckked;
        }

        public void setCheckked(boolean checkked) {
            isCheckked = checkked;
        }
    }
}
