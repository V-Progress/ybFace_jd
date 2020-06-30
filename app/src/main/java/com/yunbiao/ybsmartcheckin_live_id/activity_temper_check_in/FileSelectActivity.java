package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class FileSelectActivity extends FragmentActivity {
    private static final String TAG = "FileSelectActivity";
    //用来存储当前的路径结构
    private Stack<File> fileStack = new Stack<>();

    private File localRootPath;
    private File usbRootPath;
    private RecyclerView rlvFileList;
    private FileAdapter fileAdapter;
    private List<File> fileList = new ArrayList<>();
    private TextView tvPathTree;

    public static final int SELECT_REQUEST_CODE = 421;//请求的requestCode
    public static final String RESULT_PATH_KEY = "selectFilePath";//结果地址的Key

    //要选择的文件里类型
    public static final String SELECT_FILE_TYPE = "selectFileType";
    public static final int FILE_TYPE_DIR = 0;
    public static final int FILE_TYPE_IMG = 1;
    public static final int FILE_TYPE_XLS = 2;
    public static final String SHOW_USB_DISK = "showUsbDisk";

    public static final String CAN_CREATE_DIR = "canCreateDir";

    private static int mCurrSelectType = FILE_TYPE_DIR;//默认选文件夹
    private boolean isShowUSBDisk = true;//默认U盘可见

    private boolean canCreateDir = true;

    public static void selectFile(Activity activity,int fileType,boolean showUsbDisk, int requestCode){
        Intent intent = new Intent(activity, FileSelectActivity.class);
        intent.putExtra(SELECT_FILE_TYPE,fileType);
        intent.putExtra(SHOW_USB_DISK,showUsbDisk);
        activity.startActivityForResult(intent,requestCode);
    }

    public static void selectFile(Activity activity,int fileType,boolean showUsbDisk, boolean canCreateDir, int requestCode){
        Intent intent = new Intent(activity, FileSelectActivity.class);
        intent.putExtra(SELECT_FILE_TYPE,fileType);
        intent.putExtra(SHOW_USB_DISK,showUsbDisk);
        intent.putExtra(CAN_CREATE_DIR,canCreateDir);
        activity.startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);

        Button btnLocalPath = findViewById(R.id.btn_local_path);
        Button btnUsbDisk = findViewById(R.id.btn_usb_disk);
        Button btnCreateDir = findViewById(R.id.btn_create_dir);
        tvPathTree = findViewById(R.id.tv_path_tree);
        rlvFileList = findViewById(R.id.rlv_file_list);
        rlvFileList.addItemDecoration(new SimpleItemDecoration(10));
        rlvFileList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        fileAdapter = new FileAdapter(this,fileList,onItemClickListener);
        rlvFileList.setAdapter(fileAdapter);

        mCurrSelectType = getIntent().getIntExtra(SELECT_FILE_TYPE,FILE_TYPE_DIR);
        isShowUSBDisk = getIntent().getBooleanExtra(SHOW_USB_DISK,true);
        canCreateDir = getIntent().getBooleanExtra(CAN_CREATE_DIR,true);
        //是否可以创建目录
        if(!canCreateDir){
            btnCreateDir.setVisibility(View.GONE);
        }


        //初始化根路径
        localRootPath = Environment.getExternalStorageDirectory();
        usbRootPath = new File(SdCardUtils.getUsbDiskPath(this));
        if(!isDirCanUsed(localRootPath) && !isDirCanUsed(usbRootPath)){
            UIUtils.showShort(this,getString(R.string.no_can_used_directory));
        }

        //usb路径是否存在
        if(!usbRootPath.exists() || !isShowUSBDisk){
            btnUsbDisk.setVisibility(View.GONE);
            btnLocalPath.performClick();
        } else {
            btnUsbDisk.performClick();
        }
    }

    private FileAdapter.OnItemClickListener onItemClickListener = new FileAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            //如果选择的不是文件夹，则不允许进入下级目录
            File file = fileList.get(position);
            if(!file.isDirectory()){
                return;
            }
            //点击..或目录后返回上级或者进入下级
            if(TextUtils.equals("..",file.getPath())){
                goBack(file);
            } else {
                goForward(file);
            }
            logStack();
        }

        @Override
        public void onItemChecked(int position) {
            //选择文件
            File file = fileList.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(FileSelectActivity.this)
                    .setTitle(getString(R.string.base_tip))
                    .setMessage(getString(R.string.confirm_directore_tips) + "\n\n" + file.getPath())
                    .setCancelable(false).setPositiveButton(getString(R.string.base_ensure), (dialog, which) -> {
                        Intent intent = new Intent();
                        intent.putExtra(RESULT_PATH_KEY,file.getPath());
                        setResult(RESULT_OK,intent);
                        finish();
                    }).setNegativeButton(getString(R.string.base_cancel), (dialog, which) -> {
                        dialog.dismiss();
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    //判断文件夹是否可用
    private boolean isDirCanUsed(File file){
        return file.exists() && file.canRead() && file.canWrite();
    }

    //跳转本地目录
    public void jumpLocalPath(View view) {
        if(!fileStack.empty()){
            fileStack.clear();
        }
        goForward(localRootPath);
    }

    //跳转Usb目录
    public void jumpUsbPath(View view) {
        if(!fileStack.empty()){
            fileStack.clear();
        }
        goForward(usbRootPath);
    }

    //创建新目录
    public void createDir(View view){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.act_departList_tip_qsrbmmc))
                .setView(LayoutInflater.from(this).inflate(R.layout.dialog_depart, null))
                .setPositiveButton(getString(R.string.base_ensure), null)
                .setNegativeButton(getString(R.string.base_cancel), null)
                .setCancelable(false)
                .create();
        alertDialog.show();

        EditText edtDepartName = alertDialog.findViewById(R.id.et_departName);
        edtDepartName.setHint(getString(R.string.please_input_dir_name));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String inputText = edtDepartName.getText().toString();
            if(TextUtils.isEmpty(inputText)){
                edtDepartName.setError(getString(R.string.please_input_dir_name));
            } else {
                File peek = fileStack.peek();
                File newDir = new File(peek,inputText);
                boolean mkdir = newDir.mkdir();
                if(mkdir){
                    UIUtils.showShort(FileSelectActivity.this,getString(R.string.create_success));
                    setList(peek);
                    alertDialog.dismiss();

                    int position = fileList != null && fileList.size() > 0 ? fileList.size() - 1 : 0;
                    if(fileList != null){
                        for (int i = 0; i < fileList.size(); i++) {
                            File file = fileList.get(i);
                            if(TextUtils.equals(newDir.getPath(),file.getPath())){
                                position = i;
                                break;
                            }
                        }
                    }
                    rlvFileList.smoothScrollToPosition(position);
                } else {
                    UIUtils.showShort(FileSelectActivity.this,getString(R.string.create_failed));
                }
            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> alertDialog.dismiss());
    }

    /***
     * 前进，先删除栈顶然后取出栈顶
     * @param path
     */
    private void goBack(File path){
        Log.e(TAG, "后退: 后退← ← ← ← ← ← ← ←");
        File pop = fileStack.pop();
        Log.e(TAG, "后退: 已移除：" + pop.getName());
        File peek = fileStack.peek();
        Log.e(TAG, "后退: 当前栈顶：" + peek);
        setList(peek);
    }

    /***
     * 判断栈顶是否相等，否则前进
     * @param path
     */
    private void goForward(File path){
        if(!fileStack.empty()){
            File peek = fileStack.peek();
            if(TextUtils.equals(path.getPath(),peek.getPath())){
                Log.e(TAG, "前进: 当前已位于栈顶");
                return;
            }
        }

        Log.e(TAG, "前进: 前进 → → → → → → → →");
        fileStack.push(path);
        Log.e(TAG, "前进: 入栈：" + fileStack);
        setList(path);
    }

    //设置显示目录
    private void setList(File path){
        if(fileList.size() > 0){
            fileList.clear();
        }
        File[] files = path.listFiles(file -> {
            String name = file.getName();
            boolean b;
            if(mCurrSelectType == FILE_TYPE_IMG){
                b = file.isDirectory() || name.endsWith(".jpg") || name.endsWith(".png") || file.getName().endsWith(".jpeg");
            } else if(mCurrSelectType == FILE_TYPE_XLS){
                b = file.isDirectory() || name.endsWith(".xls") || name.endsWith(".xlsx");
            } else {
                b = file.isDirectory();
            }
            return b;
        });
        if(files != null){
            fileList.addAll(Arrays.asList(files));
        }
        if (fileStack.size() > 1) {
            fileList.add(0,new File(".."));
        }
        fileAdapter.notifyDataSetChanged();

        setPathTree();
    }

    /***
     * 日志Stack
     */
    private void logStack(){
        List<File> tempList = new ArrayList<>();
        tempList.addAll(fileStack);
        Log.e(TAG, "onItemClick: 当前栈内容↓↓↓↓↓↓↓↓");
        for (File file : tempList) {
            Log.e(TAG, "onItemClick: " + file.getName());
        }
        Log.e(TAG, "onItemClick: 当前栈内容↑↑↑↑↑↑↑↑");
        tempList.clear();
    }

    /***
     * 设置路径树
     */
    private void setPathTree(){
        File peek = fileStack.peek();
        tvPathTree.setText(peek.getPath());
    }

    static class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private List<File> fileArray;
        private OnItemClickListener onItemClickListener;
        private Context mContext;

        public FileAdapter(Context context,List<File> fileArray,OnItemClickListener clickListener) {
            this.fileArray = fileArray;
            onItemClickListener = clickListener;
            mContext = context;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_list,parent,false);
            return new VH(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            VH vh = (VH) holder;
            vh.bindData(fileArray.get(position),position);
        }

        @Override
        public int getItemCount() {
            return fileArray == null ? 0 : fileArray.size();
        }

        class VH extends RecyclerView.ViewHolder{
            ImageView ivIcon;
            TextView tvName;
            ImageView btnConfirm;
            public VH(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_icon_file_list);
                tvName = itemView.findViewById(R.id.tv_name_file_list);
                btnConfirm = itemView.findViewById(R.id.btn_confirm_file_list);
            }

            public void bindData(File file,int position){
                //加载图标
                if(file.isDirectory()){
                    ivIcon.setImageResource(R.mipmap.dir_icon);
                } else if(file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".jpeg")) {
                    Glide.with(mContext).load(file).asBitmap().into(ivIcon);
                } else {
                    ivIcon.setImageResource(R.mipmap.file_icon);
                }
                tvName.setText(file.getName());

                //判断选择按钮是否可见（如果是..或者指定类型时，其余文件不可选择）
                if (TextUtils.equals("..",file.getPath())
                        || (mCurrSelectType == FILE_TYPE_DIR && !file.isDirectory())//如果指定文件夹
                        || (mCurrSelectType == FILE_TYPE_IMG && !file.getName().endsWith(".jpg") && !file.getName().endsWith(".png") && !file.getName().endsWith(".jpeg"))//如果指定了图片
                        || (mCurrSelectType == FILE_TYPE_XLS && !file.getName().endsWith(".xls") && !file.getName().endsWith(".xlsx")))//如果指定了Excel文件
                {
                    btnConfirm.setVisibility(View.GONE);
                } else {
                    btnConfirm.setVisibility(View.VISIBLE);
                }

                View.OnClickListener onClickListener = v -> {
                    if (v.getId() == R.id.iv_icon_file_list || v.getId() == R.id.tv_name_file_list) {
                        if(onItemClickListener != null){
                            onItemClickListener.onItemClick(position);
                        }
                    } else {
                        if(onItemClickListener != null){
                            onItemClickListener.onItemChecked(position);
                        }
                    }
                };

                ivIcon.setOnClickListener(onClickListener);
                tvName.setOnClickListener(onClickListener);
                btnConfirm.setOnClickListener(onClickListener);
            }
        }

        public interface OnItemClickListener{
            void onItemClick(int position);

            void onItemChecked(int position);
        }
    }

    class SimpleItemDecoration extends RecyclerView.ItemDecoration{
        private int space;

        public SimpleItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getChildPosition(view) != 0)
                outRect.top = space;
        }
    }
}
