package cs122b.moviequiz;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class QuizBasicController extends Basic_Controller implements View.OnClickListener{

    private Button[] button;
    private Button backBut;


    private static final int Quiz_Before = 0;
    private static final int Quiz_In = 1;
    private static final int Recent_Answered = 3;
    private static final int Quiz_End = 2;

    private TextView Field_question;

    private long Length_quiz;

    private long Start;

    private long currentTime;

    private MovieQuestion currentMovieQuestion;

    private GenerateQuestion generator = new GenerateQuestion();

    private CountDownTimer quizTimer;
    private TableLayout Layout_table;

    private int Id_Quiz;
    private int Id_Question;
    private TextView Field_time;
    private int Questions_Attempted;
    private int Questions_Correct;



    private int state = Quiz_End;

    private CountDownTimer intermediateTimer;

    public QuizBasicController(){
        this(180);
    }

    public QuizBasicController(int seconds){
        super(R.layout.quiz);
        Length_quiz = seconds * 1000l;
    }


    @Override
    protected void onShow() {
        state = Quiz_Before;
        UIlinking();

        Field_time.setText(TimeCalculation(Length_quiz));
        Field_question.setTextColor(Color.BLACK);
        Field_question.setText("Quiz will start now!!!");

        intermediateTimer = new CountDownTimer(1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                StartQuiz();

            }
        }.start();
    }

    private void UIlinking(){
        button = new Button[]{
                getButton(R.id.answerButton1),
                getButton(R.id.answerButton2),
                getButton(R.id.answerButton3),
                getButton(R.id.answerButton4)
        };
        for(Button b : button) {
            b.setOnClickListener(this);
        }
        Layout_table = getView(R.id.tableLayout, TableLayout.class);
        Field_time = getTextView(R.id.countdownText);
        Field_question = getTextView(R.id.questionText);
        backBut = getButton(R.id.backButton);
    }


    private void StartQuiz(){
        state = Quiz_In;
        Id_Quiz = getActivity().getResults().NextQuizId();
        Id_Question = 0;
        currentTime = 0;
        Layout_table.setVisibility(View.VISIBLE);
        quizTimer = Timer(Length_quiz);
        GenerateQuestion();
     }

    private void GenerateQuestion(){
        Start = currentTime;
        currentMovieQuestion = generator.generateRandomQuestion();
        setToQuestion();
    }

    private CountDownTimer Timer(long timeMillis){
        return new CountDownTimer(timeMillis, 50) {

            @Override
            public void onTick(long millisUntilFinished) {
                currentTime = Length_quiz - millisUntilFinished;
                Field_time.setText(TimeCalculation(millisUntilFinished));
            }

            public void onFinish() {
                QuizFinish();
            }
        }.start();
    }

    private void QuizFinish(){
        state = Quiz_End;
        Field_question.setText("Times Up!");
        Field_question.setTextColor(Color.BLACK);
        for(Button b : button){
            b.setClickable(false);
        }
        Layout_table.setVisibility(View.INVISIBLE);
        intermediateTimer = new CountDownTimer(2000, 2000) {

            @Override
            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                Field_question.setText("Correct : (" + Questions_Correct + ") Wrong: (" + (Questions_Attempted - Questions_Correct) + ")");
                backBut.setVisibility(View.VISIBLE);
                backBut.setEnabled(true);
                backBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        back();
                    }
                });
            }
        }.start();

    }

    @Override
    protected void onHide() {
        backBut.setVisibility(View.INVISIBLE);
        backBut.setEnabled(false);
    }

    private Button CorrectButton(){
        for(Button b : button){
            if(b.getText().equals(currentMovieQuestion.getAnswer()))
                return b;
        }
        return null;
    }

    @Override
    public void onClick(View v) {

        boolean correct = currentMovieQuestion.getAnswer().equals(((Button)v).getText());
        long timeTaken = currentTime - Start;
        getActivity().getResults().AddResult(Id_Quiz, Id_Question++, correct, timeTaken);
        Questions_Attempted++;
        if(!correct) {

            ((Button) v).setTextColor(Color.RED);
        }
        else
            Questions_Correct++;

        CorrectButton().setTextColor(Color.GREEN);
        for(Button b : button){
            b.setClickable(false);
        }
        Field_question.setText((correct ? "Correct!" : "Wrong!"));
        Field_question.setTextColor((correct ? Color.BLUE : Color.DKGRAY));
        new CountDownTimer(2000, 2000) {

            @Override
            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                GenerateQuestion();
            }
        }.start();
    }

    public void onPause(){
        quizTimer.cancel();
    }

    private void setToQuestion(){
        Field_question.setTextColor(Color.BLACK);
        Field_question.setText(currentMovieQuestion.getQuestion());
        for(int i = 0; i < Math.min(currentMovieQuestion.getOptions().size(), button.length); i++){
            button[i].setTextColor(Color.BLACK);
            button[i].setText(currentMovieQuestion.getOptions().get(i));
            button[i].setClickable(true);
        }
    }

    public void onResume(){
        UIlinking();
        if(state== Quiz_Before){
            if(intermediateTimer!=null)
                intermediateTimer.cancel();
            onShow();
        }
        else if(state== Quiz_In ||state== Recent_Answered){
            quizTimer = Timer(Length_quiz - currentTime);
            if(state==3)
                GenerateQuestion();
            Layout_table.setVisibility(View.VISIBLE);
            setToQuestion();
        }
        else if(state== Quiz_End){
            QuizFinish();
        }
    }

    public void onRestoreInstanceState(Bundle inState){
        Length_quiz = inState.getLong("Length_quiz");
        Start = inState.getLong("Start");
        currentTime = inState.getLong("currentTime");
        Id_Quiz = inState.getInt("Id_Quiz");
        Id_Question = inState.getInt("Id_Question");
        Questions_Attempted = inState.getInt("Questions_Attempted");
        Questions_Correct = inState.getInt("Questions_Correct");
        String question = inState.getString("currentQuestionQ");
        String answer = inState.getString("currentQuestionA");
        ArrayList<String> options = inState.getStringArrayList("currentQuestionOptions");
        currentMovieQuestion = new MovieQuestion(question, answer, options);
        state = inState.getInt("quizState");
    }

    public void onSaveInstanceState(Bundle outState){

        outState.putInt("quizState", state);
        outState.putLong("Length_quiz", Length_quiz);
        outState.putLong("Start", Start);
        outState.putLong("currentTime", currentTime);
        outState.putInt("Id_Quiz", Id_Quiz);
        outState.putInt("Id_Question", Id_Question);
        outState.putInt("Questions_Attempted", Questions_Attempted);
        outState.putInt("Questions_Correct", Questions_Correct);
        MovieQuestion currQuest = currentMovieQuestion;
        if(currQuest==null)
            currQuest = new MovieQuestion("", "", new ArrayList<String>());
        outState.putString("currentQuestionQ", currQuest.getQuestion());
        outState.putString("currentQuestionA", currQuest.getAnswer());
        outState.putStringArrayList("currentQuestionOptions", new ArrayList<String>(currQuest.getOptions()));
    }
    private String TimeCalculation(long millis){
        int totalSeconds = (int) millis/1000;
        int minutes = totalSeconds/60;
        String output = minutes + ":";
        if(output.length()<3) {
            output = "0" + output;
        }
        String outSeconds = "" + totalSeconds%60;
        if(outSeconds.length()<2){
            outSeconds = "0" + outSeconds;
        }
        return output + outSeconds;
    }
}
