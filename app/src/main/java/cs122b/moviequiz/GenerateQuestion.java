package cs122b.moviequiz;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
public class GenerateQuestion {
    private static String DB_PATH = "/data/data/cs122b.moviequiz/databases/";
    private static String DB_NAME = "mydb";
    private SQLiteDatabase myDB;


    public GenerateQuestion() {
        String myPath = DB_PATH + DB_NAME;
        myDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public MovieQuestion generateRandomQuestion() {
        Random random = new Random();

        int choice = random.nextInt(10);

        switch(choice) {
            case 0:
                return WhichStarWasInMovie();
            case 1:
                return WhoDidNotDirectStar();
            case 2:
                return WhichMovieDidTwoStarsAppearTogether();
            case 3:
                return WhichStarWasNotInMovie();
            case 4:
                return WhenWasMovieReleased();
            case 5:
                return WhoDirectedStar();
            case 6:
                return WhichStarAppearsInBothMovies();
            case 7:
                return WhoDirectedStarInYear();
            case 8:
                return WhichStarDidNotAppearInMovieWithThisStar();
            case 9:
                return WhoDirectedMovie();
            default:
                throw new IllegalArgumentException();
        }

    }

    private MovieQuestion WhoDirectedMovie() {

        String questionTemplate = "Who directed the movie @MOVIE@?";
        String textToReplace = "@MOVIE@";
        String correctAnswerSQL = "SELECT director, title " +
                "FROM movies " +
                "ORDER BY RANDOM() " +
                "LIMIT 1;";
        int indexOfAnswer = 0;
        int replacementIndex = 1;
        String incorrectAnswerSQL = "SELECT DISTINCT director " +
                "FROM movies " +
                "WHERE director != ? " +
                "ORDER BY RANDOM() " +
                "LIMIT 3;";

        return generateQuestion(questionTemplate, correctAnswerSQL, incorrectAnswerSQL, indexOfAnswer, replacementIndex, textToReplace);
    }

    private MovieQuestion WhenWasMovieReleased() {
        String questionTemplate = "When was the movie @MOVIE@ released?";
        String textToReplace = "@MOVIE@";
        String correctAnswerSQL = "SELECT DISTINCT year, title " +
                                  "FROM movies " +
                                  "ORDER BY RANDOM() " +
                                  "LIMIT 1;";
        int indexOfAnswer = 0;
        int replacementIndex = 1;
        String incorrectAnswerSQL = "SELECT DISTINCT year " +
                                    "FROM movies " +
                                    "WHERE year != ? " +
                                    "ORDER BY RANDOM() " +
                                    "LIMIT 3;";

        return generateQuestion(questionTemplate, correctAnswerSQL, incorrectAnswerSQL, indexOfAnswer, replacementIndex, textToReplace);
    }

    private MovieQuestion WhichStarWasInMovie() {
        String questionTemplate = "Which star was in the movie @MOVIE@?";
        String textToReplace = "@MOVIE@";
        String correctAnswerSQL = "SELECT first_name, last_name, title " +
                                  "FROM movies M JOIN stars_in_movies SIM " +
                                      "ON M.id = SIM.movie_id JOIN stars S " +
                                      "ON SIM.star_id = S.id " +
                                  "WHERE title != '' AND first_name != '' AND last_name != '' " +
                                  "ORDER BY RANDOM() " +
                                  "LIMIT 1;";

        Cursor answerCursor = myDB.rawQuery(correctAnswerSQL, null);
        answerCursor.moveToFirst();
        String fname = answerCursor.getString(0);
        String lname = answerCursor.getString(1);
        String answer = fname + " " + lname;
        String title = answerCursor.getString(2);
        answerCursor.close();

        String incorrectAnswerSQL = "Select distinct first_name, last_name FROM stars WHERE stars.id NOT IN (Select stars.id from stars JOIN stars_in_movies ON stars.id = stars_in_movies.star_id join movies ON movies.id = stars_in_movies.movie_id WHERE title = ? AND stars.first_name NOT LIKE ? AND stars.last_name NOT LIKE ?) ORDER BY RANDOM() LIMIT 3;";

        List<String> options = new ArrayList<String>();
        options.add(answer);


        Cursor wrongAnswersCursor = myDB.rawQuery(incorrectAnswerSQL, new String[]{title, fname, lname});
        while (wrongAnswersCursor.moveToNext()) {

            options.add(wrongAnswersCursor.getString(0) + " " + wrongAnswersCursor.getString(1));
        }
        wrongAnswersCursor.close();


        String question = questionTemplate.replace(textToReplace, title);
        Collections.shuffle(options);

        return new MovieQuestion(question,answer,options);
    }

    private MovieQuestion WhichStarWasNotInMovie() {
        String questionTemplate = "Which star was not in the movie @MOVIE@?";
        String textToReplace = "@MOVIE@";

        String titleSqlCommand = "select title FROM movies JOIN stars_in_movies ON movies.id = stars_in_movies.movie_id GROUP BY id HAVING count(stars_in_movies.star_id) >= 3 ORDER BY RANDOM() LIMIT 1;";
        Cursor replacementTextCursor = myDB.rawQuery(titleSqlCommand, null);
        replacementTextCursor.moveToFirst();
        String title = replacementTextCursor.getString(0);
        replacementTextCursor.close();

        String answerSQL = "Select distinct first_name, last_name FROM stars WHERE stars.id NOT IN (Select stars.id from stars JOIN stars_in_movies ON stars.id = stars_in_movies.star_id join movies ON movies.id = stars_in_movies.movie_id WHERE title = ?) ORDER BY RANDOM() LIMIT 1;";
        Cursor answerCursor = myDB.rawQuery(answerSQL, new String[]{title});
        answerCursor.moveToFirst();
        String fname = answerCursor.getString(0);
        String lname = answerCursor.getString(1);
        answerCursor.close();
        String answer = fname + " " + lname;

        List<String> options = new ArrayList<String>();
        options.add(answer);

        String incorrectAnswerSQL = "Select distinct first_name, last_name FROM stars WHERE stars.id IN (Select stars.id from stars JOIN stars_in_movies ON stars.id = stars_in_movies.star_id join movies ON movies.id = stars_in_movies.movie_id WHERE title = ?)ORDER BY RANDOM() LIMIT 3;";
        Cursor incorrectAnswerCursor = myDB.rawQuery(incorrectAnswerSQL, new String[]{title} );
        while (incorrectAnswerCursor.moveToNext()) {

            options.add(incorrectAnswerCursor.getString(0) + " " + incorrectAnswerCursor.getString(1));
        }
        incorrectAnswerCursor.close();

        String question = questionTemplate.replace(textToReplace, title);
        Collections.shuffle(options);

        return new MovieQuestion(question, answer, options);
    }

    private MovieQuestion WhichMovieDidTwoStarsAppearTogether() {
        String questionTemplate = "In which movie the stars @STAR1@ and @STAR2@ appear together?";

        String answerTitleSql = "select distinct title" +
                " FROM movies M JOIN stars_in_movies SIM " +
                "   ON M.id = SIM.movie_id JOIN stars S " +
                "   ON SIM.star_id = S.id " +
                "GROUP BY SIM.movie_id\n" +
                "HAVING count(*) >=3\n" +
                "ORDER BY RANDOM()\n" +
                "LIMIT 1;";
        Cursor answerCursor = myDB.rawQuery(answerTitleSql, null);
        answerCursor.moveToFirst();
        String answer = answerCursor.getString(0);
        answerCursor.close();

        List<String> options = new ArrayList<String>();
        options.add(answer);
        String [] starsName = new String [2];
        int i =0;
        String findStarByID = "Select distinct first_name, last_name\n" +
                "FROM stars\n" +
                "WHERE stars.id IN (Select stars.id from stars JOIN stars_in_movies ON stars.id = stars_in_movies.star_id join movies ON movies.id = stars_in_movies.movie_id\n" +
                "\t\t\t\t\t\t\t\t\t\tWHERE title = ?)\n" +
                "ORDER BY RANDOM()\n" +
                "LIMIT 2;";
        Cursor replacement1Cursor = myDB.rawQuery(findStarByID, new String[]{answer});
        while (replacement1Cursor.moveToNext()) {

            starsName[i++] = (replacement1Cursor.getString(0) + " " + replacement1Cursor.getString(1));
        }

        String incorrectAnswerSQL = "select distinct title\n" +
                "from movies where title NOT LIKE  ? \n" +
                "ORDER BY RANDOM()\n" +
                "LIMIT 3;";
        Cursor incorrectAnswerCursor = myDB.rawQuery(incorrectAnswerSQL, new String[]{answer} );
        while (incorrectAnswerCursor.moveToNext()) {

            options.add(incorrectAnswerCursor.getString(0));
        }
        incorrectAnswerCursor.close();

        String question = questionTemplate.replace("@STAR1@", starsName[0]).replace("@STAR2@", starsName[1]);
        Collections.shuffle(options);
        return new MovieQuestion(question, answer, options);
    }

    private MovieQuestion WhoDirectedStar() {
        String questionTemplate = "Who directed the star @STAR@?";
        String textToReplace = "@STAR@";
        String movieWhereDirectorDirectedStar = "SELECT M.director, S.first_name||' '||S.last_name " +
                                                 "FROM movies M JOIN stars_in_movies SIM " +
                                                 "   ON M.id = SIM.movie_id JOIN stars S " +
                                                 "   ON SIM.star_id = S.id " +
                                                 "WHERE S.first_name != '' AND S.last_name != '' AND M.director != '' " +
                                                 "ORDER BY RANDOM() " +
                                                 "LIMIT 1;";
        Cursor answerCursor = myDB.rawQuery(movieWhereDirectorDirectedStar, null);
        answerCursor.moveToFirst();
        String answer = answerCursor.getString(0);
        String starReplacement = answerCursor.getString(1);
        answerCursor.close();

        List<String> options = new ArrayList<String>();
        options.add(answer);

        String incorrectAnswerSQL = "SELECT M.director " +
                                    "FROM movies M JOIN stars_in_movies SIM " +
                                    "   ON M.id = SIM.movie_id JOIN stars S " +
                                    "   ON SIM.star_id = S.id " +
                                    "WHERE M.director != '' AND S.first_name != '' AND S.last_name != '' " +
                                    "   AND (S.first_name||' '||S.last_name != ? OR M.director != ?) " +
                                    "ORDER BY RANDOM() " +
                                    "LIMIT 3;";
        Cursor incorrectAnswerCursor = myDB.rawQuery(incorrectAnswerSQL, new String[]{starReplacement, answer} );
        while (incorrectAnswerCursor.moveToNext()) {

            options.add(incorrectAnswerCursor.getString(0));
        }
        incorrectAnswerCursor.close();


        String question = questionTemplate.replace(textToReplace, starReplacement);
        Collections.shuffle(options);

        return new MovieQuestion(question, answer, options);
    }

    private MovieQuestion WhoDidNotDirectStar() {
        String questionTemplate = "Who did not direct the star @STAR@?";
        String textToReplace = "@STAR@";

        String actorsWithThreeOrMoreDirectors = "SELECT DISTINCT S.first_name, S.last_name " +
                                                "FROM movies M JOIN stars_in_movies SIM " +
                                                "ON M.id = SIM.movie_id JOIN stars S " +
                                                "ON SIM.star_id = S.id " +
                                                "GROUP BY S.first_name, S.last_name " +
                                                "HAVING COUNT(DISTINCT M.director) >= 3 " +
                                                "ORDER BY RANDOM() " +
                                                "LIMIT 1;";

        Cursor cursor = myDB.rawQuery(actorsWithThreeOrMoreDirectors, null);
        cursor.moveToFirst();
        String fname = cursor.getString(0);
        String lname = cursor.getString(1);
        String starReplacement = fname + " " + lname;
        cursor.close();

        String correctAnswerSQL = "Select distinct director\n" +
                "FROM movies JOIN stars_in_movies ON movies.id = stars_in_movies.movie_id JOIN stars ON stars.id = stars_in_movies.star_id\n" +
                "WHERE stars.first_name NOT LIKE ? AND stars.last_name NOT LIKE ?\n" +
                "ORDER BY RANDOM()\n" +
                "LIMIT 1;\n";
        Cursor answerCursor = myDB.rawQuery(correctAnswerSQL, new String[]{fname,lname} );
        answerCursor.moveToFirst();
        String answer = answerCursor.getString(0);
        answerCursor.close();

        List<String> options = new ArrayList<String>();
        options.add(answer);

        String incorrectAnswerSQL = "Select distinct director\n" +
                "FROM movies JOIN stars_in_movies ON movies.id = stars_in_movies.movie_id JOIN stars ON stars.id = stars_in_movies.star_id\n" +
                "WHERE stars.first_name LIKE ? AND stars.last_name LIKE ?\n" +
                "ORDER BY RANDOM()\n" +
                "LIMIT 3;";
        Cursor incorrectAnswerCursor = myDB.rawQuery(incorrectAnswerSQL, new String[]{fname,lname} );
        while (incorrectAnswerCursor.moveToNext()) {

            options.add(incorrectAnswerCursor.getString(0));
        }
        incorrectAnswerCursor.close();

        String question = questionTemplate.replace(textToReplace, starReplacement);
        Collections.shuffle(options);
        return new MovieQuestion(question, answer, options);
    }

    private MovieQuestion WhichStarAppearsInBothMovies() {
        String questionTemplate = "Which star appears in both movies @MOVIE1@ and @MOVIE2@?";

        String actorWithTwoMoviesSQL = "Select distinct first_name, last_name\n" +
                "FROM stars where stars.id IN (\n" +
                "Select stars_in_movies.star_id\n" +
                "FROM stars_in_movies\n" +
                "GROUP BY stars_in_movies.star_id\n" +
                "HAVING count(*) > 2\n" +
                ")\n" +
                "ORDER BY RANDOM()\n" +
                "LIMIT 1;";
        Cursor answerCursor = myDB.rawQuery(actorWithTwoMoviesSQL, null);
        answerCursor.moveToFirst();
        String fname = answerCursor.getString(0);
        String lname = answerCursor.getString(1);
        String answer = fname + " " + lname;
        answerCursor.close();


        String [] replacement = new String [2];
        String findStarByID = "select distinct movies.title\n" +
                "FROM movies JOIN stars_in_movies ON movies.id = stars_in_movies.movie_id\n" +
                "WHERE stars_in_movies.star_id IN (Select stars.id from stars where stars.first_name = ? AND stars.last_name = ?)\n" +
                "ORDER BY RANDOM()\n" +
                "LIMIT 2;";
        Cursor replacement1Cursor = myDB.rawQuery(findStarByID, new String[]{fname, lname});
        int i =0;
        while (replacement1Cursor.moveToNext()) {

            replacement[i++] = (replacement1Cursor.getString(0));
        }

        List<String> options = new ArrayList<String>();
        options.add(answer);

        String incorrectAnswerSQL = "select distinct first_name, last_name\n" +
                "from stars\n" +
                "where stars.first_name != ? AND stars.last_name != ?\n" +
                "ORDER BY RANDOM()\n" +
                "LIMIT 3;";
        Cursor incorrectAnswerCursor = myDB.rawQuery(incorrectAnswerSQL, new String[]{fname, lname} );
        while (incorrectAnswerCursor.moveToNext()) {

            options.add(incorrectAnswerCursor.getString(0) + " " + incorrectAnswerCursor.getString(1));
        }
        incorrectAnswerCursor.close();

        String question = questionTemplate.replace("@MOVIE1@", replacement[0]).replace("@MOVIE2@", replacement[1]);
        Collections.shuffle(options);

        return new MovieQuestion(question, answer, options);
    }

    private MovieQuestion WhichStarDidNotAppearInMovieWithThisStar() {
        String questionTemplate = "Which star did not appear in the same movie with the star @STAR@?";

        String moviesWithFourActorsSQL = "SELECT DISTINCT M.title, SIM1.star_id, SIM2.star_id, SIM3.star_id, SIM4.star_id " +
                                          "FROM stars_in_movies SIM1, stars_in_movies SIM2, stars_in_movies SIM3, stars_in_movies SIM4, movies M " +
                                          "WHERE SIM1.movie_id = SIM2.movie_id " +
                                          "AND SIM2.movie_id = SIM3.movie_id " +
                                          "AND SIM3.movie_id = SIM4.movie_id " +
                                          "AND SIM1.star_id != SIM2.star_id " +
                                          "AND SIM2.star_id != SIM3.star_id " +
                                          "AND SIM3.star_id != SIM4.star_id " +
                                          "AND M.id = SIM1.movie_id " +
                                          "ORDER BY RANDOM() " +
                                          "LIMIT 1;";
        Cursor cursor = myDB.rawQuery(moviesWithFourActorsSQL, null);
        cursor.moveToFirst();
        String movieTitle = cursor.getString(0);
        String star_id1 = cursor.getString(1);
        String star_id2 = cursor.getString(2);
        String star_id3 = cursor.getString(3);
        String starReplacement_id = cursor.getString(4);
        cursor.close();

        String findStarByID = "SELECT DISTINCT first_name||' '||last_name AS name " +
                "FROM stars S " +
                "WHERE S.id = ?;";
        Cursor replacement1Cursor = myDB.rawQuery(findStarByID, new String[]{star_id1});
        replacement1Cursor.moveToFirst();
        String wrongActor1 = replacement1Cursor.getString(0);
        replacement1Cursor.close();

        Cursor replacement2Cursor = myDB.rawQuery(findStarByID, new String[]{star_id2});
        replacement2Cursor.moveToFirst();
        String wrongActor2 = replacement2Cursor.getString(0);
        replacement2Cursor.close();

        Cursor replacement3Cursor = myDB.rawQuery(findStarByID, new String[]{star_id3});
        replacement3Cursor.moveToFirst();
        String wrongActor3 = replacement3Cursor.getString(0);
        replacement3Cursor.close();

        Cursor replacement4Cursor = myDB.rawQuery(findStarByID, new String[]{starReplacement_id});
        replacement4Cursor.moveToFirst();
        String starReplacement = replacement4Cursor.getString(0);
        replacement4Cursor.close();

        List<String> options = new ArrayList<String>();
        options.add(wrongActor1);
        options.add(wrongActor2);
        options.add(wrongActor3);

        String answerSQL = "SELECT S.first_name||' '||S.last_name " +
                           "FROM movies M JOIN stars_in_movies SIM " +
                           "   ON M.id = SIM.movie_id JOIN stars S " +
                           "   ON SIM.star_id = S.id " +
                           "WHERE S.first_name != '' AND S.last_name != '' " +
                           "   AND M.title != ? " +
                           "ORDER BY RANDOM() " +
                           "LIMIT 1;";
        Cursor answerCursor = myDB.rawQuery(answerSQL, new String[]{movieTitle});
        answerCursor.moveToFirst();
        String answer = answerCursor.getString(0);
        answerCursor.close();


        options.add(answer);

        String question = questionTemplate.replace("@STAR@", starReplacement);
        Collections.shuffle(options);
        return new MovieQuestion(question, answer, options);
    }

    private MovieQuestion WhoDirectedStarInYear() {
        String questionTemplate = "Who directed the star @STAR@ in year @YEAR@?";

        String movieWhereDirectorDirectedStar = "SELECT M.director, S.first_name||' '||S.last_name, M.year " +
                                                "FROM movies M JOIN stars_in_movies SIM " +
                                                "   ON M.id = SIM.movie_id JOIN stars S " +
                                                "   ON SIM.star_id = S.id " +
                                                "WHERE S.first_name != '' AND S.last_name != '' " +
                                                    "AND M.director != '' AND year != '' " +
                                                "ORDER BY RANDOM() " +
                                                "LIMIT 1;";
        Cursor answerCursor = myDB.rawQuery(movieWhereDirectorDirectedStar, null);
        answerCursor.moveToFirst();
        String answer = answerCursor.getString(0);
        String starReplacement = answerCursor.getString(1);
        String yearReplacement = answerCursor.getString(2);
        answerCursor.close();

        List<String> options = new ArrayList<String>();
        options.add(answer);

        String incorrectAnswerSQL = "SELECT M.director " +
                                    "FROM movies M JOIN stars_in_movies SIM " +
                                    "   ON M.id = SIM.movie_id JOIN stars S " +
                                    "   ON SIM.star_id = S.id " +
                                    "WHERE M.director != '' AND S.first_name != '' AND S.last_name != '' " +
                                    "   AND (S.first_name||' '||S.last_name != ? OR M.director != ? OR year != ?) " +
                                    "ORDER BY RANDOM() " +
                                    "LIMIT 3;";
        Cursor incorrectAnswerCursor = myDB.rawQuery(incorrectAnswerSQL, new String[]{starReplacement, answer, yearReplacement} );
        while (incorrectAnswerCursor.moveToNext()) {

            options.add(incorrectAnswerCursor.getString(0));
        }
        incorrectAnswerCursor.close();



        String question = questionTemplate.replace("@STAR@", starReplacement).replace("@YEAR@", yearReplacement);
        Collections.shuffle(options);

        return new MovieQuestion(question, answer, options);
    }

    private MovieQuestion generateQuestion(String questionTemplate, String correctAnswerSQL, String incorrectAnswerSQL, int correctAnswerIndex,
                                      int stringReplaceIndex, String stringToReplace) {

        Cursor answerCursor = myDB.rawQuery(correctAnswerSQL, null);
        answerCursor.moveToFirst();
        String answer = answerCursor.getString(correctAnswerIndex);
        String replacementText = answerCursor.getString(stringReplaceIndex);
        answerCursor.close();


        List<String> options = new ArrayList<String>();
        options.add(answer);


        Cursor wrongAswersCursor = myDB.rawQuery(incorrectAnswerSQL, new String[]{answer} );
        while (wrongAswersCursor.moveToNext()) {

            options.add(wrongAswersCursor.getString(0));
        }
        wrongAswersCursor.close();


        String question = questionTemplate.replace(stringToReplace, replacementText);
        Collections.shuffle(options);
        return new MovieQuestion(question, answer, options);
    }
}
