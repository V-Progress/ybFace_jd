package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class ConfigLoader {
    private static final String configFileName = "BK_CONFIG";
    private ConfigLoader(){
    }

    private static File checkConfigFile(){
        File rootFile = new File(Constants.LOCAL_ROOT_PATH);
        if(!rootFile.exists()){
            Timber.d("主目录不存在");
            return null;
        }

        File configFile = null;
        File[] files = rootFile.listFiles((dir, name) -> TextUtils.equals(configFileName,name));
        if(files == null || files.length == 0){
            Timber.d( "配置文件不存在");
            File file = new File(rootFile, configFileName);
            try {
                boolean newFile = file.createNewFile();
                if(!newFile){
                    Timber.d("checkConfigFile: 创建配置文件失败");
                } else {
                    configFile = file;
                }
            } catch (IOException e) {
                Timber.d("checkConfigFile: 创建配置文件失败: %s", e.getMessage());
            }
        } else {
            configFile = files[0];
        }
        return configFile;
    }

    /***
     * 保存的时候，取出Map中所有的Key以及对应的类型，保存一个类型标签
     *
     * 读取的时候获取Map中的所有数据，然后检查类型，调用不同的方法
     */
    public static void load() {
        try {
            File configFile = checkConfigFile();
            if(configFile == null){
                Timber.d("save: 未检测到配置文件");
                return;
            }
            String jsonStr = FileUtils.readFileToString(configFile, "UTF-8");
            List<ConfigBean> configBeans = new Gson().fromJson(jsonStr,new TypeToken<ArrayList<ConfigBean>>(){}.getType());
            if(configBeans == null || configBeans.size() == 0){
                Timber.d( "load: 配置文件为空");
                save();
                new Handler().postDelayed(() -> load(),1500);
                return;
            }

            List<String> keyList = new ArrayList<>();
            Field[] fields1 = Constants.Key.class.getFields();
            for (Field field : fields1) {
                try {
                    String string = field.get(field.getName()).toString();
                    keyList.add(string);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            Field[] fields = ThermalConst.Key.class.getFields();
            for (Field field : fields) {
                try {
                    String string = field.get(field.getName()).toString();
                    keyList.add(string);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            Iterator<ConfigBean> iterator = configBeans.iterator();
            while (iterator.hasNext()) {
                ConfigBean next = iterator.next();
                if (!keyList.contains(next.getKey())) {
                    iterator.remove();
                }
            }

            for (ConfigBean configBean : configBeans) {
                String key = configBean.getKey();
                Object value = configBean.getValue();
                String type = configBean.getType();
                if(type.contains("String")){
                    SpUtils.saveStr(key, value.toString());
                } else if(type.contains("Float")){
                    SpUtils.saveFloat(key, Float.parseFloat(value.toString()));
                } else if(type.contains("Boolean")){
                    SpUtils.saveBoolean(key, Boolean.parseBoolean(value.toString()));
                } else if(type.contains("Integer")){
                    SpUtils.saveInt(key,Math.round(Float.parseFloat(value.toString())));
                } else if(type.contains("Long")){
                    SpUtils.saveLong(key,Math.round(Double.parseDouble(value.toString())));
                }
            }
            Timber.d( "load: 写入缓存完毕");
        } catch (IOException e) {
            Timber.d( "load: 读取配置文件异常：%s", e.getMessage());
        }
    }

    public static void save(){
        File file = checkConfigFile();
        if(file == null){
            Timber.d( "save: 未检测到配置文件");
            return;
        }

        List<String> keyList = new ArrayList<>();
        Field[] fields1 = Constants.Key.class.getFields();
        for (Field field : fields1) {
            try {
                String string = field.get(field.getName()).toString();
                keyList.add(string);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Field[] fields = ThermalConst.Key.class.getFields();
        for (Field field : fields) {
            try {
                String string = field.get(field.getName()).toString();
                keyList.add(string);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        List<ConfigBean> configBeans = new ArrayList<>();
        Map<String, ?> all = SpUtils.getAll();
        if (all != null && all.size() > 0) {
            for (Map.Entry<String, ?> stringEntry : all.entrySet()) {

                String key = stringEntry.getKey();
                Object value = stringEntry.getValue();
                String typeName = value.getClass().getName();
                if(!keyList.contains(key)){
                    continue;
                }
                configBeans.add(new ConfigBean(key,value,typeName));
            }
        }
        if(configBeans.size() == 0){
            Timber.d( "save: 暂无缓存数据");
            return;
        }

        String configListJson = new Gson().toJson(configBeans);
        try {
            FileUtils.writeStringToFile(file,configListJson,"UTF-8",false);
            Timber.d( "save: 写入成功：%s", file.length());
        } catch (IOException e) {
            Timber.d( "save: 写入失败：%s", e.getMessage());
        }
    }

    static class ConfigBean{
        private String key;
        private Object value;
        private String type;

        ConfigBean(String key, Object value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @NotNull
        @Override
        public String toString() {
            return "ConfigBean{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}
