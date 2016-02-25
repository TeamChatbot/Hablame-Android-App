package chatbot.morpheus.de.hablame_android_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FEFATest extends Activity {
    private ListView eigenschaften;
    ArrayList eigenschaftenListe;
    Intent intent;
    private Button confirmButton;
    private Button exitButton;
    public String s;
    private String baseUrl = "http://194.95.221.229:8080/fefatest";
    public int i;
    public String[][] meta = new String[50][3];
    int zahl;
    Bitmap bitmap;
    Activity context;
    private int pos;
    int right = 0;
    int wrong = 0;
    int durchlauf = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this.context;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fefatest);
        intent = this.getIntent();
        holeMeta();
        eigenschaftenLaden();

        confirmButton = (Button) findViewById(R.id.confirmButton);
        eigenschaften = (ListView) findViewById(R.id.eigenschaften);
        exitButton = (Button) findViewById(R.id.exitButton);

        eigenschaften.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                pos = position;
                //Toast.makeText(getBaseContext(), parent.getItemIdAtPosition(position) + "is seleted", Toast.LENGTH_LONG).show();

            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = getIntent();
                String modus = intent.getStringExtra("modus");

                //s = eigenschaften.getSelectedItem().toString();
                s = eigenschaften.getItemAtPosition(pos).toString();
                spieleLogik(s, zahl);

                if (modus.toLowerCase().contains("starte köpfetest")) {
                    switch (durchlauf){
                        case 50:
                            Toast.makeText(getBaseContext(), "Das hast du gut gemacht.", Toast.LENGTH_LONG).show();
                            onStop();
                            onDestroy();
                            break;
                        default:
                            ladeBilder();
                    }
                }
                else if (modus.toLowerCase().contains("starte augentest")) {
                    switch (durchlauf){
                        case 40:
                            Toast.makeText(getBaseContext(), "Das hast du gut gemacht.", Toast.LENGTH_LONG).show();
                            onStop();
                            onDestroy();
                            break;
                        default:
                            ladeBilder();
                    }
                }
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setTitle("Schließen");
                builder.setMessage("Spiel Abbrechen?");

                builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        onStop();
                        dialog.dismiss();
                        onDestroy();
                    }
                });

                builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        ladeBilder();
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        /*client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "FEFATest Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://chatbot.morpheus.de.hablame_android_app/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);*/
    }


    //füllt ListView mit Inhalt
    public void eigenschaftenLaden() {
        eigenschaften = (ListView) findViewById(R.id.eigenschaften);
        eigenschaftenListe = new ArrayList<String>();
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, eigenschaftenListe);

        eigenschaftenListe.add("Freude");
        eigenschaftenListe.add("Trauer");
        eigenschaftenListe.add("Furcht");
        eigenschaftenListe.add("Zorn");
        eigenschaftenListe.add("Überraschung");
        eigenschaftenListe.add("Ekel");
        eigenschaftenListe.add("Neutral");

        eigenschaften.setAdapter(arrayAdapter);
    }


    //läd Meta file in Abhängigkeit vom Modus in ein Array
    public void holeMeta() {
        //läd Meta vom Server
        Intent intent = getIntent();
        String modus = intent.getStringExtra("modus");
        String urlstring = "";

        if (modus.toLowerCase().contains("starte köpfetest")) {

            //erstetzen mit url
            urlstring = baseUrl+"/meta_koepfe.txt";

        } else if (modus.toLowerCase().contains("starte augentest")) {

            //erstetzen mit url
            urlstring = baseUrl + "/meta_augen.txt";
        }

        try {
            URL url = new URL(urlstring);

            BufferedReader read = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            int i = 0;

            while ((line = read.readLine()) != null) {
                meta[i++] = line.split(",");
            }

            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //ermittelt random Zahl für zufällig Bildauswahl
    public int ermittleZahl(int n) {

        Random randomZahl = new Random(System.currentTimeMillis());
        zahl = randomZahl.nextInt(n);
        return zahl;
    }


    //ermittelt Bild URL und übergibt den URL LoadImage()
    public void ladeBilder() {
        String bildname = null;
        String bildurl = null;
        String modus = intent.getStringExtra("modus");

        if (modus.toLowerCase()
                .contains("starte köpfetest")) {
            i = ermittleZahl(meta.length);
            bildname = meta[i][0];
            bildurl = baseUrl+"/Koepfe/"+bildname;

        } else if (modus.toLowerCase().contains("starte augentest")) {
            i = ermittleZahl(meta.length);
            bildname = meta[i][0];
            bildurl = baseUrl+"/Augen/"+bildname;
        }
        new LoadImage().execute(bildurl);
    }


    //überprüft ausgewählten Wert im vgl. zum Bild auf Richtigkeit
    public int spieleLogik(String s, Integer z) {

        durchlauf++;

        if (meta[z][1] == s) {
            right++;
            return right;
        } else if (meta[z][2] == s) {
            right++;
            return right;
        } else {
            soundPlayback();
            wrong++;
            return wrong;
        }
    }


    //spielt den Ton ab
    public void soundPlayback() {
        SoundPoolManager.CreateInstance();
        List<Integer> sounds = new ArrayList<Integer>();
        sounds.add(R.raw.crow);
        SoundPoolManager.getInstance().setSounds(sounds);
        try {
            SoundPoolManager.getInstance().InitializeSoundPool(this, new ISoundPoolLoaded() {
                @Override
                public void onSuccess() {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //ertellt die Statistik und speichert diese auf dem Gerät
    public void erstelleStatistik(int r, int w) {

        /*Intent intent = getIntent();
        String modus = intent.getStringExtra("modus");

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date date = new Date();
        String dateString = dateFormat.format(date).toString();
        //String username = UsersData.USERNA_NAME;

        String augencounter = durchlauf+"/40 Bilder beantwortet";
        String kopfcounter = durchlauf+"/50 Bilder beantwortet";


        String filename = "Statistik " + username + " " + dateString + ".xls";

        java.io.File file = new java.io.File(inputFile);
        WorkbookSettings wbSettings = new WorkbookSettings();

        wbSettings.setLocale(new Locale("de", "DE"));

        WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
        workbook.createSheet("Testergebnisse", 0);
        WritableSheet excelSheet = workbook.getSheet(0);

        addCaption(excelSheet, 0, 0, "Name");
        addCaption(excelSheet, 1, 0, "Testerart");
        addCaption(excelSheet, 2, 0, "Richtig");
        addCaption(excelSheet, 3, 0, "Falsch");

        addCaption(excelSheet, 0, 1, username);
        addNumber(excelSheet, 2, 1, r);
        addNumber(excelSheet, 3, 1, w);

        if (modus.toLowerCase().contains("starte köpfetest")) {
            addCaption(excelSheet, 1, 1, "Köpfetest");
            addCaption(excelSheet, 0, 3, kopfcounter);
        }
        else if (modus.toLowerCase().contains("starte augentest")) {
            addCaption(excelSheet, 1, 1, "Augentest");
            addCaption(excelSheet, 0, 3, augencounter);
        }

        workbook.write();
        workbook.close();

        newN.setOutputFile("c:/temp/lars.xls");
        newN.write();*/
    }


    public void export() {
        //Exportiert Statistik
    }


    @Override
    public void onStop() {
        super.onStop();

        erstelleStatistik(right, wrong);
    }


    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected Bitmap doInBackground(String... args) {
           // Log.d("loadImge",args[0]);

            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null){
                ImageView bild = (ImageView)context.findViewById(R.id.bild);
                bild.setImageBitmap(image);

            }
            else{
                Log.d("loadImge","Image Does Not exist or Network Error" );
            }
        }
    }
}

