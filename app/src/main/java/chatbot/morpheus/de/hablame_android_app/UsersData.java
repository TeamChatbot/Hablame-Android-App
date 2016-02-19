package chatbot.morpheus.de.hablame_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by Orrimp on 14/02/16.
 */
public class UsersData {

    private static final String USERKEY = "username";
    private final Activity activity;
    private boolean isKnownUser = false;

    public UsersData(Activity activity){
        this.activity = activity;
    }

    public String loadFromPreferences() {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(USERKEY, "Alice");
    }

    public void storeUser(CharSequence name){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USERKEY, name.toString());
        editor.commit();
    }

    public boolean isKnownUser(){
        return isKnownUser;
    }


}
