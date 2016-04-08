package chatbot.morpheus.de.hablame_android_app;

import android.speech.SpeechRecognizer;

public class SpeechyErrorDescription
{

  public static String getErrorText ( int errorCode )
  {
    String errorMessage = "";

    switch ( errorCode )
    {
      case SpeechRecognizer.ERROR_AUDIO:
        errorMessage = "Audio recording error";
        break;

      case SpeechRecognizer.ERROR_CLIENT:
        errorMessage = "Client side error";
        break;

      case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
        errorMessage = "Insufficient permissions";
        break;

      case SpeechRecognizer.ERROR_NETWORK:
        errorMessage = "Network error";
        break;

      case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
        errorMessage = "Network timeout";
        break;

      case SpeechRecognizer.ERROR_NO_MATCH:
        errorMessage = "No match";
        break;

      case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
        errorMessage = "RecognitionService busy";
        break;

      case SpeechRecognizer.ERROR_SERVER:
        errorMessage = "Error from server";
        break;

      case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
        errorMessage = "No speech input";
        break;

      default:
        errorMessage = "Didn't understand, please try again.";
        break;
    }
    return errorMessage;
  }
}