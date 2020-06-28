package com.yunbiao.ybsmartcheckin_live_id.activity_temper_check_in;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.alibaba.fastjson.JSONObject;
import com.yunbiao.faceview.FaceManager;
import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.business.SyncManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.DaoManager;
import com.yunbiao.ybsmartcheckin_live_id.db2.Depart;
import com.yunbiao.ybsmartcheckin_live_id.db2.User;
import com.yunbiao.ybsmartcheckin_live_id.utils.SpUtils;
import com.yunbiao.ybsmartcheckin_live_id.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import org.jetbrains.annotations.Async;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.xutils.common.util.FileUtil;

import okhttp3.Call;
import okhttp3.Request;
import timber.log.Timber;

public class ReadExcel {

    public static void importDataToLocal(@NonNull int compId, @NonNull List<BatchImportActivity.UserCheckBean> list, @NonNull ImportCallback callback) {
        ImportTask importTask = new ImportTask(compId, list, callback);
        importTask.execute();
    }

    public static void submitExcelToServer(@NonNull Context context, @NonNull Uri uri, @NonNull SubmitCallback callback) {
        String name = getFileRealNameFromUri(context, uri);
        File tempDir = new File(Environment.getExternalStorageDirectory(), "ybTemp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File excelFile = new File(tempDir, name);
        if (excelFile.exists()) {
            excelFile.delete();
        }

        try {
            FileUtils.copyInputStreamToFile(context.getContentResolver().openInputStream(uri), excelFile);
            if (!excelFile.exists()) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int comid = SpUtils.getCompany().getComid();
        Timber.d("提交Excel：" + ResourceUpdate.IMPORT_USER_BY_EXCEL);
        Timber.d("参数：" + comid);
        Timber.d("文件：" + excelFile.getPath());
        OkHttpUtils.post()
                .url(ResourceUpdate.IMPORT_USER_BY_EXCEL)
                .addParams("comId", comid + "")
                .addFile("entryexcel", excelFile.getName(), excelFile)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        callback.onStart();
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Timber.d("错误：" + e);
                        callback.onSubmitResult(-1,null,e.getMessage());
                        callback.onFinish();
                    }

                    @Override
                    public void onResponse(String response, int id) {

                        String log = "";
                        if(!TextUtils.isEmpty(response)){
                            log = response.length() > 30 ? (response.substring(0,30) + "...") : response;
                        }
                        Timber.d("响应：" + log);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        Integer status = jsonObject.getInteger("status");
                        if(status != 1){
                            callback.onSubmitResult(-1,null,"");
                            callback.onFinish();
                            return;
                        }

                        Integer faileNum = jsonObject.getInteger("ortherNum");
                        Integer addNum = jsonObject.getInteger("addNum");
                        Integer repeatNum = jsonObject.getInteger("repeatNum");
                        SyncManager.instance().requestUser(new SyncManager.SyncCallback() {
                            @Override
                            public void onFailed(String message) {
                                callback.onSubmitResult(-2,null,message);
                                callback.onFinish();
                            }

                            @Override
                            public void onFinish() {
                                callback.onSubmitResult(status,new ImportResult(-1,addNum,faileNum,repeatNum),"");
                                callback.onFinish();
                            }
                        });
                    }
                });
    }

    public static void readEntries(Context context, File file, @NonNull ReadCallback callback) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            ReadTask readTask = new ReadTask(file.getName(), fis, callback);
            readTask.execute();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            callback.onReadData(null, new ErrorInfo(ErrorCode.OPEN_STREAM_ERROR, e.getMessage()));
        }
    }

    public static void readEntries(Context context, Uri uri, @NonNull ReadCallback callback) {
        InputStream is;
        try {
            is = context.getContentResolver().openInputStream(uri);
            String name = getFileRealNameFromUri(context, uri);
            ReadTask readTask = new ReadTask(name, is, callback);
            readTask.execute();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            callback.onReadData(null, new ErrorInfo(ErrorCode.OPEN_STREAM_ERROR, e.getMessage()));
        }
    }

    private static class ReadTask extends AsyncTask<InputStream, Void, List<Entry>> {
        private String fileName;
        private InputStream is;
        private ReadCallback callback;

        public ReadTask(@NonNull String fileName, @NonNull InputStream is, @NonNull ReadCallback readCallback) {
            this.fileName = fileName;
            this.is = is;
            this.callback = readCallback;
        }

        @Override
        protected void onPreExecute() {
            callback.onStartRead();
        }

        @Override
        protected List<Entry> doInBackground(InputStream... inputStreams) {
            File tempDir = new File(Environment.getExternalStorageDirectory(), "ybTemp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            Timber.d("创建临时目录：" + tempDir.getPath() + " --- " + tempDir.exists());

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
                    callback.onReadData(null, new ErrorInfo(ErrorCode.OPEN_EXCEL_ERROR, e.getMessage()));
                    return null;
                }
            }

            Sheet sheet = wookbook.getSheetAt(0);
            int totalRowNum = sheet.getLastRowNum();
            Timber.d("总行数：" + totalRowNum);

            // 判断用07还是03的方法获取图片
            Map<Integer, PictureData> maplist = fileName.endsWith(".xls") ? getPictures1((HSSFSheet) sheet) : getPictures2((XSSFSheet) sheet);
            if (maplist == null || maplist.size() <= 0) {
                callback.onReadData(null, new ErrorInfo(ErrorCode.READ_IMG_ERROR, "Read Image Failed"));
                return null;
            }

            // 获得表头
            Row rowHead = sheet.getRow(0);
            // 判断表头是否正确
            if (rowHead.getPhysicalNumberOfCells() != 9) {
                callback.onReadData(null, new ErrorInfo(ErrorCode.FORMAT_ERROR_HEAD, "Incorrect number of headers"));
                return null;
            }
            //列数
            int totalColumnNum = rowHead.getPhysicalNumberOfCells();

            List<Entry> entries = new ArrayList<>();
            for (int rowIndex = 1; rowIndex < totalRowNum; rowIndex++) {
                Entry entry = new Entry();
                entry.setIndex(rowIndex);
                Row row = sheet.getRow(rowIndex);
                for (int colIndex = 0; colIndex < totalColumnNum; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    String cellValue = getCellValue(cell);
                    switch (colIndex) {
                        case 0:
                            entry.setDepName(cellValue);
                            break;
                        case 1:
                            entry.setDepErName(cellValue);
                            break;
                        case 2:
                            entry.setNumber(cellValue);
                            break;
                        case 3:
                            entry.setName(cellValue);
                            break;
                        case 4:
                            entry.setSex(TextUtils.equals("男", cellValue) || TextUtils.equals("Male", cellValue) ? 1 : 0);
                            break;
                        case 5:
                            entry.setPosition(cellValue);
                            break;
                        case 6:
                            entry.setPhone(cellValue);
                            break;
                        case 8:
                            entry.setStatus(TextUtils.equals("1", cellValue) ? 1 : 0);
                            break;
                    }
                }
                String s = copyImage(tempDir, entry.getIndex(), entry.getNumber() + "_" + entry.getName(), maplist);
                entry.setPic(s);
                entry.setDepartName(entry.getDepName() + "-" + entry.getDepErName());
                entries.add(entry);
            }
            return entries;
        }

        @Override
        protected void onPostExecute(List<Entry> entryList) {
            if (entryList != null) {
                callback.onReadData(entryList, new ErrorInfo(ErrorCode.COMPLETE, ""));
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ImportTask extends AsyncTask<List<BatchImportActivity.UserCheckBean>, Void, ImportResult> {
        private int compId;
        private List<BatchImportActivity.UserCheckBean> ucList;
        private ImportCallback callback;

        public ImportTask(int compId, List<BatchImportActivity.UserCheckBean> ucList, ImportCallback callback) {
            this.compId = compId;
            this.ucList = ucList;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            callback.onStartImport();
        }

        @Override
        protected ImportResult doInBackground(List<BatchImportActivity.UserCheckBean>... lists) {
            ImportResult importResult = new ImportResult();

            for (BatchImportActivity.UserCheckBean checkBean : ucList) {
                if (!checkBean.isChecked()) {
                    importResult.skipNum++;
                    continue;
                }
                ReadExcel.Entry entry = checkBean.getEntry();
                Depart depart = checkDepart(compId, entry.getDepName());
                IdBean userId = createUserId(compId);
                if (isUserExists(compId, entry.getNumber())) {
                    importResult.alreadyExists++;
                    continue;
                }

                User user = new User();
                user.setId(userId.getId());
                user.setFaceId(String.valueOf(userId.getFaceId()));
                user.setDepartId(depart.getDepId());
                user.setName(entry.getName());
                user.setDepartName(entry.getDepName());
                user.setPosition(entry.getPosition());
                user.setNumber(entry.getNumber());
                user.setHeadPath(entry.getPic());

                boolean addResult = FaceManager.getInstance().addUser(user.getFaceId(), user.getHeadPath());
                if (addResult) {
                    importResult.successNum++;
                    long add = DaoManager.get().add(user);
                } else {
                    importResult.failedNum++;
                }
            }
            return importResult;
        }

        @Override
        protected void onPostExecute(ImportResult importResult) {
            callback.onImportComplete(importResult);
        }
    }


    public static class ImportResult {
        int skipNum = 0;
        int successNum = 0;
        int failedNum = 0;
        int alreadyExists = 0;

        public ImportResult() {
        }

        public ImportResult(int skipNum, int successNum, int failedNum, int alreadyExists) {
            this.skipNum = skipNum;
            this.successNum = successNum;
            this.failedNum = failedNum;
            this.alreadyExists = alreadyExists;
        }
    }

    private static boolean isUserExists(int compId, String number) {
        return DaoManager.get().queryNumberExists(compId, number);
    }

    private static IdBean createUserId(int compId) {
        long id = 0;
        long faceId = 0;
        List<User> users = DaoManager.get().queryUserByCompId(compId);
        if (users != null && users.size() > 0) {
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                //先取出最大的Id
                if (user.getId() > id) {
                    id = user.getId();
                }
                //再取出最大的FaceId
                int i1 = Integer.parseInt(user.getFaceId());
                if (i1 > faceId) {
                    faceId = i1;
                }
            }
        }
        id += 1;
        faceId += 1;
        Timber.d("createUserId: 生成Id：" + id + " ----- " + faceId);
        return new IdBean(id, faceId);
    }

    private static Depart checkDepart(int compId, String depName) {
        Depart depart;
        if (!DaoManager.get().checkDepartExists(compId, depName)) {
            depart = new Depart();
            depart.setCompId(compId);
            depart.setDepName(depName);
            List<Depart> departs = DaoManager.get().queryDepartByCompId(compId);
            long departId = 0l;
            if (departs != null) {
                for (Depart depart1 : departs) {
                    long depId = depart1.getDepId();
                    if (departId <= depId) {
                        departId = depId;
                    }
                }
            }
            departId += 1l;
            depart.setDepId(departId);
            depart.setId(departId);
            DaoManager.get().add(depart);
        } else {
            depart = DaoManager.get().queryDepartByComIdAndName(compId, depName);
        }
        return depart;
    }

    private static String copyImage(File file, int index, String fileName, Map<Integer, PictureData> maplist) {
        String path = "";
        if (maplist.containsKey(index)) {
            PictureData pictureData = maplist.get(index);
            byte[] data = pictureData.getData();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            File imgFile = new File(file, System.currentTimeMillis() + "_" + fileName + ".jpg");
            if (imgFile.exists()) {
                path = imgFile.getPath();
                return path;
            }
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(imgFile));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                path = imgFile.getPath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return path;
    }

    private static String getCellValue(Cell cell) {
        String value;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                value = String.valueOf((long) cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                value = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_BLANK:
            default:
                value = null;
                break;
        }
        return value;
    }

    private static String getFileRealNameFromUri(Context context, Uri fileUri) {
        if (context == null || fileUri == null) return null;
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, fileUri);
        if (documentFile == null) return null;
        return documentFile.getName();
    }

    /**
     * 获取图片和位置 (xls)
     *
     * @param sheet
     * @return
     * @throws IOException
     */
    private static Map<Integer, PictureData> getPictures1(HSSFSheet sheet) {
        Map<Integer, PictureData> map = new HashMap<Integer, PictureData>();
        HSSFPatriarch drawingPatriarch = sheet.getDrawingPatriarch();
        if (drawingPatriarch != null) {
            List<HSSFShape> list = drawingPatriarch.getChildren();
            for (HSSFShape shape : list) {
                if (shape instanceof HSSFPicture) {
                    HSSFPicture picture = (HSSFPicture) shape;
                    HSSFClientAnchor cAnchor = (HSSFClientAnchor) picture
                            .getAnchor();
                    PictureData pdata = picture.getPictureData();
                    Integer key = cAnchor.getRow1(); // 行号-列号
                    if (map.get(key) != null) {
                        key++;
                    }
                    map.put(key, pdata);
                }
            }
        }
        return map;
    }

    /**
     * 获取图片和位置 (xlsx)
     *
     * @param sheet
     * @return
     * @throws IOException
     */
    private static Map<Integer, PictureData> getPictures2(XSSFSheet sheet) {
        Map<Integer, PictureData> map = new HashMap<>();
        List<POIXMLDocumentPart> list = sheet.getRelations();
        int totalRowNum = sheet.getLastRowNum();
        if (totalRowNum == list.size()) {
            for (POIXMLDocumentPart part : list) {
                if (part instanceof XSSFDrawing) {
                    XSSFDrawing drawing = (XSSFDrawing) part;
                    List<XSSFShape> shapes = drawing.getShapes();
                    for (XSSFShape shape : shapes) {
                        XSSFPicture picture = (XSSFPicture) shape;
                        XSSFClientAnchor anchor = picture.getPreferredSize();
                        CTMarker marker = anchor.getFrom();
                        Integer key = marker.getRow();
                        map.put(key, picture.getPictureData());
                    }
                }
            }
        }
        return map;
    }

    public static class Entry {
        int index;
        String name;
        String position;
        String number;
        String phone;
        String pic;
        String depName;
        String depErName;
        String departName;
        int status;
        int sex;

        int Tag;

        public int getTag() {
            return Tag;
        }

        public void setTag(int tag) {
            Tag = tag;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getDepartName() {
            return departName;
        }

        public void setDepartName(String departName) {
            this.departName = departName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPic() {
            return pic;
        }

        public void setPic(String pic) {
            this.pic = pic;
        }

        public String getDepName() {
            return depName;
        }

        public void setDepName(String depName) {
            this.depName = depName;
        }

        public String getDepErName() {
            return depErName;
        }

        public void setDepErName(String depErName) {
            this.depErName = depErName;
        }

        public int getSex() {
            return sex;
        }

        public void setSex(int sex) {
            this.sex = sex;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "index=" + index +
                    ", name='" + name + '\'' +
                    ", position='" + position + '\'' +
                    ", number='" + number + '\'' +
                    ", phone='" + phone + '\'' +
                    ", pic='" + pic + '\'' +
                    ", depName='" + depName + '\'' +
                    ", depErName='" + depErName + '\'' +
                    ", departName='" + departName + '\'' +
                    ", status=" + status +
                    ", sex=" + sex +
                    '}';
        }
    }


    public interface ErrorCode {
        int COMPLETE = 0;
        int OPEN_STREAM_ERROR = 1;
        int OPEN_EXCEL_ERROR = 2;
        int READ_IMG_ERROR = 3;
        int FORMAT_ERROR_HEAD = 4;
    }

    public static class ErrorInfo {
        int errCode;
        String errMsg;

        public ErrorInfo(int errCode, String errMsg) {
            this.errCode = errCode;
            this.errMsg = errMsg;
        }
    }

    static class IdBean {
        private long id;
        private long faceId;

        public IdBean(long id, long faceId) {
            this.id = id;
            this.faceId = faceId;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getFaceId() {
            return faceId;
        }

        public void setFaceId(long faceId) {
            this.faceId = faceId;
        }
    }

    public interface ReadCallback {
        void onStartRead();

        void onReadData(List<Entry> entryList, ErrorInfo errorInfo);
    }

    public interface ImportCallback {
        void onStartImport();

        void onImportComplete(ImportResult importResult);
    }

    public interface SubmitCallback {
        void onStart();

        void onSubmitResult(int result,ImportResult importResult,String errMsg);

        void onFinish();
    }
}
