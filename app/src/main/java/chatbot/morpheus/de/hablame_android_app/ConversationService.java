package chatbot.morpheus.de.hablame_android_app;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.mashape.unirest.http.HttpResponse;

import de.fhws.hablame.service.api.HablameClient;


public class ConversationService extends Service implements Speechy.SpeechyCallback, Texty.TextyCallback {

    private static final String TAG = ConversationService.class.getSimpleName();
    private HablameAudioManager audioManager;
    private LocalBinder binder = new LocalBinder();

    private HablameClient client = null;
    private UiCallBack callback;
    private Speechy speechy;
    private Texty texty;
    private boolean paused = false;

    public class LocalBinder extends Binder {
        public ConversationService getService() {
            return ConversationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public void onCreate() {

        this.audioManager = new HablameAudioManager(getBaseContext());
        this.speechy = new Speechy(getApplicationContext(), this, this.audioManager, getMainLooper());
        this.texty = new Texty(getApplicationContext(), this, this.audioManager);

        //TODO webService = new WebService();
        this.client = new HablameClient();

    }

    @Override
    public void onResult(final String bestResult, final float confidence) {

        this.callback.onTextSpoken(bestResult);
        final String chatbotAnswer = getServerResponse(bestResult);
        this.callback.onTextReceived(chatbotAnswer);

        this.speechy.stopRecog();
        this.texty.speak(chatbotAnswer);

        Log.i(TAG, "Responses: " + bestResult + " Answer: " + chatbotAnswer);
    }

    @Override
    public void onSoundLevelChanged(final float value) {
        if (this.callback != null) {
            this.callback.onSoundLevelChanged(value);
        }
    }

    @Override
    public void doneWithTts() {
        Log.d(TAG, "Done with Speaking");
        speechy.restartRecog();
        this.audioManager.isListening(true);
        this.audioManager.isSpeaking(false);
    }

    public void startService(UiCallBack callBack) {
        this.callback = callBack;
        Log.d(TAG, "Bound to Activity");
    }


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

    public void onDestroy() {
        abondonAudioFocus();
        super.onDestroy();
        speechy.destroy();
        texty.destroy();

    }

    public void onPause() {
        paused = true;
        abondonAudioFocus();
        speechy.stopRecog();
        texty.stop();
    }

    public void onResume() {
        if (paused) {
            requestAudioFocus();
            speechy.restartRecog();
        }
        paused = false;
    }

    private void abondonAudioFocus() {
        this.audioManager.abandonAudioFocus();
    }

    private void requestAudioFocus() {
        this.audioManager.requestAudioFocus();
    }


}