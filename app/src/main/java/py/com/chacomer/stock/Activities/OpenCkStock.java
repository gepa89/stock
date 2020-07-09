package py.com.chacomer.stock.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import py.com.chacomer.stock.Controller.AppController;
import py.com.chacomer.stock.Helper.SQLiteHandler;
import py.com.chacomer.stock.Helper.SessionManager;
import py.com.chacomer.stock.R;

import static py.com.chacomer.stock.Data.AppConfig.*;


public class OpenCkStock extends AppCompatActivity {

    SessionManager session;
    ProgressDialog pDialog;

    private SQLiteHandler db;
    String usu = "";
    String centro = "";
    String almacen = "";

    private ListView pedidoList;

    private Button botonCrear;
    private EditText NroInv;
    Spinner spCentros;
    Spinner spAlmacenes;
    List<String> centros = new ArrayList<String>();
    List<String> almacenes = new ArrayList<String>();

    List<String> lDocs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_openckstock);

        Toolbar toolbar = findViewById(R.id.my_toolbar4);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(R.drawable.logo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        db = new SQLiteHandler(OpenCkStock.this);
        session = new SessionManager(OpenCkStock.this);

        HashMap<String, String> user = session.getUserDetails();
        usu = user.get(SessionManager.KEY_USER);

        pedidoList = findViewById(R.id.listViewPedidoData);
        loadLatest(URL_GET_LAST_DOC);

        spCentros = findViewById(R.id.spinner);
        spAlmacenes = findViewById(R.id.spinner2);
        loadSpinnerData(URL_GET_CENTROS);
        spCentros.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        Object item = parent.getItemAtPosition(pos);
                        String url = URL_GET_ALMACENES+"?alm="+item.toString();
                        loadSpinnerDataAlm(url);
                        centro = item.toString();
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        spAlmacenes.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        Object item = parent.getItemAtPosition(pos);
                        almacen = item.toString();

                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        botonCrear = findViewById(R.id.botonCrear);
        botonCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
                StringRequest stringRequest=new StringRequest(Request.Method.POST, URL_CREATE_LINEAL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject=new JSONObject(response);
                            if(jsonObject.getInt("success")==1){
                                String[] separated = almacen.split(" - ");
                                Toast.makeText(OpenCkStock.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(OpenCkStock.this, CheckLinealStock.class);
                                intent.putExtra("centro",centro);
                                intent.putExtra("almacen",separated[0]);
                                intent.putExtra("hash",jsonObject.getString("hash"));
                                intent.putExtra("id",jsonObject.getString("id"));
                                intent.putExtra("controlados",jsonObject.getString("controlados"));
                                intent.putExtra("usuario",usu);
                                startActivity(intent);
                            }

                        }catch (JSONException e){e.printStackTrace();}
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams(){
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("centro", centro);
                        String[] cod_alm = almacen.split(" - ");
                        params.put("almacen", cod_alm[0]);
                        params.put("usuario", usu);
                        return params;
                    }
                };
                int socketTimeout = 30000;
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(policy);
                requestQueue.add(stringRequest);

                //-------------------------------------------------------------------

            }
        });
    }
    private void loadLatest(String url) {

        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jObj = new JSONObject(response);
                    List<String> list = new ArrayList<String>();
                    if(jObj.has("docs")){
                        JSONArray docs = jObj.getJSONArray("docs");
                        for(int i = 0; i<docs.length();i++){
                            JSONObject obj = docs.getJSONObject(i);
                            String barrido = obj.getString("barrido");
                            String user = obj.getString("usuario");
                            String inv = obj.getString("inventario");

                            Log.d("-> "+obj.toString(),"materiales ");

                            list.add("Barrido #"+barrido+" - Doc. Inv. #"+inv+" - Usuario:"+user);
                        }
                    }else{
                        //lytMaterias
                        list.add("No se generó ningún documento de inventario por barrido");
                    }

                    final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                            OpenCkStock.this, android.R.layout.simple_list_item_1, list
                    );
                    pedidoList.setAdapter(dataAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(OpenCkStock.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    private void loadSpinnerData(String url) {

        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject=new JSONObject(response);
                    if(jsonObject.getInt("success")==1){
                        JSONArray jsonArray=jsonObject.getJSONArray("centros");
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject1=jsonArray.getJSONObject(i);
                            String country=jsonObject1.getString("centro");
                            centros.add(country);
                        }
                    }
                    spCentros.setAdapter(new ArrayAdapter<String>(OpenCkStock.this, android.R.layout.simple_spinner_dropdown_item, centros));
                }catch (JSONException e){e.printStackTrace();}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    private void loadSpinnerDataAlm(String url) {
        almacenes.clear();
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject=new JSONObject(response);
                    if(jsonObject.getInt("success")==1){
                        JSONArray jsonArray=jsonObject.getJSONArray("almacenes");
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject1=jsonArray.getJSONObject(i);
                            String alamcen=jsonObject1.getString("almacen")+" - "+jsonObject1.getString("desc");
                            almacenes.add(alamcen);

                        }
                    }

                    spAlmacenes.setAdapter(new ArrayAdapter<String>(OpenCkStock.this, android.R.layout.simple_spinner_dropdown_item, almacenes));
                }catch (JSONException e){e.printStackTrace();}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    private void getCentros() {
        String tag_string_req = "req_get_centros";

        StringRequest stReq = new StringRequest(Method.POST, URL_GET_CENTROS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(OpenCkStock.this, "entra "+response, Toast.LENGTH_LONG).show();
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(error == false){
                        Toast.makeText(OpenCkStock.this, "entro", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(OpenCkStock.this, "no entro", Toast.LENGTH_LONG).show();
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
                //params.put("codigo", pedido);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stReq, tag_string_req);;
    }

    @Override public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                // User chose the "Settings" item, show the app settings UI...
                logoutUser();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(OpenCkStock.this, Login.class);
        startActivity(intent);
        finish();
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
