package py.com.chacomer.stock.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.HashMap;

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();

    SharedPreferences pref;

    Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "ReaderLogin";


    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    public static final String KEY_USER = "user";
    public static final String KEY_USER_FULL = "name";

    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn){
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.commit();
        Log.d(TAG, "User session modified!");
    }


    public void createLoginSession(String user, String name){
        editor.putString(KEY_USER, user);
        editor.putString(KEY_USER_FULL, name);
        editor.commit();
        Log.d(TAG, "User saved!");
    }
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_USER, pref.getString(KEY_USER, null));
        user.put(KEY_USER_FULL, pref.getString(KEY_USER_FULL, null));
        // return user
        return user;
    }
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}
