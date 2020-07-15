package com.yunbiao.ybsmartcheckin_live_id.utils.excel;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public abstract class Export extends AsyncTask<Void, Integer, Integer> {
    String filePath;
    String tableName;
    String[] colName;
    ExportCallback callback;
    private String exportPath;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public Export(String filePath, String tableName, String[] colName, ExportCallback callback) {
        this.filePath = filePath;
        this.tableName = tableName;
        this.colName = colName;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.onStart();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values != null) {
            callback.onProgress(values[0], values[1]);
        }
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        //创建导出目录
        File exportDir = new File(new File(filePath).getParentFile(), dateFormat.format(new Date()));
        if (!exportDir.exists() || !exportDir.isDirectory()) {
            exportDir.mkdirs();
        }
        exportPath = exportDir.getPath();

        //按日期拆分数据
        Map<String, List<List<String>>> stringListMap = splitListForDate(callback.getDataList());
        Timber.d("根据日期拆分List：" + stringListMap.size());

        Iterator<Map.Entry<String, List<List<String>>>> iterator = stringListMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<List<String>>> next = iterator.next();
            String key = next.getKey();
            List<List<String>> value = next.getValue();

            Timber.d("当前导出日期：" + key);
            Timber.d("日期分包执行线程：" + Thread.currentThread().getName());

            splitExport(value, exportDir, key);
        }
        return 1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        callback.onFinish(result, exportPath);
    }

    /***
     * 拆分导出
     * 数据按日期拆分，如果单日数据超过1000则分包进行
     */
    private void splitExport(List<List<String>> dataList, File filePath, String fileName) {
        Timber.d("拆分导出执行线程：" + Thread.currentThread().getName());

        Timber.d("导出目录：" + filePath.getPath());
        Timber.d("当前导出日期：" + fileName);

        if (dataList == null || dataList.size() <= 0) {
            return;
        }
        int size = dataList.size();
        Timber.d("导出总量:" + size);
        if (size > 500) {
            int numY = size % 500;
            int num = size / 500;
            num += numY == 0 ? 0 : 1;
            Map<Integer, List<List<String>>> dataMap = spiltList(dataList, num);
            Timber.d("数据已拆分：" + num);

            Iterator<List<List<String>>> iterator = dataMap.values().iterator();
            int offset = 0;
            int nameOffset = 0;
            while (iterator.hasNext()) {
                List<List<String>> next = iterator.next();
                int dataExcel = createDataExcel(filePath, fileName, nameOffset, tableName, next, size, offset);
                exportFailure(dataExcel, fileName);
                nameOffset++;
                offset += next.size();
            }
        } else {
            int dataExcel = createDataExcel(filePath, fileName, 0, tableName, dataList, size, 0);
            exportFailure(dataExcel, fileName);
        }
    }

    private void exportFailure(int dataExcel, String fileName) {
            /*if(dataExcel != 1){
                Toast.makeText(APP.getContext(), fileName + "[Export failure]", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
    }

    /**
     * 创建表和数据
     * @param dir
     * @param fileName
     * @param nameOffset
     * @param tableName
     * @param dataList
     * @param maxSize
     * @param numOffset
     * @return
     */
    public abstract int createDataExcel(File dir, String fileName, int nameOffset, String tableName, List<List<String>> dataList, int maxSize, int numOffset);

    /***
     * 按日期分割list
     * @param list
     * @return
     */
    public Map<String, List<List<String>>> splitListForDate(List<List<String>> list) {
        Map<String, List<List<String>>> map = new HashMap<>();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                List<String> strings = list.get(i);
                String s = strings.get(4);
                if (map.containsKey(s)) {
                    map.get(s).add(strings);
                } else {
                    List<List<String>> dataList = new ArrayList<>();
                    dataList.add(strings);
                    map.put(s, dataList);
                }
            }
        }
        return map;
    }

    /***
     * 分割list为若干份
     * @param list
     * @param num
     * @param <T>
     * @return
     */
    public <T> Map<Integer, List<T>> spiltList(List<T> list, int num) {
        Map<Integer, List<T>> map = new HashMap<>(num);
        if (list != null && list.size() > 0) {
            int length = list.size() / num;
            for (int i = 0; i < num; i++) {
                List<T> subList;
                if (i != num - 1) {
                    subList = list.subList(i * length, i * length + length);
                } else {
                    subList = list.subList(i * length, list.size());
                }
                map.put(i, subList);
            }
        }
        return map;
    }

    /***
     * 读取图片并转换为bytes
     * @param imgPath
     * @return
     */
    protected byte[] readBitmap(String imgPath) {
        ByteArrayOutputStream imgBytesOut = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imgBytesOut);
        byte[] bytes = imgBytesOut.toByteArray().clone();
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        if (imgBytesOut != null) {
            try {
                imgBytesOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    public interface ExportCallback {
        void onProgress(int progress, int max);

        void onStart();

        List<List<String>> getDataList();

        void onFinish(int result, String filePath);
    }
}