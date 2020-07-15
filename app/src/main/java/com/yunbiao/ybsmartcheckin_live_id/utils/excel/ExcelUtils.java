package com.yunbiao.ybsmartcheckin_live_id.utils.excel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.yunbiao.ybsmartcheckin_live_id.APP;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.read.biff.WorkbookParser;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import timber.log.Timber;

public class ExcelUtils {
    /***
     * 使用Poi导出到Excel
     * @param filePath
     * @param tableName
     * @param colName
     * @param callback
     */
    public static void initExcelForPoi(String filePath, String tableName, String[] colName, Export.ExportCallback callback) {
//        ExportForJXL export = new ExportForJXL(filePath,tableName,colName,callback);
//        export.execute();

        Export export = new POIExport(filePath, tableName, colName, callback);
        export.execute();

        /*LotDataExport export = new LotDataExport(filePath, tableName, colName, callback);
        export.execute();*/
    }

    /*
     * 早期导出===================================================================================================
     */
    public static WritableFont arial14font = null;
    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";

    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     */
    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);

            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(Colour.GRAY_25);

            arial12font = new WritableFont(WritableFont.ARIAL, 10);
            arial12format = new WritableCellFormat(arial12font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);//对齐格式
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN); //设置边框

        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化Excel===================================================================================================
     *
     * @param fileName
     * @param colName
     */
    public static void initExcel(String fileName, String tableName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet(tableName, 0);
            //创建标题栏
            sheet.addCell((WritableCell) new Label(0, 0, fileName, arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            sheet.setRowView(0, 340); //设置行高

            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> boolean writeObjListToExcel(final List<T> objList, final String fileName) {
        if (objList == null || objList.size() <= 0) {
            return false;
        }
        WritableWorkbook writebook = null;
        InputStream in = null;
        try {
            WorkbookSettings setEncode = new WorkbookSettings();
            setEncode.setEncoding(UTF8_ENCODING);
            in = new FileInputStream(new File(fileName));
            Workbook workbook = Workbook.getWorkbook(in);
            writebook = Workbook.createWorkbook(new File(fileName), workbook);
            WritableSheet sheet = writebook.getSheet(0);

//				sheet.mergeCells(0,1,0,objList.size()); //合并单元格
//				sheet.mergeCells()

            for (int j = 0; j < objList.size(); j++) {
                ArrayList<String> list = (ArrayList<String>) objList.get(j);
                for (int i = 0; i < list.size(); i++) {
                    String str = list.get(i);
                    String value = TextUtils.isEmpty(str) ? "" : str;
                    int length = TextUtils.isEmpty(value) ? 0 : value.length();

                    sheet.addCell(new Label(i, j + 1, value, arial12format));
                    sheet.setColumnView(i, length <= 5 ? (length + 8) : (length + 5)); //设置列宽
                }
                sheet.setRowView(j + 1, 350); //设置行高
            }

            writebook.write();
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (writebook != null) {
                try {
                    writebook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }
/*
    public static class ExportForJXL extends AsyncTask<Void, Integer, Integer> {
        String filePath;
        String tableName;
        String[] colName;
        ExportCallback callback;
        private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public ExportForJXL(@NonNull String filePath, @NonNull String tableName, @NonNull String[] colName, @NonNull ExportCallback callback) {
            this.filePath = filePath;
            this.tableName = tableName;
            this.colName = colName;
            this.callback = callback;
        }

        private void createExecutors() {
            int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
            int frQueueSize = 5;
            LinkedBlockingQueue<Runnable> frThreadQueue = new LinkedBlockingQueue<>(frQueueSize);
            ExecutorService executorService = new ThreadPoolExecutor(NUMBER_OF_CORES, frQueueSize, 1, TimeUnit.SECONDS, frThreadQueue);
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

        private String exportPath;

        @Override
        protected void onPostExecute(Integer result) {
            callback.onFinish(result, exportPath);
        }

        *//***
     * 拆分导出
     * 数据按日期拆分，如果单日数据超过1000则分包进行
     *//*
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
            *//*if(dataExcel != 1){
                Toast.makeText(APP.getContext(), fileName + "[Export failure]", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*//*
        }

        public int createDataExcel(File dir, String fileName, int nameOffset, String tableName, List<List<String>> dataList, int maxSize, int numOffset) {
            Timber.d("创建数据执行线程：" + Thread.currentThread().getName());

            Timber.d(nameOffset + " 次写入数据量：" + dataList.size());
            if (dataList == null || dataList.size() <= 0) {
                return 1;
            }

            try {
                String suffix = nameOffset <= 0 ? "" : "_" + nameOffset;
                File excelFile = new File(dir, fileName + suffix + ".xls");

                //创建工作簿
                WritableWorkbook workbook = Workbook.createWorkbook(excelFile);
                WritableSheet sheet = workbook.createSheet(tableName, 0);

                //写入标题
                for (int i = 0; i < colName.length; i++) {
                    WritableCellFormat format = new WritableCellFormat();
                    try {
                        format.setAlignment(Alignment.CENTRE);
                        format.setVerticalAlignment(VerticalAlignment.CENTRE);
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                    try {
                        Label labelC = new Label(i, 0, colName[i]);
                        sheet.addCell(labelC);
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                }

                //写入数据
                for (int row = 0; row < dataList.size(); row++) {
                    List<String> strings = dataList.get(row);

                    int realNum = row + 1;
                    int rowNum = row + numOffset + 1;
                    publishProgress(rowNum, maxSize);
                    Timber.d("当前写入行：" + rowNum);

                    try {
                        sheet.setRowView(rowNum, 500);
                    } catch (RowsExceededException e) {
                        e.printStackTrace();
                    }

                    for (int column = 0; column < strings.size(); column++) {
                        sheet.setColumnView(column, 10);
                        String content = strings.get(column);
                        if (!TextUtils.isEmpty(content)) {
                            if (column < strings.size() - 2) {
                                Label labelC = new Label(column, realNum, content);
                                sheet.addCell(labelC);
                            } else {
                                File file = new File(content);
                                WritableImage image = new WritableImage(column, realNum, 1, 1, readBitmap(file.getPath()));
                                sheet.addImage(image);
                            }
                        } else {
                            Label labelC = new Label(column, realNum, "");
                            sheet.addCell(labelC);
                        }
                    }
                }

                Timber.d("创建数据完毕，准备写出");
                workbook.write();
                workbook.close();
                Timber.d("写出完毕");

                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return -2;
            }
        }

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

        private byte[] readBitmap(String imgPath) {
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
    }


    public static class LotDataExport extends AsyncTask<Void, Integer, Integer> {
        String filePath;
        String tableName;
        String[] colName;
        ExportCallback callback;
        private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public LotDataExport(@NonNull String filePath, @NonNull String tableName, @NonNull String[] colName, @NonNull ExportCallback callback) {
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

        private String exportPath;

        @Override
        protected Integer doInBackground(Void... voids) {
            File exportDir = new File(new File(filePath).getParentFile(), dateFormat.format(new Date()));
            if (!exportDir.exists() || !exportDir.isDirectory()) {
                exportDir.mkdirs();
            }
            exportPath = exportDir.getPath();

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
            *//*if(dataExcel != 1){
                Toast.makeText(APP.getContext(), fileName + "[Export failure]", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*//*
        }

        private int createDataExcel(File dir, String fileName, int nameOffset, String tableName, List<List<String>> dataList, int maxSize, int numOffset) {
            Timber.d("创建数据执行线程：" + Thread.currentThread().getName());
            Timber.d(nameOffset + " 次写入数据量：" + dataList.size());
            if (dataList == null || dataList.size() <= 0) {
                return 1;
            }

            try {
                String suffix = nameOffset <= 0 ? "" : "_" + nameOffset;
                File excelFile = new File(dir, fileName + suffix + ".xlsx");
                if (excelFile.exists()) {
                    excelFile.delete();
                    excelFile.createNewFile();
                }
                Timber.d("-----》 " + excelFile.getPath());

                SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook());
                Sheet sheet = workbook.createSheet(tableName);
                writeHeaderRow(workbook, sheet, colName);

                CellStyle style = getStyle(workbook);
                for (int row = 0; row < dataList.size(); row++) {
                    List<String> strings = dataList.get(row);

                    int realNum = row + 1;
                    int rowNum = row + numOffset + 1;
                    publishProgress(rowNum, maxSize);
                    Timber.d("当前写入行：" + realNum);

                    Row pRow = sheet.createRow(realNum);
                    pRow.setHeight((short) 950);

                    for (int column = 0; column < strings.size(); column++) {
                        String content = strings.get(column);

                        Cell cell;
                        if (column < strings.size() - 2) {
                            cell = pRow.createCell(column, HSSFCell.CELL_TYPE_STRING);
                            cell.setCellValue(!TextUtils.isEmpty(content) ? content : "");
                            cell.setCellStyle(style);
                        }
                        //最后两个是图片
                        else {
                            cell = pRow.createCell(column, HSSFCell.CELL_TYPE_BLANK);
                            cell.setCellStyle(style);

                            byte[] bytes = readBitmap(content);
                            if (bytes != null) {
                                Drawing patriarch = sheet.createDrawingPatriarch();
                                XSSFClientAnchor anchor = new XSSFClientAnchor(5, 5, 80, 80, (short) column, rowNum, (short) (column + 1), rowNum + 1);
                                anchor.setAnchorType(HSSFClientAnchor.DONT_MOVE_AND_RESIZE);
                                // 插入图片
                                patriarch.createPicture(anchor, workbook.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_JPEG));
                            } else {
                                Timber.d("写入图片时出现错误：创建空格");
                                cell = pRow.createCell(column, HSSFCell.CELL_TYPE_STRING);
                                cell.setCellValue("");
                            }
                        }
                    }
                }

                Timber.d("创建数据完毕，准备写出");
                writeToFile(workbook, excelFile.getPath());
                Timber.d("写出完毕");

                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return -2;
            }
        }


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


        private void writeHeaderRow(org.apache.poi.ss.usermodel.Workbook workbook, Sheet sheet, String[] colName) {
            CellStyle cellStyle = getColumnTopStyle(workbook);
            Row row = sheet.createRow(0);
            for (int i = 0; i < colName.length; i++) {
                String name = colName[i];
                Cell cell = row.createCell(i);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                HSSFRichTextString text = new HSSFRichTextString(name);
                cell.setCellValue(text);
                cell.setCellStyle(cellStyle);
            }
        }

        *//*
     * 列头单元格样式
     *//*
        public CellStyle getColumnTopStyle(org.apache.poi.ss.usermodel.Workbook workbook) {
            // 设置字体
            Font font = workbook.createFont();

            // 设置字体大小
            font.setFontHeightInPoints((short) 11);
            // 字体加粗
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            // 设置字体名字
            font.setFontName("Courier New");
            // 设置样式
            CellStyle style = workbook.createCellStyle();
            // 设置低边框
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            // 设置低边框颜色
            style.setBottomBorderColor(HSSFColor.BLACK.index);
            // 设置右边框
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);
            // 设置顶边框
            style.setTopBorderColor(HSSFColor.BLACK.index);
            // 设置顶边框颜色
            style.setTopBorderColor(HSSFColor.BLACK.index);
            // 在样式中应用设置的字体
            style.setFont(font);
            // 设置自动换行
            style.setWrapText(false);
            // 设置水平对齐的样式为居中对齐；
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            return style;
        }


        public CellStyle getStyle(org.apache.poi.ss.usermodel.Workbook workbook) {
            Font font = workbook.createFont();// 设置字体
            font.setFontHeightInPoints((short) 10);// 设置字体大小
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 字体加粗
            font.setFontName("Courier New");// 设置字体名字

            // 设置样式;
            CellStyle style = workbook.createCellStyle();

            style.setFont(font);// 在样式用应用设置的字体;
            style.setWrapText(false);// 设置自动换行;
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 设置水平对齐的样式为居中对齐;
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 设置垂直对齐的样式为居中对齐;

            style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 设置顶边框;
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// 设置底边框;
            style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 设置左边框;
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 设置右边框;

            style.setTopBorderColor(HSSFColor.BLACK.index);// 设置顶边框颜色;
            style.setBottomBorderColor(HSSFColor.BLACK.index);// 设置底边框颜色;
            style.setLeftBorderColor(HSSFColor.BLACK.index);// 设置左边框颜色;
            style.setRightBorderColor(HSSFColor.BLACK.index);// 设置右边框颜色;
            return style;
        }

        private byte[] readBitmap(String imgPath) {
            ByteArrayOutputStream imgBytesOut = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imgBytesOut);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            byte[] clone = imgBytesOut.toByteArray().clone();
            try {
                imgBytesOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return clone;
        }

        private void writeToFile(org.apache.poi.ss.usermodel.Workbook workbook, String filePath) {
            BufferedOutputStream outputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(new File(filePath), true));
                workbook.write(outputStream);
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (outputStream == null) {
                    return;
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

}
