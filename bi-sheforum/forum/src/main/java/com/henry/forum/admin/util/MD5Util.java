package com.henry.forum.admin.util;

import java.security.MessageDigest;
import java.util.Base64;

public class MD5Util {
	public static String getMD5(String str) {
		String newstr = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			newstr = encryptBase64(md5.digest(str.getBytes("utf-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newstr;
	}

	private static String encryptBase64(byte[] data) {
		java.util.Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(data);
	}

}
