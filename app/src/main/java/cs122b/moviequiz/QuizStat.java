package cs122b.moviequiz;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class QuizStat {

    private static String PATH_DB = "/data/data/cs122b.moviequiz/databases/";
    private static String NAME_DB = "mydb";
    private SQLiteDatabase myDB;

    public QuizStat(){
        String Path = PATH_DB + NAME_DB;
        myDB = SQLiteDatabase.openDatabase(Path, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void AddResult(int quizId, int questionId, boolean result, long time){
        String insertQuery = "INSERT INTO quiz_results " +"(quiz_id, question_id, correct, time) " +"VALUES " +"( '" + quizId + "', '" + questionId + "', '" + (result ? "1" : "0") + "', '" +  time + "');";
        myDB.execSQL(insertQuery);
    }

    public int QuestionsTaken(){
        String query = "SELECT COUNT(*) " +"FROM quiz_results;";
        Cursor c = myDB.rawQuery(query, new String[]{});
        if(c.moveToNext()){
            return c.getInt(0);
        }
        return 0;
    }

    public int NextQuizId(){
        String query = "SELECT max(quiz_id) " +"FROM quiz_results;";
        Cursor c = myDB.rawQuery(query, new String[]{});
        if(c.moveToNext()){
            return c.getInt(0)+1;
        }
        return 0;
    }

    public int WrongAnswers(){
        String query = "SELECT COUNT(*) " +"FROM quiz_results " +"WHERE correct='0';";
        Cursor c = myDB.rawQuery(query, new String[]{});
        if(c.moveToNext()){
            return c.getInt(0);
        }
        return 0;
    }



    public int AvgQuizTime(){
        String query = "SELECT AVG(time) " +"FROM quiz_results;";
        Cursor c = myDB.rawQuery(query, new String[]{});
        if(c.moveToNext()){
            return c.getInt(0);
        }
        return 0;
    }

    public int TotalQuizTime(){
        String query = "SELECT SUM(time) " +"FROM quiz_results;";
        Cursor c = myDB.rawQuery(query, new String[]{});
        if(c.moveToNext()){
            return c.getInt(0);
        }
        return 0;
    }

    public int QuizCount(){
        String query = "SELECT COUNT(DISTINCT quiz_id)" +"FROM quiz_results;";
        Cursor c = myDB.rawQuery(query, new String[]{});
        if(c.moveToNext()){
            return c.getInt(0);
        }
        return 0;
    }



    public int CorrectAnswers(){
        String query = "SELECT COUNT(*) " +"FROM quiz_results " +"WHERE correct='1';";
        Cursor c = myDB.rawQuery(query, new String[]{});
        if(c.moveToNext()){
            return c.getInt(0);
        }
        return 0;
    }





}
