package chatbot.morpheus.de.hablame_android_app;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.mashape.unirest.http.HttpResponse;

import de.fhws.hablame.service.api.HablameClient;


/**
 * Service to handle SpeechRecognizer, TextToSpeech and WebService requests.
 */
public class ConversationService extends Service implements Speechy.SpeechyCallback, Texty.TextyCallback {

    private static final String TAG = ConversationService.class.getSimpleName();
    private HablameAudioManager audioManager;
    private LocalBinder binder = new LocalBinder();

    private HablameClient client = null;
    private UiCallBack callback;
    private Speechy speechy;
    private Texty texty;
    private boolean paused = false;
    private Handler handler;
    private Intent openFEFA;

    /**
     * Binder pattern to allow the Activity full controll over this service.
     */
    public class LocalBinder extends Binder {
        public ConversationService getService() {
            return ConversationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    /**
     * Creates new Instanzes of SpeechRecognizer and TextToSpeech with initial data.
     * TextToSpeech is first to go and Recognizer follows. Always alternating between those two.
     */
    public void onCreate() {

        this.handler = new Handler();

        this.audioManager = new HablameAudioManager(getBaseContext());
        this.speechy = new Speechy(getApplicationContext(), this, this.audioManager, handler);
        this.texty = new Texty(getApplicationContext(), this, this.audioManager, handler);
        this.texty.speakWhenReady(getString(R.string.startstring));

        //TODO webService = new WebService();
        this.client = new HablameClient();

        //Activity to which starts when the users speaks a certain buzzword
        this.openFEFA = new Intent(getApplicationContext(), FEFAActivity.class);
        this.openFEFA.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * Contains the result of SpeechReognizer.
     * Following sequence is required:
     * 0) Destroy SpeechRecognizer
     * 1) Inform the UI about the recoqnized spoken text
     * 2) Get answer from the sever for this text
     * 3) CreateTTS and Wait for it to be ready (The earlier done the better)
     * 4) Let TTS speak the answer.
     * 5) Show the text on UI
     *
     * @param bestResult Firs and therefore best result in matching spoken words to text
     * @param confidence Confidence value from 0 to 1 for understanding the user
     */
    @Override
    public void onResult(final String bestResult, final float confidence) {
        this.speechy.destroy();

        if (startAnotherActivity(bestResult)) {
            this.texty.destroy();
        } else {
            this.texty.createAndWaitTts();
            this.callback.onTextSpoken(bestResult);
            final String chatbotAnswer = getServerResponse(bestResult);
            this.callback.onTextReceived(chatbotAnswer);
            this.texty.speakWhenReady(chatbotAnswer);
            Log.i(TAG, "Responses: " + bestResult + " Answer: " + chatbotAnswer);
        }

    }

    /**
     * Contains information about the sound level from the SpeechRecognizer
     *
     * @param value Value in dB could be from -2 to 10 or so
     */
    @Override
    public void onSoundLevelChanged(final float value) {
        if (this.callback != null) {
            this.callback.onSoundLevelChanged(value);
        }
    }

    /**
     * TextToSpeech is done we can now restart SpeechRecognizer.
     * 1) Destroy TTS,
     * 2) Create SpeechRecognizer
     * 3) Mute Speakers
     */
    @Override
    public void doneWithTts() {
        Log.d(TAG, "Done with Speaking");
        this.texty.destroy();
        this.speechy.restartRecog();
        this.audioManager.setMicroMute(false);
    }

    /**
     * Start service by registering callbacks
     *
     * @param callBack
     */
    public void startService(UiCallBack callBack) {
        this.callback = callBack;
        Log.d(TAG, "Bound to Activity");
    }


    /**
     * Get Server Response for the spoken text
     *
     * @param speechInput Spoken and identified text by SpeechRecognizer
     * @return String with Bot answer
     */
    private String getServerResponse(final String speechInput) {
        try {
            final Future<HttpResponse<String>> future = this.client.getReplyForMessageAsync(speechInput);
            final HttpResponse<String> response = future.get(); // blocking!
            return response.getBody();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Destroys SpeechRecognizer and TextToSpeech
     */
    public void onDestroy() {
        super.onDestroy();
        this.audioManager.abandonAudioFocus();
        this.speechy.destroy();
        this.texty.destroy();

    }

    /**
     * Pause everthing
     */
    public void onPause() {
        paused = true;
        this.audioManager.abandonAudioFocus();
        this.speechy.onPause();
        this.speechy.stop();
        this.texty.stop();
    }

    /**
     * Resume everthing only when paused once.
     * Activity calles onResume immediatly after onCreate
     */
    public void onResume() {
        if (paused) {
            this.audioManager.requestAudioFocus();
            this.speechy.onResume();
            this.speechy.restartRecog();
        }
        paused = false;
    }


    /** Starts another Activity when recognizing a certain buzzword
     * @param text Spoken text with the buzword
     * @return True when the activity is started, else false
     */
    private boolean startAnotherActivity(final String text) {
        boolean mached = false;
        if (text.toLowerCase().contains(FEFAActivity.BUZZWORD_HEAD.toLowerCase())) {
            mached = true;
            openFEFA.putExtra(FEFAActivity.EXTRAK_KEY, FEFAActivity.BUZZWORD_HEAD.toLowerCase());
        } else if (text.toLowerCase().contains(FEFAActivity.BUZZWORD_EYE.toLowerCase())) {
            mached = true;
            openFEFA.putExtra(FEFAActivity.EXTRAK_KEY, FEFAActivity.BUZZWORD_EYE.toLowerCase());
        }
        if (mached) {
            startActivity(openFEFA);
        }
        return mached;
    }
}