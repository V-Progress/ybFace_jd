package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;


import androidx.annotation.NonNull;

import com.yunbiao.ybsmartcheckin_live_id.printer.T;

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
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
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
        Export export = new Export(filePath, tableName, colName, callback);
        export.execute();
    }

    /***
     * 导出类
     */
    public static class Export extends AsyncTask<Void,Void,Integer> {
        String filePath;
        String tableName;
        String[] colName;
        ExportCallback callback;

        public Export(@NonNull String filePath, @NonNull String tableName, @NonNull String[] colName, @NonNull ExportCallback callback) {
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
        protected Integer doInBackground(Void... voids) {
            //创建工作簿
            HSSFWorkbook workbook = new HSSFWorkbook();
            //创建表格
            HSSFSheet sheet = workbook.createSheet(tableName);
            //写入标题
            writeHeaderRow(workbook, sheet, colName);
            //写入数据
            writeData(workbook, sheet, callback.getDataList(), colName);
            //写出到Excel文件
            writeToExcel(workbook, filePath);
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            callback.onFinish(result,filePath);
        }

        private void writeHeaderRow(HSSFWorkbook workbook, HSSFSheet sheet, String[] colName) {
            //获取首行Style
            HSSFCellStyle headerStyle = getColumnTopStyle(workbook);
            //写入首行标题
            HSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < colName.length; i++) {
                String name = colName[i];
                //设置格元素
                HSSFCell headerCell = headerRow.createCell(i);
                headerCell.setCellType(HSSFCell.CELL_TYPE_STRING);
                HSSFRichTextString text = new HSSFRichTextString(name);
                headerCell.setCellValue(text);
                headerCell.setCellStyle(headerStyle);
            }
        }

        private static void writeData(HSSFWorkbook workbook, HSSFSheet sheet, List<List<String>> stringListList, String[] colName) {
            HSSFCellStyle style = getStyle(workbook);
            //行循环
            for (int i = 0; i < stringListList.size(); i++) {
                List<String> strings = stringListList.get(i);
                HSSFRow row = sheet.createRow(i + 1);
                row.setHeight((short) 950);
                //列循环
                for (int j = 0; j < strings.size(); j++) {
                    String jStr = strings.get(j);
                    HSSFCell cell;
                    //前几个是文字
                    if (j < strings.size() - 2) {
                        cell = row.createCell(j, HSSFCell.CELL_TYPE_STRING);
                        cell.setCellValue(!TextUtils.isEmpty(jStr) ? jStr : "");
                        cell.setCellStyle(style);
                    }
                    //最后两个是图片
                    else {
                        cell = row.createCell(j, HSSFCell.CELL_TYPE_BLANK);
                        cell.setCellStyle(style);

                        ByteArrayOutputStream imgBytesOut = null;
                        try {
                            imgBytesOut = new ByteArrayOutputStream();
                            Bitmap bitmap = BitmapFactory.decodeFile(jStr);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imgBytesOut);

                            // 画图的顶级管理器，一个sheet只能获取一个
                            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
                            // anchor主要用于设置图片的属性
                            HSSFClientAnchor anchor = new HSSFClientAnchor(5, 5, 80, 80, (short) j, i + 1, (short) (j + 1), i + 2);
                            anchor.setAnchorType(HSSFClientAnchor.DONT_MOVE_AND_RESIZE);
                            // 插入图片
                            patriarch.createPicture(anchor, workbook.addPicture(
                                    imgBytesOut.toByteArray(),
                                    HSSFWorkbook.PICTURE_TYPE_JPEG));
                            if (imgBytesOut != null) {
                                imgBytesOut.flush();
                            }
                        } catch (Exception e) {
                            Timber.d("写入图片时出现错误：" + e.getMessage());
                            cell = row.createCell(j, HSSFCell.CELL_TYPE_STRING);
                            cell.setCellValue("");
                        } finally {
                            if (imgBytesOut != null) {
                                try {
                                    imgBytesOut.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            int colNumber = colName.length;
            int rowNumber = sheet.getLastRowNum();

            // 让列宽随着导出的列长自动适应
            for (int colNum = 0; colNum < colNumber; colNum++) {
                int columnWidth1 = sheet.getColumnWidth(colNum);
                Timber.d("当前列宽：" + columnWidth1);
                int columnWidth = columnWidth1 / 256;
                Timber.d("处理后列宽：" + columnWidth);
                for (int rowNum = 0; rowNum < rowNumber; rowNum++) {
                    //获取当前行
                    HSSFRow currentRow = sheet.getRow(rowNum) == null ? sheet.createRow(rowNum) : sheet.getRow(rowNum);

                    if (currentRow.getCell(colNum) != null) {
                        HSSFCell currentCell = currentRow.getCell(colNum);
                        if (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                            int length = currentCell.getStringCellValue()
                                    .getBytes().length;
                            if (columnWidth < length) {
                                columnWidth = length;
                            }
                        } else {

                        }
                    }
                }
                if (colNum == 0) {
                    sheet.setColumnWidth(colNum, (columnWidth - 2) * 256);
                } else {
                    sheet.setColumnWidth(colNum, (columnWidth + 4) * 256);
                }
            }
        }

        private static void writeToExcel(HSSFWorkbook workbook, String filePath) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(filePath));
                workbook.write(fos);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public static HSSFCellStyle getStyle(HSSFWorkbook workbook) {
            // 设置字体
            HSSFFont font = workbook.createFont();
            // 设置字体大小
            font.setFontHeightInPoints((short) 10);
            // 字体加粗
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            // 设置字体名字
            font.setFontName("Courier New");
            // 设置样式;
            HSSFCellStyle style = workbook.createCellStyle();
            // 设置底边框;
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            // 设置底边框颜色;
            style.setBottomBorderColor(HSSFColor.BLACK.index);
            // 设置左边框;
            style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            // 设置左边框颜色;
            style.setLeftBorderColor(HSSFColor.BLACK.index);
            // 设置右边框;
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);
            // 设置右边框颜色;
            style.setRightBorderColor(HSSFColor.BLACK.index);
            // 设置顶边框;
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);
            // 设置顶边框颜色;
            style.setTopBorderColor(HSSFColor.BLACK.index);
            // 在样式用应用设置的字体;
            style.setFont(font);
            // 设置自动换行;
            style.setWrapText(false);
            // 设置水平对齐的样式为居中对齐;
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            // 设置垂直对齐的样式为居中对齐;
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            return style;
        }

        /*
         * 列头单元格样式
         */
        public static HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {
            // 设置字体
            HSSFFont font = workbook.createFont();

            // 设置字体大小
            font.setFontHeightInPoints((short) 11);
            // 字体加粗
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            // 设置字体名字
            font.setFontName("Courier New");
            // 设置样式
            HSSFCellStyle style = workbook.createCellStyle();
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

        public interface ExportCallback{
            void onStart();
            List<List<String>> getDataList();
            void onFinish(int result, String filePath);
        }
    }


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
