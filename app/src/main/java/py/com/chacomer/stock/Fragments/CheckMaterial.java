package py.com.chacomer.stock.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;


import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import py.com.chacomer.stock.Activities.Main;
import py.com.chacomer.stock.Controller.AppController;
import py.com.chacomer.stock.Data.AppConfig;
import py.com.chacomer.stock.Helper.SessionManager;
import py.com.chacomer.stock.R;
public class CheckMaterial extends Fragment implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener{
    private BarcodeReader materialReader;
    private static final String TAG = CheckMaterial.class.getSimpleName();
    public CheckMaterial() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        materialReader = Main.getMaterialReader();
        if (materialReader != null) {

            // register bar code event listener
            materialReader.addBarcodeListener(this);

            // set the trigger mode to client control
            try {
                materialReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
            } catch (UnsupportedPropertyException e) {
                Log.e("Fragment", "Failed to apply properties");
//                Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }
            // register trigger state change listener
            materialReader.addTriggerListener(this);

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Disable bad read response, handle in onFailureEvent
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);
            // Apply the settings
            materialReader.setProperties(properties);
        }

        final View view = inflater.inflate(R.layout.fragment_check_material, container, false);
        return view;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume ...");
        super.onResume();
        if (materialReader != null) {
            try {
                materialReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause ...");
        super.onPause();
        if (materialReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            materialReader.release();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy ...");
        super.onDestroy();
        if (materialReader != null) {
            // unregister barcode event listener
            materialReader.removeBarcodeListener(this);

            // unregister trigger state change listener
            materialReader.removeTriggerListener(this);
        }
    }

    @Override
    public void onBarcodeEvent (final BarcodeReadEvent event) {
        Log.d(TAG, "onBarcodeEvent ...");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update UI to reflect the data
            }
        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        Log.d(TAG, "onFailureEvent ...");
    }

    // When using Automatic Trigger control do not need to implement the
    // onTriggerEvent function
    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        Log.d(TAG, "onTriggerEvent ...");
        try {
            // only handle trigger presses
            // turn on/off aimer, illumination and decoding
            materialReader.aim(event.getState());
            materialReader.light(event.getState());
            materialReader.decode(event.getState());

        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Scanner is not claimed", Toast.LENGTH_SHORT).show();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Scanner unavailable", Toast.LENGTH_SHORT).show();
        }
    }

}
