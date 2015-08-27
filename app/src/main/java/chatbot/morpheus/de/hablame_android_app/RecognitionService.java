package chatbot.morpheus.de.hablame_android_app;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;


public class RecognitionService extends Service implements RecognitionListener{

  private String LOG_TAG = "SpeechRecognitionActivity";
  private AudioManager audioManager;
  private SpeechRecognizer speechRecognizer = null;
  private Intent speechRecognizerIntent;
  //TODO private WebService webService;
  private String speech;
  private String clearedSpeech;
  private String messageFromChatbot;
  private TextToSpeech textToSpeech;
  private CharSequence name = "alice";
  private CharSequence xname = "Alice";
  private Locale localeSpanish = new Locale ("es", "ES"); //neue Locale zum umstellen auf Spanisch
  protected static boolean callSuccessful = false;

  public IBinder onBind(Intent intent) {
    return null;
  }

  public void onCreate() {

    speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMANY); //f�r spanisch durch localeSpanish ersetzen

    //TODO webService = new WebService();
  }

  public int onStartCommand(Intent intent, int flags, int startId) {

    Log.i(LOG_TAG, "onStartCommand");
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
    Log.i(LOG_TAG, "Device muted");

    if(speechRecognizer == null) {

      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
      speechRecognizer.setRecognitionListener(this);
    }

    if(textToSpeech == null) {

      textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
        public void onInit(int status) {
          if(status!=TextToSpeech.ERROR)
            textToSpeech.setLanguage(Locale.GERMANY); //f�r spanisch durch localeSpanish ersetzen
        }
      });
    }

    speechRecognizer.startListening(speechRecognizerIntent);

    return START_STICKY;
  }

  public void onReadyForSpeech(Bundle params) {
    Log.i(LOG_TAG, "onReadyForSpeech");
  }

  public void onBeginningOfSpeech() {
    Log.i(LOG_TAG, "onBeginningOfSpeech");
    Conversation.speechInputLevel.setMax(10);
  }

  public void onRmsChanged(float rmsdB) {
    Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
    Conversation.speechInputLevel.setProgress((int) rmsdB);
  }

  public void onBufferReceived(byte[] buffer) {
    Log.i(LOG_TAG, "onBufferReceived: " + buffer);
  }

  public void onEndOfSpeech() {
    Log.i(LOG_TAG, "onEndOfSpeech");
  }

  public void onError(int error) {

    speechRecognizer.cancel();
    String errorMessage = ErrorDescription.getErrorText(error);

    Log.d(LOG_TAG, "FAILED " + errorMessage);
    Conversation.speechOutput.setText(errorMessage);

    if(error != 5) {
      speechRecognizer.startListening(speechRecognizerIntent);
    }
  }

  public void onResults(Bundle results) {

    callSuccessful = true;
    Log.i(LOG_TAG, "onResults");
    speechRecognizer.cancel();
    ArrayList<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    speech = text.get(0);

    if(Conversation.onListeningForName == true) {

      if(checkIfNameIsSaid() == true) {
        doExecution(clearedSpeech);
      }
      else {
        Conversation.speechOutput.setText("Name wurde nicht gesagt");
        speechRecognizer.startListening(speechRecognizerIntent);
      }
    }
    else {
      doExecution(speech);
    }
  }

  public boolean checkIfNameIsSaid() {

    if(speech.contains(name) | speech.contains(xname) == true) {
      Log.i(LOG_TAG, "Name wurde erkannt: " +name);
      clearedSpeech = speech.replace(name.toString(), "");
      clearedSpeech = speech.replace(xname.toString(), "");

      return true;
    }
    else {
      return false;
    }
  }

  public void doExecution(String input) {

    //TODO messageFromChatbot = webService.sendMessageToChatbot(input);

    if(callSuccessful == true) {

      Log.i(LOG_TAG, "ChatbotService successful called");

      Conversation.speechOutput.setText(input);
      Conversation.chatbotAnswer.setText(messageFromChatbot);

      audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
      Log.i(LOG_TAG, "Device unmuted");

      if(textToSpeech!=null) {
        if(messageFromChatbot!=null) {
          if (!textToSpeech.isSpeaking()) {
            CharSequence toSpeak = messageFromChatbot;
            textToSpeech.speak(toSpeak.toString(), TextToSpeech.QUEUE_FLUSH, null);
            Log.i(LOG_TAG, "textToSpeech");
          }
        }
        else {
          Conversation.chatbotAnswer.setText("Request to Chatbot WebService failed");
        }
      }

      while(textToSpeech.isSpeaking()) {

      }
      audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
      Log.i(LOG_TAG, "Device muted");
      speechRecognizer.startListening(speechRecognizerIntent);
    }

    else {
      Conversation.speechOutput.setText(input);
      Conversation.chatbotAnswer.setText("Chatbot returned NULL");
      speechRecognizer.startListening(speechRecognizerIntent);
    }
  }

  public void onPartialResults(Bundle partialResults) {
    Log.i(LOG_TAG, "onPartialResults");
  }

  public void onEvent(int eventType, Bundle params) {
    Log.i(LOG_TAG, "onEvent");
  }

  public void onDestroy(){

    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
    speechRecognizer.destroy();

    if(textToSpeech!=null){
      textToSpeech.stop();
      textToSpeech.shutdown();
    }
    super.onDestroy();
  }
}