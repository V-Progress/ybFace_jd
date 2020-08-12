package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;

import org.xutils.common.util.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdsLoader extends AsyncTask<Void,Void, AdsLoader.RuleBean> {
    private static AdsLoader adsLoader;
    private LoadCallback callback;
    private static final String ruleName = "rule.txt";

    private static AsyncTask<Void,Void,Integer> copyTask;

    public AdsLoader(LoadCallback loadCallback) {
        callback = loadCallback;
    }

    public interface Callback<T>{
        void onResult(T t);
    }
    public static void copyDirToDir(File destDir, File targetDir,Callback<Integer> callback){
        if(copyTask != null && !copyTask.isCancelled()){
            return;
        }

        copyTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                if(!destDir.exists() || destDir.isFile()){
                    return -1;//源路径不存在
                }

                File[] files = destDir.listFiles(pathname -> checkFile(pathname) || TextUtils.equals(ruleName, pathname.getName()));
                if(files == null){
                    return -2;//无可用文件
                }

                if(targetDir == null){
                    return -3;
                }

                if(!targetDir.exists()){
                    targetDir.mkdirs();
                }

                int copyNum = 0;
                for (File file : files) {
                    boolean copy = FileUtil.copy(file.getPath(), new File(targetDir, file.getName()).getPath());
                    if(copy){
                        copyNum = 0;
                    }
                }
                return copyNum;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                copyTask.cancel(true);
                if(callback != null) callback.onResult(integer);
            }
        };
    }


    public static void start(LoadCallback loadCallback){
        if(adsLoader != null && !adsLoader.isCancelled()){
            adsLoader.cancel(true);
            adsLoader.clearCallback();
            adsLoader = null;
        }

        adsLoader = new AdsLoader(loadCallback);
        adsLoader.execute();
    }

    private void clearCallback() {
        callback = null;
    }

    @Override
    protected void onPreExecute() {
        if(callback != null) callback.onStart();
    }

    @Override
    protected RuleBean doInBackground(Void... voids) {
        //检查主目录
        File customAdsDir = new File(Constants.CUSTOM_ADS_PATH);
        if(!customAdsDir.exists() || customAdsDir.isFile()){
            return new RuleBean(DIR_NOT_EXISTS);
        }

        //检查媒体文件
        File[] files = customAdsDir.listFiles(pathname -> checkFile(pathname));
        if(files == null || files.length == 0){
            return new RuleBean(DIR_NO_FILE);
        }

        //检查规则文件(如果规则文件列表为空，则默认加载所有文件)
        RuleBean ruleBean = null;
        File[] ruleFiles = customAdsDir.listFiles((dir, name) -> TextUtils.equals(ruleName,name));
        if(ruleFiles != null && ruleFiles.length > 0){
            try {
                ruleBean = new Gson().fromJson(new FileReader(ruleFiles[0]), RuleBean.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //检查规则文件播放列表
        if(ruleBean == null){
            ruleBean = new RuleBean();
        }
        ruleBean.setResult(LOAD_COMPLETE);

        //检查播放时间
        if(ruleBean.getPlayTimeSeconds() < 1){
            ruleBean.setPlayTimeSeconds(5);
        }
        List<String> playList = ruleBean.getPlayList();
        //如果规则文件的规则不为空则转换播放列表
        if(playList != null && playList.size() > 0){
            Iterator<String> iterator = playList.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                File playFile = new File(customAdsDir,next);
                if(!playFile.exists()){
                    iterator.remove();
                }
            }
            List<String> playPathList = new ArrayList<>();
            for (String s : playList) {
                playPathList.add(new File(customAdsDir,s).getParent());
            }
            ruleBean.setPlayList(playPathList);
        }
        //如果规则为空则生成播放列表
        else {
            List<String> playPathList = new ArrayList<>();
            for (File file : files) {
                playPathList.add(file.getPath());
            }
            ruleBean.setPlayList(playPathList);
        }

        return ruleBean;
    }

    private static boolean checkFile(File file){
        return file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".mp4");
    }

    @Override
    protected void onPostExecute(RuleBean playBean) {
        if(callback != null) callback.onResult(playBean);
    }

    public interface LoadCallback{
        void onStart();

        void onResult(RuleBean playBean);
    }

    public static final int DIR_NOT_EXISTS = -1;
    public static final int DIR_NO_FILE = -2;
    public static final int LOAD_COMPLETE = 0;
    public static class RuleBean{
        private int result;
        private int playTimeSeconds;
        private List<String> playList;

        public RuleBean() {
        }

        public RuleBean(int result) {
            this.result = result;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public int getPlayTimeSeconds() {
            return playTimeSeconds;
        }

        public void setPlayTimeSeconds(int playTimeSeconds) {
            this.playTimeSeconds = playTimeSeconds;
        }

        public List<String> getPlayList() {
            return playList;
        }

        public void setPlayList(List<String> playList) {
            this.playList = playList;
        }

        @Override
        public String toString() {
            return "PlayBean{" +
                    "result=" + result +
                    ", playTimeSeconds=" + playTimeSeconds +
                    ", playList=" + playList +
                    '}';
        }
    }
}
