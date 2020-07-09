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

import static py.com.chacomer.stock.Data.AppConfig.*;


public class GetOCData extends AppCompatActivity {

    SessionManager session;
    ProgressDialog pDialog;

    private SQLiteHandler db;
    String OC = "";
    String usu = "";
    private Button btnIr;
    private EditText NroInv;

    @Override
    protected void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_getocdata);

        Toolbar toolbar = findViewById(R.id.my_toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(R.drawable.logo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        db = new SQLiteHandler(GetOCData.this);
        session = new SessionManager(GetOCData.this);

        HashMap<String, String> user = session.getUserDetails();
        usu = user.get(SessionManager.KEY_USER);

        pDialog = new ProgressDialog(GetOCData.this);
        pDialog.setCancelable(false);

        btnIr = findViewById(R.id.botonCrear);
        NroInv = findViewById(R.id.inNroOC);
        btnIr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OC = NroInv.getText().toString();
                getOC(OC, usu);
            }
        });

    }

    private void getOC(final String OC, final String usuario) {
        String tag_string_req = "req_get_oc";

        StringRequest stReq = new StringRequest(Method.POST, URL_GET_OC, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                //Toast.makeText(GetDocument.this, "entra "+response, Toast.LENGTH_LONG).show();
                try{
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if(error == false){
                        String mens = jObj.getString("message");
                        Toast.makeText(GetOCData.this, ""+mens, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(GetOCData.this, CheckReceptionStock.class);
                        intent.putExtra("codigo",OC);
                        intent.putExtra("usuario",usuario);
                        // intent.putExtra("mensaje",mensaje);
                        startActivity(intent);
                        finish();
                    }else{
                        String mens = jObj.getString("message");
                        NroInv.setText("");
                        Toast.makeText(GetOCData.this, ""+mens, Toast.LENGTH_LONG).show();
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
                params.put("codigo", OC);
                params.put("usuario", usuario);
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
        Intent intent = new Intent(GetOCData.this, Login.class);
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
