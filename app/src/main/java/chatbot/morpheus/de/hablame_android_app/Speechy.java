package chatbot.morpheus.de.hablame_android_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Orrimp on 15/02/16.
 */
public class Speechy implements RecognitionListener {

    private final static String TAG = Speechy.class.getSimpleName();
    private final Context contex;
    private final Intent intent;
    private final SpeechyCallback callback;
    private final HablameAudioManager audioManager;
    private final Looper mainLopper;
    private SpeechRecognizer recog;

    public Speechy(Context context, SpeechyCallback callback, HablameAudioManager audioManager, final Looper mainLooper) {
        this.contex = context;
        this.callback = callback;
        this.audioManager = audioManager;
        this.mainLopper = mainLooper;

        this.intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        this.intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        this.intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMANY); //fuer spanisch durch localeSpanish ersetzen
        this.intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1); //I need only the best result not all
    }

    /**
     * SpeechRecognizer should be used only from the application's main thread.
     * @return
     */
    private SpeechRecognizer createAndStart() {
        //User will talk, don't disturb with sounds
        this.audioManager.isSpeaking(false);
        this.audioManager.isListening(true);

        Handler handler = new Handler(this.mainLopper);
        handler.post(new Runnable() {
            @Override
            public void run() {
                recog = SpeechRecognizer.createSpeechRecognizer(contex);
                recog.setRecognitionListener(Speechy.this);
                recog.startListening(intent);
            }
        });

        return this.recog;
    }

    public void restartRecog() {
        destroy();
        createAndStart();
    }

    public void stopRecog() {
        if (recog != null) {
            this.recog.cancel();
        }
    }

    public void destroy() {
        if (recog != null) {
            this.recog.cancel();
            this.recog.destroy();
        }
        audioManager.isSpeaking(true);
        audioManager.isListening(true);

    }

    @Override
    public void onReadyForSpeech(final Bundle params) {
        Log.d(TAG, "I am ready");
        //Disable ready sound of speech recognizer by setting volume to 0

    }

    @Override
    public void onBeginningOfSpeech() {
        //User is talking we should enable media sound again
    }


    @Override
    public void onRmsChanged(final float rmsdB) {
        this.callback.onSoundLevelChanged(rmsdB);
    }

    @Override
    public void onBufferReceived(final byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "Done with listening");
        this.callback.onSoundLevelChanged(0f);
    }

    @Override
    public void onError(final int error) {
        String errorMessage = SpeechyErrorDescription.getErrorText(error);
        Log.d(TAG, "FAILED " + errorMessage);
        //TODO What do to in case of errror?
        restartRecog();
    }

    @Override
    public void onResults(final Bundle results) {
        //In case TextToSpeech wants to say something enable media volume
        final ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        final float[] confi = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
        if (text.size() > 0 && confi.length > 0) {
            this.callback.onResult(text.get(0), confi[0]);
        }
    }

    @Override
    public void onPartialResults(final Bundle partialResults) {

    }

    @Override
    public void onEvent(final int eventType, final Bundle params) {
        //Reserved ...
        //Could be used to handle mute and unmute way better
    }


    public interface SpeechyCallback {
        /**
         * Returns the result of SpeechEngine with the best result and corresponding confidence level
         *
         * @param bestResult Firs and therefore best result in matching spoken words to text
         * @param confidence Confidence value from 0 to 1 for understanding the user
         */
        void onResult(final String bestResult, final float confidence);

        /**
         * The sound volume of microphone while the user is speaking
         *
         * @param value Value in dB could be from -2 to 10 or so
         */
        void onSoundLevelChanged(final float value);
    }
}
