package com.yunbiao.ybsmartcheckin_live_id.printer;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.provider.Settings;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;


public class ToolUtil {
	private static final String tag = "ToolUtil";
	private static boolean mHaveRoot = false;
	private static int batteryLever = 0;
	private static final ThreadLocal<SimpleDateFormat> messageFormat = new ThreadLocal<SimpleDateFormat>();
	private static final String ACTION_USB_PERMISSION =
			"com.android.example.USB_PERMISSION";
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[]PERMISSION_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

//	private static LogUtil Log = LogUtil.getLogUtil(ToolUtil.class);

	private static final SimpleDateFormat getDateFormat(String format) {
		SimpleDateFormat df = messageFormat.get();
		if (df == null) {
			df = new SimpleDateFormat(format, Locale.getDefault());
			messageFormat.set(df);
		}

		return df;
	}

	public static void requestStoragePermission(Context context){
		/*int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if(permission!= PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions((Activity) context, PERMISSION_STORAGE,REQUEST_EXTERNAL_STORAGE);
		}*/
	}

	public static boolean haveRoot() {
		if (!mHaveRoot) {
			int ret = execRootCmdSlient("echo test"); // 閫氳繃鎵ц娴嬭瘯鍛戒护鏉ユ娴�
			if (ret != -1) {
			//	Log.i(tag, "have root!");
				mHaveRoot = true;
			} else {
			//	Log.i(tag, "not root!");
			}
		} else {
			//Log.i(tag, "mHaveRoot = true, have root!");
		}
		return mHaveRoot;
	}

	public static boolean upgradedirUSBPermission(Context context){
		UsbManager mManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
		if (mManager == null) {
		//	Log.i("FingerScanner", "--->getSystemService failed ");
		//	Log.i("FingerScanner", "FindAndOpenFpDev End");

		}

		//Log.i("FingerScanner", "--->getSystemService OK");
		HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
		if (deviceList != null && deviceList.size() != 0) {
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			while (deviceIterator.hasNext()) {
				UsbDevice device = deviceIterator.next();
				PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0,
						new Intent(ACTION_USB_PERMISSION), 0);
				mManager.requestPermission(device, mPermissionIntent);
				ToolUtil.upgradedirPermission(device.getDeviceName());
			//	Log.i("upgradedirUSBPermission", device.getDeviceName()+"--->device: VID = " + device.getVendorId() + "  PID = " + device.getProductId()+" permission"+mManager.hasPermission(device));

			}
		}
		ToolUtil.haveRoot();
		execRootCmdSlient(
				"chmod 777 /dev/ttyS0;"+
						"chmod 777 /dev/ttyS1;"+
						"chmod 777 /dev/ttyS2;"+
						"chmod 777 /dev/ttyS3;"+
						"chmod 777 /dev/bus/;"+
						"chmod 777 /dev/bus/usb/;"+
						"chmod 777 /dev/bus/usb/0*;"+
						"chmod 777 /dev/bus/usb/001/*;"+
						"chmod 777 /dev/bus/usb/002/*;"+
						"chmod 777 /dev/bus/usb/003/*;"+
						"chmod 777 /dev/bus/usb/004/*;"+
						"chmod 777 /dev/bus/usb/005/*;"
		);
		return true;
	}

	protected static int execRootCmdSlient(String paramString)
	{
		try{
			Process localProcess = Runtime.getRuntime().exec("su");
			Object localObject = localProcess.getOutputStream();
			DataOutputStream localDataOutputStream = new DataOutputStream(
					(OutputStream) localObject);
			String str = String.valueOf(paramString);
			localObject = str + "\n";
			localDataOutputStream.writeBytes((String) localObject);
			localDataOutputStream.flush();
			localDataOutputStream.writeBytes("exit\n");
			localDataOutputStream.flush();
			localProcess.waitFor();
			//localObject = localProcess.exitValue();
			//return -1;
			return localProcess.exitValue();
		}catch (Exception localException){
			localException.printStackTrace();
		}
		return 0;
	}

	public static boolean upgradedirUSBPermission(Context context, int pid, int vid){
		UsbManager mManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
		if (mManager == null) {
		//	Log.i("FingerScanner", "--->getSystemService failed ");
		//	Log.i("FingerScanner", "FindAndOpenFpDev End");

		}

	//	Log.i("FingerScanner", "--->getSystemService OK");
		HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
		if (deviceList != null && deviceList.size() != 0) {
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			while (deviceIterator.hasNext()) {
				UsbDevice device = deviceIterator.next();
				if(device.getVendorId()==vid&&device.getProductId()==pid){
					PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0,
							new Intent(ACTION_USB_PERMISSION), 0);
					mManager.requestPermission(device, mPermissionIntent);
					ToolUtil.upgradedirPermission(device.getDeviceName());
				//	Log.i("upgradedirUSBPermission", device.getDeviceName()+"--->device: VID = " + device.getVendorId() + "  PID = " + device.getProductId()+" permission"+mManager.hasPermission(device));
					return true;
				}

			}
		}
		return false;
	}

	public static boolean executeOrder(String order){
		Process process = null;
		DataOutputStream os = null;
		try {
		//	Log.d("ToolUtil", "order = " + order);
			String cmd = order;
			process = Runtime.getRuntime().exec("su"); // 鍒囨崲鍒皉oot甯愬彿
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}




	public static boolean upgradedirPermission(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			//Log.d("ToolUtil", "upgradedirPermission = " + pkgCodePath);
			String cmd = "chmod -R 0777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); // 鍒囨崲鍒皉oot甯愬彿
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if(process!=null){
					process.destroy();
				}
			} catch (Exception e) {
			//	Log.exception(tag, e);
			}
		}
		return true;
	}

	public static boolean changePermission(String permission, String path) {
		Process process = null;
		DataOutputStream os = null;
		try {
		///	Log.d("ToolUtil", "changePermission permission: " + permission
			//		+ "   path: " + path);
			String cmd = "chmod " + permission + " " + path;
			process = Runtime.getRuntime().exec("su"); // 鍒囨崲鍒皉oot甯愬彿
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if(process!=null){
					process.destroy();
				}
			} catch (Exception e) {
			}
		}
		return true;
	}

	public static String getAndroidID(Activity activity) {
		String androidID;
		androidID = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
	//	Log.i(tag, "Android id is " + androidID);
		return androidID;
	}

	public static void setBatteryLevel(int level)  { //鐧惧垎姣旓紝鏈�澶�100
		//Log.i(tag, "Set battery level " + level);
		batteryLever = level;
	}

	public static int getBatteryLevel() {
		return batteryLever;
	}

	public static String getNowTime(String format){//"yyyy-MM-dd HH:mm:ss SSS"


		return getDateFormat(format).format(new Date())+":";
	}

	public static String getLocalBlueToothName(){
		String btName = "";
		btName = BluetoothAdapter.getDefaultAdapter().getName();
		return btName;
	}

	public static String getLocalHostIp(){
		String ipaddress = "";
		try
		{
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			// 閬嶅巻鎵�鐢ㄧ殑缃戠粶鎺ュ彛
			while (en.hasMoreElements())
			{
				NetworkInterface nif = en.nextElement();// 寰楀埌姣忎竴涓綉缁滄帴鍙ｇ粦瀹氱殑鎵�鏈塱p
				Enumeration<InetAddress> inet = nif.getInetAddresses();
				// 閬嶅巻姣忎竴涓帴鍙ｇ粦瀹氱殑鎵�鏈塱p
				while (inet.hasMoreElements())
				{
					InetAddress ip = inet.nextElement();
					if (!ip.isLoopbackAddress())
					{
						return ipaddress = "鏈満IP" + "锛�" + ip.getHostAddress();
					}
				}

			}
		}
		catch (SocketException e)
		{
		//	Log.e("feige", "鑾峰彇鏈湴ip鍦板潃澶辫触");
		//	Log.exception(tag, e);
		}
		return ipaddress;
	}
	
	public static boolean moveFile(String srcPath, String dirPath) {
		Process process = null;
		DataOutputStream os = null;
		try {
	//		Log.d("ToolUtil", "mv  "+srcPath +" "+ dirPath);
			String cmd = "mv  "+srcPath +" "+ dirPath;
			process = Runtime.getRuntime().exec("su"); // 鍒囨崲鍒皉oot甯愬彿
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if(process!=null){
					process.destroy();
				}
			} catch (Exception e) {
		//		Log.exception(tag, e);
			}
		}
		return true;
	}

}
