package util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.util.Log;

public class conversion {

	public static byte[] hexStringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];

		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}

		byte[] byteArray = b;
		b = null;

		return byteArray;
	}

	public static final String CHARS = "0123456789ABCDEF";

	public static String toHexString(byte[] data) {
		StringBuilder sb = new StringBuilder();
		String strResult = "";

		for (int i = 0; i < data.length; ++i) {
			sb.append(CHARS.charAt((data[i] >> 4) & 0x0F)).append(
					CHARS.charAt(data[i] & 0x0F));
		}

		strResult = sb.toString();
		sb = null;

		return strResult;
	}
	
	// Dec To Hex
	private static final int numberOfBitsInAHalfByte = 4;
	private static final int halfByte = 0x0F;
	private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String decToHex(int dec, int outLen) {
		StringBuilder hexBuilder = new StringBuilder(outLen);
		String strResult = "";

		hexBuilder.setLength(outLen);
		for (int i = outLen - 1; i >= 0; --i) {
			int j = dec & halfByte;
			hexBuilder.setCharAt(i, hexDigits[j]);
			dec >>= numberOfBitsInAHalfByte;
		}

		strResult = hexBuilder.toString();
		hexBuilder = null;

		return strResult;
	}
	
	// Hex To Ascii
	public static String HexToAscii(String hex) {

		String ascii = "";
		String str;
		
		// Convert hex string to length
		int rmd, length;
		length = hex.length();
		rmd = length % 2;
		if (rmd == 1)
			hex = "0" + hex;

		// split into two characters
		for (int i = 0; i < hex.length() - 1; i += 2) {
			
			// split the hex into pairs
			String pair = hex.substring(i, (i + 2));
			// convert hex to decimal
			int dec = Integer.parseInt(pair, 16);
			str = CheckCode(dec);
			ascii = ascii + str;
		}
		return ascii;
	}

	public static String CheckCode(int dec) {
		String str;
		
		// convert the decimal to character
		str = Character.toString((char) dec);

		if (dec < 32 || dec > 126 && dec < 161)
			str = "n/a";
		return str;
	}

	// 문자열을 헥사 스트링으로 변환
	public static String stringToHex(String s) {
		String result = "";

		for (int i = 0; i < s.length(); i++) {
			result += String.format("%02X", (int) s.charAt(i));
		}

		return result;
	}

	// SHA256 알고리즘 (무결성 인증)
	public static String Integrity_SHA256(String data) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(hexStringToByteArray(data));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.i("SHA-256 Error : ", e.toString());
			return "";
		}
		
		data="";

		return bytesToHex(md.digest());
	}
	
	// Sha256 암호화 알고리즘
	public static String SHA256(String planText) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(planText.getBytes());
			byte byteData[] = md.digest();

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				String hex = Integer.toHexString(0xff & byteData[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
				
			planText = "";

			return hexString.toString();
				
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	// Sha256 암호화 알고리즘 (거래연동 정보 암호화에 사용)
	public static String transSHA256(String planText) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(planText.getBytes("EUC-KR"));
			byte byteData[] = md.digest();

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				String hex = Integer.toHexString(0xff & byteData[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
					
			planText = "";
			
			return hexString.toString();
				
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuffer result = new StringBuffer();
		String strResult = "";

		for (byte byt : bytes)
			result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));

		strResult = result.toString();
		result = null;

		return strResult;
	}

	// AES_CBC_128 암호화 알고리즘
	public static String AES_CBC_128_ENCRYPT(String plainText, String passkey) {
		byte[] encrypted = null;

		String hexText = "";
		String iv = "00000000000000000000000000000000";
		IvParameterSpec ivspec;
		ivspec = new IvParameterSpec(hexStringToByteArray(iv));

		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_ENCRYPT", e.toString());
			return "";
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_ENCRYPT", e.toString());
			return "";
		}

		SecretKeySpec key = new SecretKeySpec(hexStringToByteArray(passkey), "AES");
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_ENCRYPT", e.toString());
			return "";
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_ENCRYPT", e.toString());
			return "";
		}

		try {
			encrypted = cipher.doFinal(hexStringToByteArray(plainText));
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_ENCRYPT", e.toString());
			return "";
		} catch (BadPaddingException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_ENCRYPT", e.toString());
			return "";
		}
		hexText = byteArrayToHex(encrypted);

		ivspec = null;
		plainText = "";
		passkey = "";
		
		return hexText;
	}

	// AES_CBC_128 복호화 알고리즘
	public static String AES_CBC_128_DECRYPT(String plainText, String passkey) {
		byte[] encrypted = null;

		String iv = "00000000000000000000000000000000";
		IvParameterSpec ivspec;
		ivspec = new IvParameterSpec(hexStringToByteArray(iv));

		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_DECRYPT Error : ", e.toString());
			return "";
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_DECRYPT Error : ", e.toString());
			return "";
		}
		SecretKeySpec key = new SecretKeySpec(hexStringToByteArray(passkey), "AES");
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_DECRYPT Error : ", e.toString());
			return "";
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_DECRYPT Error : ", e.toString());
			return "";
		}

		try {
			encrypted = cipher.doFinal(hexStringToByteArray(plainText));
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_DECRYPT Error : ", e.toString());
			return "";
		} catch (BadPaddingException e) {
			e.printStackTrace();
			Log.i("AES_CBC_128_DECRYPT Error : ", e.toString());
			return "";
		}

		String hexText = byteArrayToHex(encrypted);

		encrypted = null;
		ivspec = null;
		
		plainText = "";
		passkey = "";
		
		return hexText;
	}

	public static String byteArrayToHex(byte[] a) {
		String strResult = "";
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(String.format("%02x", b & 0xff));

		strResult = sb.toString();
		sb = null;

		return strResult;
	}
	
	public static String getHexToDec(String hex) {
		long v = Long.parseLong(hex, 16);
		return String.valueOf(v);
	}
}
