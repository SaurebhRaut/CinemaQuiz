package cs122b.moviequiz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainBasicController extends Basic_Controller implements View.OnClickListener{

    private Button TakeQuiz;
    private Button Statistics;

    public QuizBasicController quizController;
    public StatBasicController statController;

    public MainBasicController() {
        super(R.layout.main);
        quizController = new QuizBasicController();
        statController = new StatBasicController();
    }

    @Override
    protected void onShow() {
        TakeQuiz = getButton(R.id.takeQuizButton);
        TakeQuiz.setOnClickListener(this);
        Statistics = getButton(R.id.quizStatsButton);
        Statistics.setOnClickListener(this);
    }

    @Override
    protected void onHide() {

    }


    @Override
    public void onClick(View v) {
        if(v.equals(TakeQuiz)){
            goToController(quizController);
        }
        else if(v.equals(Statistics)){
            goToController(statController);
        }
    }

    public void onRestoreInstanceState(Bundle inState){
        quizController.onRestoreInstanceState(inState);
        statController.onRestoreInstanceState(inState);
    }

    public void onSaveInstanceState(Bundle outState){
        quizController.onSaveInstanceState(outState);
        statController.onSaveInstanceState(outState);
    }
}
