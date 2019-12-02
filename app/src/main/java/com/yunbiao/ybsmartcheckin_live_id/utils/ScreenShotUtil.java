package com.yunbiao.ybsmartcheckin_live_id.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.yunbiao.ybsmartcheckin_live_id.afinel.ResourceUpdate;
import com.yunbiao.ybsmartcheckin_live_id.common.NetTool;
import com.yunbiao.ybsmartcheckin_live_id.system.HeartBeatClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;

import static android.app.Activity.RESULT_OK;

public class ScreenShotUtil {

    private static final String TAG = "ScreenShotUtil";

    private static final String CLASS1_NAME = "android.view.SurfaceControl";

    private static final String CLASS2_NAME = "android.view.Surface";

    private static final String METHOD_NAME = "screenshot";

    private static ScreenShotUtil instance;

    private Display mDisplay;

    private DisplayMetrics mDisplayMetrics;

    private Matrix mDisplayMatrix;

    private WindowManager wm;

    private int ssOutTime = 30;

    private SimpleDateFormat format;
    private final ExecutorService singleThreadExecutor;
    private DisplayMetrics metrics;

    private ScreenShotUtil() {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    public static ScreenShotUtil getInstance() {
        synchronized (ScreenShotUtil.class) {
            if (instance == null) {
                instance = new ScreenShotUtil();
            }
        }
        return instance;
    }

    private Bitmap screenShot(int width, int height) {
        Log.i(TAG, "android.os.Build.VERSION.SDK : " + android.os.Build.VERSION.SDK_INT);
        Class<?> surfaceClass = null;
        Method method = null;
        try {
            Log.i(TAG, "width : " + width);
            Log.i(TAG, "height : " + height);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {

                surfaceClass = Class.forName(CLASS1_NAME);
            } else {
                surfaceClass = Class.forName(CLASS2_NAME);
            }
            method = surfaceClass.getDeclaredMethod(METHOD_NAME, int.class, int.class);
            method.setAccessible(true);
            return (Bitmap) method.invoke(null, width, height);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, e.toString());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.toString());
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.toString());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    public interface ScreenShotCallback {
        void onShotted(boolean isSucc, String filePath);
    }

    /**
     * Takes a screenshot of the current display and shows an animation.
     */
    @SuppressLint("NewApi")
    public void takeScreenshot(final Context context, final ScreenShotCallback callback) {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean isSucc = false;

                SoftReference<Bitmap> soft = null;
                String path = ResourceUpdate.SCREEN_CACHE_PATH;
                File file1 = new File(path);
                if (!file1.exists()) {
                    file1.mkdirs();
                }
                String sid = HeartBeatClient.getDeviceNo();
                String filePath = path + sid + ".png";

                Log.e(TAG, "run: 111111111111111111111");
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    Log.e(TAG, "run: 4444444444444444444444");
                    wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    mDisplay = wm.getDefaultDisplay();
                    mDisplayMatrix = new Matrix();
                    mDisplayMetrics = new DisplayMetrics();
                    // We need to orient the screenshot correctly (and the Surface api seems to take screenshots
                    // only in the natural orientation of the device :!)
                    mDisplay.getRealMetrics(mDisplayMetrics);
                    float[] dims =
                            {
                                    mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels
                            };
                    float degrees = getDegreesForRotation(mDisplay.getRotation());
                    boolean requiresRotation = (degrees > 0);
                    if (requiresRotation) {
                        // Get the dimensions of the device in its native orientation
                        mDisplayMatrix.reset();
                        mDisplayMatrix.preRotate(-degrees);
                        mDisplayMatrix.mapPoints(dims);
                        dims[0] = Math.abs(dims[0]);
                        dims[1] = Math.abs(dims[1]);
                    }

                    Bitmap mScreenBitmap = screenShot((int) dims[0], (int) dims[1]);
                    if (mScreenBitmap != null) {
                        if (requiresRotation) {
                            // Rotate the screenshot to the current orientation
                            Bitmap ss = Bitmap.createBitmap(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels,
                                    Bitmap.Config.ARGB_8888);
                            Canvas c = new Canvas(ss);
                            c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
                            c.rotate(degrees);
                            c.translate(-dims[0] / 2, -dims[1] / 2);
                            c.drawBitmap(mScreenBitmap, 0, 0, null);
                            c.setBitmap(null);
                            mScreenBitmap = ss;
                            if (ss != null && !ss.isRecycled()) {
                                ss.recycle();
                            }
                        }
                        // Optimizations
                        mScreenBitmap.setHasAlpha(false);
                        mScreenBitmap.prepareToDraw();
                    }
                    soft = new SoftReference<>(mScreenBitmap);
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    if (ShellUtils.checkRootPermission()) {
                        ShellUtils.execCommand("/system/bin/screencap -p " + filePath, true);
                        Log.e(TAG, "run: 222222222222222222222222");
                        for (int i = 0; i < ssOutTime; i++) {
                            Log.e(TAG, "run: 333333333333333333333");
                            File file = new File(filePath);
                            if (file != null && file.exists() && file.isFile()) {
                                soft = new SoftReference<>(BitmapFactory.decodeFile(filePath));
                                break;
                            }
                            SystemClock.sleep(300);
                        }
                    } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Log.e(TAG, "run: 999999999999999999999");
                        soft = new SoftReference<>(cutoutFrame(context));
                    }
                }

                if ((soft != null) && (soft.get() != null)) {
                    isSucc = saveBitmap2file(soft.get(), filePath);
                }

                if (callback != null) {
                    Log.e(TAG, "onClick: 55555555555555" + soft);
                    callback.onShotted(isSucc, filePath);
                }
            }
        });
    }

    public void sendCutFinish(String sid, String filePath) {
        File file = new File(filePath);
        Log.e(TAG, "sendCutFinish: " + (file != null && file.exists()));

        HashMap<String, String> params = new HashMap<>();
        params.put("sid", sid);
        OkHttpUtils.post().url(ResourceUpdate.SCREEN_UPLOAD_URL).params(params).addFile("screenimage",file.getName(),file).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: " + (e == null ?" NULL" : e.getMessage()));
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: " + response);
            }
        });
    }

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private static int RECORD_REQUEST_CODE = 5;
    private ImageReader mImageReader;
    private VirtualDisplay virtualDisplay;
    private int width;
    private int height;
    private int dpi;

    public void init_LOLLIPOP(Activity context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG, "init_LOLLIPOP: aaaaaaaaaaaaaaaaaaaaaa");
            projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            Intent captureIntent = null;
            captureIntent = projectionManager.createScreenCaptureIntent();
            context.startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
            metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = metrics.widthPixels;
            height = metrics.heightPixels;
            dpi = metrics.densityDpi;
            Log.e(TAG, "屏幕参数: " + width + "---" + height + "---" + dpi);
        }
    }

    public void onResult(int requestCode, int resultCode, Intent data) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
                Log.e(TAG, "init_LOLLIPOP: bbbbbbbbbbbbbbbbbbb");
                //######## 截屏逻辑 ########
                mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                if (mImageReader == null) {
                    int maxImages = 2;
                    mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, maxImages);
                    Log.e(TAG, "ImageReader参数: " + mImageReader.getWidth() + "---" + mImageReader.getHeight() + "---" + mImageReader.getMaxImages());
                    Surface surface = mImageReader.getSurface();

                    virtualDisplay = mediaProjection
                            .createVirtualDisplay("mediaprojection", width, height, dpi,
                                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null, null);
                }
            }
        }
    }

    /**
     * 进行截屏
     *
     * @param context
     */
    private Bitmap screenShot(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        Image image = mImageReader.acquireLatestImage();  //获取缓冲区中的图像，关键代码
        if (image == null)
            return null;

        Log.e(TAG, "run: -------------------------");
        //Image -> Bitmap
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int rowStride = planes[0].getRowStride();  //Image中的行宽，大于Bitmap所设的真实行宽
        byte[] oldBuffer = new byte[rowStride * height];
        buffer.get(oldBuffer);
        byte[] newBuffer = new byte[width * 4 * height];

        Bitmap bitmap = Bitmap.createBitmap(metrics, width, height, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < height; ++i) {
            System.arraycopy(oldBuffer, i * rowStride, newBuffer, i * width * 4, width * 4);  //跳过多余的行宽部分，关键代码
        }
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(newBuffer));  //用byte数组填充bitmap，关键代码
        image.close();

        return bitmap;
    }

    /**
     * 通过底层来获取下一帧的图像
     *
     * @return bitmap
     */
    private Bitmap cutoutFrame(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        if (mImageReader == null) {
            Log.e(TAG, "onActivityResult: 6666666666666666666666666");
            return null;
        }
        if (virtualDisplay == null) {
            Log.e(TAG, "onActivityResult: 77777777777777777777777777777777");
            return null;
        }
        Log.e(TAG, "onActivityResult: 88888888888888888888888888888888");
        SystemClock.sleep(1000);
        Image image = mImageReader.acquireNextImage();

        if (image == null) {
            Log.e(TAG, "onActivityResult: 99999999999999999999999999999999999999");
            return null;
        }
        Log.e(TAG, "图片参数: " + image.getWidth() + "---" + image.getHeight() + "---" + image.getTimestamp());
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        width = width + rowPadding / pixelStride;
        Log.e(TAG, "cutoutFrame: ---" + width + "-----" + height);
        Bitmap bitmap = Bitmap.createBitmap(metrics, width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

//        Bitmap newBitmap = Bitmap.createBitmap(height,width,Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(newBitmap);
//        Paint paint = new Paint();
//        Matrix matrix = new Matrix();
//        //图片镜像并旋转90度
//        matrix.setScale(-1, 1);
//        matrix.postTranslate(width, 0);
//        matrix.postRotate(90 ,width/2,height/2);
//        matrix.postTranslate(0,(width-height)/2);
//        canvas.drawBitmap(bitmap, matrix , paint );
        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    /**
     * 通过底层来获取下一帧的图像
     *
     * @return bitmap
     */
    private Bitmap cutoutFrame2(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        if (mImageReader == null) {
            Log.e(TAG, "onActivityResult: 6666666666666666666666666");
            return null;
        }
        if (virtualDisplay == null) {
            Log.e(TAG, "onActivityResult: 77777777777777777777777777777777");
            return null;
        }
        Log.e(TAG, "onActivityResult: 88888888888888888888888888888888");
        SystemClock.sleep(1000);
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            Log.e(TAG, "onActivityResult: 99999999999999999999999999999999999999");
            return null;
        }
        Log.e(TAG, "onActivityResult: 1010101011010101010101010101010101");
        int iw = image.getWidth();
        int ih = image.getHeight();


        //Image -> Bitmap
//        final Image.Plane[] planes = image.getPlanes();
//        final ByteBuffer buffer = planes[0].getBuffer();
//        int rowStride = planes[0].getRowStride();  //Image中的行宽，大于Bitmap所设的真实行宽
//        byte[] oldBuffer = new byte[rowStride * height];
//        buffer.get(oldBuffer);
//        byte[] newBuffer = new byte[width * 4 * height];


        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        Log.e(TAG, "cutoutFrame2: array:" + bytes.length);
        buffer.get(bytes);

        Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap newBitmap = Bitmap.createBitmap(this.width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        Matrix matrix = new Matrix();
        //图片镜像并旋转90度
        matrix.setScale(-1, 1);
        matrix.postTranslate(iw, 0);
        matrix.postRotate(90, iw / 2, ih / 2);
        matrix.postTranslate(0, (iw - ih) / 2);
        canvas.drawBitmap(temp, matrix, paint);
        return newBitmap;
    }

    //
    private boolean saveBitmap2file(Bitmap bmp, String fileName) {
        int quality = 100;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, quality, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        byte[] buffer = new byte[1024];
        int len = 0;
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdir();
                file.getParentFile().createNewFile();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            try {
                file.getParentFile().delete();
                file.getParentFile().createNewFile();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        }
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            while ((len = is.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
            stream.flush();
        } catch (FileNotFoundException e) {
            Log.i(TAG, e.toString());
            return false;
        } catch (IOException e) {
            Log.i(TAG, e.toString());
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.i(TAG, e.toString());
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.i(TAG, e.toString());
                }
            }
        }
        return true;
    }

    /**
     * @return the current display rotation in degrees
     */
    private float getDegreesForRotation(int value) {
        switch (value) {
            case Surface.ROTATION_90:
                return 360f - 90f;
            case Surface.ROTATION_180:
                return 360f - 180f;
            case Surface.ROTATION_270:
                return 360f - 270f;
        }
        return 0f;
    }

}
