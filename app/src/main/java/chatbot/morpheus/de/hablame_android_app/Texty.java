package chatbot.morpheus.de.hablame_android_app;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * Created by Orrimp on 15/02/16.
 */
public class Texty extends UtteranceProgressListener implements TextToSpeech.OnInitListener {

    private static final String TAG = Texty.class.getSimpleName();
    private final TextyCallback callback;
    private final Context context;
    private final HablameAudioManager audioManager;
    private TextToSpeech textToSpeech;
    private boolean mTtsInitialized = false;
    private String message;


    public Texty(Context context, TextyCallback callback, HablameAudioManager audioManager) {
        this.context = context;
        this.callback = callback;
        this.audioManager = audioManager;
        this.textToSpeech = new TextToSpeech(context, this);
        this.textToSpeech.setOnUtteranceProgressListener(this);

        new Timer("waitForTTS", true).schedule(new TimerTask() {
            @Override
            public void run() {
                onDone(null);
            }
        }, 3000);
    }

    public void speak(String message) {

        if(mTtsInitialized){
            this.textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "hablame");
        }
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
        this.audioManager.isSpeaking(true);
        this.audioManager.isListening(false);
    }

    @Override
    public void onDone(final String utteranceId) {
        this.callback.doneWithTts();

    }

    @Override
    public void onError(final String utteranceId) {
        Log.d(TAG, "ERROR IN TTS: " + utteranceId);
        onDone(utteranceId);
    }

    @Override
    public void onInit(final int status) {
        if (status == TextToSpeech.SUCCESS) {
            this.mTtsInitialized = true;
            Log.d(TAG, "INTIZIALIZED TTS");
            this.textToSpeech.setLanguage(Locale.GERMAN);
            this.textToSpeech.speak(context.getString(R.string.startstring), TextToSpeech.QUEUE_FLUSH, null, "startstring");
        }else if (status == TextToSpeech.ERROR) {
            Toast.makeText(context, "Text Ausgabe funktioniert nicht", Toast.LENGTH_LONG).show();
        }
    }

    public interface TextyCallback {
        /**
         * Is called when TextToSpeech is done with talking
         */
        public void doneWithTts();
    }
}
