package py.com.chacomer.stock.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import py.com.chacomer.stock.Controller.AppController;
import py.com.chacomer.stock.Data.AppConfig;
import py.com.chacomer.stock.Helper.SessionManager;
import py.com.chacomer.stock.R;

public class AssignEan extends AppCompatActivity implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener{
    private BarcodeReader barcodeReader;
    EditText etEan;
    EditText etMaterial;
    Button btnRegister;
    SessionManager session;
    ProgressDialog pDialog;
    String usu;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        barcodeReader = Main.getBarcodeReader();
        setContentView(R.layout.activity_assign_ean);
        etEan = findViewById(R.id.etEan);
        etMaterial = findViewById(R.id.etMaterial);
        btnRegister = findViewById(R.id.btnRegister);
        session = new SessionManager(getApplicationContext());
        Intent in = getIntent();
        final Bundle bundle = in.getExtras();

        HashMap<String, String> user = session.getUserDetails();
        usu = user.get(SessionManager.KEY_USER);

        pDialog = new ProgressDialog(AssignEan.this);
        pDialog.setCancelable(false);
        if(barcodeReader != null){
            barcodeReader.addBarcodeListener(this);
            try{
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            }catch (UnsupportedPropertyException e){
                Toast.makeText(this, "Error al aplicar la propiedad", Toast.LENGTH_SHORT).show();
            }

            barcodeReader.addTriggerListener(this);

            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);

            barcodeReader.setProperties(properties);
        }

        btnRegister.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Boolean flag;
                String ean = etEan.getText().toString().trim();
                String mat = etMaterial.getText().toString().trim();
                flag = false;

                if(ean.equals("")){
                    Toast.makeText(AssignEan.this,"Debe ingresar un número de EAN", Toast.LENGTH_LONG).show();
                    flag = true;
                }
                if(mat.equals("")){
                    Toast.makeText(AssignEan.this,"Debe ingresar un código de material", Toast.LENGTH_LONG).show();
                    flag = true;
                }
                if(flag == false){
                    sendPoscion(ean, mat);
                }
            }
        });

    }
    private void sendPoscion(final String ean,final String mat) {
        //Toast.makeText(CheckStock.this, ""+pedido+", "+ean+", "+cantidad, Toast.LENGTH_LONG).show();
        String tag_string_req = "req_send_ean";

        pDialog.setMessage("Enviando material...");
        showDialog();

        StringRequest stReq = new StringRequest(Method.POST, AppConfig.URL_SEND_EAN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try{
                    JSONObject jObj = new JSONObject(response);
                    String mens = jObj.getString("mensaje");
                    Toast.makeText(AssignEan.this, ""+mens, Toast.LENGTH_SHORT).show();
                    boolean error = jObj.getBoolean("error");
                    if(error == false){
                        etEan.setText("");
                        etMaterial.setText("");
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(AssignEan.this, "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("ean", ean);
                params.put("material", mat);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //checkPosicion();
                //Toast.makeText(CheckStock.this, ""+ String.format("%18s", event.getBarcodeData()).replace(' ', '0'), Toast.LENGTH_SHORT).show();
                etEan.setText(event.getBarcodeData());
                //Toast.makeText(AssignEan.this,""+event.getBarcodeData(), Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        // TODO Auto-generated method stub
    }
    public static void restartActivity(Activity act){

        Intent intent=new Intent();
        intent.setClass(act, act.getClass());
        act.startActivity(intent);
        act.finish();

    }
    @Override
    public void onResume(){
        super.onResume();
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(this, "Scanner no disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            barcodeReader.release();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (barcodeReader != null) {
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(this);
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
