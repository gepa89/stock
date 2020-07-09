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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


import java.util.List;

import py.com.chacomer.stock.Controller.AppController;
import py.com.chacomer.stock.Data.AppConfig;
import py.com.chacomer.stock.Helper.SessionManager;
import py.com.chacomer.stock.R;

public class CheckStock extends AppCompatActivity implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener{
    private BarcodeReader barcodeReader;
    private ListView barcodeList;
    private Button btnNuevo;
    Button brtCant;
    Button btnMatCod;
    private ListView pedidoList;
    private String nroPedido = "";
    String cantidad;
    String matAnterior = "";
    String nroMaterial = "";
    EditText inMatCod;
    ImageView bar_logo;
    private Boolean flag = false;
    private Boolean terminar = false;
    private TextView tituloStock;
    TextView mDes;
    TextView mData;
    TextView matCodig;
    TextView matCod;
    TextView refUbic;
    TextView inCant;
    SessionManager session;
    ProgressDialog pDialog;
    String usu;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        barcodeReader = Main.getBarcodeReader();
        setContentView(R.layout.activity_checkstock);

        tituloStock = findViewById(R.id.stockHeader);
        mDes = findViewById(R.id.matDes);
        mData = findViewById(R.id.matNU);
        matCodig = findViewById(R.id.matCod);
        refUbic = findViewById(R.id.refUbi);
        bar_logo = findViewById(R.id.logo_bar);
        brtCant = findViewById(R.id.cant_reg);
        inMatCod = findViewById(R.id.inMatCodLin);
        inCant = findViewById(R.id.cant_in);
        btnNuevo = findViewById(R.id.btn_nuevo_stock);
        btnMatCod = findViewById(R.id.btnMatCodLin);
        session = new SessionManager(getApplicationContext());
        Intent in = getIntent();
        final Bundle bundle = in.getExtras();


        HashMap<String, String> user = session.getUserDetails();
        usu = user.get(SessionManager.KEY_USER);
        tituloStock.setText(Html.fromHtml("<b>Control de Stock </b>  "+ bundle.getString("mensaje") +"ado<br/>Orden <b>#"+bundle.getString("codigo")+"</b>. Pendiente: <b>"+bundle.getString("c_material")+" material(es)</b>"));

        pDialog = new ProgressDialog(CheckStock.this);
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
        getPosicionRestart(bundle.getString("codigo"));
        btnNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckStock.this, GetDocument.class);
                startActivity(intent);
                finish();
            }
        });
        btnMatCod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String gcode = inMatCod.getText().toString();
                String mCodig = matCodig.getText().toString();
                String[] parts = mCodig.split(":");
                Toast.makeText(CheckStock.this, gcode.trim()+" "+parts[1].trim(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(CheckStock.this, ""+ String.format("%18s", event.getBarcodeData()).replace(' ', '0'), Toast.LENGTH_SHORT).show();
                if(gcode.trim().equals(parts[1].trim())){
                  LinearLayout lytMateriales = findViewById(R.id.formCant);
                  lytMateriales.setVisibility(View.VISIBLE);
                }else{
                   Toast.makeText(CheckStock.this, "No corresponde Material", Toast.LENGTH_SHORT).show();
                }
            }
        });
        brtCant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String val = inCant.getText().toString();
                if(!val.isEmpty()){
                    Integer cant = Integer.parseInt(val);
                    if(cant >= 0){
                        final String cantidad = inCant.getText().toString();
                        final String pedido = bundle.getString("codigo");
                        final String ean = refUbic.getText().toString();
                        final String material = inMatCod.getText().toString();
                        new AlertDialog.Builder(CheckStock.this)
                                .setMessage(Html.fromHtml("¿Desea confirmar el registro? <br/><b><i>La operación no se puede deshacer</i></b>."))
                                .setCancelable(false)
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        sendPoscion(pedido, ean, material, cantidad, usu);
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();

                    }else{
                        Toast.makeText(CheckStock.this, "Favor ingrese una cantidad válida", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(CheckStock.this, "Favor ingrese una cantidad", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
    private void sendPoscion(final String pedido,final String ean,final String mat,final String cantidad, final String usuario) {
        //Toast.makeText(CheckStock.this, ""+pedido+", "+ean+", "+cantidad, Toast.LENGTH_LONG).show();
        String tag_string_req = "req_send_posicion";

        pDialog.setMessage("Enviando material...");
        showDialog();

        StringRequest stReq = new StringRequest(Method.POST, AppConfig.URL_SEND_POSICION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(error == false){
                        Toast.makeText(CheckStock.this, "Material registrado correctamente", Toast.LENGTH_SHORT).show();
                        getPosicion(pedido);
                    }else{
                        String mens = jObj.getString("mensaje");
                        Toast.makeText(CheckStock.this, ""+mens, Toast.LENGTH_SHORT).show();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(CheckStock.this, "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                params.put("codigo", pedido);
                params.put("ean", ean);
                params.put("material", mat);
                params.put("cantidad", cantidad);
                params.put("usuario", usuario);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
    }
    private void savePosicion(){

    }
    private void getPosicion(final String codigo) {
        String tag_string_req = "req_get_posicion";

        pDialog.setMessage("Cargando material...");
        showDialog();

        StringRequest stReq = new StringRequest(Method.POST, AppConfig.URL_POSICION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();

                LinearLayout lytAll = findViewById(R.id.linearLayout);
                LinearLayout lytContent = findViewById(R.id.formCant);

                LinearLayout lytNuevo = findViewById(R.id.lytNuevoStock);

                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String msg = jObj.getString("mensaje");
                    if(error == false){
                        JSONObject materiales = jObj.getJSONObject("materiales");
                        if(!msg.isEmpty()){
                            msg = "<br/>"+msg+"";
                        }
                        tituloStock.setText(Html.fromHtml("<b>Control de Stock </b> <br/>Orden <b>#"+codigo+"</b>. Pendiente: <b>"+jObj.getString("c_material")+" material(es)</b>"+msg+""));
                        //Toast.makeText(CheckStock.this, "entra "+materiales.getString("pd_des_mat"), Toast.LENGTH_LONG).show();
                        mDes.setText(Html.fromHtml("<b>"+materiales.getString("pd_des_mat")+"</b>"));
                        mData.setText(Html.fromHtml("<i><b>Ubic.:</b> "+materiales.getString("pd_ubicacion")+"</i>"));
                        matCodig.setText(Html.fromHtml("<i><b>Código:</b> "+materiales.getString("pd_material")+"</i>"));
                        refUbic.setText(materiales.getString("pd_ean"));
                        inCant.setText("");
                        inMatCod.setText("");
                        lytContent.setVisibility(View.INVISIBLE);
                        lytNuevo.setVisibility(View.INVISIBLE);
                        lytAll.setVisibility(View.VISIBLE);
                    }else{
                        String mens = jObj.getString("mensaje");
                        Toast.makeText(CheckStock.this, ""+mens, Toast.LENGTH_LONG).show();
                        lytNuevo.setVisibility(View.VISIBLE);
                        lytAll.setVisibility(View.INVISIBLE);
                        lytContent.setVisibility(View.INVISIBLE);
                        tituloStock.setText(Html.fromHtml("<b>Control de Stock </b>"));
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(CheckStock.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                params.put("codigo", codigo);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);
    }
    private void getPosicionRestart(final String codigo) {
        String tag_string_req = "req_get_posicion";

        pDialog.setMessage("Cargando material...");
        showDialog();

        StringRequest stReq = new StringRequest(Method.POST, AppConfig.URL_POSICION_RESTART, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();

                LinearLayout lytAll = findViewById(R.id.linearLayout);
                LinearLayout lytContent = findViewById(R.id.formCant);

                LinearLayout lytNuevo = findViewById(R.id.lytNuevoStock);

                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    String msg = jObj.getString("mensaje");
                    if(error == false){
                        JSONObject materiales = jObj.getJSONObject("materiales");
                        if(!msg.isEmpty()){
                            msg = "<br/>"+msg+"";
                        }
                        tituloStock.setText(Html.fromHtml("<b>Control de Stock </b> <br/>Orden <b>#"+codigo+"</b>. Pendiente: <b>"+jObj.getString("c_material")+" material(es)</b>"+msg+""));
                        //Toast.makeText(CheckStock.this, "entra "+materiales.getString("pd_des_mat"), Toast.LENGTH_LONG).show();
                        mDes.setText(Html.fromHtml("<b>"+materiales.getString("pd_des_mat")+"</b>"));
                        mData.setText(Html.fromHtml("<i><b>Ubic.:</b> "+materiales.getString("pd_ubicacion")+"</i>"));
                        matCodig.setText(Html.fromHtml("<i><b>Código:</b> "+materiales.getString("pd_material")+"</i>"));
                        refUbic.setText(materiales.getString("pd_ean"));
                        inCant.setText("");
                        lytContent.setVisibility(View.INVISIBLE);
                        lytNuevo.setVisibility(View.INVISIBLE);
                        lytAll.setVisibility(View.VISIBLE);
                    }else{
                        String mens = jObj.getString("mensaje");
                        Toast.makeText(CheckStock.this, ""+mens, Toast.LENGTH_LONG).show();
                        lytNuevo.setVisibility(View.VISIBLE);
                        lytAll.setVisibility(View.INVISIBLE);
                        lytContent.setVisibility(View.INVISIBLE);
                        tituloStock.setText(Html.fromHtml("<b>Control de Stock </b>"));
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(CheckStock.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                params.put("codigo", codigo);
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
                String gean = refUbic.getText().toString();
                String[] parts = gean.split("-");
                List<String> list = Arrays.asList(parts);
                //Toast.makeText(CheckStock.this, ""+ String.format("%18s", event.getBarcodeData()).replace(' ', '0'), Toast.LENGTH_SHORT).show();
                if(list.contains(String.format("%18s", event.getBarcodeData()).replace(" ", ""))){
                    LinearLayout lytMateriales = findViewById(R.id.formCant);
                    lytMateriales.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(CheckStock.this, "No corresponde Material", Toast.LENGTH_SHORT).show();
                }
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
