package py.com.chacomer.stock.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;


import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import py.com.chacomer.stock.Controller.AppController;
import py.com.chacomer.stock.Data.AppConfig;
import py.com.chacomer.stock.Fragments.CheckMaterial;
import py.com.chacomer.stock.Helper.SessionManager;
import py.com.chacomer.stock.R;

public class CheckPedido extends Activity implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener{
    private BarcodeReader barcodeReader;
    private ListView barcodeList;
    private Button btnFinalizar;
    private Button btnNuevo;
    private ListView pedidoList;
    private String nroPedido = "";
    String cantidad;
    String matAnterior = "";
    String nroMaterial = "";
    EditText in_cantidad;
    private Boolean flag = false;
    private Boolean terminar = false;
    private TextView tituloPedido;
    private TextView lblMaterial;
    SessionManager session;
    ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpedido);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnNuevo = findViewById(R.id.btn_nuevo);
        in_cantidad = findViewById(R.id.inputCant);
        CheckMaterial ckMaterial = new CheckMaterial();

        ckMaterial.setArguments(getIntent().getExtras());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        session = new SessionManager(getApplicationContext());
        barcodeReader = Main.getBarcodeReader();

        pDialog = new ProgressDialog(CheckPedido.this);
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

        pedidoList = findViewById(R.id.listViewPedidoData);

        btnFinalizar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                HashMap<String, String> user = session.getUserDetails();
                String usu = user.get(SessionManager.KEY_USER);
                cantidad = in_cantidad.getText().toString();
                if(!cantidad.isEmpty()){
                    terminar = true;
                    sendMaterial(nroPedido, matAnterior, cantidad,usu);

                }else{
                    Toast.makeText(CheckPedido.this, "Favor ingresar cantidad.", Toast.LENGTH_LONG).show();
                }

            }
        });
        btnNuevo.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                restartActivity(CheckPedido.this);
            }
        });
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(CheckPedido.this,""+event.getBarcodeData(), Toast.LENGTH_LONG).show();

                HashMap<String, String> user = session.getUserDetails();
                String usu = user.get(SessionManager.KEY_USER);
                if(nroPedido.isEmpty()){
                    nroPedido = event.getBarcodeData();
                    checkPedido(nroPedido, usu);
                }else if(nroMaterial.isEmpty()){
                    nroMaterial = event.getBarcodeData();
                    //Log.d("AAAA-> "+nroPedido+" - "+nroMaterial," aqui mensajes ");
                    getMaterial(nroMaterial,nroPedido);
                    if(flag == true){
                        cantidad = in_cantidad.getText().toString(); // Same
                        if(!cantidad.isEmpty()){
                            sendMaterial(nroPedido, matAnterior, cantidad, usu);
                            in_cantidad.setText("0"); // Same
                        }else{
                            Toast.makeText(CheckPedido.this, "Favor ingresar cantidad.", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        //Toast.makeText(CheckPedido.this, "flag es falso.", Toast.LENGTH_LONG).show();
                    }
                    nroMaterial = "";
                }
            }
        });
    }
    private void sendPedido(final String nroPedido, final String usuario){
        String tag_string_req = "req_send_pedido";

        pDialog.setMessage("Registrando Pedido...");
        showDialog();


        StringRequest stReq = new StringRequest(Method.POST, AppConfig.URL_SEND_PEDIDO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try{
                    JSONObject jObj = new JSONObject(response);
                    List<String> list = new ArrayList<String>();
                    if(jObj.has("mensajes")){
                        JSONArray mensajes = jObj.getJSONArray("mensajes");
                        for(int i = 0; i<mensajes.length();i++){
                            JSONObject obj = mensajes.getJSONObject(i);
                            String cod = obj.getString("Codigo");
                            switch(cod) {
                                case "0099":
                                    flag = false;
                                    LinearLayout lytMateriales = findViewById(R.id.lytMaterias);
                                    lytMateriales.setVisibility(View.GONE);
                                    LinearLayout lytNuevo = findViewById(R.id.lytNuevoPedido);
                                    lytNuevo.setVisibility(View.VISIBLE);
                                    break;
                                case "0004":
                                    flag = true;
                                    break;
                            }

                            Log.d("-> "+obj.toString(),"materiales ");

                            list.add(obj.getString("Codigo")+" - "+obj.getString("Mensaje"));
                        }
                        Log.d("-> "+mensajes.length(),"mensajes ");
                    }else{
                        //lytMaterias
                        LinearLayout lytMateriales = findViewById(R.id.lytMaterias);
                        lytMateriales.setVisibility(View.VISIBLE);
                        list.add("Pedido OK");
                        Log.d("-> Cero mensajes","mensajes ");
                    }

                    final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                            CheckPedido.this, android.R.layout.simple_list_item_1, list
                    );
                    pedidoList.setAdapter(dataAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(CheckPedido.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckPedido.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("codigo", nroPedido);
                params.put("usuario", usuario);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
    }
    private void sendMaterial(final String nroPedido, final String nroMaterial, final String cantidad, final String usuario){
        String tag_string_req = "req_send_material";

        pDialog.setMessage("Registrando Material...");
        showDialog();

        StringRequest stReq = new StringRequest(Method.POST, AppConfig.URL_SEND_MATERIAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try{
                    HashMap<String, String> user = session.getUserDetails();
                    String usuario = user.get(SessionManager.KEY_USER);

                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String mensaje = jObj.getString("message");
                    if(terminar == true){
                        sendPedido(nroPedido,usuario);
                    }else{
                        Toast.makeText(CheckPedido.this, mensaje, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(CheckPedido.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    restartActivity(CheckPedido.this);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckPedido.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("codigo", nroPedido);
                params.put("material", nroMaterial);
                params.put("cantidad", cantidad);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
    }
    private void getMaterial(final String nroMaterial, final String nroPedido){
        //Toast.makeText(CheckPedido.this,nroMaterial+" - "+nroPedido, Toast.LENGTH_LONG).show();
        String tag_string_req = "req_check_material";

        pDialog.setMessage("Cargando Material...");
        showDialog();

        StringRequest stReq = new StringRequest(Method.POST, AppConfig.URL_GET_MATERIAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try{
                    //Toast.makeText(CheckPedido.this, "Respuesta: " + response, Toast.LENGTH_LONG).show();
                      lblMaterial = findViewById(R.id.lblMaterial_in);
                      JSONObject jObj = new JSONObject(response);
                      boolean error = jObj.getBoolean("error");
                      if (!error) {
                          String materiales = jObj.getString("material");
                          matAnterior = jObj.getString("cod_mat");
                          lblMaterial.setText(Html.fromHtml(materiales));
                          flag = true;
                      } else {
                         String er_msg = jObj.getString("message");
                          lblMaterial.setText(Html.fromHtml(er_msg));
                          matAnterior = "";
                      }
                } catch (JSONException e) {
                   e.printStackTrace();
                   Toast.makeText(CheckPedido.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckPedido.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("codigo", nroPedido);
                params.put("material", nroMaterial);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
    }
    private void checkPedido(final String nroPedido, final String usuario){
        String tag_string_req = "req_check_pedido";

        pDialog.setMessage("Cargando Pedido...");
        showDialog();

        StringRequest stReq = new StringRequest(Method.POST, AppConfig.URL_GET_PEDIDO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        tituloPedido = findViewById(R.id.pedidoHeader);
                        JSONArray materiales = jObj.getJSONArray("materiales");
                        tituloPedido.setText(Html.fromHtml("Pedido #: <b>" + nroPedido + "</b>    |    <b>" + materiales.length() + "</b> material(es)"));

                        List<String> list = new ArrayList<String>();
                        if(jObj.has("mensajes")){
                            JSONArray mensajes = jObj.getJSONArray("mensajes");
                            for(int i = 0; i<mensajes.length();i++){
                                JSONObject obj = mensajes.getJSONObject(i);
                                Log.d("-> "+obj.toString(),"materiales ");
                                list.add(obj.getString("Mensaje"));
                            }
                            Log.d("-> "+mensajes.length(),"mensajes ");
                        }else{
                            //lytMaterias
                            LinearLayout lytMateriales = findViewById(R.id.lytMaterias);
                            lytMateriales.setVisibility(View.VISIBLE);
                            list.add("Pedido OK");
                            Log.d("-> Cero mensajes","mensajes ");
                        }

                        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                                CheckPedido.this, android.R.layout.simple_list_item_1, list
                        );
                        pedidoList.setAdapter(dataAdapter);

                    } else {
                        Toast.makeText(CheckPedido.this,
                                jObj.getString("message"), Toast.LENGTH_LONG).show();
                        restartActivity(CheckPedido.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(CheckPedido.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    restartActivity(CheckPedido.this);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CheckPedido.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("codigo", nroPedido);
                params.put("usuario", usuario);
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
