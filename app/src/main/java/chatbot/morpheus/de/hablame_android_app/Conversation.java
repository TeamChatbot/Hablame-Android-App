package chatbot.morpheus.de.hablame_android_app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

//TODO FIX REPLACE WITH FRAGMENT
public class Conversation
        extends Activity
{
  //Variable declarations
  //Buttons
  protected static Button buttonEndConversation;
  protected static ImageButton logoGreen;

  //Text
  //TODO FIXME REMOVE STATIC
  protected static TextView headerTopLine;
  protected static TextView headerBottomLine;
  protected static TextView tvUserName;
  protected static TextView speechOutput;
  protected static TextView tvBotName;
  protected static TextView chatbotAnswer;

  //Intents
  private Intent recognitionService = null;
  private Intent intentBackJump = null;

  //Boolean
  protected static boolean onListeningForName = false;

  //Other
  protected static ProgressBar speechInputLevel;


  @Override
  protected void onCreate ( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.activity_conversation );
    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
    /**
     * Disabled Screen Rotation, set to only Portrait Mode
     */



    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy( policy );

    buttonEndConversation = (Button) findViewById( R.id.buttonEndConversation );
    logoGreen = (ImageButton) findViewById( R.id.logoGreen );

    headerTopLine = (TextView) findViewById( R.id.headerTopLine );
    headerBottomLine = (TextView) findViewById( R.id.headerBottomLine );

    Typeface custom_font = Typeface.createFromAsset( getAssets(), "fonts/Logobloqo2.ttf" );
    headerTopLine.setTypeface( custom_font );

    speechOutput = (TextView) findViewById( R.id.tvUserMessage );
    tvBotName = (TextView) findViewById( R.id.tvBotName );
    chatbotAnswer = (TextView) findViewById( R.id.tvBotMessage );

    speechInputLevel = (ProgressBar) findViewById( R.id.speechInputLevel );
    speechInputLevel.setVisibility( View.VISIBLE );

    /**
     *     Get the provided Data from the MainActivity
     - here: the Username given by the User -
     and assign it to the Username-TextView tvUserName
     */
    Intent intentForwardJump = getIntent();
    String userName = intentForwardJump.getExtras().getString( "userName" );

    tvUserName = (TextView) findViewById( R.id.tvUserName );
    tvUserName.setText( userName );

    /**
     * Initialize the recognitionService Intent and start the Service.
     */
    recognitionService = new Intent( this, RecognitionService.class );
    startService( recognitionService );
    
    /**
     *    Exit the Conversation via click-action on buttonEndConversation,
     stop the RecognitionService and go back to the MainActivity.

     Delete the Conversation-Activity from the Backstack,
     to serve correctly the double-click exit function.
     */
    intentBackJump = new Intent( this, MainActivity.class );
    intentBackJump.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP
                                     | Intent.FLAG_ACTIVITY_NEW_TASK
                                     | Intent.FLAG_ACTIVITY_CLEAR_TASK );

    buttonEndConversation.setOnClickListener( new OnClickListener()
    {
      @Override
      public void onClick ( View v )
      {
        stopService( recognitionService );
        speechInputLevel.setProgress( -2 );
        startActivity( intentBackJump );
      }
    } );

  }

  @Override
  public boolean onCreateOptionsMenu ( Menu menu )
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate( R.menu.menu_conversation, menu );
    return true;
  }

  @Override
  public boolean onOptionsItemSelected ( MenuItem item )
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if ( id == R.id.action_settings )
    {
      return true;
    }

    return super.onOptionsItemSelected( item );
  }

  public void onResume ()
  {

    super.onResume();
    // startService( recognitionService );
  }

  protected void onPause ()
  {

    super.onPause();
    //stopService( recognitionService );
  }

  protected void onDestroy ()
  {

    super.onDestroy();
  }
}
