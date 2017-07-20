package com.example.hello;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.nfc.*;
import android.nfc.tech.IsoDep;
import android.util.Log;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import java.io.IOException;
import android.widget.Toast;

public class TestPlugin extends CordovaPlugin {

    private static final String STATUS_NFC_OK = "NFC_OK";
    private static final String STATUS_NO_NFC = "NO_NFC";
    private static final String STATUS_NFC_DISABLED = "NFC_DISABLED";

    private static final String TAG = "NfcPlugin";

    private PendingIntent pendingIntent = null;

    String jsonData = "";

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "execute_Action " + action);
        Log.d(TAG, "execute_args " + args.getString(0));

        String encodingData = Conversion.AES_CBC_128_ENCRYPT("12345678123456781234567800000000", "12345612345612345612345612345622");
        
        String decodingData = Conversion.AES_CBC_128_DECRYPT(encodingData, "12345612345612345612345612345622");

        String sha256Data = Conversion.SHA256("Test Data is Futurenmore");
        
        String hexToDexData = Conversion.getHexToDec("abcdefff");
        
        int a = 20;
        int b = 4;
        
        int calData = a / b;
        
        Log.e("encoding", ""+encodingData);
        Log.e("decoding", ""+decodingData);
        Log.e("sha256_Data", ""+sha256Data);
        Log.e("hexToDex", ""+hexToDexData);
        Log.e("calculate", ""+calData);

        jsonData = args.getString(0);

        if (getNfcStatus().equals(STATUS_NFC_DISABLED)) {
            Log.d(TAG, "getNfcStatus().equals(STATUS_NFC_DISABLED)");
            String message = "NFC is off.";
            callbackContext.error(message);
            return true; // short circuit
        } else if (getNfcStatus().equals(STATUS_NO_NFC)) {
            Log.d(TAG, "getNfcStatus().equals(STATUS_NO_NFC)");
            String message = "Device can not use NFC.";
            callbackContext.error(message);
            return true;
        } else {
            Log.d(TAG, "getNfcStatus().equals(STATUS_NFC_OK)");
            String message = "Welcome ! ";
            callbackContext.success(message);

            startNfc();

        }

        return true;

	}

    private void createPendingIntent() {
        Log.d(TAG, "createPendingIntent()");
        if (pendingIntent == null) {
            Log.d(TAG, "createPendingIntent()_null");
            Activity activity = getActivity();
            Intent intent = new Intent(activity, activity.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        }
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

    private PendingIntent getPendingIntent() {
        Log.d(TAG, "getPendingIntent()");
        return pendingIntent;
    }

    private Intent getIntent() {
        Log.d(TAG, "getIntent()");
        return getActivity().getIntent();
    }

    private void setIntent(Intent intent) {
        Log.d(TAG, "setIntent()");
        getActivity().setIntent(intent);
    }

    private void startNfc() {
        Log.d(TAG, "startNfc()");
        createPendingIntent(); // onResume can call startNfc before execute

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

                if (nfcAdapter != null && !getActivity().isFinishing()) {
                    nfcAdapter.enableForegroundDispatch(getActivity(), getPendingIntent(), null, null);
                }
            }
        });
    }

    private void stopNfc() {
        Log.d(TAG, "stopNfc");
        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

                if (nfcAdapter != null) {
                    nfcAdapter.disableForegroundDispatch(getActivity());
                }
            }
        });
    }

    /*@Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "onPause " + getIntent());
        super.onPause(multitasking);
        if (multitasking) {
            // nfc can't run in background
            stopNfc();            
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        Log.d(TAG, "onResume " + getIntent());
        super.onResume(multitasking);
        startNfc();
    }*/

    @Override
    public void onPause(boolean multitasking) {
        Log.d(TAG, "onPause()");
        Log.d(TAG, "onPause " + getIntent()+", "+multitasking);
        super.onPause(multitasking);
        
        if(multitasking) {
            stopNfc();
        }
        
    }

    @Override
    public void onResume(boolean multitasking) {
        Log.d(TAG, "onResume()");
        Log.d(TAG, "onResume " + getIntent()+", "+multitasking);
        super.onResume(multitasking);
        startNfc();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent()");
        Log.d(TAG, "onNewIntent " + intent);
        super.onNewIntent(intent);
        setIntent(intent);
        /*parseMessage();*/

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                
        String[] TechList = tag.getTechList();
        if(find(TechList, "android.nfc.tech.IsoDep") < 0){
            return;
        }

        IsoDep iso = IsoDep.get(tag);

        try{
            if(!iso.isConnected()){
                iso.connect();
            }

            if(jsonData.equals("Select")) {
                Certification(iso);
            } else {
                Toast.makeText(getActivity(), "ZZZZ", Toast.LENGTH_SHORT).show();
            }

            
                        
            if(iso.isConnected()){
                iso.close();
            }

        } catch(IOException e) {
            Log.e("IsoDep Error", e.toString());
        }
        
    }
	
    void parseMessage() {
        Log.d(TAG, "parseMessage()");
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "parseMessage " + getIntent());
                Intent intent = getIntent();
                String action = intent.getAction();
                Log.d(TAG, "intent_action " + action);
                
                if (action == null) {
                    return;
                }

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                
                String[] TechList = tag.getTechList();
                if(find(TechList, "android.nfc.tech.IsoDep") < 0){
                    return;
                }

                IsoDep iso = IsoDep.get(tag);

                try{
                    if(!iso.isConnected()){
                        iso.connect();
                    }

                    Certification(iso);
                        
                    if(iso.isConnected()){
                        iso.close();
                    }

                } catch(IOException e) {
                    Log.e("IsoDep Error", e.toString());
                }

            }
        });
    }

    private void Certification(IsoDep iso) {
        Log.d(TAG, "Certification()");
        int res = 0;
        String strResponse[] = new String[1];
        String strErrMsg[] = new String[1];
        
        res = 0;
        res = Function.SelectFile(iso, strResponse, strErrMsg);
        if(res < 0){
            Log.e("SelectFile","Card Select Failed");
            strResponse = null;
            strErrMsg = null;
            return;
        }

        Log.e("Card_Result",""+strResponse[0]);

        String strResult = strResponse[0].substring(strResponse[0].length() - 4, strResponse[0].length());

        Log.e("strResult",""+strResult);        

        Toast.makeText(getActivity(), ""+strResult, Toast.LENGTH_LONG).show();
                    
        if(iso.isConnected()){
            try {
                iso.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  
    }

    public int find(String[] arr, String s){
        Log.d(TAG, "find()");
        for(int i=0; i<arr.length; i++){
            if(arr[i].indexOf(s) >= 0){
                return i;
            }
        }
        return -1;
    }


}
