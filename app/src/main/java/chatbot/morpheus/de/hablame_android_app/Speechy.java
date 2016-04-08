package chatbot.morpheus.de.hablame_android_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Orrimp on 15/02/16.
 * Please take a look at the sequence diagram in the doc folder.
 * SpeechRecoginzer is only on when TextToSpeech is off.
 * Have to desroy and rebuild it compeletely anew to ensure stable usage.
 * OnError case the SpeechRecognizer is useless, recreate.
 */
public class Speechy implements RecognitionListener {

    private final static String TAG = Speechy.class.getSimpleName();
    private final Context contex;
    private final Intent intent;
    private final SpeechyCallback callback;
    private final HablameAudioManager audioManager;
    private final Handler handler;
    private SpeechRecognizer recog;

    //Need few Runnables because actions done on SpeechRecognizer have to be executed on MainThread (MainLooper)
    private Runnable destroy = new Runnable() {
        @Override
        public void run() {
            destroyRecog();
        }
    };

    private Runnable destroyAndCreate = new Runnable() {
        @Override
        public void run() {
            destroyRecog();
            createAndStart();
        }
    };
    private Runnable create = new Runnable() {
        @Override
        public void run() {
            createAndStart();
        }
    };
    private Runnable pause = new Runnable() {
        @Override
        public void run() {
            stopRecog();
        }
    };

    private boolean pauseRecog = false;


    /**
     * Creates the meta data for the SpeechRecognizer including the language model, langauge and results
     *
     * @param context      Context, should be ApplicationContext
     * @param callback     Callback to be informed about the result of the speechrecognition
     * @param audioManager Audiomanager to handle speakers
     * @param handler      Handler from the MainLoop (onCreate has one) becasue SpeechReocognizer need MainLoop
     */
    public Speechy(Context context, SpeechyCallback callback, HablameAudioManager audioManager, final Handler handler) {
        this.contex = context;
        this.callback = callback;
        this.audioManager = audioManager;
        this.handler = handler;

        this.intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        this.intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        this.intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMANY); //fuer spanisch durch localeSpanish ersetzen
        this.intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1); //I need only the best result not all
    }

    /**
     * SpeechRecognizer should be used only from the application's main thread.
     * Restarts the Engine by creating a new one, registering listener and start listening
     *
     * @return Instanze of created SpeechRecognizer, always a new one
     */
    private SpeechRecognizer createAndStart() {
        //User will talk, don't disturb with sounds
        this.audioManager.setMicroMute(false);

        recog = SpeechRecognizer.createSpeechRecognizer(contex);
        recog.setRecognitionListener(Speechy.this);
        recog.startListening(intent);

        return this.recog;
    }

    private void destroyRecog() {
        if (recog != null) {
            recog.cancel();
            recog.stopListening();
            recog.destroy();
        }
    }

    private void stopRecog() {
        if (recog != null) {
            recog.cancel();
            recog.stopListening();
        }
    }

    /**
     * Restarts the SpeechRecognizer by destroying the current one and creating a new one
     */
    public void restartRecog() {
        if (!pauseRecog) {
            handler.post(destroyAndCreate);
        } else {
            handler.post(destroy);
        }
    }

    /**
     * Stops the SpeechRecognizer without destroying it
     */
    public void stop() {
        handler.post(pause);
    }

    /**
     * Destroys the Speechrecognizer and releases all resources
     */
    public void destroy() {
        handler.post(destroy);
        audioManager.setMicroMute(true);
    }

    /**
     * Informs this instance about the state of the SpeechRecognizer. Is called after the first sound is trigged.
     *
     * @param params I do not know whats inside.
     */
    @Override
    public void onReadyForSpeech(final Bundle params) {
        Log.d(TAG, "I am ready");

    }

    @Override
    public void onBeginningOfSpeech() {
    }


    /**
     * Informs this instance about the change in the sound level.
     * It is about value of max value of 10. (I dont even know why)
     *
     * @param rmsdB
     */
    @Override
    public void onRmsChanged(final float rmsdB) {
        this.callback.onSoundLevelChanged(rmsdB);
    }

    @Override
    public void onBufferReceived(final byte[] buffer) {

    }

    /**
     * I am done listening but not done processing
     * Reset the sound level to 0.
     */
    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "Done with listening");
        this.callback.onSoundLevelChanged(0f);
    }

    @Override
    public void onError(final int error) {
        String errorMessage = SpeechyErrorDescription.getErrorText(error);
        Log.d(TAG, "FAILED " + errorMessage + ", restarting SpeechRecoqnition");
        handler.post(destroyAndCreate);
    }

    /**
     * Contains all the data of processed speech recognition including the spoken String and confidence value of its accuracity
     * The string with highges accuracity is at index 0.
     *
     * @param results Contains the data in RESULTS_RECOGNITION and CONFIDENCE_SCORES.
     */
    @Override
    public void onResults(final Bundle results) {
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

    public void onResume() {
        this.pauseRecog = false;
    }

    public void onPause() {
        this.pauseRecog = true;
    }


    /**
     * Event to inform the service about the important data from SpeechReognizer including result and sound level.
     */
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
