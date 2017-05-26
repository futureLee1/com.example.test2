package com.example.hello;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.nfc.*;
import android.util.Log;
import android.app.Activity;

public class TestPlugin extends CordovaPlugin {

    private static final String STATUS_NFC_OK = "NFC_OK";
    private static final String STATUS_NO_NFC = "NO_NFC";
    private static final String STATUS_NFC_DISABLED = "NFC_DISABLED";

    private static final String TAG = "NfcPlugin";

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		
		/*if (action.equals("greet")) {

            String name = args.getString(0);
            String message = "Hello, " + name + "\n action is "+args.getString(0);
            callbackContext.success(message);

            return true;

        } else {
            String message = "action is "+args.getString(0);
            callbackContext.error("Error, message");

            return false;

        }

        if (action.equals("nfcState")) {

            String actionString = args.getString(0);
            String message = "Hello, " + name + "\n action is "+args.getString(0);
            callbackContext.success(message);

            return true;

        } else {
            String message = "action is "+args.getString(0);
            callbackContext.error("Error, message");

            return false;

        }*/

        Log.d(TAG, "execute " + action);

        if (getNfcStatus().equals(STATUS_NFC_DISABLED)) {
            Log.d(TAG, "getNfcStatus().equals(STATUS_NFC_DISABLED)");
            String message = "NFC is off.";
            callbackContext.error(message);
            return true; // short circuit
        } else if (STATUS_NO_NFC) {
            Log.d(TAG, "getNfcStatus().equals(STATUS_NO_NFC)");
            String message = "Device can not use NFC.";
            callbackContext.error(message);
            return true;
        } else {
            Log.d(TAG, "getNfcStatus().equals(STATUS_NFC_OK)");
            String message = "Welcome ! ";
            callbackContext.success(message);
        }

        return true;

	}

    private String getNfcStatus() {
        Log.d(TAG, "In_getNfcStatus()");

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            return STATUS_NO_NFC;
        } else if (!nfcAdapter.isEnabled()) {
            return STATUS_NFC_DISABLED;
        } else {
            return STATUS_NFC_OK;
        }
    }

    private Activity getActivity() {
        Log.d(TAG, "In_getActivity()");
        return this.cordova.getActivity();
    }
	
}
