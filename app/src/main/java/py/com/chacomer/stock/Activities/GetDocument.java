package py.com.chacomer.stock.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import py.com.chacomer.stock.Controller.AppController;
import py.com.chacomer.stock.Helper.SQLiteHandler;
import py.com.chacomer.stock.Helper.SessionManager;
import py.com.chacomer.stock.R;

import static py.com.chacomer.stock.Data.AppConfig.URL_CKECK_PEDIDO;


public class GetDocument extends AppCompatActivity {

    SessionManager session;
    ProgressDialog pDialog;

    private SQLiteHandler db;
    String pedido = "";

    private Button btnIr;
    private EditText NroInv;

    @Override
    protected void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_getdocument);

        Toolbar toolbar = findViewById(R.id.my_toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(R.drawable.logo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        db = new SQLiteHandler(GetDocument.this);
        session = new SessionManager(GetDocument.this);

        pDialog = new ProgressDialog(GetDocument.this);
        pDialog.setCancelable(false);

        btnIr = findViewById(R.id.botonCrear);
        NroInv = findViewById(R.id.inNroOC);
        btnIr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pedido = NroInv.getText().toString();

                getInventario(pedido);
            }
        });

    }

    private void getInventario(final String pedido) {
        String tag_string_req = "req_get_inventario";

        pDialog.setMessage("Cargando Pedido...");
        showDialog();

        StringRequest stReq = new StringRequest(Method.POST, URL_CKECK_PEDIDO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                //Toast.makeText(GetDocument.this, "entra "+response, Toast.LENGTH_LONG).show();
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(error == false){
                        String inventario = jObj.getString("codigo");
                        String cantidad = jObj.getString("c_materiales");
                        String mensaje = jObj.getString("mensaje");
                        Intent intent = new Intent(GetDocument.this, CheckStock.class);
                        intent.putExtra("codigo",inventario);
                        intent.putExtra("c_material",cantidad);
                        intent.putExtra("mensaje",mensaje);
                        startActivity(intent);
                        finish();
                    }else{
                        String mens = jObj.getString("mensaje");
                        NroInv.setText("");
                        Toast.makeText(GetDocument.this, ""+mens, Toast.LENGTH_LONG).show();
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
                params.put("codigo", pedido);
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
        Intent intent = new Intent(GetDocument.this, Login.class);
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
