package chatbot.morpheus.de.hablame_android_app;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;


/**
 * Created by Orrimp on 15/02/16.
 * Please take a look at the sequence diagram in the doc folder.
 * Although it is not neccessary to recreate TTS anew after one usage it is more stable and secure.
 * It should only speak when its ready intializing. Only one message is permitted to be spoken.
 */
public class Texty extends UtteranceProgressListener implements TextToSpeech.OnInitListener {

    private static final String TAG = Texty.class.getSimpleName();
    private final TextyCallback callback;
    private final Context context;
    private final HablameAudioManager audioManager;
    private final Handler handler;
    private TextToSpeech textToSpeech;
    private boolean mTtsInitialized = false;
    private String message;


    /** Creates a new instance of TextToSpeech service which waits for it be ready.
     * @param context Context of the application (getApplicationContext)
     * @param callback Callback to inform about finshed speeaking
     * @param audioManager The audioManager wrapper to handle changes in Speaker and Microphone
     * @param handler
     */
    public Texty(Context context, TextyCallback callback, HablameAudioManager audioManager, final Handler handler) {
        this.context = context;
        this.callback = callback;
        this.audioManager = audioManager;
        this.handler = handler;
        createAndWaitTts();
    }

    /**
     * Creates a new instance of TextToSpeach.
     * OnInit is called when TTS is fully initialized
     */
    public void createAndWaitTts() {
        this.message = null;
        handler.post(new Runnable() {
            @Override
            public void run() {
                textToSpeech = new TextToSpeech(context, Texty.this);
                textToSpeech.setOnUtteranceProgressListener(Texty.this);
            }
        });

    }

    /** Informs the TTS about a new message to be spoken.
     * Triest to speak it immediatly or store it for later use after OnInit is called
     * @param message Message with content at best
     */
    public void speakWhenReady(final String message) {
        if(mTtsInitialized){
            speak(message);
        }else{
            //Wait for TTS bo be fully initialized, hew will talk then
            this.message = message;
        }
    }

    /**
     * Stopps the TTS engine, which does not destroy it.
     * Interrupts the spoken words.
     */
    public void stop() {
        if (textToSpeech != null) {
            this.textToSpeech.stop();
        }
    }

    /**
     * Full destroy the TTS engine and set the internal state to be unitialized
     */
    public void destroy() {
        if (this.textToSpeech != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textToSpeech.stop();
                    textToSpeech.shutdown();
                    textToSpeech = null;
                    mTtsInitialized = false;
                }
            });
        }
    }

    /** Ask the TTS engine if it is speaking. Could return answer. Use registerd listener instead
     * @return True in case is speaking, false otherwise.
     */
    public boolean isSpeaking() {
        if (textToSpeech != null) {
            return textToSpeech.isSpeaking();
        }
        return false;
    }

    /** Is called when TTS engine starts speaking.
     * @param utteranceId Id of spoken words
     */
    @Override
    public void onStart(final String utteranceId) {
        this.audioManager.setMicroMute(true);
    }

    /** Is called when TTS engine is done with speaking
     * Registered callback is fired.
     * @param utteranceId Id of spoken words
     */
    @Override
    public void onDone(final String utteranceId) {
        this.callback.doneWithTts();
    }

    /** Is bad implemented by Google, lets wait and see.
     * The only thing we can do is restart the TTS engine.
     * @param utteranceId
     */
    @Override
    public void onError(final String utteranceId) {
        Log.d(TAG, "ERROR IN TTS: " + utteranceId);
        onDone(utteranceId);
    }

    /** Is called when TTS engine is fully intiliazed and capable of speaking
     * When intialized, TTS will try to speak the message stored in this instance
     * Do not call this method but just wait for it to be called.
     * @param status
     */
    @Override
    public void onInit(final int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "INTIZIALIZED TTS");
            this.mTtsInitialized = true;
            this.textToSpeech.setLanguage(Locale.GERMAN);
            this.speak(message);
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(context, "Text Ausgabe funktioniert nicht", Toast.LENGTH_LONG).show();
        }
    }

    /** Tell the TTS to apeak it now but will not work when TTS is not itialized
     * @param message
     */
    private void speak(final String message) {
        if (message != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "hablame");
            }else{
                this.textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    /**
     * Callback to inform about TTS Engine done speaking
     */
    public interface TextyCallback {

        /**
         * Is called when TextToSpeech is done with talking
         */
        public void doneWithTts();
    }
}
