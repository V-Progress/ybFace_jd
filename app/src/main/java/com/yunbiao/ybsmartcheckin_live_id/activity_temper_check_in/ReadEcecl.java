package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;

public class ReadEcecl {
    static class Entry {

        public void setName(String name) {

        }

        public void setPosition(String position) {
        }

        public void setNumber(String number) {
        }

        public void setPhone(String phone) {
        }

        public void setHead(String pic) {

        }

        public void setDepName(String depName) {

        }

        public void setDepErName(String depErName) {

        }

        public void setSex(int i) {

        }

        public void setDepartName(String s) {

        }
    }

    public interface ReadCallback{

    }

    public interface ErrorCode{
        int COMPLETE = 0;
        int OPEN_STREAM_ERROR = 1;
        int OPEN_EXCEL_ERROR = 2;
        int READ_IMG_ERROR = 3;
        int EXPORT_IMG_ERROR = 4;
        int FORMAT_ERROR_HEAD = 5;
    }

    public static class ErrorInfo{
        int errCode;
        String errMsg;

        public ErrorInfo(int errCode, String errMsg) {
            this.errCode = errCode;
            this.errMsg = errMsg;
        }
    }

    public static ErrorInfo readEntries(Context context, File file,ReadCallback callback){
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ErrorInfo(ErrorCode.OPEN_STREAM_ERROR,e.getMessage());
        }
        return readData(file.getName(),fis,callback);
    }

    public static ErrorInfo readEntries(Context context,Uri uri, ReadCallback callback){
        InputStream is;
        try {
            is = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ErrorInfo(ErrorCode.OPEN_STREAM_ERROR,e.getMessage());
        }
        String name = getFileRealNameFromUri(context, uri);
        return readData(name,is,callback);
    }

    private static ErrorInfo readData(String fileName, InputStream is,ReadCallback callback){
        Workbook wookbook = null;
        try {
            // 2003版本的excel，用.xls结尾
            wookbook = new HSSFWorkbook(is);// 得到工作簿
        } catch (Exception ex) {
            try {
                // 2007版本的excel，用.xlsx结尾
                wookbook = new XSSFWorkbook(is);// 得到工作簿
            } catch (IOException e) {
                e.printStackTrace();
                return new ErrorInfo(ErrorCode.OPEN_EXCEL_ERROR,e.getMessage());
            }
        }

        Sheet sheet = wookbook.getSheetAt(0);
        int totalRowNum = sheet.getLastRowNum();

        Map<Integer, PictureData> maplist = null;
        try{
            // 判断用07还是03的方法获取图片
            if (fileName.endsWith(".xls")) {
                maplist = getPictures1((HSSFSheet) sheet, totalRowNum);
            } else if (fileName.endsWith(".xlsx")) {
                maplist = getPictures2((XSSFSheet) sheet, totalRowNum);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ErrorInfo(ErrorCode.READ_IMG_ERROR,e.getMessage());
        }

        List<String> imgUrl = null;//图片路径集合
        try {
            imgUrl = printImg(maplist);
        } catch (IOException e) {
            e.printStackTrace();
            return new ErrorInfo(ErrorCode.EXPORT_IMG_ERROR,e.getMessage());
        }

        // 获得表头
        Row rowHead = sheet.getRow(0);
        // 判断表头是否正确
        Log.e(TAG, "readData: " + rowHead.getPhysicalNumberOfCells());
        if (rowHead.getPhysicalNumberOfCells() != 8) {
            Log.e(TAG,"表头的数量不对!");
            return new ErrorInfo(ErrorCode.FORMAT_ERROR_HEAD,"表头的数量不对!");
        }

        for (int i = 1; i < totalRowNum; i++) {
            // 获得第i行对象
            Row row = sheet.getRow(i);

            // 获得获得第i行第0列的 String类型对象
            Cell cell = row.getCell((short) 0);
            String depName = cell.getStringCellValue();

            cell = row.getCell((short) 1);
            String depErName = cell.getStringCellValue();

            cell = row.getCell((short) 2);
            try {
                String number = (int) cell.getNumericCellValue() + "";
            } catch (Exception e) {
                String number = cell.getStringCellValue();
                e.printStackTrace();
            }

            cell = row.getCell((short) 3);
            String name = cell.getStringCellValue();

            cell = row.getCell((short) 4);
            String position = cell.getStringCellValue();

            cell = row.getCell((short) 5);
            String phone = cell.getStringCellValue();



        }



        return null;
    }

    public static String getFileRealNameFromUri(Context context, Uri fileUri) {
        if (context == null || fileUri == null) return null;
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, fileUri);
        if (documentFile == null) return null;
        return documentFile.getName();
    }

/*

    public static List<Entry> getDataFromExcel(Context context, Uri uri){

        // 判断表头是否正确
        System.out.println(rowHead.getPhysicalNumberOfCells());
        if (rowHead.getPhysicalNumberOfCells() != 8) {
            System.out.println("表头的数量不对!");
        }
        // 要获得属性
        String depName = "";
        String depErName = "";
        String number;
        String name = "";
        String position = "";
        String phone = "";
        String pic = "";
        // 获得所有数据
        List<Entry> entries = new ArrayList<>();
        for (int i = 1; i <= totalRowNum; i++) {
            // 获得第i行对象
            Row row = sheet.getRow(i);

            // 获得获得第i行第0列的 String类型对象
            Cell cell = row.getCell((short) 0);
            depName = cell.getStringCellValue().toString();


            cell = row.getCell((short) 1);
            depErName = cell.getStringCellValue().toString();

            cell = row.getCell((short) 2);
            try {
                number = (int) cell.getNumericCellValue() + "";
            } catch (Exception e) {
                number = cell.getStringCellValue().toString();
                e.printStackTrace();
            }

            cell = row.getCell((short) 3);
            name = cell.getStringCellValue();

            cell = row.getCell((short) 4);
            position = cell.getStringCellValue();

            cell = row.getCell((short) 5);
            phone = cell.getStringCellValue();

//			cell = row.getCell((short) 6);
            pic = imgUrl.get(i - 1);
            Entry entry = new Entry();
            if (StringUtil.isNotNull(depName)) {
                if (StringUtil.isNotNull(depErName)) {
                    entry.setDepartName(depName + "-" + depErName);//存在二级部门 部门名称为 部门名称+"-"+子部门名称
                } else {
                    entry.setDepartName(depName);
                }
            }
            entry.setName(name);
            entry.setNumber(number);
            entry.setPosition(position);
            entry.setPhone(phone);
            entry.setHead(pic);
            entries.add(entry);
//			System.out.println("字段1：" + depName + ",字段2：" + depErName
//					+ ",字段3：" +number  + ",字段4：" + name + ",字段5：" + position
//					+",字段6:"+phone+",字段7:"+pic);
        }
//		for (Entry<String, PictureData> entry : maplist.entrySet()) {
//
//			System.out.println("Key = " + entry.getKey() + ", Value = "
//					+ entry.getValue());
//
//		}
        return entries;
    }
*/

    public static List<Entry> getDataFromExcel(String filePath) throws IOException {
        // 判断是否为excel类型文件
        if (!filePath.endsWith(".xls") && !filePath.endsWith(".xlsx")) {
            System.out.println("文件不是excel类型");
        }

        FileInputStream fis = null;
        Workbook wookbook = null;
        Sheet sheet = null;
        try {
            // 获取一个绝对地址的流
            fis = new FileInputStream(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // 2003版本的excel，用.xls结尾
            wookbook = new HSSFWorkbook(fis);// 得到工作簿
        } catch (Exception ex) {
            try {
                // 2007版本的excel，用.xlsx结尾
                fis = new FileInputStream(filePath);
                wookbook = new XSSFWorkbook(fis);// 得到工作簿
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<Integer, PictureData> maplist = null;

        sheet = wookbook.getSheetAt(0);
        // 获得数据的总行数
        int totalRowNum = sheet.getLastRowNum();
        // 判断用07还是03的方法获取图片
        if (filePath.endsWith(".xls")) {
            maplist = getPictures1((HSSFSheet) sheet, totalRowNum);
        } else if (filePath.endsWith(".xlsx")) {
            maplist = getPictures2((XSSFSheet) sheet, totalRowNum);
        }
        List<String> imgUrl = null;//图片路径集合
        try {
            imgUrl = printImg(maplist);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 得到一个工作表

        // 获得表头
        Row rowHead = sheet.getRow(0);

        // 判断表头是否正确
        System.out.println(rowHead.getPhysicalNumberOfCells());
        if (rowHead.getPhysicalNumberOfCells() != 8) {
            System.out.println("表头的数量不对!");
        }
        // 要获得属性
        String depName = "";
        String depErName = "";
        String number;
        String name = "";
        String position = "";
        String phone = "";
        String pic = "";
        // 获得所有数据
        List<Entry> entries = new ArrayList<>();
        for (int i = 1; i <= totalRowNum; i++) {
            // 获得第i行对象
            Row row = sheet.getRow(i);

            // 获得获得第i行第0列的 String类型对象
            Cell cell = row.getCell((short) 0);
            depName = cell.getStringCellValue().toString();


            cell = row.getCell((short) 1);
            depErName = cell.getStringCellValue().toString();

            cell = row.getCell((short) 2);
            try {
                number = (int) cell.getNumericCellValue() + "";
            } catch (Exception e) {
                number = cell.getStringCellValue().toString();
                e.printStackTrace();
            }

            cell = row.getCell((short) 3);
            name = cell.getStringCellValue().toString();

            cell = row.getCell((short) 4);
            position = cell.getStringCellValue().toString();

            cell = row.getCell((short) 5);
            phone = cell.getStringCellValue().toString();

//			cell = row.getCell((short) 6);
            pic = imgUrl.get(i - 1);
            Entry entry = new Entry();
            if (StringUtil.isNotNull(depName)) {
                if (StringUtil.isNotNull(depErName)) {
                    entry.setDepartName(depName + "-" + depErName);//存在二级部门 部门名称为 部门名称+"-"+子部门名称
                } else {
                    entry.setDepartName(depName);
                }
            }
            entry.setName(name);
            entry.setNumber(number);
            entry.setPosition(position);
            entry.setPhone(phone);
            entry.setHead(pic);
            entries.add(entry);
//			System.out.println("字段1：" + depName + ",字段2：" + depErName
//					+ ",字段3：" +number  + ",字段4：" + name + ",字段5：" + position
//					+",字段6:"+phone+",字段7:"+pic);
        }
//		for (Entry<String, PictureData> entry : maplist.entrySet()) {
//
//			System.out.println("Key = " + entry.getKey() + ", Value = "
//					+ entry.getValue());
//
//		}
        return entries;
    }


    public static List<Entry> getDataFromExcel(InputStream inputStream, String fileType) {
        // 判断是否为excel类型文件
        if (!fileType.equals("xls") && !fileType.equals("xlsx")) {
            System.out.println("文件不是excel类型");
        }

        FileInputStream fis = null;
        Workbook wookbook = null;
        Sheet sheet = null;
        try {
            // 获取一个绝对地址的流
            fis = (FileInputStream) inputStream;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // 2003版本的excel，用.xls结尾
            wookbook = new HSSFWorkbook(fis);// 得到工作簿
        } catch (Exception ex) {
            try {
                // 2007版本的excel，用.xlsx结尾
                fis = (FileInputStream) inputStream;
                wookbook = new XSSFWorkbook(fis);// 得到工作簿
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<Entry> entries = new ArrayList<>();
        Map<Integer, PictureData> maplist = null;
        if (wookbook == null) {
            return entries;
        }
        sheet = wookbook.getSheetAt(0);
        // 获得数据的总行数
        int totalRowNum = sheet.getLastRowNum();
        // 判断用07还是03的方法获取图片
        try {
            if (fileType.equals("xls")) {
                maplist = getPictures1((HSSFSheet) sheet, totalRowNum);
            } else if (fileType.endsWith("xlsx")) {
                maplist = getPictures2((XSSFSheet) sheet, totalRowNum);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        List<String> imgUrl = null;//图片路径集合
        try {
            imgUrl = printImg(maplist);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 得到一个工作表

        // 获得表头
        Row rowHead = sheet.getRow(0);

        // 判断表头是否正确
        System.out.println(rowHead.getPhysicalNumberOfCells());
        if (rowHead.getPhysicalNumberOfCells() != 8) {
            System.out.println("表头的数量不对!");
        }
        // 要获得属性
        String depName = "";
        String depErName = "";
        String number = null;
        String name = "";
        String sex = "";
        String position = "";
        String phone = "";
        String pic = "";
        // 获得所有数据
        int imgIndex = 0;
        for (int i = 1; i <= totalRowNum; i++) {
            // 获得第i行对象
            Row row = sheet.getRow(i);
            // 获得获得第i行第0列的 String类型对象
            Cell cell = row.getCell((short) 0);
            if (cell == null) {
                continue;
            }
            depName = cell.getStringCellValue().toString();
            if (StringUtil.isEmpty(depName) || StringUtil.isEmpty(depName.trim())) {
                continue;
            }
            imgIndex++;
        }
        if (imgIndex == imgUrl.size()) {
            for (int i = 1; i <= imgIndex; i++) {
                // 获得第i行对象
                Row row = sheet.getRow(i);
                // 获得获得第i行第0列的 String类型对象
                Cell cell = row.getCell((short) 0);
                try {
                    depName = cell.getStringCellValue().toString();
                } catch (Exception e3) {
                    depName = (int) cell.getNumericCellValue() + "";
                    e3.printStackTrace();
                }
                if (StringUtil.isEmpty(depName) || StringUtil.isEmpty(depName.trim())) {
                    continue;
                }
                depName = depName.trim();
//				System.out.println(i);
                cell = row.getCell((short) 1);
                if (cell != null) {
                    try {
                        depErName = cell.getStringCellValue().toString();
                    } catch (Exception e2) {
                        depErName = (int) cell.getNumericCellValue() + "";
                        e2.printStackTrace();
                    }
                }
                if (StringUtil.isNotNull(depErName)) {
                    depErName = depErName.trim();//去空格
                }
                cell = row.getCell((short) 2);
                if (cell != null) {
                    try {
                        number = (int) cell.getNumericCellValue() + "";
                    } catch (Exception e) {
                        number = cell.getStringCellValue().toString();
                        e.printStackTrace();
                    }
                }
                //去掉所以空格
                number = number.replaceAll(" ", "");
                cell = row.getCell((short) 3);
                if (cell != null) {
                    try {
                        name = cell.getStringCellValue().toString();
                    } catch (Exception e1) {
                        name = (int) cell.getNumericCellValue() + "";
                        e1.printStackTrace();
                    }
                }
                cell = row.getCell((short) 4);
                if (cell != null) {
                    try {
                        sex = cell.getStringCellValue().toString();
                    } catch (Exception e) {
                        sex = (int) cell.getNumericCellValue() + "";
                        e.printStackTrace();
                    }
                }
                if (StringUtil.isNotNull(sex)) {
                    sex = sex.replaceAll(" ", "");
                }
                cell = row.getCell((short) 5);
                if (cell != null) {
                    try {
                        position = cell.getStringCellValue().toString();
                    } catch (Exception e) {
                        position = (int) cell.getNumericCellValue() + "";
                        e.printStackTrace();
                    }
                }
                cell = row.getCell((short) 6);
                if (cell != null) {
                    int cellType = cell.getCellType();
                    if (cellType == 0) {
                        DataFormatter dataFormatter = new DataFormatter();
                        dataFormatter.addFormat("###########", null);
                        phone = dataFormatter.formatCellValue(cell);
                    } else {
                        phone = cell.toString();
                    }
                }
                if (StringUtil.isNotNull(phone)) {
                    phone = phone.replaceAll(" ", "");
                }
                pic = imgUrl.get(i - 1);
//		        imgIndex++;
                Entry entry = new Entry();
                entry.setDepName(depName);
                entry.setDepErName(depErName);
                if (StringUtil.isNotNull(depName)) {
                    if (StringUtil.isNotNull(depErName)) {
                        entry.setDepartName(depName + "-" + depErName);//存在二级部门 部门名称为 部门名称+"-"+子部门名称
                    } else {
                        entry.setDepartName(depName);
                    }
                }
                entry.setName(name);
                entry.setNumber(number);
                entry.setPosition(position);
                entry.setPhone(phone);
                entry.setHead(pic);
                if (TextUtils.isEmpty(sex)) {
                    entry.setSex(1);//默认 男
                } else {
                    entry.setSex(sex.equals("男") || sex.toUpperCase().equals("MALE") ? 1 : 0);
                }
                entries.add(entry);
//				System.out.println("字段1：" + depName + ",字段2：" + depErName
//						+ ",字段3：" +number  + ",字段4：" + name + ",字段5：" + position
//						+",字段6:"+phone+",字段7:"+pic);
            }
        }
//		for (Entry<String, PictureData> entry : maplist.entrySet()) {
//
//			System.out.println("Key = " + entry.getKey() + ", Value = "
//					+ entry.getValue());
//
//		}
        return entries;
    }


    /**
     * 获取图片和位置 (xls)
     *
     * @param sheet
     * @param totalRowNum
     * @return
     * @throws IOException
     */
    public static Map<Integer, PictureData> getPictures1(HSSFSheet sheet, int totalRowNum)
            throws IOException {
        Map<Integer, PictureData> map = new HashMap<Integer, PictureData>();
        HSSFPatriarch drawingPatriarch = sheet.getDrawingPatriarch();
        if (drawingPatriarch != null) {
            List<HSSFShape> list = drawingPatriarch.getChildren();
//			if(totalRowNum==list.size()){
            for (HSSFShape shape : list) {
                if (shape instanceof HSSFPicture) {
                    HSSFPicture picture = (HSSFPicture) shape;
                    HSSFClientAnchor cAnchor = (HSSFClientAnchor) picture
                            .getAnchor();
                    PictureData pdata = picture.getPictureData();
//						String key = c; // 行号-列号
                    Integer key = cAnchor.getRow1(); // 行号-列号
//					System.out.println(cAnchor.getRow1() + "-" + cAnchor.getCol1());
//						System.out.println(index);
                    if (map.get(key) != null) {
                        key++;
                    }
//					System.out.println(key);
                    map.put(key, pdata);
                }
            }
//			}
        }
        System.out.println("111" + map.size());
        return map;
    }

    /**
     * 获取图片和位置 (xlsx)
     *
     * @param sheet
     * @param totalRowNum
     * @return
     * @throws IOException
     */
    public static Map<Integer, PictureData> getPictures2(XSSFSheet sheet, int totalRowNum)
            throws IOException {
        Map<Integer, PictureData> map = new HashMap<Integer, PictureData>();
        List<POIXMLDocumentPart> list = sheet.getRelations();
        if (totalRowNum == list.size()) {
            for (POIXMLDocumentPart part : list) {
                if (part instanceof XSSFDrawing) {
                    XSSFDrawing drawing = (XSSFDrawing) part;
                    List<XSSFShape> shapes = drawing.getShapes();
                    for (XSSFShape shape : shapes) {
                        XSSFPicture picture = (XSSFPicture) shape;
                        XSSFClientAnchor anchor = picture.getPreferredSize();
                        CTMarker marker = anchor.getFrom();
//						String key = marker.getRow() + "-" + marker.getCol();
                        Integer key = marker.getRow();
                        map.put(key, picture.getPictureData());
                    }
                }
            }
        }
        return map;
    }
    // 图片写出
    public static List<String> printImg(Map<Integer, PictureData> sheetList)
            throws IOException {
        List<String> imgUrl = new ArrayList<>();
        // for (Map<String, PictureData> map : sheetList) {
        Object[] key = sheetList.keySet().toArray();
        Arrays.sort(key);
        for (int i = 0; i < sheetList.size(); i++) {
            // 获取图片流
            PictureData pic = sheetList.get(key[i]);
            // 获取图片索引
//			String picName = key[i].toString();
            // 获取图片格式
            String ext = pic.suggestFileExtension();

            byte[] data = pic.getData();

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            Log.e(TAG, "printImg: 图片大小：" + bitmap.getWidth() + " --- " + bitmap.getHeight());
        }
        return imgUrl;
    }

    private static final String TAG = "ReadEcecl";
    public static void main(String[] args) throws Exception {
        getDataFromExcel("C:\\Users\\sybyt\\Desktop\\公司电脑迁移\\" + "人员批量上传模板.xls");

    }

   /* public static List<FileEntity> getFilesFromRequest(HttpServletRequest request) {
        List<FileEntity> files = new ArrayList<FileEntity>();

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        try {
            for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
                InputStream inputstream = entity.getValue().getInputStream();
//		        if (!(inputstream.markSupported())) {
//		          inputstream = new PushbackInputStream(inputstream, 8);
//		        }

                String fileName = entity.getValue().getOriginalFilename();
                String prefix =
                        fileName.lastIndexOf(".") >= 1 ? fileName.substring(fileName.lastIndexOf(".") + 1) : null;
                FileEntity file = new FileEntity();
                file.setInputStream(inputstream);
                file.setFileType(prefix);
                file.setFileName(fileName);
                files.add(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }*/

    static class StringUtil {

        public static boolean isNotNull(String depErName) {
            return depErName != null;
        }

        public static boolean isEmpty(String depName) {
            return TextUtils.isEmpty(depName);
        }
    }
}
