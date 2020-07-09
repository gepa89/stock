package py.com.chacomer.stock.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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

public class CheckReceptionStock extends AppCompatActivity implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener{
    private BarcodeReader barcodeReader;
    private TextView tituloStock;
    TextView tvMaterial;
    TextView tvCounter;
    LinearLayout lytClosing;
    LinearLayout lytOpenP;
    EditText etCant;
    EditText etNroPal;
    SessionManager session;
    ProgressDialog pDialog;
    Button btnCerrarOC;
    Button btnCerrarPallet;

    String usu;
    String nroc = "";
    String nrPallet = "";
    String idDoc = "";
    String hash = "";
    String sndMat = "";
    String controlados = "";
    Integer cc;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        barcodeReader = Main.getBarcodeReader();
        setContentView(R.layout.activity_checkreceptionstock);
        tituloStock = findViewById(R.id.tituloStock);
        tvMaterial = findViewById(R.id.tvMaterial);
        tvCounter = findViewById(R.id.tvCounterOC);
        lytClosing = findViewById(R.id.lytClosing);
        lytOpenP = findViewById(R.id.lytOpenP);
        btnCerrarOC = findViewById(R.id.btnCerrarOC);
        btnCerrarPallet = findViewById(R.id.btnCerrarPallet);

        etCant = findViewById(R.id.etCant);
        etNroPal = findViewById(R.id.etNroPallet);

        session = new SessionManager(getApplicationContext());
        Intent in = getIntent();
        final Bundle bundle = in.getExtras();

        nroc = bundle.getString("codigo");
        Toolbar toolbar = findViewById(R.id.my_toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(R.drawable.logo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        HashMap<String, String> user = session.getUserDetails();
        usu = user.get(SessionManager.KEY_USER);
        tituloStock.setText(Html.fromHtml("<b>Documento #"+ nroc +" </b>"));
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

        btnCerrarOC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDoc(nroc,usu);

            }
        });

        btnCerrarPallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(CheckReceptionStock.this, ""+nrPallet+" "+nroc, Toast.LENGTH_SHORT).show();
                closePallet(nrPallet,nroc);

            }
        });

    }

    private void closePallet(final String pallet, final String nroc) {
        String tag_string_req = "req_close_pallet";
        StringRequest stReq = new StringRequest(Method.POST, URL_CLOSE_PALLET, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    Toast.makeText(CheckReceptionStock.this, ""+jObj.getString("message"), Toast.LENGTH_SHORT).show();
                    if(error == false){
                        tvMaterial.setText("");
                        tvCounter.setText("");
                        etNroPal.setText("");
                        etCant.setVisibility(View.INVISIBLE);
                        lytOpenP.setVisibility(View.VISIBLE);
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
                params.put("pallet", pallet);
                params.put("nroc", nroc);
                params.put("usu", usu);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
    }

    private void closeDoc(final String nroc, final String usu) {
        String tag_string_req = "req_close_oc";
        StringRequest stReq = new StringRequest(Method.POST, URL_CLOSE_DOC_REC, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    Toast.makeText(CheckReceptionStock.this, ""+jObj.getString("message"), Toast.LENGTH_SHORT).show();
                    if(error == false){
                        Intent intent = new Intent(CheckReceptionStock.this, GetOCData.class);
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
                params.put("codigo", nroc);
                params.put("usuario", usu);
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
                //Toast.makeText(CheckReceptionStock.this, " "+rEan, Toast.LENGTH_LONG).show();
                if (lytClosing.getVisibility() == View.VISIBLE) {
                    String val = etCant.getText().toString();
                    if(!val.isEmpty()){
                        Integer cant = Integer.parseInt(val);
                        if(cant > 0){
                            saveMaterial(val, sndMat, nroc, nrPallet, usu);
                            getMaterial(rEan, nroc, usu);
                        }else{
                            Toast.makeText(CheckReceptionStock.this, "Favor ingrese una cantidad válida", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        getMaterial(rEan, nroc, usu);
                    }

                } else {
                    getMaterial(rEan, nroc, usu);
                }
            }
        });
    }

    private void saveMaterial(final String cant, final String material, final String nroc,final String nroPallet, final String usu) {
        String tag_string_req = "req_get_material";
        StringRequest stReq = new StringRequest(Method.POST, URL_SAVE_MATERIAL_REC, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    Toast.makeText(CheckReceptionStock.this, ""+jObj.getString("message"), Toast.LENGTH_LONG).show();
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
                params.put("nroc", nroc);
                params.put("nrPallet", nroPallet);
                params.put("cant", cant);
                params.put("usu", usu);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
    }

    private void getMaterial(final String mat, final String pedido, final String usu) {
        String tag_string_req = "req_get_material";
        StringRequest stReq = new StringRequest(Method.POST, URL_CKECK_MATERIAL_REC, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(error==false){
                        etCant.getText().clear();
                        sndMat = jObj.getString("cod_mat");
                        nrPallet = jObj.getString("pallet");
                        tvMaterial.setText(Html.fromHtml("<b>Material #"+ jObj.getString("cod_mat") +" </b>  <br/><b><big>"+ jObj.getString("desc_mat") +"</big></b>"));
                        tvCounter.setText(Html.fromHtml("<small><i>Pallet #"+jObj.getString("pallet")+" - "+jObj.getString("in_pallet")+" item(s) en pallet.</i>"));
                        etCant.setVisibility(View.VISIBLE);
                        lytClosing.setVisibility(View.VISIBLE);
                        lytOpenP.setVisibility(View.GONE);
                    }else{
                        Toast.makeText(CheckReceptionStock.this, ""+jObj.getString("message"), Toast.LENGTH_LONG).show();
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
                params.put("pedido", pedido);
                String val = etNroPal.getText().toString();
                if(!val.isEmpty()){
                    Integer nPal = Integer.parseInt(val);
                    if(nPal > 0){
                        params.put("nopallet", val);
                    }
                }
                params.put("usu", usu);
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
