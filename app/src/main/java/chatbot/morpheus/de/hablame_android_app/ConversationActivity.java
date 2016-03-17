package chatbot.morpheus.de.hablame_android_app;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ConversationActivity extends Activity implements UiCallBack {
    private static final String TAG = ConversationActivity.class.getSimpleName();
    private static final long PROGRESS_ANIMATION_DURATION = 250; // 0.25 second
    private final List<ObjectAnimator> progressAnimators = new ArrayList<ObjectAnimator>();
    private final LinearInterpolator acc = new LinearInterpolator();

    private static final int RMS_SOUND_LEVEL_DB = 10;
    protected ImageButton logoGreen;
    private Queue<String> messages = new ArrayDeque<>(10);

    protected EditText userName;
    protected TextView speechOutput, botName, chatbotAnswer, userMessage;

    //Service
    private Intent recognitionService = null;
    private Intent recogIntent = null;
    private ConversationService service;
    private InputMethodManager inputMethod;

    //Other
    protected static ProgressBar speechInputLevel;
    private Intent openFEFA = null;
    private Context context;
    private boolean isUserNameEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        final Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Logobloqo2.ttf");
        final UsersData currentUser = new UsersData(this);
        inputMethod = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
        context = this;

        userMessage = (TextView) findViewById(R.id.tvUserMessage);

        userName = (EditText) findViewById(R.id.tvUserName);
        userName.setText(currentUser.loadFromPreferences());
        toTextView();
        userName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    if (userName.getText().length() <= 0) {
                        toEditText();
                    } else {
                        toTextView();
                        currentUser.storeToPreferences(userName.getText());
                    }

                    return true;
                }
                return false;
            }
        });

        logoGreen = (ImageButton) findViewById(R.id.logoGreen);
        logoGreen.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                //TODO Make ultra long klick
                toEditText();
                return true;
            }
        });


        speechOutput = (TextView) findViewById(R.id.tvUserMessage);
        botName = ((TextView) findViewById(R.id.tvBotName));
        chatbotAnswer = (TextView) findViewById(R.id.tvBotMessage);
        chatbotAnswer.setText(getString(R.string.startstring));
        botName.setTypeface(custom_font);
        userName.setTypeface(custom_font);

        speechInputLevel = (ProgressBar) findViewById(R.id.speechInputLevel);
        speechInputLevel.setMax(RMS_SOUND_LEVEL_DB);

        /**
         * Initialize the recognitionService Intent and start the Service.
         */
        recognitionService = new Intent(this, ConversationService.class);
        bindService(recognitionService, mConnection, Context.BIND_AUTO_CREATE);

    }

    private void toEditText() {
        userName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        userName.setCursorVisible(true);
        userName.setClickable(true);
        userName.setHint("Wie ist dein Name?");
        userName.setText("");
        userName.requestFocus();
        inputMethod.showSoftInput(userName, InputMethodManager.SHOW_FORCED);

    }

    private void toTextView() {
        userName.setInputType(InputType.TYPE_NULL);
        userName.setCursorVisible(false);
        userName.setClickable(false);
        userName.setHint("");
        inputMethod.hideSoftInputFromWindow(userName.getWindowToken(), 0);
    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder ibinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ConversationService.LocalBinder binder = (ConversationService.LocalBinder) ibinder;
            service = binder.getService();
            service.startService(ConversationActivity.this);
            Log.d(TAG, "Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
            service.onDestroy();
            service.stopSelf();
        }
    };

    public void onResume() {

        super.onResume();
        if (service != null) {
            this.service.onResume();
        }

        if (userName.isClickable() && userName.isInEditMode()) {
            inputMethod.showSoftInput(userName, InputMethodManager.SHOW_FORCED);
        }
    }

    protected void onPause() {
        super.onPause();
        speechInputLevel.setProgress(-2);
        if (service != null) {
            this.service.onPause();
        }
        if (inputMethod.isActive()) {
            inputMethod.hideSoftInputFromWindow(userName.getWindowToken(), 0);
        }
    }

    protected void onDestroy() {

        stopService(recognitionService);
        recognitionService = null;
        super.onDestroy();
    }

    @Override
    public void onTextSpoken(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userMessage.setText(text);
            }
        });
        //startet FEFA Activity
    }

    @Override
    public void onTextReceived(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatbotAnswer.setText(text);
            }
        });
    }

    @Override
    public void onSoundLevelChanged(final float value) {
        final int soundLevel = Math.max((int) value, 0);
        if (progressAnimators.size() <= soundLevel) {
            createAnimators(soundLevel);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (soundLevel > speechInputLevel.getMax()) {
                    speechInputLevel.setMax(soundLevel);
                }
                progressAnimators.get(soundLevel).start();
                speechInputLevel.setProgress(soundLevel);
            }
        });
    }

    private void createAnimators(final int soundLevel) {
        final int createAmount = soundLevel - progressAnimators.size() + 1;
        final int lastIndex = progressAnimators.size();

        for (int i = 0; i <= createAmount; i++) {
            progressAnimators.add(createAnimator(lastIndex + i));
        }

        if (progressAnimators.get(soundLevel) == null) {
            progressAnimators.add(soundLevel, createAnimator(soundLevel));
        }

    }

    private ObjectAnimator createAnimator(int value) {
        final ObjectAnimator animation = ObjectAnimator.ofFloat(speechInputLevel, "progress", value, 0);
        animation.setDuration(PROGRESS_ANIMATION_DURATION);
        animation.setInterpolator(acc);
        return animation;
    }


}
