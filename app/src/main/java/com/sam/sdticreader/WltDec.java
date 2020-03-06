package com.sam.sdticreader;


public class WltDec
{
	//public native int test(byte[] bytes);

	public byte[] decodeToBitmap(byte[] wltData)
	{
		byte[] bmpData = new byte[54 + 0x7e * (0x66 * 3 + 2)];
		wlt2bmp(wltData, bmpData);
		return bmpData;
	}

	private native int wlt2bmp(byte[] inArray, byte[] outArray);

	static
	{
		System.loadLibrary("wlt2bmp");
		//Log.i("WELLCOM", "success!");
	}
}
