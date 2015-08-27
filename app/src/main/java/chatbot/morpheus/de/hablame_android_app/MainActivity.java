package chatbot.morpheus.de.hablame_android_app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;


public class MainActivity
        extends Activity
{
  //Variable declarations
  //Buttons
  protected static Button buttonStartConversation;
  protected static ImageButton logoRed;

  //Text
  protected static TextView headerTopLine;
  protected static TextView headerBottomLine;
  protected static EditText userName;

  //Strings
  protected static String userNameString;

  //Intents
  private Intent intentForwardJump = null;

  //Boolean
  private boolean doubleBackToExitPressedOnce = false;

  public void onCreate ( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.activity_main );
    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy( policy );

    buttonStartConversation = (Button) findViewById( R.id.buttonStartConversation );
    logoRed = (ImageButton) findViewById( R.id.logoRed );

    headerTopLine = (TextView) findViewById( R.id.headerTopLine );
    headerBottomLine = (TextView) findViewById( R.id.headerBottomLine );

    userName = (EditText) findViewById( R.id.userName );

    intentForwardJump = new Intent( this, Conversation.class );

    /*
    setting a custom Font for the header Topline ("Háblame");
    Font: Logobloqo2.ttf in main/Assets/Fonts;
    URL: http://www.dafont.com/de/logobloqo2.font
    License: "Free for commercial and personal use."
    Up to Date: 27.08.2015
    */
    Typeface custom_font = Typeface.createFromAsset( getAssets(), "fonts/Logobloqo2.ttf" );
    headerTopLine.setTypeface( custom_font );

    buttonStartConversation.setOnClickListener( new OnClickListener()
    {
      @Override
      public void onClick ( View v )
      {
        /*
        Jump to Conversation Activity if the username is not null,
        else give back an error message on the screen.
        Provide the Username to the Conversation Activity.
        */
        if ( userName.getText().toString().equals( "" ) )
        {
          Toast toast = Toast.makeText( getApplicationContext(), "Bitte gib einen Namen an!", Toast.LENGTH_SHORT );
          toast.show();
        }
        else
        {
          userNameString = userName.getText().toString();
          intentForwardJump.putExtra( "userName", userNameString );
          startActivity( intentForwardJump );
        }
      }
    } );
  }

  public void onResume ()
  {

    super.onResume();
    this.doubleBackToExitPressedOnce = false;
  }

  /*
  Implemented functionality to exit the application via
  double-click on the back button from Android.
  Give back a Message to the User if the Button was pressed only once.
   */
  @Override
  public void onBackPressed ()
  {
    if ( doubleBackToExitPressedOnce )
    {
      super.onBackPressed();
      return;
    }
    this.doubleBackToExitPressedOnce = true;
    Toast.makeText( this, "Drücke erneut um die App zu Verlassen", Toast.LENGTH_SHORT ).show();
  }

  protected void onPause ()
  {

    super.onPause();

  }

  protected void onDestroy ()
  {

    super.onDestroy();
  }

} //endActivity


//--------------------------------------------------------------------------------------------------------------------//
//----------------------------------Old Version of the MainActivity---------------------------------------------------//
/*
package de.morpheus.chatbot.chatbotapp;

        import android.app.Activity;
        import android.content.Intent;
        import android.content.pm.ActivityInfo;
        import android.os.Bundle;
        import android.os.StrictMode;
        import android.view.View;
        import android.widget.CompoundButton;
        import android.widget.CompoundButton.OnCheckedChangeListener;
        import android.widget.ProgressBar;
        import android.widget.Switch;
        import android.widget.TextView;
        import android.widget.ToggleButton;


public class MainActivity extends Activity {

  private Intent recognitionService = null;
  protected static ToggleButton turnRecognitionOnOff;
  protected static ProgressBar speechInputLevel;
  protected static TextView speechOutput;
  protected static TextView chatbotAnswer;
  protected static boolean onListeningForName = false;
  private boolean isChecked;


  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.old_activity_main);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);

    turnRecognitionOnOff = (ToggleButton) findViewById(R.id.turnRecognitionOnOff);
    speechOutput = (TextView) findViewById(R.id.speechOutput);
    chatbotAnswer = (TextView) findViewById(R.id.chatbotAnswer);
    speechInputLevel = (ProgressBar) findViewById(R.id.speechInputLevel);
    speechInputLevel.setVisibility(View.VISIBLE);

    recognitionService = new Intent(this, RecognitionService.class);

    turnRecognitionOnOff.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        checkToggleButton(isChecked);
      }
    });
  }

  private void checkToggleButton(boolean isChecked) {
    if (isChecked) {
      startService(recognitionService);
    }
    else {
      stopService(recognitionService);
      speechInputLevel.setProgress(-2);
    }
    isChecked = isChecked;
  }

  public void onListeningForName(View view) {
    onListeningForName = ((Switch) view).isChecked();
  }

  public void onResume() {
    super.onResume();
    checkToggleButton(isChecked)
  }

  protected void onPause() {
    super.onPause();
  }

  protected void onDestroy(){

    super.onDestroy();
  }
}

*/
