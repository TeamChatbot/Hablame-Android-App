package chatbot.morpheus.de.hablame_android_app;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.mashape.unirest.http.HttpResponse;

import de.fhws.hablame.service.api.HablameClient;


public class ConversationService extends Service implements Speechy.SpeechyCallback, AudioManager.OnAudioFocusChangeListener, Texty.TextyCallback {

    private static final String TAG = ConversationService.class.getSimpleName();
    private AudioManager audioManager;
    private LocalBinder binder = new LocalBinder();

    private HablameClient client = null;
    private UiCallBack callback;
    private Speechy speechy;
    private Texty texty;

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

        this.speechy = new Speechy(getApplicationContext(), this);
        this.texty = new Texty(getApplicationContext(), this);

        speechy.createAndStart();

        //TODO webService = new WebService();
        this.client = new HablameClient();
        this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }


    @Override
    public void onResult(final String bestResult, final float confidence) {

        this.callback.onTextSpoken(bestResult);
        final String chatbotAnswer = getServerResponse(bestResult);
        this.callback.onTextReceived(chatbotAnswer);

        this.speechy.stopRecog();
        this.texty.speak(chatbotAnswer);
        this.speechy.restartRecog();

        this.requestAudioFocus();

        Log.i(TAG, "Responses: " + bestResult + " Answer: " + chatbotAnswer);
    }

    @Override
    public void onSoundLevelChanged(final float value) {
        if(this.callback!=null){
            this.callback.onSoundLevelChanged(value);
        }
    }

    @Override
    public void doneWithTts() {
        speechy.restartRecog();
    }

    public void startService(UiCallBack callBack) {
        this.callback = callBack;
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
    }

    public void onPause() {
        abondonAudioFocus();
    }

    public void onResume() {
        requestAudioFocus();
        //TODO sppech recognizer pause
    }

    private void abondonAudioFocus() {
        this.audioManager.abandonAudioFocus(this);
    }

    private void requestAudioFocus() {
        this.audioManager.requestAudioFocus(this, TextToSpeech.Engine.DEFAULT_STREAM, AudioManager.AUDIOFOCUS_GAIN);
        this.audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }

    @Override
    public void onAudioFocusChange(final int focusChange) {

    }
}