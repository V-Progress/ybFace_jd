package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.yunbiao.ybsmartcheckin_live_id.R;
import com.yunbiao.ybsmartcheckin_live_id.utils.SdCardUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;

import java.io.File;
import java.io.FileFilter;
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

    public static final int SELECT_DIRECTORY = 421;
    public static final String SELECT_FILE_PATH = "selectFilePath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);

        Button btnLocalPath = findViewById(R.id.btn_local_path);
        Button btnUsbDisk = findViewById(R.id.btn_usb_disk);
        tvPathTree = findViewById(R.id.tv_path_tree);
        rlvFileList = findViewById(R.id.rlv_file_list);
        rlvFileList.addItemDecoration(new SimpleItemDecoration(10));
        rlvFileList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        fileAdapter = new FileAdapter(fileList,onItemClickListener);
        rlvFileList.setAdapter(fileAdapter);

        //初始化根路径
        localRootPath = Environment.getExternalStorageDirectory();
        usbRootPath = new File(SdCardUtils.getUsbDiskPath(this));

        //本地路径是否存在
        if(!localRootPath.exists()){
            btnLocalPath.setVisibility(View.GONE);
        }

        //usb路径是否存在
        if(!usbRootPath.exists()){
            btnUsbDisk.setVisibility(View.GONE);
            btnLocalPath.performClick();
        } else {
            btnUsbDisk.performClick();
        }

        if(!isDirCanUsed(localRootPath) && !isDirCanUsed(usbRootPath)){
            UIUtils.showShort(this,getString(R.string.no_can_used_directory));
        }
    }

    private FileAdapter.OnItemClickListener onItemClickListener = new FileAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            File file = fileList.get(position);
            Log.e(TAG, "点击目录" + file.getName());
            if(TextUtils.equals("..",file.getPath())){
                goBack(file);
            } else {
                goForward(file);
            }
            logStack();
        }

        @Override
        public void onItemChecked(int position) {
            File file = fileList.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(FileSelectActivity.this)
                    .setTitle(getString(R.string.base_tip))
                    .setMessage(getString(R.string.confirm_directore_tips) + "\n\n" + file.getPath())
                    .setCancelable(false).setPositiveButton(getString(R.string.base_ensure), (dialog, which) -> {
                        Intent intent = new Intent();
                        intent.putExtra(SELECT_FILE_PATH,file.getPath());
                        setResult(RESULT_OK,intent);
                        finish();
                    }).setNegativeButton(getString(R.string.base_cancel), (dialog, which) -> {
                        dialog.dismiss();
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    private boolean isDirCanUsed(File file){
        return file.exists() && file.canRead() && file.canWrite();
    }

    public void jumpLocalPath(View view) {
        if(!fileStack.empty()){
            fileStack.clear();
        }
        goForward(localRootPath);
    }

    public void jumpUsbPath(View view) {
        if(!fileStack.empty()){
            fileStack.clear();
        }
        goForward(usbRootPath);
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

    private void setList(File path){
        if(fileList.size() > 0){
            fileList.clear();
        }
        File[] files = path.listFiles(pathname -> pathname.isDirectory());
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

        public FileAdapter(List<File> fileArray,OnItemClickListener clickListener) {
            this.fileArray = fileArray;
            onItemClickListener = clickListener;
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
                int iconId = file.isDirectory() ? R.mipmap.dir_icon : R.mipmap.file_icon;
                ivIcon.setImageResource(iconId);
                tvName.setText(file.getName());

                if (TextUtils.equals("..",file.getPath())) {
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
