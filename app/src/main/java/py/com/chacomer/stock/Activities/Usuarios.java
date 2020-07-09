package py.com.chacomer.stock.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import py.com.chacomer.stock.R;

public class Usuarios extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        listView = findViewById(R.id.listView);

        getJSON("http://192.168.12.50/stock/get_data.php?table=prt_users");

    }

    private void getJSON(final String urlWebService){
        class GetJSON extends AsyncTask<Void, Void, String>{
            @Override
            protected void onPreExecute(){
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s){
                super.onPostExecute(s);
                try{
                    loadIntoListView(s);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids){
                try{
                    URL url = new URL(urlWebService);
                    URLConnection con = url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while((json = bufferedReader.readLine()) != null){
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                }catch(Exception e){
                    return null;
                }
            }
        }

        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }

    private void loadIntoListView(String json) throws JSONException{

        JSONObject jsonObject = new JSONObject(json);
        JSONArray result = jsonObject.getJSONArray("prt_users");
        String success = jsonObject.getString("success");
        String[] data = new String[result.length()];

        Log.i("---------->"+result.length(),"data encontrada");
        for(int i = 0; i<result.length();i++){
            JSONObject obj = result.getJSONObject(i);
            data[i] = obj.getString("prt_user") + " - " + obj.getString("prt_email");
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(arrayAdapter);
    }
}
