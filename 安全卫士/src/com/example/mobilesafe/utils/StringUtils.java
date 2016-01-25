package com.example.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Application;

public class StringUtils {

	/**
	 * ½«inputstream×ª³ÉString
	 * @return
	 * @throws IOException 
	 */
	public static String input2String(InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len=-1;
		while((len = in.read(buffer))!=-1)
		{
			bos.write(buffer, 0, len);
		}
		bos.flush();
		return bos.toString();
	}
	

}
