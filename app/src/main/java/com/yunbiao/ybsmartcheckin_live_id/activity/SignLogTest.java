package com.yunbiao.ybsmartcheckin_live_id.activity;

import com.yunbiao.ybsmartcheckin_live_id.afinel.Constants;
import com.yunbiao.ybsmartcheckin_live_id.db2.Sign;

import org.apache.commons.io.output.FileWriterWithEncoding;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class SignLogTest {
    private static SignLogTest instance;
    private int INITIAL = 5;
    private int PERIOD = 5;
    private TimeUnit unit = TimeUnit.MINUTES;

    private StringBuffer faceRecordContent = new StringBuffer();
    private StringBuffer saveRecordContent = new StringBuffer();
    private StringBuffer uploadRecordContent = new StringBuffer();
    private StringBuffer uploadArrayRecordContent = new StringBuffer();

    private File faceFile;
    private File saveFile;
    private File uploadFile;
    private File arrayFile;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private SignLogTest(){
        if(Constants.DEVICE_TYPE != Constants.DeviceType.CHECK_IN){
            return;
        }

        String dirPath = Constants.LOGO_DIR_PATH + "/test/";

        String faceFilePath = dirPath + "/face";
        String saveFilePath = dirPath + "/save";
        String uploadFilePath = dirPath + "/upload";
        String arrayFilePath = dirPath + "/array";

        File dirFile = new File(dirPath);
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }

        faceFile = new File(faceFilePath);
        if(!faceFile.exists()){
            try {
                faceFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        saveFile = new File(saveFilePath);
        if(!saveFile.exists()){
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        uploadFile = new File(uploadFilePath);
        if(!uploadFile.exists()){
            try {
                uploadFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        arrayFile = new File(arrayFilePath);
        if(!arrayFile.exists()){
            try {
                arrayFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable writeRunnable = () -> {
            writeFace();
            writeSave();
            writeUpload();
            writeArray();
        };
        scheduledExecutorService.scheduleAtFixedRate(writeRunnable,INITIAL,PERIOD, unit);
    }

    public static SignLogTest getInstance(){
        if(instance == null){
            synchronized (SignLogTest.class){
                if(instance == null){
                    instance = new SignLogTest();
                }
            }
        }
        return instance;
    }

    public void addFaceContent(long time, String content){
        faceRecordContent.append("\n")
                .append(dateFormat.format(time))
                .append(" --> ")
                .append(time)
                .append(" --> ")
                .append(content);
    }

    public void addSaveContent(long time, String content){
        saveRecordContent.append("\n")
                .append(dateFormat.format(time))
                .append(" --> ")
                .append(time)
                .append(" --> ")
                .append(content);
    }

    public void addUploadContent(long time, String content){
        uploadRecordContent.append("\n")
                .append(dateFormat.format(time))
                .append(" --> ")
                .append(time)
                .append(" --> ")
                .append(content);
    }

    public void addArrayContent(List<Sign> signList, String response){
        long time = System.currentTimeMillis();
        uploadArrayRecordContent.append("\n")
                .append(dateFormat.format(time))
                .append(" --> ")
                .append(time)
                .append(" --> ")
                .append(response)
                .append(" --> ");
        if(signList == null || signList.size() <= 0){
            uploadRecordContent.append("空");
        } else {
            for (Sign sign : signList) {
                uploadRecordContent.append("[").append(sign.getTime()).append(",").append(sign.getName()).append("]").append("，");
            }
        }
    }

    private void writeFace(){
        if(!faceFile.exists() || faceRecordContent.length() <= 0){
            Timber.d("暂无人脸记录");
            return;
        }
        BufferedWriter writer = null;
        try {
            String content = new String(faceRecordContent.toString().getBytes(),StandardCharsets.UTF_8);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(faceFile,true),StandardCharsets.UTF_8));
            writer.write(content);
            writer.flush();
            faceRecordContent.setLength(0);
            Timber.d("人脸记录写入完成");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(writer);
        }
    }

    private void writeSave(){
        if(!saveFile.exists() || saveRecordContent.length() <= 0){
            Timber.d("暂无存储记录");
            return;
        }
        BufferedWriter writer = null;
        try {
            String content = new String(saveRecordContent.toString().getBytes(), StandardCharsets.UTF_8);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile,true),StandardCharsets.UTF_8));
            writer.write(content);
            writer.flush();
            saveRecordContent.setLength(0);
            Timber.d("存储记录写入完成");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(writer);
        }
    }

    private void writeUpload(){
        if(!uploadFile.exists() || uploadRecordContent.length() <= 0){
            Timber.d("暂无上传记录");
            return;
        }
        BufferedWriter writer = null;
        try {
            String content = new String(uploadRecordContent.toString().getBytes(),StandardCharsets.UTF_8);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(uploadFile,true),StandardCharsets.UTF_8));
            writer.write(content);
            writer.flush();
            uploadRecordContent.setLength(0);
            Timber.d("上传记录写入完成");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(writer);
        }
    }

    private void writeArray(){
        if(!arrayFile.exists() || arrayFile.length() <= 0){
            Timber.d("暂无批量记录");
            return;
        }
        BufferedWriter writer = null;
        try {
            String content = new String(uploadArrayRecordContent.toString().getBytes(),StandardCharsets.UTF_8);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(arrayFile,true),StandardCharsets.UTF_8));
            writer.write(content);
            writer.flush();
            uploadArrayRecordContent.setLength(0);
            Timber.d("批量记录写入完成");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(writer);
        }
    }

    private void close(Closeable os){
        if(os != null){
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
