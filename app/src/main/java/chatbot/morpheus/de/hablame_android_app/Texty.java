package chatbot.morpheus.de.hablame_android_app;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;


/**
 * Created by Orrimp on 15/02/16.
 */
public class Texty extends UtteranceProgressListener implements TextToSpeech.OnInitListener {

    private static final String TAG = Texty.class.getSimpleName();
    private final TextyCallback callback;
    private final Context context;
    private TextToSpeech textToSpeech;


    public Texty(Context context, TextyCallback callback) {
        this.context = context;
        this.callback = callback;
        create();
    }

    public void speak(String message) {
        if (message != null) {
            if (!this.textToSpeech.isSpeaking()) {
                //TODO: Add check if chatbotAnswer is may bigger than getMaxSpeechInputLength() of TextToSpeech!
                int status = -1;
                if (Build.VERSION.SDK_INT >= 21) {
                    status = this.textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString());
                } else if (Build.VERSION.SDK_INT < 21) {
                    status = this.textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                }
                Log.i(TAG, "textToSpeech " + status);
            }
        } else {
            Log.d(TAG, "Something is null: tts " + textToSpeech + " chatbot " + message);
        }
        textToSpeech.setOnUtteranceProgressListener(this);
    }


    public void create() {
        this.textToSpeech = new TextToSpeech(context, this);
    }

    public void stop() {
        if (textToSpeech != null) {
            this.textToSpeech.stop();
        }
    }

    public void destroy() {
        if (this.textToSpeech != null) {
            this.textToSpeech.shutdown();
            this.textToSpeech = null;
        }
    }

    public boolean isSpeaking() {
        if (textToSpeech != null) {
            return textToSpeech.isSpeaking();
        }
        return false;
    }

    @Override
    public void onStart(final String utteranceId) {

    }

    @Override
    public void onDone(final String utteranceId) {
        this.callback.doneWithTts();
    }

    @Override
    public void onError(final String utteranceId) {

    }

    @Override
    public void onInit(final int status) {
        textToSpeech.setLanguage(Locale.GERMANY);
    }

    public interface TextyCallback {
        /**
         * Is called when TextToSpeech is done with talking
         */
        public void doneWithTts();
    }
}
