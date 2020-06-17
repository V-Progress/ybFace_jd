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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lcw.library.imagepicker.ImagePicker;
import com.yunbiao.ybsmartcheckin_live_id.BR;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.ExcelUtils;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import jxl.Cell;
import jxl.CellFeatures;
import jxl.CellType;
import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

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
            userCheckList.get(position).setChecked(checked);
        });
        rlvData.setAdapter(batchImportAdapter);

        batchContent.setFileName("请选择导入文件(.xls)、(.xlsx)");
        batchContent.setCheckedAvailable(true);
    }

    final String XLSX = "application/x-excel";
    final String XLSX1 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    final String XLS = "application/vnd.ms-excel";

    public class ActionPresenter{
        public void chooseFile(View view){
            //调用系统文件管理器打开指定路径目录
            //intent.setDataAndType(Uri.fromFile(dir.getParentFile()), "file/*.txt");
//            intent.setType("application/vnd.ms-excel"); //华为手机mate7不支持
//            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); //华为手机mate7不支持
//            intent.setType("text/plain");

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String[] mimeTypes = {XLS, XLSX1, XLSX};
            intent.setType("application/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(intent, 1111);
        }

        public void onAllChecked(CompoundButton buttonView, boolean isChecked) {

        }

        public void onReverseChecked(CompoundButton buttonView, boolean isChecked) {

        }
    }

    private static final String TAG = "BatchImportActivity";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1111 && resultCode == RESULT_OK){
            Uri xls = data.getData();
            batchContent.setFileName(getFileRealNameFromUri(this, xls));



//            DocumentFile documentFile = DocumentFile.fromSingleUri(this,xls);
            try {
                InputStream inputStream = getContentResolver().openInputStream(xls);
                WorkbookSettings workbookSettings = new WorkbookSettings();
                workbookSettings.setEncoding("UTF-8");
                Workbook workbook = Workbook.getWorkbook(inputStream,workbookSettings);
                String[] sheetNames = workbook.getSheetNames();
                if (sheetNames.length <= 0) {
                    Log.e(TAG, "onActivityResult: 文件内没有表格");
                    return;
                }
                for (String sheetName : sheetNames) {
                    Log.e(TAG, "表名：" + sheetName);
                }

                Sheet sheet = workbook.getSheet(sheetNames[0]);
                int rows = sheet.getRows();
                if(rows <= 0){
                    Log.e(TAG, "onActivityResult: 该表格内没有数据");
                    return;
                }

                InputStream is = getContentResolver().openInputStream(xls);
                try {
                    org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(is);
                    int numberOfSheets = wb.getNumberOfSheets();
                    org.apache.poi.ss.usermodel.Sheet sheetA = wb.getSheetAt(numberOfSheets);

                    List<? extends PictureData> allPictures = wb.getAllPictures();
                    for (PictureData allPicture : allPictures) {

                    }


                    org.apache.poi.ss.usermodel.Cell cell;
                    for (Row row : sheetA) {
                        cell = row.getCell(0);


                    }


                } catch (InvalidFormatException e) {
                    e.printStackTrace();
                }


                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 1; i < rows; i++) {
                    Cell[] row = sheet.getRow(i);
                    for (Cell cell : row) {
                        stringBuffer.append(cell.getContents()).append("(").append(cell.getType()).append(")").append(",");
                    }
                    Log.e(TAG, i + "行：" + stringBuffer.toString());
                    stringBuffer.setLength(0);
                }
                inputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BiffException e) {
                e.printStackTrace();
            }

        }
    }

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
                checkBox.setChecked(userCheckBean.isChecked());
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

    public class UserCheckBean{
        private String name;
        private String depart;
        private String sexStr;
        private String position;
        private byte[] imageBytes;
        private boolean isChecked;

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

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }
}
