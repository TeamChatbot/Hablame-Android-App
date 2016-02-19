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
    private static final String USERNA_NAME = "user_name";
    private final Context context;

    public UsersData(Context context){
        this.context = context;
    }

    public String loadFromPreferences() {
        SharedPreferences sharedPref = context.getSharedPreferences(USERNA_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(USERKEY, "Alice");
    }

    public void storeToPreferences(CharSequence name){
        SharedPreferences sharedPref = context.getSharedPreferences(USERNA_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USERKEY, name.toString());
        editor.commit();
    }



}
