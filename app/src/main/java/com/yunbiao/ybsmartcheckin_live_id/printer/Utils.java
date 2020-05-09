package com.yunbiao.ybsmartcheckin_live_id.printer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;


public class Utils {
	public Utils(){
		
	}
	
	// 等待时间（间隔时间）
	public synchronized void sleep(long msec) {
		try {
			wait(msec);
		} catch (InterruptedException e) {
		}
	}
	/**
	 * 获取当前系统的语言环境
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean isZh(Context context) {
		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("zh"))
			return true;
		else
			return false;
	}

	/**
	 * 获取Assets子文件夹下的文件数据流数组InputStream[]
	 * 
	 * @param context
	 * @return InputStream[]
	 */
	@SuppressWarnings("unused")
	private static InputStream[] getAssetsImgaes(String imgPath, Context context) {
		String[] list = null;
		InputStream[] arryStream = null;
		try {
			list = context.getResources().getAssets().list(imgPath);
			arryStream = new InputStream[3];
			for (int i = 0; i < list.length; i++) {
				InputStream is = context.getResources().getAssets()
						.open(imgPath + File.separator + list[i]);
				arryStream[i] = is;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return arryStream;
	}

	/*
	 * 未转换为十六进制字节的字符串
	 * 
	 * @param paramString
	 * 
	 * @return byte[]
	 */
	public static byte[] hexStr2Bytesnoenter(String paramString) {
		String[] paramStr = paramString.split(" ");
		byte[] arrayOfByte = new byte[paramStr.length];

		for (int j = 0; j < paramStr.length; j++) {
			arrayOfByte[j] = Integer.decode("0x" + paramStr[j]).byteValue();
		}
		return arrayOfByte;
	}
	
	// 字符串转换16进制
	public static byte[] getHexCmd(String paramString) {
		String[] paramStr = paramString.split(" ");
		byte[] arrayOfByte = new byte[paramStr.length];

		for (int j = 0; j < paramStr.length; j++) {
			if (String.valueOf(arrayOfByte[j]).length() == 4) {
				byte d1 = Byte.valueOf(String.valueOf(arrayOfByte[j])
						.substring(0, 2));
				byte d2 = Byte.valueOf(String.valueOf(arrayOfByte[j])
						.substring(2, 4));
				arrayOfByte[j] = Integer.decode("0x" + d1).byteValue();
				arrayOfByte[j + 1] = Integer.decode("0x" + d2).byteValue();
			}
			if (!isHexNumberRex(String.valueOf(arrayOfByte[j]))) {
				break;
			}
			arrayOfByte[j] = Integer.decode("0x" + paramStr[j]).byteValue();
		}
		return arrayOfByte;
	}
	
	private static boolean isOctNumberRex(String str){
        String validate = "\\d+";
        return str.matches(validate);  
    }  
    private static boolean isHexNumberRex(String str){
        String validate = "(?i)[0-9a-f]+";
//    	String validate = "[\u4e00-\u9fa5]";  
        return str.matches(validate);  
    }  
	
	/**
	 * 统计指定字符串中某个符号出现的次数
	 * 
	 * @param str
	 * @return int
	 */
	public static int Count(String strData, String str) {
		int iBmpNum = 0;
		for (int i = 0; i < strData.length(); i++) {
			String getS = strData.substring(i, i + 1);
			if (getS.equals(str)) {
				iBmpNum++;
			}
		}
		//System.out.println(str + "出现了:" + iBmpNum + "次");
		return iBmpNum;
	}
	
	/**
	 * 字符串转换为16进制
	 * 
	 * @param strPart
	 * @return
	 */
	@SuppressLint({ "UseValueOf", "DefaultLocale" })
	public static String stringTo16Hex(String strPart) {
		if (strPart == "")
			return "";
		try {
			byte[] b = strPart.getBytes("gbk"); // 数组指定编码格式，解决中英文乱码
			String str = "";
			for (int i = 0; i < b.length; i++) {
				Integer I = new Integer(b[i]);
				@SuppressWarnings("static-access")
                String strTmp = I.toHexString(b[i]);
				if (strTmp.length() > 2)
					strTmp = strTmp.substring(strTmp.length() - 2) + " ";
				else
					strTmp = strTmp.substring(0, strTmp.length()) + " ";
				str = str + strTmp;
			}
			return str.toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * @Title:intToHexString
	 * @Description:10进制数字转成16进制
	 * @param a 转化数据
	 * @param len 占用字节数
	 * @return String
	 */
	public static String intToHexString(int a, int len) {
		len <<= 1;
		String hexString = Integer.toHexString(a);
		int b = len - hexString.length();
		if (b > 0) {
			for (int i = 0; i < b; i++) {
				hexString = "0" + hexString;
			}
		}
		return hexString;
	}
	
	@SuppressLint("DefaultLocale")
	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}
	/**
	 * 通过选择文件获取路径
	 * @param context
	 * @param uri
	 * @return String
	 */
	public static String getPath(Context context, Uri uri) {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, projection,
						null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}
	
	
	/**
	 * 获取单色位图
	 * @param inputBMP
	 * @return Bitmap
	 */
	public static Bitmap getSinglePic(Bitmap inputBMP) {
		int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
		int[] colorTemp = new int[inputBMP.getWidth() * inputBMP.getHeight()];
		inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
				inputBMP.getWidth(), inputBMP.getHeight());
		Bitmap returnBMP = Bitmap.createBitmap(inputBMP.getWidth(),
				inputBMP.getHeight(), Config.RGB_565);
		int lightNumber = 127;// 曝光度，這個顔色是中間值，如果大於中間值，那就是黑色，否則白色，数值越小，曝光度越高
//		for (int j = 0; j < colorTemp.length; j++) {
//			// 将颜色数组中的RGB值取反，255减去当前颜色值就获得当前颜色的反色
//			// 網上的，但是我要進行曝光處理，使他變成單色圖
//			colorTemp[j] = Color.rgb(Color.red(pix[j]) > lightNumber ? 255 : 0,
//					Color.green(pix[j]) > lightNumber ? 255 : 0,
//					Color.blue(pix[j]) > lightNumber ? 255 : 0);
//		}
		for (int j = 0; j < colorTemp.length; j++) {
			colorTemp[j] = Color.rgb(Color.red(pix[j]), Color.green(pix[j]),
					Color.blue(pix[j]));
		}
		for (int i = 0; i < colorTemp.length; i++) {
			// 這裏需要思考一下，上一步有可能得到：純紅，純黃，純藍，黑色，白色這樣5種顔色，前三種是應該變成白色還是黑色呢？
			// 發現這是一個很複雜的問題，涉及到不同區域閒顔色的對比，如果是黑色包圍紅色，那紅色就應該是白色，反之變成黑色。。。
			// 似乎衹能具體問題具體分析，這裏就先把黃色設成白色，藍色=白色，紅色=黑色
			int r = Color.red(pix[i]);
			int g = Color.green(pix[i]);
			int b = Color.blue(pix[i]);
			// 有兩種顔色以上的混合，那就是變成黑色但目前这种方法，对于黑白的曝光效果更出色，
			// 原理是设置一个曝光值，然后三种颜色相加大于3倍的曝光值，才是黑色，否则白色
			if (r + g + b > 3 * lightNumber) {
				colorTemp[i] = Color.rgb(255, 255, 255);
			} else {
				colorTemp[i] = Color.rgb(0, 0, 0);
			}
		}
		returnBMP.setPixels(colorTemp, 0, inputBMP.getWidth(), 0, 0,
				inputBMP.getWidth(), inputBMP.getHeight());
		return returnBMP;
	}
	
	/**
	 * jpg png bmp 彩色图片转换Bitmap数据为int[]数组
	 * @param bm
	 * @return int[]
	 */
	public static int[] getPixelsByBitmap(Bitmap bm) {
		int width, heigh;
		width = bm.getWidth();
		heigh = bm.getHeight();
		int iDataLen = width * heigh;
		int[] pixels = new int[iDataLen];
		bm.getPixels(pixels, 0, width, 0, 0, width, heigh);
		return pixels;
	}
	  	

	/**
	 * BitmapOption 位图选项
	 * @param inSampleSize
	 * @return
	 */
	private static Options getBitmapOption(int inSampleSize) {
		System.gc();
		Options options = new Options();
		options.inPurgeable = true;
		options.inSampleSize = inSampleSize;
		options.inPreferredConfig = Config.ARGB_4444; // T4 二维码图片效果最佳
		return options;
	}

	/**
	 * 获取Bitmap数据
	 * 
	 * @param imgPath
	 * @return
	 */
	public static Bitmap getBitmapData(String imgPath) {
		Bitmap bm = BitmapFactory.decodeFile(imgPath, getBitmapOption(1)); // 将图片的长和宽缩小为原来的1/2
		return bm;
	}
	
	/** ---保存Bitmap到本地路径-------------------------------------------------------*/
	private static final String SD_PATH = "/sdcard/masung/pic/";
    private static final String IN_PATH = "/masung/pic/";

    /**
     * 随机生产文件名
     * 
     * @return
     */
    private static String generateFileName() {
        return UUID.randomUUID().toString();
    }
/**
     * 保存bitmap到本地
     * 
     * @param context
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Context context, Bitmap mBitmap) {
        String savePath;
        File filePic;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = SD_PATH;
        } else {
            savePath = context.getApplicationContext().getFilesDir()
                    .getAbsolutePath()
                    + IN_PATH;
        }
        try {
            filePic = new File(savePath + generateFileName() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block 自动生成的catch块
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }
    
    /**
	 * 将bitmap保存为JPG格式的图片
	 * @param bitmap
	 */
    public static String imagePath = "";
	public static String saveMypic(Bitmap bitmap, Context context) {
		//非空判断
		if (bitmap == null) {
			return "";
		}
		// 保存图片
		try {
			String filePath = Environment.getExternalStorageDirectory().getCanonicalPath() + "/masung";
			File file = new File(filePath);
			if (!file.exists()) {
				file.mkdirs();
			}
			imagePath = filePath + "/_MS_001.jpg";
			File image = new File(imagePath);
			if (image.exists()) {
				file.delete();
			}
			FileOutputStream stream = new FileOutputStream(image);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			stream.flush();
			stream.close();
			T.showShort(context, "保存成功，已经保存到:"+imagePath);
		} catch (Exception e) {
			e.printStackTrace();
			T.showShort(context, "保存失败!");
		}
		return imagePath;
	}
    /** ------------------------------END---------------------------------------*/
	
	/** 
	 * SharedPreferences存储数据方式工具类 
	 * @author zuolongsnail 
	 */
	public final static String SETTING = "masung";
	// 移除数据
	public static void removeValue(Context context, String key) {
    	Editor sp =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
    	sp.clear();  
    	sp.commit(); 
    }
	
    public static void putValue(Context context, String key, int value) {
         Editor sp =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
         sp.putInt(key, value);  
         sp.commit();  
    }  
    public static void putValue(Context context, String key, boolean value) {
         Editor sp =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
         sp.putBoolean(key, value);  
         sp.commit();  
    }  
    public static void putValue(Context context, String key, String value) {
         Editor sp =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
         sp.putString(key, value);  
         sp.commit();  
    }  
    public static int getValue(Context context, String key, int defValue) {
        SharedPreferences sp =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        int value = sp.getInt(key, defValue);  
        return value;  
    }  
    public static boolean getValue(Context context, String key, boolean defValue) {
        SharedPreferences sp =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, defValue);  
        return value;  
    }  
    public static String getValue(Context context, String key, String defValue) {
        SharedPreferences sp =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        String value = sp.getString(key, defValue);
        return value;  
    }
    
}
