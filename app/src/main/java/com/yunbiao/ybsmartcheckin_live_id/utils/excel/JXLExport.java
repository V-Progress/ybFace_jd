package com.yunbiao.ybsmartcheckin_live_id.utils.excel;


import android.text.TextUtils;

import java.io.File;
import java.util.List;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import timber.log.Timber;

public class JXLExport extends Export{
    public JXLExport(String filePath, String tableName, String[] colName, ExportCallback callback) {
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
}