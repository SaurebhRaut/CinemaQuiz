package cs122b.moviequiz;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private ArrayList<Basic_Controller> ActiveBasicControllers = new ArrayList<>();

    private MainBasicController Main_Controller;
    private QuizStat Results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SQLiteDatabase myDB= null;

        try {

            myDB = this.openOrCreateDatabase("mydb", MODE_PRIVATE, null);


            createResultsTable(myDB);
            importMoviesTable(myDB);
            importStarsTable(myDB);
            importStarsInMoviesTable(myDB);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Results = new QuizStat();
        setCurrentController(Main_Controller = new MainBasicController());
    }

    public void back(){
        if(hasController()) {
            getCurrentController().hide(this);
            ActiveBasicControllers.remove(ActiveBasicControllers.size()-1);
            if(hasController())
                getCurrentController().show(this);
        }
    }

    protected void onPause(){
        super.onPause();
        getCurrentController().onPause();
    }



    protected void onSaveInstanceState(Bundle outBundle){
        super.onSaveInstanceState(outBundle);
        ArrayList<Integer> controlStackIds = new ArrayList<Integer>();
        for(Basic_Controller c : ActiveBasicControllers){
            c.onSaveInstanceState(outBundle);
            if(c instanceof MainBasicController){
                controlStackIds.add(0);
            }
            else if(c instanceof QuizBasicController){
                controlStackIds.add(1);
            }
            else if(c instanceof StatBasicController)
                controlStackIds.add(2);
        }
        outBundle.putIntegerArrayList("controlStack", controlStackIds);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume(){
        super.onResume();
        getCurrentController().onResume();
    }

    public Basic_Controller getCurrentController(){
        return ActiveBasicControllers.get(ActiveBasicControllers.size()-1);
    }

    public boolean hasController(){
        return !ActiveBasicControllers.isEmpty();
    }

    public void setCurrentController(Basic_Controller basicController){
        if(hasController())
            getCurrentController().hide(this);
        ActiveBasicControllers.add(basicController);
        basicController.show(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void createResultsTable(SQLiteDatabase myDB){
        String createTableSQL = "CREATE TABLE IF NOT EXISTS quiz_results( " +
                "quiz_id INTEGER NOT NULL, " +
                "question_id INTEGER NOT NULL, " +
                "correct INTEGER NOT NULL, " +
                "time INTEGER NOT NULL, " +
                "PRIMARY KEY(quiz_id, question_id)" +
                ");";
        myDB.execSQL(createTableSQL);
    }

    private void importStarsTable(SQLiteDatabase myDB) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS stars( " +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "first_name VARCHAR NOT NULL, " +
                                    "last_name VARCHAR NOT NULL, " +
                                    "dob VARCHAR" +
                                ");";

        String insertRowTemplate = "INSERT INTO stars (id, first_name, last_name, dob) VALUES (@TABLE_VALUES@);";

        String fileName = "stars.csv";

        String tableName = "stars";

        createAndImportTable(createTableSQL, insertRowTemplate, fileName, tableName, myDB);
    }

    private void importMoviesTable(SQLiteDatabase myDB) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS movies( " +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "title VARCHAR NOT NULL, " +
                                    "year INTEGER NOT NULL, " +
                                    "director VARCHAR NOT NULL" +
                                ");";

        String insertRowTemplate = "INSERT INTO movies (id, title, year, director) VALUES (@TABLE_VALUES@);";

        String fileName = "movies.csv";

        String tableName = "movies";

        createAndImportTable(createTableSQL, insertRowTemplate, fileName, tableName, myDB);
    }

    private void importStarsInMoviesTable(SQLiteDatabase myDB) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS stars_in_movies( " +
                                    "star_id INTEGER NOT NULL, " +
                                    "movie_id INTEGER NOT NULL," +
                                    "FOREIGN KEY(star_id) REFERENCES stars(id)," +
                                    "FOREIGN KEY(movie_id) REFERENCES movies(id)" +
                                ");";

        String insertRowTemplate = "INSERT INTO stars_in_movies (star_id, movie_id) VALUES (@TABLE_VALUES@);";

        String fileName = "stars_in_movies.csv";

        String tableName = "stars_in_movies";

        createAndImportTable(createTableSQL, insertRowTemplate, fileName, tableName, myDB);
    }

    private void createAndImportTable(String createSQL, String importTemplateSQL, String fileName, String tableName, SQLiteDatabase myDB) {
        try {

            myDB.execSQL(createSQL);

            Cursor cursor = myDB.rawQuery("select count(*) from " + tableName, null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();

            if(count == 0) {

                StringBuffer movieImport = new StringBuffer();

                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(
                            new InputStreamReader(getAssets().open(fileName)));

                    String mLine = reader.readLine();
                    while (mLine != null) {

                        myDB.execSQL(importTemplateSQL.replace("@TABLE_VALUES@", mLine));

                        mLine = reader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onRestoreInstanceState(Bundle inBundle){
        super.onRestoreInstanceState(inBundle);
        ActiveBasicControllers.clear();
        Main_Controller = new MainBasicController();
        ArrayList<Integer> controlStackIds = inBundle.getIntegerArrayList("controlStack");
        for(Integer i : controlStackIds){
            if(i.intValue()==0) {
                Main_Controller.onRestoreInstanceState(inBundle);
                Main_Controller.setActivity(this);
                ActiveBasicControllers.add(Main_Controller);
            }
            else if(i.intValue()==1) {
                Main_Controller.quizController.onRestoreInstanceState(inBundle);
                Main_Controller.quizController.setActivity(this);
                ActiveBasicControllers.add(Main_Controller.quizController);
            }
            else if(i.intValue()==2) {
                Main_Controller.statController.onRestoreInstanceState(inBundle);
                Main_Controller.statController.setActivity(this);
                ActiveBasicControllers.add(Main_Controller.statController);
            }
        }
        if(hasController())
            setContentView(getCurrentController().getView());
    }

    public QuizStat getResults(){
        return Results;
    }
}





