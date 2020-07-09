package py.com.chacomer.stock.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import py.com.chacomer.stock.Adapter.AdapterMaterial;
import py.com.chacomer.stock.Controller.AppController;
import py.com.chacomer.stock.Data.AppConfig;
import py.com.chacomer.stock.Entities.Materials;
import py.com.chacomer.stock.Helper.SessionManager;
import py.com.chacomer.stock.R;

public class CheckLocation extends AppCompatActivity implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener{
    private BarcodeReader barcodeReader;
    AdapterMaterial adapterMaterial;
    SessionManager session;
    ProgressDialog pDialog;
    String usu;
    ListView listView;
    Button btnFind;
    EditText etFind;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        barcodeReader = Main.getBarcodeReader();
        setContentView(R.layout.activity_location);

        session = new SessionManager(getApplicationContext());
        Intent in = getIntent();
        final Bundle bundle = in.getExtras();
        listView = findViewById(R.id.listData);
        btnFind = findViewById(R.id.btnFind);
        etFind = findViewById(R.id.etFind);

        HashMap<String, String> user = session.getUserDetails();
        usu = user.get(SessionManager.KEY_USER);
        pDialog = new ProgressDialog(CheckLocation.this);
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
        btnFind.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                String material = etFind.getText().toString();
                if(!material.isEmpty()){
                    String ean = "";
                    getPosicion(ean, material);
                }else{
                    Toast.makeText(CheckLocation.this, "Debe ingresar un Código de Material. ", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    public void getPosicion(final String ean, final String material){
        String tag_string_req = "req_get_material";

        pDialog.setMessage("Buscando Material...");
        showDialog();

        StringRequest stReq = new StringRequest(Method.POST, AppConfig.URL_GET_LIST_MATERIAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try{
                    List<Materials> listMovies = new ArrayList<>();
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(error == false){
                        if(jObj.has("mats")){
                            etFind.getText().clear();
                            JSONArray materiales = jObj.getJSONArray("mats");

                            for(int i = 0; i<materiales.length();i++){

                                JSONObject obj = materiales.getJSONObject(i);
                                Materials Material = new Materials();
                                Material.setName(obj.getString("desc_mat"));
                                Material.setUbicacion(obj.getString("desc_ubicacion"));
                                Material.setCentro(obj.getString("desc_centro"));
                                Material.setAlmacen(obj.getString("desc_almacen"));
                                Material.setCodigo(obj.getString("cod_mat"));
                                Material.setStock(obj.getString("desc_cant"));

                                listMovies.add(Material);
                            }
                            adapterMaterial = new AdapterMaterial(getApplicationContext(), listMovies);
                            listView.setAdapter(adapterMaterial);
                        }
                    }else{
                        Toast.makeText(CheckLocation.this, "Error: "+jObj.getString("message"), Toast.LENGTH_LONG).show();
                    }
                    //Toast.makeText(CheckLocation.this, "Res: " + listMovies.toString(), Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(CheckLocation.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    restartActivity(CheckLocation.this);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckLocation.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("ean", ean);
                params.put("material", material);
                Log.w("material enviado",material);
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
                String material = "";
                getPosicion(event.getBarcodeData(), material);
            //Toast.makeText(CheckStock.this,""+event.getBarcodeData(), Toast.LENGTH_LONG).show();
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
