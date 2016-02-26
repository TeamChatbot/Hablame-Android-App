package chatbot.morpheus.de.hablame_android_app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;
import jxl.write.Number;

public class FEFAActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = FEFAActivity.class.getSimpleName();
    public static final String BUZZWORD_HEAD = "starte köpfe test";
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
    String username;

    MediaPlayer p = new MediaPlayer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fefatest);
        eigenschaften = (ListView) findViewById(R.id.eigenschaften);
        this.modus = getIntent().getExtras().getString(EXTRAK_KEY);
        this.eigenschaften.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eigenschaftenListe));

        this.imageView = (ImageView) findViewById(R.id.bild);
        username = new UsersData(this).loadFromPreferences();
        new LoadMetaData().execute((Void) null);
        eigenschaften.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final String s = eigenschaften.getItemAtPosition(position).toString();
        spieleLogik(s, metaData);
        if(durchlauf < meta.size()){
            this.metaData = this.meta.get(ermittleZahl(meta.size()));
            ladeBilder(metaData);
        }else{
            Toast.makeText(getBaseContext(), "Das hast du gut gemacht. Du hast alle Bilder beantwortet.", Toast.LENGTH_LONG).show();
            erstelleStatistik(right, wrong, durchlauf);
            this.finish();
        }
    }


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
        p = MediaPlayer.create(this, R.raw.crow);
        p.start();
    }

    //erstellt Statistik und speichert diese in ein xls File
    public void erstelleStatistik(int r, int w, int d) {

        WritableWorkbook workbook;
        String testartT = "";
        String festzahl = "";
        String durchlaufD = ""+d+"";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date date = new Date();
        String dateString = dateFormat.format(date).toString();


        String dateiname = "Statistik "+ username +" "+ dateString +".xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File (sdCard.getAbsolutePath() + "/FEFA-Test");

        directory.mkdirs();

        File file = new File(directory, dateiname);

        WorkbookSettings wbSettings = new WorkbookSettings();

        wbSettings.setLocale(new Locale("de", "DE"));

        if (modus.equals(BUZZWORD_HEAD)){
            testartT = "Köpfetest";
            festzahl = "50";
        }
        else if (modus.equals(BUZZWORD_EYE)){
            testartT = "Augentest";
            festzahl = "40";
        }

        try {

            workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("Ergebnisse", 0);
            Label name = new Label(0, 0, "Name");
            Label testart = new Label(0, 1, "Testart");
            Label richtig = new Label(0, 2, "Richtig");
            Label falsch = new Label(0, 3, "Falsch");
            Label durchlauf = new Label(0, 4, "Durchläufe");
            Label nameV = new Label(1, 0, username);
            Label testartV = new Label(1, 1, testartT);
            Number richtigV = new Number(1, 2, r);
            Number falschV = new Number(1, 3, w);
            Label duchlaufV = new Label(1, 4, durchlaufD+ "/" + festzahl);

            sheet.addCell(name);
            sheet.addCell(testart);
            sheet.addCell(richtig);
            sheet.addCell(falsch);
            sheet.addCell(durchlauf);
            sheet.addCell(nameV);
            sheet.addCell(testartV);
            sheet.addCell(richtigV);
            sheet.addCell(falschV);
            sheet.addCell(duchlaufV);

        workbook.write();
            workbook.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onStop() {
        erstelleStatistik(right, wrong, durchlauf);
        super.onStop();
    }

    //läd Bilder in ImageView anhand von Metadaten und random Zahlermittlung
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

    //läd Metadaten vom Server
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

    @Override
    public void onBackPressed() {
        erstelleStatistik(right, wrong, durchlauf);
        super.onBackPressed();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        p.release();
    }

}

