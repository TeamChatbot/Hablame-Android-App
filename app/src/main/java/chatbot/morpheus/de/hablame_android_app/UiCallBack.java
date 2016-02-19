package chatbot.morpheus.de.hablame_android_app;

/**
 * Created by Orrimp on 15/02/16.
 */
public interface UiCallBack {

    public void onTextSpoken(String text);
    public void onTextReceived(String text);
    public void onSoundLevelChanged(float value);
}
