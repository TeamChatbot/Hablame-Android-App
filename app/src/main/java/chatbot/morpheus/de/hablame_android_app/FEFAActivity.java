package chatbot.morpheus.de.hablame_android_app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FEFAActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = FEFAActivity.class.getSimpleName();
    public static final String BUZZWORD_HEAD = "starte köpfetest";
    public static final String BUZZWORD_EYE = "starte augentest";
    public static final String EXTRAK_KEY = "modus";
    private static final String baseUrl = "http://194.95.221.229:8080/fefatest";
    private final String[] eigenschaftenListe = new String[]{"Freude", "Trauer", "Furcht", "Zorn", "Überraschung", "Ekel", "Neutral"};
    public final List<MetaData> meta = new ArrayList<>();

    private ListView eigenschaften;
    int right = 0;
    int wrong = 0;
    int durchlauf = 0;
    private String modus;
    private ImageView imageView;
    private MetaData metaData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fefatest);
        eigenschaften = (ListView) findViewById(R.id.eigenschaften);
        this.modus = getIntent().getExtras().getString(EXTRAK_KEY);
        this.eigenschaften.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eigenschaftenListe));

        this.imageView = (ImageView) findViewById(R.id.bild);
        String username = new UsersData(this).loadFromPreferences();
        new LoadMetaData().execute((Void) null);
        eigenschaften.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final String s = eigenschaften.getItemAtPosition(position).toString();
        spieleLogik(s, metaData);
        if(durchlauf <= meta.size()){
            this.metaData = this.meta.get(ermittleZahl(meta.size()));
            ladeBilder(metaData);
        }else{
            //TODO Exit game
            //this.finish();
            //Toast
        }
    }


//        confirmButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent intent = getIntent();
//                String modus = intent.getStringExtra("modus");
//
//                //s = eigenschaften.getSelectedItem().toString();
//
//
//                if (modus.toLowerCase().contains("starte köpfetest")) {
//                    switch (durchlauf){
//                        case 50:
//                            Toast.makeText(getBaseContext(), "Das hast du gut gemacht.", Toast.LENGTH_LONG).show();
//                            onStop();
//                            onDestroy();
//                            break;
//                        default:
//                            ladeBilder();
//                    }
//                }
//                else if (modus.toLowerCase().contains("starte augentest")) {
//                    switch (durchlauf){
//                        case 40:
//                            Toast.makeText(getBaseContext(), "Das hast du gut gemacht.", Toast.LENGTH_LONG).show();
//                            onStop();
//                            onDestroy();
//                            break;
//                        default:
//                            ladeBilder();
//                    }
//                }
//            }
//        });
//
//}

    //ermittelt random Zahl für zufällig Bildauswahl

    public int ermittleZahl(int n) {
        final Random randomZahl = new Random(System.currentTimeMillis());
        return randomZahl.nextInt(n);
    }


    //ermittelt Bild URL und übergibt den URL LoadImage()
    public void ladeBilder(MetaData meta) {
        String bildurl = null;

        if (modus.equals(BUZZWORD_HEAD)) {
            bildurl = baseUrl + "/Koepfe/" + meta.fileName;
            ;

        } else if (modus.equals(BUZZWORD_EYE)) {
            bildurl = baseUrl + "/Augen/" + meta.fileName;
        }
        new LoadImage().execute(bildurl);
    }


    //überprüft ausgewählten Wert im vgl. zum Bild auf Richtigkeit
    public void spieleLogik(String s, MetaData z) {

        durchlauf++;

        if (z.traitFirst.equals(s) || z.traitSecond.equals(s)) {
            right++;
        } else {
            soundPlayback();
            wrong++;
        }
    }


    //spielt den Ton ab
    public void soundPlayback() {
        //TODO ADD SOUNDS
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
        Bitmap bitmap = null;

        protected Bitmap doInBackground(String... args) {
            try {
                final URLConnection urlConnection = new URL(args[0]).openConnection();
                bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {
            imageView.setImageBitmap(image);
        }
    }

    private class LoadMetaData extends AsyncTask<Void, Void, Void> {

        //läd Meta vom Server
        private String urlstring = "";
        private BufferedReader read = null;

        @Override
        protected Void doInBackground(final Void... params) {

            if (modus.equals(BUZZWORD_HEAD)) {
                urlstring = baseUrl + "/meta_koepfe.txt";
            } else if (modus.equals(BUZZWORD_EYE)) {
                urlstring = baseUrl + "/meta_augen.txt";
            }

            try {
                URLConnection urlConnection = new URL(urlstring).openConnection();
                read = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                ;
                String line = "";

                while ((line = read.readLine()) != null) {
                    meta.add(new MetaData(line.split(",")));
                }

            } catch (IOException ex) {
                Log.e(TAG, ex.getLocalizedMessage());
            } finally {
                if (read != null) {
                    try {
                        read.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            metaData = meta.get(0);
            ladeBilder(metaData);
        }
    }

    private class MetaData {
        String fileName = "";
        String traitFirst = "";
        String traitSecond = "";

        public MetaData(String fileName, String traitFirst, String traitSecond) {
            this.fileName = fileName;
            this.traitFirst = traitFirst;
            this.traitSecond = traitSecond;
        }

        public MetaData(final String[] split) {
            this(split[0], split[1], split[2]);
        }
    }
}

