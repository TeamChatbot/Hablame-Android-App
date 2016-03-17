package chatbot.morpheus.de.hablame_android_app;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

/** Has to handle everthing concerinng AudioManager like muting and unmuting the speakers and microphone
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


    /**
     * For speakers output we need audiofocus for DEFAULT_STREAM == MUSIC_STREAM
     */
    public void requestAudioFocus() {
        this.audioManager.requestAudioFocus(this, TextToSpeech.Engine.DEFAULT_STREAM, AudioManager.AUDIOFOCUS_GAIN);
    }

    /**
     * Abandon Audiofocus in case of app minimized
     */
    public void abandonAudioFocus() {
        this.audioManager.abandonAudioFocus(this);
    }

    /**
     * Could be used to identify when TTS and Speechrecognizer is using AudioManager because they steal audio focus
     * @param focusChange
     */
    @Override
    public void onAudioFocusChange(final int focusChange) {

    }

    /** I am listening to the user right now, turn on or off the micro
     * @param microphoneMute
     */
    public void setMicroMute(final boolean microphoneMute) {
        this.audioManager.setMicrophoneMute(microphoneMute);
    }

}
