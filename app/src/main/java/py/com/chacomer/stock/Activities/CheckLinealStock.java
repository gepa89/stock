package py.com.chacomer.stock.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.HashMap;
import java.util.Map;

import py.com.chacomer.stock.Controller.AppController;
import py.com.chacomer.stock.Helper.SessionManager;
import py.com.chacomer.stock.R;

import static py.com.chacomer.stock.Data.AppConfig.*;

public class CheckLinealStock extends AppCompatActivity implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener{
    private BarcodeReader barcodeReader;
    private TextView tituloStock;
    TextView tvMaterial;
    TextView tvCounter;
    LinearLayout lytClosing;
    EditText etCant;
    EditText etComment;
    EditText etNwMat;
    Button btnMatCodLin;

    SessionManager session;
    ProgressDialog pDialog;
    Button btnCerrarDoc;
    String usu;
    String centro = "";
    String almacen = "";
    String idDoc = "";
    String hash = "";
    String sndMat = "";
    String controlados = "";
    Integer cc;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        barcodeReader = Main.getBarcodeReader();
        setContentView(R.layout.activity_checklinealstock);
        tituloStock = findViewById(R.id.tituloStock);
        tvMaterial = findViewById(R.id.tvMaterial);
        tvCounter = findViewById(R.id.tvCounterOC);
        lytClosing = findViewById(R.id.lytClosing);
        btnCerrarDoc = findViewById(R.id.btnCerrarOC);
        etCant = findViewById(R.id.etCant);
        etComment = findViewById(R.id.etComment);
        etNwMat = findViewById(R.id.inMatCodLin);
        btnMatCodLin = findViewById(R.id.btnMatCodLin);
        session = new SessionManager(getApplicationContext());
        Intent in = getIntent();
        final Bundle bundle = in.getExtras();

        centro = bundle.getString("centro");
        almacen = bundle.getString("almacen");
        idDoc = bundle.getString("id");
        hash = bundle.getString("hash");
        controlados = bundle.getString("controlados");
        Toolbar toolbar = findViewById(R.id.my_toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(R.drawable.logo_comagro);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        HashMap<String, String> user = session.getUserDetails();
        usu = user.get(SessionManager.KEY_USER);
        tituloStock.setText(Html.fromHtml("<b>Documento #"+ bundle.getString("id") +" </b>  <br/><b>Centro: </b>"+ bundle.getString("centro") +"<br/><b>Almacén: </b>"+bundle.getString("almacen")+"."));
        tvCounter.setText(Html.fromHtml("<small><i>"+controlados+" material(es) registrado(s)</i><small>"));
        cc = Integer.parseInt(controlados);
        pDialog = new ProgressDialog(CheckLinealStock.this);
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

        btnMatCodLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rEan = etNwMat.getText().toString();
                String flg = "0";
                if(!rEan.isEmpty()){
                    if (lytClosing.getVisibility() == View.VISIBLE) {
                        String val = etCant.getText().toString();
                        String com = etComment.getText().toString();
                        if(!val.isEmpty()){
                            Integer cant = Integer.parseInt(val);
                            if(cant >= 0){
                                String str = val+ centro+" "+almacen+" "+idDoc+" "+sndMat+" "+usu;
                                saveMaterial(val, centro, almacen, idDoc, sndMat, usu, com);
                                getMaterial(rEan, centro, almacen, idDoc, hash, usu, flg);
                                //Toast.makeText(CheckLinealStock.this, "envio cantidad "+str, Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(CheckLinealStock.this, "Favor ingrese una cantidad válida", Toast.LENGTH_LONG).show();
                            }
                        }else{
                            getMaterial(rEan, centro, almacen, idDoc, hash, usu, flg);
                        }

                    } else {
                        getMaterial(rEan, centro, almacen, idDoc, hash, usu, flg);
                    }
                }else{
                    Toast.makeText(CheckLinealStock.this, "Favor ingrese un código de material", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnCerrarDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(cc > 0){
                    String val = etCant.getText().toString();
                    String com = etComment.getText().toString();
                    if(!val.isEmpty()){
                        Integer cant = Integer.parseInt(val);
                        if(cant >= 0){
                            saveMaterial(val, centro, almacen, idDoc, sndMat, usu, com);
                            closeDoc(idDoc, usu);
                        }else{
                            Toast.makeText(CheckLinealStock.this, "Favor ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(CheckLinealStock.this, "Debe ingresar una cantidad antes de cerrar el documento.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CheckLinealStock.this, "Documento vacío, no se puede cerrar.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void closeDoc(final String idDoc, final String usu) {
        String tag_string_req = "req_close_doc";
        StringRequest stReq = new StringRequest(Method.POST, URL_CLOSE_DOCUMENT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    Toast.makeText(CheckLinealStock.this, ""+jObj.getString("message"), Toast.LENGTH_SHORT).show();
                    if(error == false){
                        Intent intent = new Intent(CheckLinealStock.this, OpenCkStock.class);
                        startActivity(intent);
                        finish();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
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
                params.put("idDoc", idDoc);
                params.put("usu", usu);
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
                String rEan = event.getBarcodeData();
                String flg = "1";
                if (lytClosing.getVisibility() == View.VISIBLE) {
                    String val = etCant.getText().toString();
                    String com = etComment.getText().toString();
                    if(!val.isEmpty()){
                        Integer cant = Integer.parseInt(val);
                        if(cant >= 0){
                            String str = val+ centro+" "+almacen+" "+idDoc+" "+sndMat+" "+usu;
                            saveMaterial(val, centro, almacen, idDoc, sndMat, usu, com);
                            getMaterial(rEan, centro, almacen, idDoc, hash, usu, flg);
                            //Toast.makeText(CheckLinealStock.this, "envio cantidad "+str, Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(CheckLinealStock.this, "Favor ingrese una cantidad válida", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        getMaterial(rEan, centro, almacen, idDoc, hash, usu, flg);
                    }

                } else {
                    getMaterial(rEan, centro, almacen, idDoc, hash, usu, flg);
                }
            }
        });
    }

    private void saveMaterial(final String cant, final String centro, final String almacen, final String idDoc, final String material, final String usu, final String comment) {
        String tag_string_req = "req_get_material";
        StringRequest stReq = new StringRequest(Method.POST, URL_SAVE_MATERIAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    Toast.makeText(CheckLinealStock.this, ""+jObj.getString("message"), Toast.LENGTH_LONG).show();
                }catch(JSONException e){
                    e.printStackTrace();
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
                params.put("material", material);
                params.put("centro", centro);
                params.put("almacen", almacen);
                params.put("idDoc", idDoc);
                params.put("cant", cant);
                params.put("usu", usu);
                params.put("com", comment);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
    }

    private void getMaterial(final String mat, final String centro, final String almacen, final String idDoc, final String hash, final String usu, final String flg) {
        String tag_string_req = "req_get_material";
        StringRequest stReq = new StringRequest(Method.POST, URL_CKECK_MATERIAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jObj = new JSONObject(response);
                    Log.w("txt", ""+jObj);
                    boolean error = jObj.getBoolean("error");
                    if(error==false){
                        etCant.setText("0");
                        etComment.getText().clear();
                        sndMat = jObj.getString("cod_mat");
                        controlados = jObj.getString("controlados");
                        cc = Integer.parseInt(controlados);
                        tvMaterial.setText(Html.fromHtml("<b>Material #"+ jObj.getString("cod_mat") +" </b>  <br/><b><big>"+ jObj.getString("desc_mat") +"</big></b>"));
                        tvCounter.setText(Html.fromHtml("<small><i>"+controlados+" material(es) registrado(s)</i><small>"));
                        lytClosing.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(CheckLinealStock.this, ""+jObj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                }catch(JSONException e){
                    e.printStackTrace();
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
                params.put("material", mat);
                params.put("centro", centro);
                params.put("almacen", almacen);
                params.put("idDoc", idDoc);
                params.put("hash", hash);
                params.put("usu", usu);
                params.put("flg", flg);


                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
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
