package com.yunbiao.ybsmartcheckin_live_id.utils.excel;


import android.text.TextUtils;

import com.yunbiao.ybsmartcheckin_live_id.OutputLog;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import timber.log.Timber;

public class POIExport extends Export{

    public POIExport(String filePath, String tableName, String[] colName, ExportCallback callback) {
        super(filePath, tableName, colName, callback);
    }

    @Override
    public int createDataExcel(File dir, String fileName, int nameOffset, String tableName, List<List<String>> dataList, int maxSize, int numOffset) {
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
            OutputLog.getInstance().addExportLog("创建文件：" + excelFile.getPath());

            SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook());
            Sheet sheet = workbook.createSheet(tableName);
            writeHeaderRow(workbook, sheet, colName);

            CellStyle style = getStyle(workbook);
            for (int row = 0; row < dataList.size(); row++) {
                List<String> strings = dataList.get(row);

                int realNum = row + 1;
                int progress = row + numOffset + 1;
                publishProgress(progress, maxSize);
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
                            try{
                                Drawing patriarch = sheet.createDrawingPatriarch();
                                XSSFClientAnchor anchor = new XSSFClientAnchor(5, 5, 80, 80, (short) column, realNum, (short) (column + 1), realNum + 1);
                                anchor.setAnchorType(HSSFClientAnchor.DONT_MOVE_AND_RESIZE);
                                // 插入图片
                                patriarch.createPicture(anchor, workbook.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_JPEG));
                            }catch (Exception e){
                                e.printStackTrace();
                                OutputLog.getInstance().addExportLog("写入图片时出现错误：" + e.getMessage());
                                Timber.d("写入图片时出现错误：创建空格");
                                cell = pRow.createCell(column, HSSFCell.CELL_TYPE_STRING);
                                cell.setCellValue("");
                            }
                        } else {
                            OutputLog.getInstance().addExportLog("读取图片失败");
                            Timber.d("写入图片时出现错误：创建空格");
                            cell = pRow.createCell(column, HSSFCell.CELL_TYPE_STRING);
                            cell.setCellValue("");
                        }
                    }
                }
            }

            autoCellSize(sheet,colName.length,sheet.getLastRowNum());

            Timber.d("创建数据完毕，准备写出");
            OutputLog.getInstance().addExportLog("准备写出");
            writeToFile(workbook, excelFile.getPath());
            OutputLog.getInstance().addExportLog("写出成功");
            Timber.d("写出完毕");
            return 1;
        } catch (Exception e) {
            OutputLog.getInstance().addExportLog("写出异常：" + e.getMessage());
            e.printStackTrace();
            return -2;
        }
    }

    private void autoCellSize(Sheet sheet,int colNumber,int rowNumber){
        // 让列宽随着导出的列长自动适应
        for (int colNum = 0; colNum < colNumber; colNum++) {
            int columnWidth1 = sheet.getColumnWidth(colNum);
            Timber.d("当前列宽：" + columnWidth1);
            int columnWidth = columnWidth1 / 256;
            Timber.d("处理后列宽：" + columnWidth);
            for (int rowNum = 0; rowNum < rowNumber; rowNum++) {
                //获取当前行
                Row currentRow = sheet.getRow(rowNum) == null ? sheet.createRow(rowNum) : sheet.getRow(rowNum);

                if (currentRow.getCell(colNum) != null) {
                    Cell currentCell = currentRow.getCell(colNum);
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

    private void writeHeaderRow(Workbook workbook, Sheet sheet, String[] colName) {
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

    private CellStyle getStyle(Workbook workbook) {
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

    /*
     * 列头单元格样式
     */
    private CellStyle getColumnTopStyle(Workbook workbook) {
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

    private void writeToFile(Workbook workbook, String filePath) {
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
}
