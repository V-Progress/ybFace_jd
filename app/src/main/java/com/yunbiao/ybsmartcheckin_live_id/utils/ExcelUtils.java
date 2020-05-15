package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.text.TextUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
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
     * 初始化Excel
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

    public interface WriteDataCallback{
        void onStart();

        void onError(Exception e);

        void onSuccess();
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
}
