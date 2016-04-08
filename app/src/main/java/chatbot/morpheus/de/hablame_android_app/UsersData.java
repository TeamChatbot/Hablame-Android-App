package chatbot.morpheus.de.hablame_android_app;

import android.content.Context;
import android.content.SharedPreferences;

/** Represents the currently loged in user
 * Changing users is done by a long press on hablame logo.
 * User is stored in preferences
 * Multiple Activities can access the user name
 * Created by Orrimp on 14/02/16.
 */
public class UsersData {

    private static final String USERKEY = "username";
    private static final String USERNA_NAME = "user_name";
    private final Context context;

    public UsersData(Context context){
        this.context = context;
    }

    /** Loads the user name from shared preferences in private mode
     * @return user name or alice in case of error
     */
    public String loadFromPreferences() {
        SharedPreferences sharedPref = context.getSharedPreferences(USERNA_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(USERKEY, "Alice");
    }

    /** Stores the user in shared preferences in private mode
     * @param name user name to be stored
     */
    public void storeToPreferences(CharSequence name){
        SharedPreferences sharedPref = context.getSharedPreferences(USERNA_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USERKEY, name.toString());
        editor.commit();
    }
}
