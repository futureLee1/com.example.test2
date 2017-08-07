package com.example.hello;

import java.io.IOException;

import android.nfc.tech.IsoDep;

public class Function {
	private static String MutualauthKey = "";
	private static String SessionKey = "";

	// Apdu 응답코드를 이용해 카드와 통신 수행
	public static int Apdu(IsoDep iso, String strCmd, String[] strResponse, String[] strErrMsg) {
		strResponse[0] = "";
		strErrMsg[0] = "";
		byte[] byteApdu = null;
		byte[] result = null;

		try {
			byteApdu = Conversion.hexStringToByteArray(strCmd);
			iso.setTimeout(5000);
			result = iso.transceive(byteApdu);
		} catch (IOException e) {
			e.printStackTrace();
			strErrMsg[0] = e.toString();
			return -1;
		}

		strResponse[0] = Conversion.toHexString(result);

		if (strResponse[0].equals("")) {
			strErrMsg[0] = "Apdu Response data is null";
			return -1;
		}

		if (!strResponse[0].substring(strResponse[0].length() - 4,
				strResponse[0].length()).equals("9000")) {
			return -1;
		}

		System.gc();
		System.runFinalization();

		return 0;
	}

	// 카드의 파일 검색 수행
	public static int SelectFile(IsoDep iso, String[] strResponse, String[] strErrMsg) {
		int res = 0;
		String strCmd = "";

		strCmd = "00A4040007D4004F54501010";
		res = Apdu(iso, strCmd, strResponse, strErrMsg);
		if (res < 0) {
			return -1;
		}

		return 0;
	}

	// 카드와 인증 수행
	public static int MutualAuthentication(IsoDep iso, String[] strResponse, String[] strErrMsg, String R1R2) {
		int res = 0;
		String strCmd = "";
		String KeyBlock = "";
		String Padding = "8000000000000000";
		String HR = "1122334455667788";
		String HostCryptogram = "";
		String MAC = "";

		// Key 생성
		KeyBlock = Conversion.Integrity_SHA256(R1R2);
		if (KeyBlock.equals("")) {
			return -1;
		}
		MutualauthKey = KeyBlock.substring(0, 32);

		// HostCryptogram 생성
		HostCryptogram = Conversion.AES_CBC_128_ENCRYPT(HR + Padding,
				MutualauthKey);
		if (HostCryptogram.equals("")) {
			return -1;
		}

		// MAC 생성
		MAC = Conversion.Integrity_SHA256(HostCryptogram + HR);
		if (KeyBlock.equals("")) {
			return -1;
		}
		MAC = MAC.substring(0, 16);

		strCmd = "8482000018" + HostCryptogram + MAC;
		res = Apdu(iso, strCmd, strResponse, strErrMsg);
		if (res < 0) {
			return -1;
		}

		// SessionKey 생성
		String Crytogram = strResponse[0].substring(0, 32);
		MAC = strResponse[0].substring(32, 48);
		res = CreateSessionKey(Crytogram, MAC);
		if (res < 0) {
			return -1;
		}

		return 0;
	}

	// 세션키 생성 수행
	public static int CreateSessionKey(String Crytogram, String MAC) {
		String HostCryptogram = Conversion.AES_CBC_128_DECRYPT(Crytogram, MutualauthKey);
		if (HostCryptogram.equals("")) {
			return -1;
		}

		// MAC 검증
		String MAC_Verify = Conversion.Integrity_SHA256(Crytogram + HostCryptogram);
		if (MAC_Verify.equals("")) {
			return -1;
		}
		MAC_Verify = MAC_Verify.toUpperCase();
		MAC_Verify = MAC_Verify.substring(0, 16);

		SessionKey = HostCryptogram;

		return 0;
	}

	// R1 Create
	public static int GetChallenge(IsoDep iso, String[] strResponse, String[] strErrMsg) {
		int res = 0;
		String strCmd = "";

		strCmd = "0084000010";
		res = Apdu(iso, strCmd, strResponse, strErrMsg);
		if (res < 0) {
			return -1;
		}

		strResponse[0] = strResponse[0].substring(0, strResponse[0].length() - 4);

		return 0;
	}

	/************ New Smart OTP Function ************/

	public static int SelectFile_New(IsoDep iso, String[] strResponse, String[] strErrMsg) {
		int res = 0;
		String strCmd = "";

		strCmd = "00A4040007D400001577101000";
		res = Apdu(iso, strCmd, strResponse, strErrMsg);
		if (res < 0) {
			return -1;
		}

		return 0;
	}

	public static int GetData_VENDOR_CODE(IsoDep iso, String[] strResponse, String[] strErrMsg) {
		int res = 0;
		String strCmd = "";

		strCmd = "00CA000003";
		res = Apdu(iso, strCmd, strResponse, strErrMsg);
		if (res < 0) {
			return -1;
		}
		strResponse[0] = strResponse[0].substring(0, strResponse[0].length() - 4);

		return 0;
	}

	public static int GetData_VALID_DATE(IsoDep iso, String[] strResponse, String[] strErrMsg) {
		int res = 0;
		String strCmd = "";

		strCmd = "00CA010004";
		res = Apdu(iso, strCmd, strResponse, strErrMsg);
		if (res < 0) {
			return -1;
		}
		strResponse[0] = strResponse[0].substring(0, strResponse[0].length() - 4);

		return 0;
	}

	public static int GetData_Serial_Num(IsoDep iso, String[] strResponse, String[] strErrMsg) {
		int res = 0;
		String strCmd = "";

		strCmd = "00CA020006";
		res = Apdu(iso, strCmd, strResponse, strErrMsg);
		if (res < 0) {
			return -1;
		}
		strResponse[0] = strResponse[0].substring(0, strResponse[0].length() - 4);

		return 0;
	}

	public static int GenerateOTP(IsoDep iso, String[] strResponse, String[] strErrMsg, String V, String OPIN, String OC, String Account, String Amount) {
		int res = 0;
		String strCmd = "";
		String strEncrypt = "";
		String TM = "";
		String HTI = "";

		// HTI 생성
		if (!Account.equals("") && !Amount.equals("")) {
			Account = Account.replaceAll("-", "");
			Amount = Amount.replaceAll(",", "");
			HTI = Account + Amount;
			HTI = util.conversion.transSHA256(HTI);
		}

		// 기관코드
		OC = util.conversion.decToHex(Integer.parseInt(OC), 6);

		// TM(시간정보) 생성
		long UTC_Milliseconds = System.currentTimeMillis();
		long Seconds = UTC_Milliseconds / 1000;
		TM = util.conversion.decToHex((int) Seconds, 8);

		// OPIN 6자리 변환
		if (OPIN.length() < 6) {
			OPIN = OPIN + "00";
		}

		if (HTI.equals("")) {
			strEncrypt = util.conversion.AES_CBC_128_ENCRYPT(V + OPIN + OC + TM + "8000000000", SessionKey);

			if (strEncrypt.equals("")) {
				return -1;
			}

			strCmd = "9422010010" + strEncrypt;
			res = Apdu(iso, strCmd, strResponse, strErrMsg);
			if (res < 0) {
				return -1;
			}
		} else {
			strEncrypt = util.conversion.AES_CBC_128_ENCRYPT(V + OPIN + OC + TM + HTI + "8000000000", SessionKey);
			if (strEncrypt.equals("")) {
				return -1;
			}

			strCmd = "9422010030" + strEncrypt;
			res = Apdu(iso, strCmd, strResponse, strErrMsg);
			if (res < 0) {
				return -1;
			}
		}

		return 0;
	}

}