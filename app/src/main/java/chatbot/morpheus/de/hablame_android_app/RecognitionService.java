package chatbot.morpheus.de.hablame_android_app;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.mashape.unirest.http.HttpResponse;

import de.fhws.hablame.service.api.HablameClient;


public class RecognitionService extends Service implements RecognitionListener {

    private String LOG_TAG = "SpeechRecognitionActivity";
    private AudioManager audioManager;
    private SpeechRecognizer speechRecognizer = null;
    private Intent speechRecognizerIntent;
    private String speech;
    private TextToSpeech textToSpeech;
    private CharSequence name = "alice";
    private CharSequence xname = "Alice";
    private boolean listening = false;
    private LocalBinder binder = new LocalBinder();

    private HablameClient client = null;

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }
    //Bound service http://developer.android.com/guide/components/bound-services.html

    public class LocalBinder extends Binder {
        public RecognitionService getService() {
            return RecognitionService.this;
        }
    }

    public void onCreate() {

        this.speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        this.speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        this.speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.GERMANY); //f�r spanisch durch localeSpanish ersetzen

        //TODO webService = new WebService();
        this.client = new HablameClient();

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(LOG_TAG, "onStartCommand");
        this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        this.audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        Log.i(LOG_TAG, "Device muted");

        if (this.speechRecognizer == null) {

            this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            this.speechRecognizer.setRecognitionListener(this);
        }

        if (this.textToSpeech == null) {

            this.textToSpeech = new TextToSpeech(this.getApplicationContext(), new TextToSpeech.OnInitListener() {
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR)
                        RecognitionService.this.textToSpeech.setLanguage(Locale.GERMANY);
                }
            });
        }

        this.restartListening();


        return Service.START_NOT_STICKY;
    }

    public void restartListening() {
        if (this.listening)
            this.speechRecognizer.cancel();

        this.speechRecognizer.setRecognitionListener(this);

        this.speechRecognizer.startListening(this.speechRecognizerIntent);
        this.listening = true;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        Conversation.speechInputLevel.setMax(10);
    }

    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        Conversation.speechInputLevel.setProgress((int) rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    public void onError(int error) {
//    if ( error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_NETWORK )
//    {
//      this.speechRecognizer.cancel ();
//    }

        String errorMessage = ErrorDescription.getErrorText(error);

        Log.d(LOG_TAG, "FAILED " + errorMessage);
        Conversation.speechOutput.setText(errorMessage);

        this.restartListening();
    }

    public void onResults(Bundle results) {

        //this.callSuccessful = true;
        Log.i(LOG_TAG, "onResults");
        //this.speechRecognizer.cancel ();
        ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        this.speech = text.get(0);

//    if ( Conversation.onListeningForName == true )
//    {
//
//      if ( this.checkIfNameIsSaid () == true )
//      {
//        this.doExecution ( this.clearedSpeech );
//      }
//      else
//      {
//        Conversation.speechOutput.setText ( "Name wurde nicht gesagt" );
//        this.speechRecognizer.reStartListening ( this.speechRecognizerIntent );
//      }
//    }
//    else
        {
            this.doExecution(this.speech);
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    public boolean checkIfNameIsSaid() {
        if (speech.contains(name) || speech.contains(xname) == true) {
            Log.i(LOG_TAG, "Name wurde erkannt: " + name);

            return true;
        } else {
            return false;
        }
    }

    public void doExecution(String speechInput) {
        if (speechInput == null)
            return;

        String chatbotAnswer = null;

        try {
            Future<HttpResponse<String>> future = this.client.getReplyForMessageAsync(speechInput);
            HttpResponse<String> response;
            response = future.get(); // blocking!
            chatbotAnswer = response.getBody();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        Log.i(LOG_TAG, "ChatbotService successful called");

        //TODO FIXME Use Listeners which register with the RecognitionService from the Activity
        Conversation.speechOutput.setText(speechInput);
        Conversation.chatbotAnswer.setText(chatbotAnswer);

        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        Log.i(LOG_TAG, "Device unmuted");

        if (this.textToSpeech != null) {
            if (chatbotAnswer != null) {
                if (!this.textToSpeech.isSpeaking()) {
                    //TODO: Add check if chatbotAnswer is may bigger than getMaxSpeechInputLength() of TextToSpeech!

                    // used for android min sdk >= 21
                    //this.textToSpeech.speak ( chatbotAnswer, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID ().toString () );
                    // used for android min sdk < 21
                    this.textToSpeech.speak(chatbotAnswer, TextToSpeech.QUEUE_FLUSH, null);
                    Log.i(LOG_TAG, "textToSpeech");
                }
            } else {
                Conversation.chatbotAnswer.setText("Request to Chatbot WebService failed");
            }
        }

        // TODO: Is there a better way to wait until the tts is finished speaking?
        while (this.textToSpeech.isSpeaking()) {
            // intentionally empty!
        }
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        Log.i(LOG_TAG, "Device muted");
//    speechRecognizer.startListening ( this.speechRecognizerIntent );
        this.restartListening();
    }

    public void onDestroy() {

        this.audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        this.speechRecognizer.cancel();
        this.speechRecognizer.destroy();

        if (this.textToSpeech != null) {
            this.textToSpeech.stop();
            this.textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}