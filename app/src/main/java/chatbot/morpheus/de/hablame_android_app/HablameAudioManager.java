package chatbot.morpheus.de.hablame_android_app;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

/**
 * Created by Orrimp on 20/02/16.
 */
public class HablameAudioManager implements AudioManager.OnAudioFocusChangeListener {
    private final Context context;
    private final AudioManager audioManager;
    private final int streamVolume;
    private boolean isSpeaking = false;
    private boolean isListening = false;

    public HablameAudioManager(final Context contex) {
        this.context = contex;
        this.audioManager = (AudioManager) contex.getSystemService(Context.AUDIO_SERVICE);
//        this.audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        final int maxValue = audioManager.getStreamMaxVolume(TextToSpeech.Engine.DEFAULT_STREAM);
        final int currentV = audioManager.getStreamVolume(TextToSpeech.Engine.DEFAULT_STREAM);
        streamVolume = currentV >=1 ? currentV : maxValue / 2; // getting system volume into var for later un-muting

        this.requestAudioFocus();
    }


    public void requestAudioFocus() {
        this.audioManager.requestAudioFocus(this, TextToSpeech.Engine.DEFAULT_STREAM, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void abandonAudioFocus() {
        this.audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(final int focusChange) {

    }

    public void isListening(final boolean microphoneMute) {
        this.audioManager.setMicrophoneMute(!microphoneMute);
    }

    public void isSpeaking(final boolean muteSpeakers){
        this.setSoundVolume(muteSpeakers ? streamVolume :  0 );
    }

    private void setSoundVolume(final int streamVolume) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVolume, 0); // again setting the system volume back to the original, un-mutting
    }

}
