package py.com.chacomer.stock.Activities;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.ImageButton;
import android.widget.TextView;

import py.com.chacomer.stock.Helper.SQLiteHandler;
import py.com.chacomer.stock.Helper.SessionManager;
import py.com.chacomer.stock.R;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeReader;

import java.util.HashMap;
//#F58989 color comagro
public class Main extends AppCompatActivity{
    private static BarcodeReader barcodeReader;
    private static BarcodeReader materialReader;
    private AidcManager manager;

    private SQLiteHandler db;
    private SessionManager session;
    private ImageButton btnAutomaticBarcode;
    private ImageButton btnVerifPedido;
    private ImageButton btnVerifPedido2;
    private ImageButton btnCreateDoc;
    private ImageButton buttonVerUbi;
    private ImageButton buttonAssignEan;

    TextView tvHeader;
    Button btn_UserAct;

    @Override public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvHeader = findViewById(R.id.btnBarcode);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(R.drawable.logo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        db = new SQLiteHandler(Main.this);
        session = new SessionManager(Main.this);

        HashMap<String, String> user = session.getUserDetails();
        String usuario = user.get(SessionManager.KEY_USER_FULL);
        tvHeader.setText("Bienvenido "+usuario+"!");

        if(!session.isLoggedIn()){
            logoutUser();
        }

        AidcManager.create(this, new CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                materialReader = manager.createBarcodeReader();
            }
        });

        ActivitySetting();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    static BarcodeReader getBarcodeReader(){ return barcodeReader;}
    public static BarcodeReader getMaterialReader(){ return materialReader;}
    public void ActivitySetting(){
        btnAutomaticBarcode = findViewById(R.id.buttonAutomaticBarcode);
        btnAutomaticBarcode.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                Intent barcodeIntent;
                barcodeIntent = new Intent(Main.this, GetDocument.class);
                startActivity(barcodeIntent);
            }
        });

        btnCreateDoc = findViewById(R.id.ctrlLineal);
        btnCreateDoc.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                //Control de stock lineal
                Intent beIntent;
                beIntent = new Intent(Main.this, OpenCkStock.class);
                startActivity(beIntent);
            }
        });

        btnVerifPedido = findViewById(R.id.buttonVerPed);
        btnVerifPedido.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                //Control de OC CD Luque
                Intent beIntent;
                beIntent = new Intent(Main.this, GetOCData.class);
                startActivity(beIntent);
            }
        });
        btnVerifPedido2 = findViewById(R.id.buttonVerPed2);
        btnVerifPedido2.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                Intent beIntent;
                beIntent = new Intent(Main.this, CheckPedido.class);
                startActivity(beIntent);
            }
        });
        buttonVerUbi = findViewById(R.id.buttonVerUbi);
        buttonVerUbi.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                //Ubicación de materialesbuttonVerUbi
                Intent beIntent;
                beIntent = new Intent(Main.this, CheckLocation.class);
                startActivity(beIntent);
            }
        });
        buttonAssignEan = findViewById(R.id.buttonEanAssign);
        buttonAssignEan.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                //Asignación de EAN
                Intent beIntent;
                beIntent = new Intent(Main.this, AssignEan.class);
                startActivity(beIntent);
            }
        });
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
        Intent intent = new Intent(Main.this, Login.class);
        startActivity(intent);
        finish();
    }
    @Override protected void onDestroy(){
        super.onDestroy();
        if(materialReader != null){
            materialReader.close();
            materialReader = null;
        }
        if(barcodeReader != null){
            barcodeReader.close();
            barcodeReader = null;
        }
        if(manager != null){
            manager.close();
        }
    }
}