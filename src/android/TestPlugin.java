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

        if (!getNfcStatus().equals(STATUS_NFC_OK)) {
            callbackContext.error(getNfcStatus());
            return true; // short circuit
        } else {
            String message = "Hello, NFC is On ! ";
            callbackContext.success(message);
        }

	}

    private String getNfcStatus() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (nfcAdapter == null) {
            return STATUS_NO_NFC;
        } else if (!nfcAdapter.isEnabled()) {
            return STATUS_NFC_DISABLED;
        } else {
            return STATUS_NFC_OK;
        }
    }
	
}
