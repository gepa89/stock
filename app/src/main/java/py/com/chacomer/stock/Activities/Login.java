package py.com.chacomer.stock.Activities;

import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import py.com.chacomer.stock.R;
import py.com.chacomer.stock.Controller.AppController;
import py.com.chacomer.stock.Data.AppConfig;
import py.com.chacomer.stock.Helper.SQLiteHandler;
import py.com.chacomer.stock.Helper.SessionManager;

public class Login extends AppCompatActivity{
    EditText in_name;
    Button btn_login;
    ProgressDialog pDialog;
    SessionManager session;
    SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        in_name = findViewById(R.id.login_name);
        btn_login = findViewById(R.id.btn_login);

        pDialog = new ProgressDialog(Login.this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(Login.this);

        session = new SessionManager(Login.this);

        if(session.isLoggedIn()){
            Intent intent = new Intent(Login.this, Main.class);
            startActivity(intent);
            finish();
        }

        btn_login.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                String name = in_name.getText().toString().trim();

                if(!name.isEmpty()){
                    checkLogin(name);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Favor ingresar usuario!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }
    private void checkLogin(final String name){
        String tag_string_req = "req_login";

        pDialog.setMessage("Accediendo...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_LOGIN_CSTOCK, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        session.setLogin(true);
                        JSONObject user = jObj.getJSONObject("user");
                        db.addUser(user.getString("prt_user"), user.getString("prt_user_name"));
                        session.createLoginSession(user.getString("prt_user"),user.getString("prt_user_name"));
                        Intent intent = new Intent(Login.this, Main.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this,
                                jObj.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Login.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Login.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("user", name);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
